package cn.courselens;

import android.content.*;import android.database.Cursor;import android.os.*;import android.provider.CalendarContract;import android.content.pm.PackageManager;import org.json.*;

final class AiPhoneContext {
 static SharedPreferences settings(Context context){return context.getSharedPreferences("ai_phone_context",0);}
 static JSONObject read(Context context)throws Exception{
  JSONObject result=new JSONObject().put("model",Build.MODEL).put("android",Build.VERSION.RELEASE).put("language",java.util.Locale.getDefault().toLanguageTag());Intent battery=context.registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));if(battery!=null){int scale=battery.getIntExtra(BatteryManager.EXTRA_SCALE,0),level=battery.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);if(scale>0&&level>=0)result.put("battery_percent",level*100/scale);result.put("charging",battery.getIntExtra(BatteryManager.EXTRA_PLUGGED,0)!=0);}
  result.put("calendar",calendar(context)).put("notifications",AiNotificationListener.snapshot(context)).put("screen",new JSONObject().put("sharing",AiScreenService.active).put("available",AiScreenService.frame()!=null).put("captured_at_epoch_ms",AiScreenService.capturedAt));return result;
 }
 static JSONObject calendar(Context context)throws Exception{
  JSONObject result=new JSONObject().put("available",false);if(!settings(context).getBoolean("calendar",false)){return result.put("reason","未启用日历上下文");}if(context.checkSelfPermission(android.Manifest.permission.READ_CALENDAR)!=PackageManager.PERMISSION_GRANTED)return result.put("reason","没有日历读取权限");
  long from=System.currentTimeMillis()-86400000L,until=from+31L*86400000;android.net.Uri.Builder uri=CalendarContract.Instances.CONTENT_URI.buildUpon();ContentUris.appendId(uri,from);ContentUris.appendId(uri,until);JSONArray events=new JSONArray();boolean truncated=false;try(Cursor cursor=context.getContentResolver().query(uri.build(),new String[]{CalendarContract.Instances.TITLE,CalendarContract.Instances.BEGIN,CalendarContract.Instances.END,CalendarContract.Instances.EVENT_LOCATION,CalendarContract.Instances.ALL_DAY},null,null,CalendarContract.Instances.BEGIN+" ASC")){if(cursor==null)return result.put("reason","日历服务不可用");while(cursor.moveToNext()){if(events.length()>=100){truncated=true;break;}events.put(new JSONObject().put("title",limit(cursor.getString(0),300)).put("start_epoch_ms",cursor.getLong(1)).put("end_epoch_ms",cursor.getLong(2)).put("location",limit(cursor.getString(3),300)).put("all_day",cursor.getInt(4)!=0));}}catch(RuntimeException e){return result.put("reason","日历读取失败");}return result.put("available",true).put("from_epoch_ms",from).put("until_epoch_ms",until).put("truncated",truncated).put("events",events);
 }
 static String limit(String value,int max){if(value==null)return "";return value.substring(0,Math.min(max,value.length()));}
}
