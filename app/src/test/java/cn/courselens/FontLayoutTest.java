package cn.courselens;

import android.app.*;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import android.graphics.*;
import java.io.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;
import static org.robolectric.util.ReflectionHelpers.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class FontLayoutTest {
 private static void layout(View view,int width){view.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));view.layout(0,0,width,view.getMeasuredHeight());}
 private static void readable(TextView view){
  assertNotNull(view.getLayout());
  assertEquals("Button label is truncated: "+view.getText(),view.length(),view.getLayout().getLineEnd(view.getLayout().getLineCount()-1));
  assertTrue("Clipped button: "+view.getText()+" height="+view.getHeight()+" text="+view.getLayout().getHeight(),view.getHeight()-view.getCompoundPaddingTop()-view.getCompoundPaddingBottom()>=view.getLayout().getHeight());
  for(int i=0;i<view.getLayout().getLineCount();i++){assertEquals("Button label is ellipsized: "+view.getText(),0,view.getLayout().getEllipsisCount(i));assertTrue("Button text exceeds its width: "+view.getText(),view.getLayout().getLineWidth(i)<=view.getWidth()-view.getCompoundPaddingLeft()-view.getCompoundPaddingRight()+1);}
 }
 @Test public void sharedActionsKeepLabelsReadableAtLargeFontSizes(){
  for(String screen:new String[]{"w320dp-h720dp-xhdpi","w360dp-h800dp-560dpi","w411dp-h891dp-xxhdpi"})for(float scale:new float[]{1f,1.3f,2f}){
   RuntimeEnvironment.setQualifiers(screen);RuntimeEnvironment.setFontScale(scale);
   try(ActivityController<Activity> controller=Robolectric.buildActivity(Activity.class).setup()){
    Activity a=controller.get();a.setTheme(R.style.AppTheme);SecondaryUi ui=new SecondaryUi(a);LinearLayout body=new LinearLayout(a);body.setOrientation(LinearLayout.VERTICAL);a.setContentView(body);LinearLayout row=ui.actionRow(body);
    Button save=ui.action(row,"保存配置",()->{}),remove=ui.action(row,"删除",()->{}),cancel=ui.action(row,"取消",()->{});
    layout(body,ui.dp(a.getResources().getConfiguration().screenWidthDp-32));for(Button button:new Button[]{save,remove,cancel})readable(button);
   }
  }
 }
 @Test public void dialogActionsKeepLabelsReadableAtLargeFontSizes(){
  for(float scale:new float[]{1f,1.3f,2f}){
   RuntimeEnvironment.setQualifiers("w320dp-h720dp-xhdpi");RuntimeEnvironment.setFontScale(scale);
   try(ActivityController<Activity> controller=Robolectric.buildActivity(Activity.class).setup()){
    Activity a=controller.get();a.setTheme(R.style.AppTheme);AlertDialog dialog=new AlertDialog.Builder(a,R.style.SecondaryDialog).setMessage("合成提示").setNegativeButton("不保存退出",null).setPositiveButton("保存并退出",null).show();new SecondaryUi(a).styleDialog(dialog,false);layout(dialog.getWindow().getDecorView(),new SecondaryUi(a).dp(280));readable(dialog.getButton(-1));readable(dialog.getButton(-2));dialog.dismiss();
   }
  }
 }
 @Test @Config(sdk={33,34}) public void dialogStyleOverridesInheritedButtonAutoSizing(){
  RuntimeEnvironment.setQualifiers("w360dp-h800dp-xhdpi");RuntimeEnvironment.setFontScale(1f);
  try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=controller.get();SecondaryUi ui=new SecondaryUi(a);AlertDialog dialog=new AlertDialog.Builder(a,R.style.SecondaryDialog).setMessage("合成提示").setPositiveButton("确定",null).show();Button button=dialog.getButton(-1);
   button.setAutoSizeTextTypeUniformWithPresetSizes(new int[]{30},TypedValue.COMPLEX_UNIT_SP);
   ui.styleDialog(dialog,false);layout(dialog.getWindow().getDecorView(),ui.dp(320));
   assertEquals("Explicit app size must override inherited auto-sizing",TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,14,a.getResources().getDisplayMetrics()),button.getTextSize(),.01f);dialog.dismiss();
  }
 }
 @Test @Config(sdk={33,34}) public void explicitButtonSizesOverrideAutoSizingAndStillFollowSystemFont(){
  RuntimeEnvironment.setQualifiers("w360dp-h800dp-xhdpi");RuntimeEnvironment.setFontScale(1.3f);
  try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=controller.get();LinearLayout row=new LinearLayout(a);
   Button[] buttons={new Button(a),new RadioButton(a),new ToggleButton(a)};
   for(Button button:buttons){button.setText("合成");button.setAutoSizeTextTypeUniformWithPresetSizes(new int[]{30},TypedValue.COMPLEX_UNIT_SP);SecondaryUi.buttonTextSize(button,15);row.addView(button,new LinearLayout.LayoutParams(0,-2,1));}
   layout(row,new SecondaryUi(a).dp(328));
   for(Button button:buttons){assertEquals(TextView.AUTO_SIZE_TEXT_TYPE_NONE,button.getAutoSizeTextType());assertEquals(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,15,a.getResources().getDisplayMetrics()),button.getTextSize(),.01f);}
  }
 }
 @Test public void iconButtonsKeepTheirSizeWhenFontSizeChanges(){
  for(float scale:new float[]{1f,1.3f,2f}){
   RuntimeEnvironment.setQualifiers("w360dp-h800dp-560dpi");RuntimeEnvironment.setFontScale(scale);
   try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup()){
    MainActivity a=controller.get();SecondaryUi ui=new SecondaryUi(a);LinearLayout body=new LinearLayout(a);Button shared=ui.iconAction(body,R.drawable.ic_more,"菜单",()->{});TextView main=callInstanceMethod(a,"icon",ClassParameter.from(int.class,R.drawable.ic_more),ClassParameter.from(String.class,"菜单"),ClassParameter.from(Runnable.class,(Runnable)()->{}));body.addView(main,new LinearLayout.LayoutParams(ui.dp(48),ui.dp(48)));layout(body,ui.dp(360));
    for(TextView button:new TextView[]{shared,main}){assertEquals(ui.dp(48),button.getWidth());assertEquals(ui.dp(48),button.getHeight());assertEquals(ui.dp(24),button.getCompoundDrawables()[0].getBounds().width());assertEquals(ui.dp(24),button.getCompoundDrawables()[0].getBounds().height());assertEquals("",button.getText().toString());}
   }
  }
 }
 @Test public void timetableAndChatUseDeclaredSpWithoutExtraScaling()throws Exception{
  for(float scale:new float[]{1f,1.3f,2f}){
   RuntimeEnvironment.setQualifiers("w360dp-h800dp-xxhdpi");RuntimeEnvironment.setFontScale(scale);
   try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup()){
    MainActivity a=controller.get();setField(a,"data","1|1|2|合成课程名称|测试校区A101|1-30");setField(a,"week",1);callInstanceMethod(a,"home");View root=a.findViewById(android.R.id.content);int w=a.getResources().getDisplayMetrics().widthPixels,h=a.getResources().getDisplayMetrics().heightPixels;root.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));root.layout(0,0,w,h);TextView course=root.findViewWithTag("course-card");assertNotNull(course);assertEquals(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,12,a.getResources().getDisplayMetrics()),course.getTextSize(),.01f);
    LinearLayout history=new LinearLayout(a);TextView chat=callInstanceMethod(a,"aiMessage",ClassParameter.from(LinearLayout.class,history),ClassParameter.from(String.class,"合成聊天正文"),ClassParameter.from(boolean.class,false));assertEquals(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,16,a.getResources().getDisplayMetrics()),chat.getTextSize(),.01f);System.out.println("font="+scale+" coursePx="+course.getTextSize()+" chatPx="+chat.getTextSize());
   }
  }
 }
 @Test public void screenshotSizedLayoutAtDefaultFont()throws Exception{
  RuntimeEnvironment.setQualifiers("w360dp-h800dp-560dpi");RuntimeEnvironment.setFontScale(1f);
  try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=controller.get();setField(a,"data","1|1|2|合成课程名称|测试校区教学楼A101阶梯教室|1-30");setField(a,"week",1);callInstanceMethod(a,"home");View root=a.findViewById(android.R.id.content);snapshot(root,a,"default-timetable.png");TextView course=root.findViewWithTag("course-card");System.out.println("default course: fontPx="+course.getTextSize()+" widthPx="+course.getWidth()+" lines="+course.getLineCount());
   callInstanceMethod(a,"aiAssistant");LinearLayout history=root.findViewWithTag("ai-history");history.removeAllViews();callInstanceMethod(a,"aiMessage",ClassParameter.from(LinearLayout.class,history),ClassParameter.from(String.class,"合成消息"),ClassParameter.from(boolean.class,true));TextView chat=callInstanceMethod(a,"aiMessage",ClassParameter.from(LinearLayout.class,history),ClassParameter.from(String.class,"你好！这是用于检查文字大小的合成消息，可以查看课表、任务和快递，也可以继续聊天。"),ClassParameter.from(boolean.class,false));snapshot(root,a,"default-chat.png");System.out.println("default chat: fontPx="+chat.getTextSize()+" widthPx="+chat.getWidth()+" lines="+chat.getLineCount());
  }
 }
 private static void snapshot(View root,Activity a,String name)throws Exception{
  org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(java.time.Duration.ofMillis(600));
  int w=a.getResources().getDisplayMetrics().widthPixels,h=a.getResources().getDisplayMetrics().heightPixels;root.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.EXACTLY));root.layout(0,0,w,h);
  File folder=new File(System.getProperty("user.home"),".codex/morrow-ui-previews/font-layout");folder.mkdirs();Bitmap bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);root.draw(new Canvas(bitmap));try(FileOutputStream out=new FileOutputStream(new File(folder,name))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}finally{bitmap.recycle();}
 }
}
