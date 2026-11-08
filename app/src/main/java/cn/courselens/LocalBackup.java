package cn.courselens;

import android.content.*;
import android.util.AtomicFile;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.*;

final class LocalBackup {
 private static final String TASK="chaoxing-work-[0-9a-f]{64}\\.json";
 private static final String[] SETTINGS={"themeMode:i","accentColor:i","autoCheckUpdates:b"},PARCEL={"recognition:b","reminders:b","widgetCodes:b"};
 private static final String[] TIMETABLE={"courses:s","notes:s","week:i","term:s","classTimes:s","school:s","courseAdjustments:s","displayChoices:ss","schoolImported:b","syncCourses:s","syncProtectedCourses:ss","syncClassTimes:s","syncSource:s","webTimetableUrl:s","aiExams:s"};
 private static boolean exists(AtomicFile file){return file.getBaseFile().exists()||new File(file.getBaseFile()+".bak").exists();}
 private static AtomicFile file(Context c,String name){return new AtomicFile(new File(c.getNoBackupFilesDir(),name));}
 static String timetableName(Context c,String id){return Timetables.preferenceName(c,id);}
 private static boolean table(Context c,String name){String base=timetableName(c,"");return name.equals(base)||name.matches(java.util.regex.Pattern.quote(base)+"\\.timetable\\.[0-9a-f-]{36}");}
 private static Map<String,String> keys(Context c,String name){
  Map<String,String> result=new LinkedHashMap<>();String[] fields=name.equals("appSettings")?SETTINGS:name.equals("parcelSettings")?PARCEL:table(c,name)?TIMETABLE:new String[0];
  for(String field:fields){int split=field.indexOf(':');result.put(field.substring(0,split),field.substring(split+1));}return result;
 }
 private static boolean registryKey(String key){return key.equals("ids")||key.equals("active")||key.equals("name.")||key.matches("name\\.[0-9a-f-]{36}");}
 private static String type(Context c,String store,String key){return store.equals("timetables")&&registryKey(key)?"s":keys(c,store).get(key);}
 static JSONObject snapshot(Context c,Collection<String> extra)throws Exception{
  Set<String> stores=new LinkedHashSet<>(Arrays.asList("timetables","appSettings","parcelSettings"));for(String id:Timetables.names(c).keySet())stores.add(timetableName(c,id));stores.addAll(extra);
  JSONObject prefs=new JSONObject(),files=new JSONObject();
  for(String store:stores){JSONObject values=new JSONObject();for(var entry:c.getSharedPreferences(store,0).getAll().entrySet())if(type(c,store,entry.getKey())!=null){Object value=entry.getValue();values.put(entry.getKey(),value instanceof Set?new JSONArray((Set<?>)value):value);}prefs.put(store,values);}
  synchronized(ChaoxingCache.class){File[] tasks=c.getFilesDir().listFiles();if(tasks==null)throw new IOException("任务目录无法读取");
  Set<String> taskNames=new TreeSet<>();for(File task:tasks){String name=task.getName().replaceFirst("\\.bak$","");if(name.matches(TASK))taskNames.add(name);}
  for(String name:taskNames){File task=new File(c.getFilesDir(),name),backup=new File(task+".bak");if(Math.max(task.length(),backup.length())>10*1024*1024)throw new IOException("任务缓存过大");byte[] bytes=new AtomicFile(task).readFully();if(bytes.length>10*1024*1024)throw new IOException("任务缓存过大");files.put(name,new String(bytes,StandardCharsets.UTF_8));}}
  ParcelDatabase db=ParcelDatabase.get(c);JSONArray parcels=new JSONArray(),seen=new JSONArray();
  db.runInTransaction(()->{try{for(ParcelRecord record:db.parcels().backupAll())parcels.put(encodeRecord(record));for(ParcelDatabase.Seen record:db.parcels().backupSeen())seen.put(new JSONObject().put("fingerprint",record.fingerprint).put("expiresAt",record.expiresAt));}catch(Exception e){throw new IllegalStateException(e);}});
  return new JSONObject().put("schema",1).put("createdAt",System.currentTimeMillis()).put("preferences",prefs).put("tasks",files).put("parcels",parcels).put("seen",seen);
 }
 private static JSONObject encodeRecord(ParcelRecord record)throws Exception{JSONObject value=new JSONObject();for(Field field:ParcelRecord.class.getFields())if(!Modifier.isStatic(field.getModifiers()))value.put(field.getName(),field.get(record));return value;}
 private static ParcelRecord decodeRecord(JSONObject value)throws Exception{
  ParcelRecord record=new ParcelRecord();for(Field field:ParcelRecord.class.getFields())if(!Modifier.isStatic(field.getModifiers())){Object v=value.get(field.getName());if(field.getType()==String.class){if(!(v instanceof String)||((String)v).length()>200000)throw new IOException("快递字段无效");field.set(record,v);}else if(field.getType()==boolean.class){if(!(v instanceof Boolean))throw new IOException("快递状态无效");field.setBoolean(record,(Boolean)v);}else{if(!(v instanceof Integer)&&!(v instanceof Long))throw new IOException("快递数值无效");long n=((Number)v).longValue();if(n<0||field.getType()==int.class&&n>Integer.MAX_VALUE)throw new IOException("快递数值超出范围");if(field.getType()==int.class)field.setInt(record,(int)n);else field.setLong(record,n);}}
  if(record.id<=0)throw new IOException("快递标识无效");ParcelRepository.validate(record);if(!record.trackingEvents.isEmpty())new JSONArray(record.trackingEvents);return record;
 }
 private static void value(SharedPreferences.Editor editor,String key,String type,Object value)throws Exception{
  switch(type){
   case "s":if(!(value instanceof String))throw new IOException("备份文本类型无效");if(editor!=null)editor.putString(key,(String)value);break;
   case "b":if(!(value instanceof Boolean))throw new IOException("备份开关类型无效");if(editor!=null)editor.putBoolean(key,(Boolean)value);break;
   case "i":if(!(value instanceof Integer))throw new IOException("备份整数类型无效");if(editor!=null)editor.putInt(key,(Integer)value);break;
   case "ss":if(!(value instanceof JSONArray))throw new IOException("备份列表类型无效");Set<String> strings=new HashSet<>();JSONArray rows=(JSONArray)value;for(int i=0;i<rows.length();i++){if(!(rows.get(i) instanceof String))throw new IOException("备份列表无效");strings.add(rows.getString(i));}if(editor!=null)editor.putStringSet(key,strings);break;
   default:throw new IOException("备份包含不支持的设置");
  }
 }
 static void validate(Context c,JSONObject data)throws Exception{
  if(data.getInt("schema")!=1)throw new IOException("不支持此数据版本，请更新 Morrow");JSONObject prefs=data.getJSONObject("preferences"),tasks=data.getJSONObject("tasks");
  for(String required:new String[]{"timetables","appSettings","parcelSettings",timetableName(c,"")})if(!prefs.has(required))throw new IOException("备份缺少必要设置");
  for(Iterator<String> it=prefs.keys();it.hasNext();){String store=it.next();if(!store.equals("timetables")&&!store.equals("appSettings")&&!store.equals("parcelSettings")&&!table(c,store))throw new IOException("备份设置范围无效");JSONObject values=prefs.getJSONObject(store);for(Iterator<String> k=values.keys();k.hasNext();){String key=k.next();String t=type(c,store,key);if(t==null)throw new IOException("备份包含未知设置");value(null,key,t,values.get(key));}
   if(table(c,store)){ClassTimes times=ClassTimes.parse(values.optString("classTimes",""));times.validate(Course.parse(values.optString("courses","")));CourseAdjustment.parse(values.optString("courseAdjustments",""));Course.parse(values.optString("syncCourses",""));if(values.has("term"))java.time.LocalDate.parse(values.getString("term"));if(values.has("aiExams"))DeepSeekPlan.examList(values.getString("aiExams"));int week=values.optInt("week",1);if(week<1||week>60)throw new IOException("课表周次无效");}
  }
  JSONObject registry=prefs.getJSONObject("timetables");Set<String> ids=new HashSet<>();ids.add("");for(String id:registry.optString("ids","").split("\n"))if(!id.isEmpty()){if(!id.matches("[0-9a-f-]{36}")||!ids.add(id)||!prefs.has(timetableName(c,id)))throw new IOException("课表索引无效");}if(!ids.contains(registry.optString("active","")))throw new IOException("当前课表不存在");
  for(Iterator<String> it=tasks.keys();it.hasNext();){String name=it.next();if(!name.matches(TASK))throw new IOException("任务文件名无效");String json=tasks.getString(name);if(json.getBytes(StandardCharsets.UTF_8).length>10*1024*1024)throw new IOException("任务缓存过大");ChaoxingCache.decode(json);}
  JSONArray parcels=data.getJSONArray("parcels"),seen=data.getJSONArray("seen");if(parcels.length()>20000||seen.length()>100000)throw new IOException("快递记录过多");Set<Long> parcelIds=new HashSet<>();for(int i=0;i<parcels.length();i++)if(!parcelIds.add(decodeRecord(parcels.getJSONObject(i)).id))throw new IOException("快递标识重复");for(int i=0;i<seen.length();i++){JSONObject row=seen.getJSONObject(i);if(row.getString("fingerprint").length()>512||row.getLong("expiresAt")<0)throw new IOException("快递去重记录无效");}
 }
 private static void write(AtomicFile file,byte[] bytes)throws IOException{FileOutputStream out=null;try{out=file.startWrite();out.write(bytes);file.finishWrite(out);out=null;if(!Arrays.equals(bytes,file.readFully()))throw new IOException("备份写入校验失败");}catch(IOException e){if(out!=null)file.failWrite(out);throw e;}}
 private static byte[] bytes(JSONObject data){return data.toString().getBytes(StandardCharsets.UTF_8);}
 private static JSONObject decrypt(Context c,byte[] encrypted,char[] password)throws Exception{byte[] plain=BackupCrypto.decrypt(encrypted,password);try{JSONObject data=new JSONObject(new String(plain,StandardCharsets.UTF_8));validate(c,data);return data;}finally{Arrays.fill(plain,(byte)0);}}
 static JSONObject open(Context c,InputStream in,char[] password)throws Exception{return decrypt(c,BackupCrypto.read(in),password);}
 static byte[] export(Context c,char[] password)throws Exception{JSONObject data=snapshot(c,Collections.emptySet());validate(c,data);byte[] plain=bytes(data);try{return BackupCrypto.encrypt(plain,password);}finally{Arrays.fill(plain,(byte)0);}}
 static void stage(Context c,JSONObject data)throws Exception{
  validate(c,data);String secret=UUID.randomUUID().toString()+UUID.randomUUID();DeepSeekKey key=new DeepSeekKey(c,"local_backup_key");key.save(secret);char[] password=secret.toCharArray();byte[] plain=bytes(data);
  try{write(file(c,"restore-pending"),BackupCrypto.encrypt(plain,password));}finally{Arrays.fill(password,'\0');Arrays.fill(plain,(byte)0);}
 }
 private static Set<String> stores(JSONObject data)throws JSONException{Set<String> names=new HashSet<>();data.getJSONObject("preferences").keys().forEachRemaining(names::add);return names;}
 private static void apply(Context c,JSONObject data)throws Exception{
  JSONObject prefs=data.getJSONObject("preferences"),tasks=data.getJSONObject("tasks");
  ParcelDatabase db=ParcelDatabase.get(c);db.runInTransaction(()->{try{
   for(String store:stores(data)){SharedPreferences pref=c.getSharedPreferences(store,0);SharedPreferences.Editor edit=pref.edit();for(String key:pref.getAll().keySet())if(type(c,store,key)!=null)edit.remove(key);JSONObject values=prefs.getJSONObject(store);for(Iterator<String> it=values.keys();it.hasNext();){String key=it.next();value(edit,key,type(c,store,key),values.get(key));}if(!edit.commit())throw new IOException("设置写入失败");}
   ChaoxingCache.clearAll(c);for(Iterator<String> it=tasks.keys();it.hasNext();){String name=it.next();write(new AtomicFile(new File(c.getFilesDir(),name)),tasks.getString(name).getBytes(StandardCharsets.UTF_8));}
   db.parcels().clear();db.parcels().forget();JSONArray parcels=data.getJSONArray("parcels");for(int i=0;i<parcels.length();i++)db.parcels().insert(decodeRecord(parcels.getJSONObject(i)));JSONArray seen=data.getJSONArray("seen");for(int i=0;i<seen.length();i++){JSONObject row=seen.getJSONObject(i);ParcelDatabase.Seen record=new ParcelDatabase.Seen();record.fingerprint=row.getString("fingerprint");record.expiresAt=row.getLong("expiresAt");db.parcels().remember(record);}
  }catch(Exception e){throw new IllegalStateException("恢复写入失败",e);}});
 }
 // Runs before workers, services or activities start; a durable encrypted journal survives process death.
 static String recover(Context c)throws Exception{
  AtomicFile pending=file(c,"restore-pending"),journal=file(c,"restore-journal");if(!exists(pending)&&!exists(journal))return null;
  char[] password=null;try{password=new DeepSeekKey(c,"local_backup_key").load().toCharArray();return recover(c,password);}
  catch(Exception e){if(exists(journal))throw e;pending.delete();return "恢复准备文件无效，未更改现有数据。请重新选择备份。";}finally{if(password!=null)Arrays.fill(password,'\0');}
 }
 static String recover(Context c,char[] password)throws Exception{
  AtomicFile pending=file(c,"restore-pending"),journal=file(c,"restore-journal");
  try{
   if(exists(journal)){JSONObject previous=decrypt(c,journal.readFully(),password);apply(c,previous);pending.delete();if(exists(pending))throw new IOException("待恢复文件无法删除");journal.delete();if(exists(journal))throw new IOException("恢复日志无法删除");return "上次恢复未完成，已保留恢复前的数据。";}
   JSONObject incoming=decrypt(c,pending.readFully(),password),previous=snapshot(c,stores(incoming));validate(c,previous);byte[] plain=bytes(previous);try{write(journal,BackupCrypto.encrypt(plain,password));}finally{Arrays.fill(plain,(byte)0);}
   try{apply(c,incoming);pending.delete();if(exists(pending))throw new IOException("待恢复文件无法删除");journal.delete();if(exists(journal))throw new IOException("恢复日志无法删除");return "本地备份已恢复。学习通缓存需登录原账号后查看，系统授权请重新核对。";}
   catch(Exception e){apply(c,previous);pending.delete();if(exists(pending))throw new IOException("待恢复文件无法删除");journal.delete();if(exists(journal))throw new IOException("恢复日志无法删除");return "恢复失败，已保留恢复前的数据。请检查存储空间后重试。";}
  }catch(Exception e){if(exists(journal))throw e;pending.delete();return "恢复准备文件无效，未更改现有数据。请重新选择备份。";}
 }
}
