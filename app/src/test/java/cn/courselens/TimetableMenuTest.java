package cn.courselens;

import android.graphics.*;
import android.os.Looper;
import android.view.*;
import android.widget.TextView;
import java.io.*;
import java.time.Duration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.ReflectionHelpers.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class TimetableMenuTest {
 @Test public void normalMenu()throws Exception{check("w360dp-h800dp-notnight",1,"normal");}
 @Test public void narrowLargeMenu()throws Exception{check("w320dp-h640dp-notnight",2,"large");}
 @Test public void darkMenu()throws Exception{check("w360dp-h800dp-night",1,"dark");}
 private void check(String qualifiers,float font,String name)throws Exception {
  RuntimeEnvironment.setQualifiers(qualifiers);RuntimeEnvironment.setFontScale(font);
  try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=controller.get();assertEquals("本地课表 · 未绑定来源",Timetables.sourceLabel(a,""));
   Timetables.add(a,"合成学校课表","squ",ClassTimes.DEFAULT,"2026-09-01");String nativeId=Timetables.active(a);a.getPreferences(0).edit().putBoolean("schoolImported",true).commit();assertEquals("宿迁学院",Timetables.sourceLabel(a,nativeId));
   a.getPreferences(0).edit().putString("school","xzit").commit();assertEquals("徐州工程学院",Timetables.sourceLabel(a,nativeId));
   Timetables.add(a,"合成网页课表名称较长时也应完整显示","web",ClassTimes.DEFAULT,"2026-09-01");String webId=Timetables.active(a);a.getPreferences(0).edit().putBoolean("schoolImported",true).putString("webTimetableUrl","https://portal.example.edu.cn/").commit();assertEquals("网页导入 · portal.example.edu.cn",Timetables.sourceLabel(a,webId));
   callInstanceMethod(a,"reloadTimetable");settle();View content=a.findViewById(android.R.id.content);layout(content);content.findViewWithTag("timetable-settings").performClick();settle();layout(content);
   TextView summary=content.findViewWithTag("timetable-source-summary");assertTrue(summary.getContentDescription().toString().contains("portal.example.edu.cn"));assertTrue(summary.getText().toString().contains("…"));assertEquals(2,summary.getLineCount());assertTrue(((View)getField(a,"root")).getWidth()<=168*a.getResources().getDisplayMetrics().density+1);assertNotNull(content.findViewWithTag("refresh-timetable"));snapshot(a,content,name+"-menu");readable(getField(a,"root"));
   content.findViewWithTag("timetable-switch").performClick();settle();layout(content);
   TextView chosen=content.findViewWithTag("timetable-"+webId);assertTrue(chosen.isSelected());assertTrue(chosen.getContentDescription().toString().contains("portal.example.edu.cn"));
   assertTrue(((TextView)content.findViewWithTag("timetable-"+nativeId)).getContentDescription().toString().contains("徐州工程学院"));readable(getField(a,"root"));snapshot(a,content,name+"-picker");
   content.findViewWithTag("timetable-").performClick();settle();assertEquals("",Timetables.active(a));layout(content);content.findViewWithTag("timetable-settings").performClick();settle();
   assertTrue(((TextView)content.findViewWithTag("timetable-source-summary")).getContentDescription().toString().contains("未绑定来源"));assertEquals("https://portal.example.edu.cn/",Timetables.preferences(a,webId).getString("webTimetableUrl",null));
  }
 }
 private void readable(View view){
  if(view instanceof TextView){TextView text=(TextView)view;assertNotNull(text.getLayout());assertTrue(text.getLayout().getHeight()<=text.getHeight()-text.getCompoundPaddingTop()-text.getCompoundPaddingBottom()+1);assertTrue(text.getHeight()>=48*text.getResources().getDisplayMetrics().density);}
  if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++)readable(group.getChildAt(i));}
 }
 private void layout(View view){android.util.DisplayMetrics dm=view.getResources().getDisplayMetrics();view.measure(View.MeasureSpec.makeMeasureSpec(dm.widthPixels,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(dm.heightPixels,View.MeasureSpec.EXACTLY));view.layout(0,0,dm.widthPixels,dm.heightPixels);}
 private void snapshot(MainActivity a,View content,String name)throws Exception {
  String path=System.getProperty("courselens.parcelPreviewDir");if(path==null)return;File folder=new File(path);folder.mkdirs();Bitmap bitmap=Bitmap.createBitmap(content.getWidth(),content.getHeight(),Bitmap.Config.ARGB_8888);content.draw(new Canvas(bitmap));try(FileOutputStream out=new FileOutputStream(new File(folder,"timetable-"+name+".png"))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}bitmap.recycle();
 }
 private void settle(){shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(400));}
}
