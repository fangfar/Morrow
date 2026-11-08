package cn.courselens;

import android.content.Context;
import android.util.AtomicFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,shadows=LocalBackupTest.FailWrite.class)
public class LocalBackupTest {
 private Context context;private final char[] password="synthetic-backup-password".toCharArray();
 @Implements(AtomicFile.class) public static class FailWrite extends DataManagementTest.AtomicRename {
  static String failName;@org.robolectric.annotation.RealObject AtomicFile real;
  @Implementation protected FileOutputStream startWrite()throws IOException{
   if(real.getBaseFile().getName().equals(failName)){failName=null;throw new IOException("Synthetic disk failure");}
   return org.robolectric.shadow.api.Shadow.directlyOn(real,AtomicFile.class,"startWrite");
  }
 }
 @Before public void setup()throws Exception{
  context=RuntimeEnvironment.getApplication();FailWrite.failName=null;
  org.robolectric.util.ReflectionHelpers.setStaticField(ParcelDatabase.class,"instance",androidx.room.Room.inMemoryDatabaseBuilder(context,ParcelDatabase.class).allowMainThreadQueries().build());
  Timetables.preferences(context).edit().clear().putString("courses","1|1|2|合成课程|A101|1-8").putString("courseAdjustments","").putString("term","2026-08-31").putInt("week",1).commit();
  ParcelRecord record=new ParcelRecord();record.code="1-2-3";record.station="合成驿站";record.deletedAt=123;ParcelDatabase.get(context).parcels().insert(record);
  ChaoxingClient.Result tasks=new ChaoxingClient.Result();ChaoxingClient.Work work=new ChaoxingClient.Work("手动任务","合成作业","未完成","");work.courseKey="local";work.workKey="local:synthetic";tasks.items.add(work);new ChaoxingCache(context,"").save(tasks);
 }
 @After public void cleanup(){FailWrite.failName=null;ParcelDatabase.get(context).close();org.robolectric.util.ReflectionHelpers.setStaticField(ParcelDatabase.class,"instance",null);}
 private File file(String name){return new File(context.getNoBackupFilesDir(),name);}
 private void archive(String name,JSONObject data)throws Exception{Files.write(file(name).toPath(),BackupCrypto.encrypt(data.toString().getBytes(StandardCharsets.UTF_8),password));}
 private JSONObject snapshot()throws Exception{return LocalBackup.snapshot(context,Collections.emptySet());}
 @Test public void encryptionAuthenticatesPasswordContentHeaderAndVersion()throws Exception{
  byte[] plain="private-synthetic-data".getBytes(StandardCharsets.UTF_8),encrypted=BackupCrypto.encrypt(plain,password);
  assertFalse(new String(encrypted,StandardCharsets.ISO_8859_1).contains("private-synthetic-data"));assertArrayEquals(plain,BackupCrypto.decrypt(encrypted,password));assertFalse(Arrays.equals(encrypted,BackupCrypto.encrypt(plain,password)));
  assertThrows(IOException.class,()->BackupCrypto.decrypt(encrypted,"wrong".toCharArray()));
  byte[] damaged=encrypted.clone();damaged[damaged.length-1]^=1;byte[] tampered=damaged;assertThrows(IOException.class,()->BackupCrypto.decrypt(tampered,password));
  damaged=encrypted.clone();damaged[7]=2;byte[] future=damaged;assertThrows(IOException.class,()->BackupCrypto.decrypt(future,password));
  assertThrows(IOException.class,()->BackupCrypto.decrypt(Arrays.copyOf(encrypted,20),password));
 }
 @Test public void snapshotCoversDataAndExcludesCredentials()throws Exception{
  context.getSharedPreferences("school_accounts",0).edit().putString("password","synthetic-secret").commit();context.getSharedPreferences("appSettings",0).edit().putInt("accentColor",3).putString("unknownSecret","synthetic-secret").commit();
  Timetables.add(context,"合成第二课表","squ",ClassTimes.DEFAULT,"2026-08-31");JSONObject data=snapshot();LocalBackup.validate(context,data);
  assertFalse(data.toString().contains("synthetic-secret"));assertEquals(2,Timetables.names(context).size());
  assertEquals(1,data.getJSONObject("tasks").length());assertEquals(1,data.getJSONArray("parcels").length());
  byte[] encrypted=LocalBackup.export(context,password);assertEquals(data.getJSONArray("parcels").toString(),LocalBackup.open(context,new ByteArrayInputStream(encrypted),password).getJSONArray("parcels").toString());
 }
 @Test public void invalidDataAndPathsNeverModifyCurrentData()throws Exception{
  JSONObject data=snapshot();data.getJSONObject("tasks").put("../outside.json","{}");JSONObject invalid=data;assertThrows(Exception.class,()->LocalBackup.validate(context,invalid));
  data=snapshot();data.put("schema",2);archive("restore-pending",data);assertTrue(LocalBackup.recover(context,password).contains("未更改"));assertFalse(file("restore-journal").exists());assertTrue(Timetables.preferences(context).getString("courses","").contains("合成课程"));
 }
 @Test public void successfulRestoreReplacesDataOnlyAfterStaging()throws Exception{
  JSONObject before=snapshot();archive("restore-pending",before);Timetables.preferences(context).edit().putString("courses","2|1|2|后来课程|B101|1").commit();new ChaoxingCache(context,"").save(new ChaoxingClient.Result());ParcelDatabase.get(context).parcels().clear();
  assertTrue(Timetables.preferences(context).getString("courses","").contains("后来课程"));assertTrue(LocalBackup.recover(context,password).contains("已恢复"));assertTrue(Timetables.preferences(context).getString("courses","").contains("合成课程"));assertEquals(1,new ChaoxingCache(context,"").load().items.size());assertEquals(1,ParcelDatabase.get(context).parcels().deleted().size());assertFalse(file("restore-journal").exists());assertFalse(file("restore-pending").exists());
 }
 @Test public void failedWriteRollsBackPreferencesTasksAndDatabase()throws Exception{
  JSONObject incoming=snapshot();archive("restore-pending",incoming);Timetables.preferences(context).edit().putString("courses","2|1|2|保留课程|B101|1").commit();new ChaoxingCache(context,"").save(new ChaoxingClient.Result());ParcelDatabase.get(context).parcels().clear();JSONObject before=snapshot();FailWrite.failName=before.getJSONObject("tasks").keys().next();
  assertTrue(LocalBackup.recover(context,password).contains("恢复失败"));JSONObject after=snapshot();assertEquals(before.getJSONObject("preferences").toString(),after.getJSONObject("preferences").toString());assertEquals(before.getJSONObject("tasks").toString(),after.getJSONObject("tasks").toString());assertEquals(before.getJSONArray("parcels").toString(),after.getJSONArray("parcels").toString());
 }
 @Test public void interruptedRestoreRollsBackBeforeAcceptingAnyNewWork()throws Exception{
  JSONObject before=snapshot();archive("restore-journal",before);archive("restore-pending",before);Timetables.preferences(context).edit().putString("courses","").commit();ParcelDatabase.get(context).parcels().clear();
  assertTrue(LocalBackup.recover(context,password).contains("上次恢复未完成"));assertTrue(Timetables.preferences(context).getString("courses","").contains("合成课程"));assertEquals(1,ParcelDatabase.get(context).parcels().deleted().size());
 }
 @Test public void settingsUseSystemFilesAndCancellationDoesNotStageRestore()throws Exception{
  try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity activity=controller.get();org.robolectric.util.ReflectionHelpers.callInstanceMethod(activity,"menu");org.robolectric.util.ReflectionHelpers.callInstanceMethod(activity,"dataManagement");android.view.View root=org.robolectric.util.ReflectionHelpers.getField(activity,"root");root.findViewWithTag("backup-export").performClick();assertEquals(android.content.Intent.ACTION_CREATE_DOCUMENT,Shadows.shadowOf(activity).getNextStartedActivityForResult().intent.getAction());
   root.findViewWithTag("backup-import").performClick();assertEquals(android.content.Intent.ACTION_OPEN_DOCUMENT,Shadows.shadowOf(activity).getNextStartedActivityForResult().intent.getAction());
   BackupUi ui=org.robolectric.util.ReflectionHelpers.getField(activity,"backupUi");org.robolectric.util.ReflectionHelpers.callInstanceMethod(ui,"confirm",org.robolectric.util.ReflectionHelpers.ClassParameter.from(JSONObject.class,snapshot()));android.app.AlertDialog dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();assertTrue(dialog.isShowing());dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).performClick();assertFalse(file("restore-pending").exists());assertFalse(file("restore-journal").exists());
  }
 }
}
