package cn.courselens;

import android.content.Context;
import android.os.*;
import android.webkit.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import org.json.*;

/** A user-triggered, same-origin read using the browser's existing session. */
final class WebTimetableSync {
 interface Callback {void complete(String courses,ClassTimes times,String error);}
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final ExecutorService worker;private final Callback callback;private final String origin,script;
 private WebView web;private int generation;private boolean done,reading;
 private final Runnable timeout=()->finish(null,null,"同步超时，请在学校账号中重新打开课表网页后重试。");
 WebTimetableSync(Context context,String url,ExecutorService worker,Callback callback)throws IOException {
  if(!WebTimetableActivity.allowed(url))throw new IOException("同步来源地址无效，请重新导入。");
  this.worker=worker;this.callback=callback;origin=WebTimetableActivity.origin(url);
  try(InputStream in=context.getAssets().open("web-timetable-extract.js");ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buffer=new byte[4096];int count;while((count=in.read(buffer))!=-1)out.write(buffer,0,count);script=new String(out.toByteArray(),StandardCharsets.UTF_8);}
  web=new WebView(context);WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);settings.setSaveFormData(false);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);CookieManager.getInstance().setAcceptThirdPartyCookies(web,false);
  web.setWebViewClient(new WebViewClient(){
   @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){return !sameOrigin(request.getUrl().toString());}
   @Override public void onPageStarted(WebView view,String at,android.graphics.Bitmap favicon){generation++;reading=false;if(!sameOrigin(at))finish(null,null,"学校登录已失效，请重新登录后同步。");}
   @Override public void onPageFinished(WebView view,String at){if(!done&&!reading&&sameOrigin(at)){reading=true;extract(generation);}}
   @Override public void onReceivedSslError(WebView view,SslErrorHandler handler,android.net.http.SslError error){handler.cancel();finish(null,null,"学校网站证书异常，已停止同步。");}
   @Override public void onReceivedError(WebView view,WebResourceRequest request,WebResourceError error){if(request.isForMainFrame())finish(null,null,"学校网页暂时无法连接，请稍后重试。");}
  });
  handler.postDelayed(timeout,30000);web.loadUrl(url);
 }
 private boolean sameOrigin(String url){return WebTimetableActivity.allowed(url)&&origin.equalsIgnoreCase(WebTimetableActivity.origin(url));}
 private void extract(int token){
  if(done||token!=generation)return;
  web.evaluateJavascript(script.replace("__MORROW_REQUEST_ID__",""+token),ignored->poll(token));
 }
 private void poll(int token){
  if(done||token!=generation)return;
  web.evaluateJavascript("JSON.stringify(window.__morrowCourseRead||null)",value->{
   if(done||token!=generation)return;
   try{
    Object decoded=new JSONTokener(value).nextValue();if(!(decoded instanceof String)||((String)decoded).length()>1200000)throw new IOException();
    JSONObject job=new JSONObject((String)decoded);if(job.getInt("id")!=token)throw new IOException();
    JSONArray pages=job.optJSONArray("pages");if(pages==null){handler.postDelayed(()->poll(token),250);return;}
    String cookie=null;JSONObject schoolPage=null;
    for(int i=0;i<pages.length();i++){JSONObject page=pages.getJSONObject(i);if(sameOrigin(page.getString("url"))&&SchoolClient.webPage(page.getString("url"))){schoolPage=page;cookie=CookieManager.getInstance().getCookie(page.getString("url"));break;}}
    JSONObject selected=schoolPage;String session=cookie;
    worker.execute(()->{
     SchoolClient client=new SchoolClient();String courses=null;ClassTimes times=null;
     try{if(selected!=null){courses=client.syncWebPage(selected.getString("url"),selected.getString("html"),session);times=client.classTimes;}else courses=parsePages(pages,origin);if(Course.parse(courses).isEmpty())throw new IOException();}
     catch(Exception invalid){handler.post(()->{if(!done&&token==generation)handler.postDelayed(()->extract(token),1000);});return;}
     finally{client.logout();}
     String ready=courses;ClassTimes readyTimes=times;handler.post(()->{if(!done&&token==generation)finish(ready,readyTimes,null);});
    });
   }catch(Exception invalid){finish(null,null,"未能读取来源课表，请重新登录并进入课表页后重试。");}
  });
 }
 static String parsePages(JSONArray pages,String origin)throws Exception {
  Set<String> rows=new LinkedHashSet<>();
  for(int i=0;i<pages.length();i++){
   JSONObject page=pages.getJSONObject(i);String url=page.getString("url");
   if(!WebTimetableActivity.allowed(url)||!origin.equalsIgnoreCase(WebTimetableActivity.origin(url)))continue;
   WebCourseParser.Result parsed=WebCourseParser.parse(page.getString("html"));for(Course course:Course.parse(parsed.courses))rows.add(course.encode());
  }
  if(rows.isEmpty())throw new IOException("来源未返回课程");return String.join("\n",rows);
 }
 void cancel(){if(done)return;done=true;release();}
 private void finish(String courses,ClassTimes times,String error){if(done)return;done=true;release();callback.complete(courses,times,error);}
 private void release(){handler.removeCallbacksAndMessages(null);if(web!=null){web.stopLoading();web.destroy();web=null;}}
}
