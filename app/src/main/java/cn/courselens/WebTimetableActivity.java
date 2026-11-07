package cn.courselens;

import android.app.*;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.*;
import android.widget.*;
import java.net.URI;
import java.util.*;
import org.json.JSONTokener;

/** User-driven login; pages never receive a native JavaScript bridge. */
public final class WebTimetableActivity extends Activity {
 private final java.util.concurrent.ExecutorService worker=java.util.concurrent.Executors.newSingleThreadExecutor();private boolean extracting;private int pageGeneration;
 private Button previous;private boolean sessionReady;private WebView web;private EditText address;private TextView status;private SecondaryUi ui;
 static boolean allowed(String value){try{URI uri=new URI(value);return ("https".equalsIgnoreCase(uri.getScheme())||"http".equalsIgnoreCase(uri.getScheme()))&&uri.getHost()!=null&&uri.getRawUserInfo()==null&&(uri.getPort()==-1||uri.getPort()>0&&uri.getPort()<=65535);}catch(Exception e){return false;}}
 @Override protected void attachBaseContext(android.content.Context base){super.attachBaseContext(Appearance.wrap(base));}
 @Override public void onCreate(Bundle state){
  super.onCreate(state);Appearance.edgeToEdge(this);ui=new SecondaryUi(this);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(0,0,0,0);body.setBackgroundColor(ui.SURFACE);setContentView(body);
  androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(body,(v,insets)->{androidx.core.graphics.Insets bars=insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()|androidx.core.view.WindowInsetsCompat.Type.ime());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return insets;});
  LinearLayout controls=new LinearLayout(this);controls.setOrientation(LinearLayout.VERTICAL);controls.setPadding(ui.dp(12),0,ui.dp(12),0);body.addView(controls);
  LinearLayout navigation=ui.actionRow(controls);LinearLayout.LayoutParams navBounds=(LinearLayout.LayoutParams)navigation.getLayoutParams();navBounds.setMargins(0,ui.dp(4),0,ui.dp(4));
  previous=ui.action(navigation,"上一页",()->{if(web!=null&&web.canGoBack())web.goBack();});previous.setTag("web-previous");previous.setEnabled(false);ui.action(navigation,"识别课表",this::extract);ui.action(navigation,"关闭网页",this::finish);
  LinearLayout location=new LinearLayout(this);location.setGravity(android.view.Gravity.CENTER_VERTICAL);controls.addView(location);address=new EditText(this);address.setHint("http(s)://教务网站地址");address.setContentDescription("教务网站地址");address.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);address.setSingleLine(true);address.setMinimumHeight(ui.dp(48));location.addView(address,new LinearLayout.LayoutParams(0,-2,1));
  ui.action(location,"打开",()->{if(!sessionReady)return;String url=address.getText().toString().trim();if(!allowed(url)){status.setText("请输入有效的 HTTP 或 HTTPS 教务网址。");return;}web.loadUrl(url);address.clearFocus();((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(address.getWindowToken(),0);});
  status=ui.text("自行登录至课表页后识别；HTTP 连接不加密。",12,ui.MUTED);status.setAccessibilityLiveRegion(android.view.View.ACCESSIBILITY_LIVE_REGION_POLITE);controls.addView(status);
  web=new WebView(this);web.setTag("web-timetable-content");web.getSettings().setUseWideViewPort(true);web.getSettings().setLoadWithOverviewMode(true);web.getSettings().setSupportZoom(true);web.getSettings().setBuiltInZoomControls(true);web.getSettings().setDisplayZoomControls(false);web.getSettings().setJavaScriptEnabled(true);web.getSettings().setDomStorageEnabled(true);web.getSettings().setAllowFileAccess(false);web.getSettings().setAllowContentAccess(false);web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);web.getSettings().setSaveFormData(false);CookieManager.getInstance().setAcceptThirdPartyCookies(web,false);
  web.setWebViewClient(new WebViewClient(){@Override public void onPageStarted(WebView view,String url,android.graphics.Bitmap favicon){pageGeneration++;}@Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){boolean block=!allowed(request.getUrl().toString());if(block)status.setText("仅支持 HTTP / HTTPS 网页，不能打开外部应用。");return block;}@Override public void onPageFinished(WebView view,String url){address.setText(url);previous.setEnabled(view.canGoBack());}@Override public void doUpdateVisitedHistory(WebView view,String url,boolean reload){previous.setEnabled(view.canGoBack());}@Override public void onReceivedSslError(WebView view,android.webkit.SslErrorHandler handler,android.net.http.SslError error){handler.cancel();status.setText("网站证书异常，已停止连接。");}});
  body.addView(web,new LinearLayout.LayoutParams(-1,0,1));WebStorage.getInstance().deleteAllData();CookieManager.getInstance().removeAllCookies(done->{if(isFinishing()||isDestroyed())return;sessionReady=true;String url=getIntent().getStringExtra("url");if(url!=null&&allowed(url)){address.setText(url);web.loadUrl(url);}});
 }
 static String origin(String url){URI uri=URI.create(url);return uri.getScheme()+"://"+uri.getRawAuthority()+"/";}
 private void extract(){
  if(!allowed(web.getUrl())){status.setText("请先打开教务课表网页。");return;}
  if(extracting)return;extracting=true;int token=pageGeneration;status.setText("正在识别教务系统并读取课表…");
  try(java.io.Reader reader=new java.io.InputStreamReader(getAssets().open("web-timetable-extract.js"),java.nio.charset.StandardCharsets.UTF_8)){StringBuilder script=new StringBuilder();char[] buffer=new char[2048];int count;while((count=reader.read(buffer))!=-1)script.append(buffer,0,count);web.evaluateJavascript(script.toString().replace("__MORROW_REQUEST_ID__",""+token),ignored->pollExtraction(token,0));}catch(Exception e){extracting=false;status.setText("无法读取课表，请重新打开网页导入。");}
 }
 private void pollExtraction(int token,int attempt){
  if(isFinishing()||isDestroyed())return;if(token!=pageGeneration||attempt>=60){extracting=false;status.setText(token!=pageGeneration?"网页已切换，请重新识别课表。":"读取超时，请等待课表加载完成后重试。");return;}
  web.evaluateJavascript("JSON.stringify(window.__morrowCourseRead||null)",value->{
   if(isFinishing()||isDestroyed())return;if(token!=pageGeneration){extracting=false;return;}
   try{Object decoded=new JSONTokener(value).nextValue();if(!(decoded instanceof String)||((String)decoded).length()>1200000)throw new IllegalArgumentException("页面过大，请进入单独的个人课表页。");org.json.JSONObject job=new org.json.JSONObject((String)decoded);if(job.getInt("id")!=token)throw new IllegalArgumentException("网页状态已变化，请重新识别。");
    org.json.JSONArray pages=job.optJSONArray("pages");if(pages==null){web.postDelayed(()->pollExtraction(token,attempt+1),250);return;}
    extracting=false;Set<String> rows=new LinkedHashSet<>();Set<String> systems=new LinkedHashSet<>();Set<String> parsedUrls=new HashSet<>();String failure=job.optString("error","");
    for(int i=0;i<pages.length();i++){org.json.JSONObject page=pages.getJSONObject(i);String url=page.getString("url"),content=page.getString("html");if(!allowed(url)||parsedUrls.contains(url))continue;if(SchoolClient.webPage(url)){extractSchool(url,content);return;}
     WebCourseParser.Result parsed=WebCourseParser.parse(content);if(!parsed.courses.isEmpty()){for(Course course:Course.parse(parsed.courses))rows.add(course.encode());systems.add(parsed.system);parsedUrls.add(url);}
    }
    if(rows.isEmpty())throw new IllegalArgumentException(failure.isEmpty()?"暂不支持当前课表格式，请进入完整个人课表或列表模式后重试。":failure);
    String courses=String.join("\n",rows);status.setText("已识别："+String.join("、",systems)+"，请核对课程与周次。");preview(courses,Course.parse(courses),getIntent().getStringExtra("times"));
   }catch(Exception e){extracting=false;String message=e.getMessage();status.setText((message!=null&&(message.startsWith("已识别")||message.startsWith("页面过大")||message.startsWith("暂不支持")||message.startsWith("课表接口")||message.startsWith("URP")||message.startsWith("网页状态"))?message:"课表识别失败，当前页面格式暂不支持，请切换列表模式后重试。")+" 原课表保留。");}
  });
 }

 private void extractSchool(String url,String html){
  String session=CookieManager.getInstance().getCookie(url);int token=pageGeneration;extracting=true;status.setText("正在读取宿迁学院课表数据…");
  worker.execute(()->{SchoolClient client=new SchoolClient();try{String courses=client.syncWebPage(url,html,session);List<Course> parsed=Course.parse(courses);if(parsed.isEmpty())throw new IllegalArgumentException();client.classTimes.validate(parsed);runOnUiThread(()->{extracting=false;if(isFinishing()||isDestroyed())return;if(token!=pageGeneration){status.setText("网页已切换，请重新识别课表。");return;}status.setText("课表已识别，请核对预览。");preview(courses,parsed,client.classTimes.encode());});}catch(Exception e){runOnUiThread(()->{extracting=false;if(!isFinishing()&&!isDestroyed()&&token==pageGeneration)status.setText("宿迁学院课表读取失败，请确认已登录并进入个人课表页后重试；原课表保留。");});}finally{client.logout();}});
 }

 private void preview(String courses,List<Course> parsed,String initialTimes){
  LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(ui.dp(20),ui.dp(12),ui.dp(20),0);
  StringBuilder summary=new StringBuilder("识别到 "+parsed.size()+" 个时段，请核对星期、节次和周次。\n");for(Course c:parsed)summary.append(c.name).append(" · 星期").append(c.day).append(" · ").append(c.start).append('-').append(c.end).append("节 · ").append(c.weeks).append("周 · ").append(c.room).append('\n');body.addView(ui.text(summary.toString(),14,ui.INK));
  body.addView(ui.text("作息时间（每行 HH:mm-HH:mm，请按学校实际时间修改）",14,ui.MUTED));EditText times=new EditText(this);times.setContentDescription("学校作息时间");times.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);times.setText(initialTimes);body.addView(times);TextView error=ui.text("",14,ui.DANGER);body.addView(error);ScrollView scroll=new ScrollView(this);scroll.addView(body);
  AlertDialog dialog=new AlertDialog.Builder(this,R.style.SecondaryDialog).setTitle("预览课表").setView(scroll).setNegativeButton("继续浏览",null).setPositiveButton("确认导入",null).create();dialog.show();ui.styleDialog(dialog,false);dialog.getButton(-1).setOnClickListener(v->{try{ClassTimes classTimes=ClassTimes.parse(times.getText().toString());classTimes.validate(parsed);setResult(RESULT_OK,new Intent().putExtra("courses",courses).putExtra("times",classTimes.encode()).putExtra("url",origin(web.getUrl())).putExtra("timetable",getIntent().getStringExtra("timetable")).putExtra("baseline",getIntent().getStringExtra("baseline")));dialog.dismiss();finish();}catch(Exception e){error.setText(e.getMessage());}});
 }
 @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
 @Override protected void onDestroy(){worker.shutdownNow();if(web!=null){web.stopLoading();((android.view.ViewGroup)web.getParent()).removeView(web);web.destroy();}super.onDestroy();}
}
