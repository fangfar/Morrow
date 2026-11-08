package cn.courselens;

import android.content.SharedPreferences;
import android.os.Looper;
import android.view.View;
import java.time.Duration;
import java.util.concurrent.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.ReflectionHelpers.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34)
public class TimetableSyncIntegrationTest {
 private static final String ORIGINAL="1|1|2|合成数学|旧教室|1-8\n2|3|4|合成英语|旧教室|1-8",FRESH=ORIGINAL.replace("旧教室","新教室");
 @Test public void importStoresOriginalAndManualRefreshPreservesLocalDeletion()throws Exception {
  try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=controller.get();SchoolClient client=new SchoolClient(){@Override public void prepare(){assertNotEquals(Looper.getMainLooper(),Looper.myLooper());cachedCourses=FRESH;}};
   callInstanceMethod(a,"acceptImport",ClassParameter.from(String.class,ORIGINAL),ClassParameter.from(SchoolClient.class,client),ClassParameter.from(String.class,"squ"));
   SharedPreferences prefs=a.getPreferences(0);assertEquals(ORIGINAL,prefs.getString(TimetableSync.BASELINE,null));
   String edited="1|1|2|合成数学|自选教室|1-8";setField(a,"data",edited);callInstanceMethod(a,"save");setField(a,"school",client);
   callInstanceMethod(a,"refreshTimetable");ExecutorService worker=getField(a,"worker");worker.submit(()->{}).get(5,TimeUnit.SECONDS);settle();
   assertFalse((Boolean)getField(a,"busy"));assertEquals(edited,getField(a,"data"));assertEquals(edited,prefs.getString("courses",null));assertEquals(FRESH,prefs.getString(TimetableSync.BASELINE,null));
   ((View)getField(a,"root")).findViewWithTag("timetable-settings").performClick();assertNotNull("Visible timetable dropdown lacks sync",((View)getField(a,"root")).findViewWithTag("refresh-timetable"));
  }
 }
 @Test public void sourceFailureAndConcurrentEditsDoNotAdvanceSnapshot()throws Exception {
  try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=controller.get();SharedPreferences prefs=a.getPreferences(0);prefs.edit().putString("courses",ORIGINAL).putString(TimetableSync.BASELINE,ORIGINAL).putString("syncSource","squ").putBoolean("schoolImported",true).commit();callInstanceMethod(a,"reloadTimetable");
   for(boolean offline:new boolean[]{true,false}){
    CountDownLatch gate=new CountDownLatch(1);setField(a,"school",new SchoolClient(){@Override public void prepare()throws Exception{gate.await(5,TimeUnit.SECONDS);if(offline)throw new java.io.IOException("offline");cachedCourses=FRESH;}});
    callInstanceMethod(a,"refreshTimetable");String edited="3|1|2|合成手动"+offline+"|自选|1-8";setField(a,"data",edited);callInstanceMethod(a,"save");gate.countDown();ExecutorService worker=getField(a,"worker");worker.submit(()->{}).get(5,TimeUnit.SECONDS);settle();
    assertEquals(edited,getField(a,"data"));assertEquals(ORIGINAL,prefs.getString(TimetableSync.BASELINE,null));
   }
  }
 }
 @Test public void baselineIsIsolatedPerTimetable(){
  try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=controller.get();a.getPreferences(0).edit().putString(TimetableSync.BASELINE,ORIGINAL).commit();
   Timetables.add(a,"另一份合成课表","xzit",ClassTimes.XZIT,"2026-09-01");assertFalse(a.getPreferences(0).contains(TimetableSync.BASELINE));Timetables.select(a,"");assertEquals(ORIGINAL,a.getPreferences(0).getString(TimetableSync.BASELINE,null));
  }
 }
 @Test public void syncKeepsUserClassTimesAndNotes()throws Exception {
  try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=controller.get();SharedPreferences prefs=a.getPreferences(0);prefs.edit().putString("courses",ORIGINAL).putString(TimetableSync.BASELINE,ORIGINAL).putString("syncSource","squ").putString("syncClassTimes",ClassTimes.DEFAULT.encode()).putString("classTimes",ClassTimes.XZIT.encode()).putString("notes","合成个人备注").putBoolean("schoolImported",true).commit();callInstanceMethod(a,"reloadTimetable");
   setField(a,"school",new SchoolClient(){@Override public void prepare(){cachedCourses=FRESH;}});callInstanceMethod(a,"refreshTimetable");ExecutorService worker=getField(a,"worker");worker.submit(()->{}).get(5,TimeUnit.SECONDS);settle();
   assertEquals(FRESH,getField(a,"data"));assertEquals(ClassTimes.XZIT.encode(),prefs.getString("classTimes",null));assertEquals("合成个人备注",prefs.getString("notes",null));
  }
 }
 private static void settle(){shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(400));}
}
