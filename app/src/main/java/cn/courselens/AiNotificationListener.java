package cn.courselens;

import android.app.Notification;import android.content.Context;import android.service.notification.*;import org.json.*;

public final class AiNotificationListener extends NotificationListenerService {
 private static volatile AiNotificationListener connected;
 @Override public void onListenerConnected(){super.onListenerConnected();connected=this;}
 @Override public void onListenerDisconnected(){connected=null;super.onListenerDisconnected();}
 @Override public void onDestroy(){if(connected==this)connected=null;super.onDestroy();}
 static JSONObject snapshot(Context context)throws Exception{JSONObject result=new JSONObject().put("available",false);if(!AiPhoneContext.settings(context).getBoolean("notifications",false))return result.put("reason","未启用通知上下文");AiNotificationListener listener=connected;if(listener==null)return result.put("reason","通知访问未授权或尚未连接");try{StatusBarNotification[] active=listener.getActiveNotifications();JSONArray rows=new JSONArray();if(active!=null)for(StatusBarNotification item:active){if(rows.length()>=20)break;if(item==null||item.getPackageName().equals(context.getPackageName()))continue;Notification notification=item.getNotification();if(notification==null||notification.extras==null||(notification.flags&Notification.FLAG_GROUP_SUMMARY)!=0)continue;try{CharSequence title=notification.extras.getCharSequence(Notification.EXTRA_TITLE),text=notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);if(text==null)text=notification.extras.getCharSequence(Notification.EXTRA_TEXT);String heading=AiPhoneContext.limit(title==null?null:title.toString(),200),body=AiPhoneContext.limit(text==null?null:text.toString(),800);boolean secret=(heading+body).matches("(?is).*(验证码|verification code|one.time password|一次性密码).*" );rows.put(new JSONObject().put("app",item.getPackageName()).put("posted_at_epoch_ms",item.getPostTime()).put("title",secret?"敏感验证码通知，正文未提供":heading).put("text",secret?"":body));}catch(RuntimeException ignored){}}return result.put("available",true).put("scope","当前可见通知，不包含历史通知").put("truncated",active!=null&&active.length>20).put("items",rows);}catch(RuntimeException e){return result.put("reason","通知读取失败");}}
}
