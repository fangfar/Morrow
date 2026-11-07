package cn.courselens;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.*;
import android.widget.*;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=33,qualifiers="w320dp-h640dp")
public class WebTimetablePreviewTest {
 private List<Course> sample(){return Arrays.asList(new Course(5,3,4,"长课程名与完整说明测试课程","B203","1,3,5","合成教师","原文：教室 B203；班级说明","合成教学班"),new Course(1,1,2,"合成数学","A101","1,2,3,4"));}
 private AlertDialog preview(WebTimetableActivity activity,String times)throws Exception {
  var method=WebTimetableActivity.class.getDeclaredMethod("preview",String.class,List.class,String.class);method.setAccessible(true);
  List<Course> courses=sample();method.invoke(activity,courses.get(0).encode()+"\n"+courses.get(1).encode(),courses,times);return ShadowAlertDialog.getLatestAlertDialog();
 }
 private List<View> tagged(View view,String tag){List<View> found=new ArrayList<>();if(tag.equals(view.getTag()))found.add(view);if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)found.addAll(tagged(((ViewGroup)view).getChildAt(i),tag));return found;}
 private int top(View view){int value=view.getTop();while(view.getParent() instanceof View){view=(View)view.getParent();value+=view.getTop();}return value;}
 @Test public void weekdayOrderCompactWeeksAndExpandableDetails()throws Exception {
  try(var controller=Robolectric.buildActivity(WebTimetableActivity.class).setup()){
   AlertDialog dialog=preview(controller.get(),ClassTimes.DEFAULT.encode());View root=dialog.getWindow().getDecorView();List<View> rows=tagged(root,"preview-course");assertEquals(2,rows.size());
   assertEquals("合成数学",((TextView)((ViewGroup)rows.get(0)).getChildAt(0)).getText().toString());
   assertEquals("第 1–2 节 · 第 1–4 周",((TextView)((ViewGroup)rows.get(0)).getChildAt(1)).getText().toString());
   LinearLayout friday=(LinearLayout)rows.get(1);TextView toggle=(TextView)friday.getChildAt(friday.getChildCount()-2),details=(TextView)friday.getChildAt(friday.getChildCount()-1);
   assertEquals(View.GONE,details.getVisibility());toggle.performClick();assertEquals(View.VISIBLE,details.getVisibility());assertEquals(sample().get(0).details,details.getText().toString());toggle.performClick();assertEquals(View.GONE,details.getVisibility());
   EditText times=root.findViewWithTag("preview-times");assertEquals(View.GONE,((View)times.getParent()).getVisibility());root.findViewWithTag("preview-times-toggle").performClick();assertEquals(View.VISIBLE,((View)times.getParent()).getVisibility());
   assertTrue(dialog.getButton(AlertDialog.BUTTON_POSITIVE).isShown());dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();assertFalse(controller.get().isFinishing());
  }
 }
 @Test public void invalidTimesExpandEditorAndValidImportPreservesOriginalOrderAndFields()throws Exception {
  Intent intent=new Intent(RuntimeEnvironment.getApplication(),WebTimetableActivity.class).putExtra("url","https://school.example/table").putExtra("timetable","synthetic").putExtra("baseline","original");
  try(var controller=Robolectric.buildActivity(WebTimetableActivity.class,intent).setup()){
   WebTimetableActivity activity=controller.get();AlertDialog dialog=preview(activity,"invalid");View root=dialog.getWindow().getDecorView();dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();assertTrue(dialog.isShowing());assertFalse(activity.isFinishing());
   EditText times=root.findViewWithTag("preview-times");assertEquals(View.VISIBLE,((View)times.getParent()).getVisibility());assertEquals(View.VISIBLE,root.findViewWithTag("preview-error").getVisibility());
   times.setText(ClassTimes.DEFAULT.encode());dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();assertTrue(activity.isFinishing());Intent result=Shadows.shadowOf(activity).getResultIntent();
   assertEquals(sample().get(0).encode()+"\n"+sample().get(1).encode(),result.getStringExtra("courses"));assertEquals("synthetic",result.getStringExtra("timetable"));assertEquals("original",result.getStringExtra("baseline"));assertEquals("https://school.example/",result.getStringExtra("url"));
  }
 }
 @Test public void largeFontAndLongListKeepActionsOutsideScrollableContent()throws Exception {
  RuntimeEnvironment.setFontScale(2f);
  try(var controller=Robolectric.buildActivity(WebTimetableActivity.class).setup()){
   List<Course> courses=new ArrayList<>();StringJoiner encoded=new StringJoiner("\n");
   for(int i=0;i<73;i++){Course c=new Course(i%7+1,1,2,"合成课程的长名称用于检查换行"+i,"教室 A101","1,2,3,4,5,6,7,8","合成教师","完整原文补充用于展开核对");courses.add(c);encoded.add(c.encode());}
   var method=WebTimetableActivity.class.getDeclaredMethod("preview",String.class,List.class,String.class);method.setAccessible(true);method.invoke(controller.get(),encoded.toString(),courses,ClassTimes.DEFAULT.encode());
   AlertDialog dialog=ShadowAlertDialog.getLatestAlertDialog();View root=dialog.getWindow().getDecorView();int width=dialog.getWindow().getAttributes().width,height=dialog.getWindow().getAttributes().height;
   root.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));root.layout(0,0,width,height);
   ScrollView scroll=root.findViewWithTag("preview-scroll");assertEquals(73,tagged(root,"preview-course").size());assertTrue(scroll.getHeight()>0);assertTrue(scroll.getChildAt(0).getHeight()>scroll.getHeight());
   Button confirm=dialog.getButton(AlertDialog.BUTTON_POSITIVE);
   assertTrue("Actions must not be covered by the list",top(scroll)+scroll.getHeight()<=top(confirm));assertTrue(confirm.getHeight()>0);assertTrue(top(confirm)+confirm.getHeight()<=height);
  }finally{RuntimeEnvironment.setFontScale(1f);}
 }
}
