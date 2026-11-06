package cn.courselens;

import android.content.*;import android.content.pm.*;import android.media.AudioManager;import android.net.Uri;import android.provider.AlarmClock;import android.provider.Settings;import org.json.*;import java.util.*;

final class AiPhoneActions {
 static String describe(JSONObject value)throws Exception{
  if(value==null)throw new IllegalArgumentException("缺少手机操作");String action=value.getString("action");
  if(action.equals("launch_app")){DeepSeekPlan.fields(value,"action","app");String app=DeepSeekPlan.str(value,"app","");if(app.isEmpty())throw new IllegalArgumentException("应用未指定");return "打开手机应用："+app;}
  if(action.equals("set_alarm")){DeepSeekPlan.fields(value,"action","hour","minute","label");int hour=DeepSeekPlan.integer(value,"hour",-1),minute=DeepSeekPlan.integer(value,"minute",-1);if(hour<0||hour>23||minute<0||minute>59)throw new IllegalArgumentException("请提供完整闹钟时间");java.time.LocalDateTime now=java.time.LocalDateTime.now(),next=now.toLocalDate().atTime(hour,minute);if(!next.isAfter(now))next=next.plusDays(1);return "交给系统时钟设置 "+next.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))+" 闹钟："+DeepSeekPlan.str(value,"label","");}
  if(action.equals("set_timer")){DeepSeekPlan.fields(value,"action","seconds","label");int seconds=DeepSeekPlan.integer(value,"seconds",0);if(seconds<1||seconds>86400)throw new IllegalArgumentException("计时器应为1秒至24小时");return "交给系统时钟设置 "+seconds+" 秒计时器："+DeepSeekPlan.str(value,"label","");}
  if(action.equals("dial")){DeepSeekPlan.fields(value,"action","number");String number=DeepSeekPlan.str(value,"number","");if(!number.matches("\\+?[0-9][0-9 -]{1,24}"))throw new IllegalArgumentException("请提供普通电话号码");return "打开拨号器并填写号码（不会直接拨出）："+number;}
  if(action.equals("open_settings")){DeepSeekPlan.fields(value,"action","setting");String setting=value.getString("setting");if(!Arrays.asList("wifi","bluetooth","display","sound","battery","app").contains(setting))throw new IllegalArgumentException("不支持此设置入口");return "打开手机系统设置："+setting;}
  if(action.equals("set_volume")){DeepSeekPlan.fields(value,"action","percent");int percent=DeepSeekPlan.integer(value,"percent",-1);if(percent<0||percent>100)throw new IllegalArgumentException("音量应为0至100%");return "将手机媒体音量调为 "+percent+"%";}
  throw new IllegalArgumentException("不支持此手机操作");
 }
 static Intent intent(Context context,JSONObject value)throws Exception{
  describe(value);String action=value.getString("action");if(action.equals("launch_app")){String app=value.getString("app"),pkg=null;for(ResolveInfo info:context.getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0)){if(info.activityInfo==null)continue;String label=info.loadLabel(context.getPackageManager()).toString();if(info.activityInfo.packageName.equals(app)||label.equalsIgnoreCase(app)){if(pkg!=null&&!pkg.equals(info.activityInfo.packageName))throw new IllegalArgumentException("应用名称有歧义，请提供完整包名");pkg=info.activityInfo.packageName;}}if(pkg==null)throw new IllegalArgumentException("未找到该已安装应用");Intent launch=context.getPackageManager().getLaunchIntentForPackage(pkg);if(launch==null)throw new IllegalArgumentException("该应用没有可打开的入口");return launch;}
  if(action.equals("set_alarm"))return new Intent(AlarmClock.ACTION_SET_ALARM).putExtra(AlarmClock.EXTRA_HOUR,value.getInt("hour")).putExtra(AlarmClock.EXTRA_MINUTES,value.getInt("minute")).putExtra(AlarmClock.EXTRA_MESSAGE,value.optString("label")).putExtra(AlarmClock.EXTRA_SKIP_UI,false);
  if(action.equals("set_timer"))return new Intent(AlarmClock.ACTION_SET_TIMER).putExtra(AlarmClock.EXTRA_LENGTH,value.getInt("seconds")).putExtra(AlarmClock.EXTRA_MESSAGE,value.optString("label")).putExtra(AlarmClock.EXTRA_SKIP_UI,false);
  if(action.equals("dial"))return new Intent(Intent.ACTION_DIAL,Uri.fromParts("tel",value.getString("number"),null));
  if(action.equals("open_settings")){String name=value.getString("setting");String target=name.equals("wifi")?Settings.ACTION_WIFI_SETTINGS:name.equals("bluetooth")?Settings.ACTION_BLUETOOTH_SETTINGS:name.equals("display")?Settings.ACTION_DISPLAY_SETTINGS:name.equals("sound")?Settings.ACTION_SOUND_SETTINGS:name.equals("battery")?Settings.ACTION_BATTERY_SAVER_SETTINGS:Settings.ACTION_APPLICATION_DETAILS_SETTINGS;Intent result=new Intent(target);if(name.equals("app"))result.setData(Uri.parse("package:"+context.getPackageName()));return result;}
  throw new IllegalArgumentException("该操作不通过外部应用执行");
 }
 static void execute(android.app.Activity activity,JSONObject value)throws Exception{describe(value);if(value.getString("action").equals("set_volume")){AudioManager audio=activity.getSystemService(AudioManager.class);int max=audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);audio.setStreamVolume(AudioManager.STREAM_MUSIC,Math.round(max*value.getInt("percent")/100f),AudioManager.FLAG_SHOW_UI);}else activity.startActivity(intent(activity,value));}
}
