package cn.courselens;

import android.content.Intent;
import android.view.View;
import android.os.Looper;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.ReflectionHelpers.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=34)
public class SchoolAccountNavigationTest {
 @Test public void accountEntryOpensPageAndSchoolPickerReturnsToIt()throws Exception {
  for(String source:new String[]{"web","squ","xzit"}){
   try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
    MainActivity a=controller.get();setField(a,"loginSchool",source);int[] prepared={0};
    setField(a,"school",new SchoolClient(){@Override public void prepare(){prepared[0]++;}});
    callInstanceMethod(a,"menu");settle();View settings=getField(a,"root");settings.findViewWithTag("school-account").performClick();settle();
    ExecutorService worker=getField(a,"worker");worker.submit(()->{}).get();settle();
    View page=getField(a,"root");assertEquals("学校账号",page.getTag());assertNull(getField(a,"navigationOverlay"));
    View selector=page.findViewWithTag("select-school");assertNotNull(selector);
    assertEquals(source.equals("web")?0:1,prepared[0]);
    assertEquals(!source.equals("web"),page.findViewWithTag("school-password-input")!=null);
    assertEquals(source.equals("web"),page.findViewWithTag("school-web-login")!=null);
    selector.performClick();settle();MainActivity.NavigationOverlay picker=getField(a,"navigationOverlay");
    assertSame(selector,picker.anchor);assertNotNull(picker.body.findViewWithTag("school-web"));assertNotNull(picker.body.findViewWithTag("school-squ"));assertNotNull(picker.body.findViewWithTag("school-xzit"));
    a.onBackPressed();settle();assertSame(page,getField(a,"root"));
    a.onBackPressed();settle();assertSame(settings,getField(a,"root"));assertEquals(source,getField(a,"loginSchool"));
   }
  }
 }
 @Test public void webAccountPageOpensBrowserForCurrentTimetable(){
  try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=controller.get();setField(a,"loginSchool","web");callInstanceMethod(a,"menu");settle();
   ((View)getField(a,"root")).findViewWithTag("school-account").performClick();settle();
   ((View)getField(a,"root")).findViewWithTag("school-web-login").performClick();
   Intent intent=shadowOf(a).getNextStartedActivity();assertEquals(WebTimetableActivity.class.getName(),intent.getComponent().getClassName());
   assertEquals(Timetables.active(a),intent.getStringExtra("timetable"));assertEquals((String)getField(a,"data"),intent.getStringExtra("baseline"));
  }
 }
 @Test public void switchingAdaptedSchoolsDoesNotAnimateTheAccountPage(){
  try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=controller.get();setField(a,"loginSchool","squ");
   ExecutorService original=getField(a,"worker");original.shutdownNow();
   // Keep this navigation check offline; school connection behavior is tested separately.
   setField(a,"worker",new java.util.concurrent.ThreadPoolExecutor(0,1,0,java.util.concurrent.TimeUnit.MILLISECONDS,new java.util.concurrent.LinkedBlockingQueue<>()){@Override public void execute(Runnable task){}});
   callInstanceMethod(a,"menu");settle();((View)getField(a,"root")).findViewWithTag("school-account").performClick();settle();
   View content=a.findViewById(android.R.id.content);content.measure(View.MeasureSpec.makeMeasureSpec(1080,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1920,View.MeasureSpec.EXACTLY));content.layout(0,0,1080,1920);
   for(String id:new String[]{"xzit","squ"}){
    ((View)getField(a,"root")).findViewWithTag("select-school").performClick();settle();
    MainActivity.NavigationOverlay picker=getField(a,"navigationOverlay");picker.body.findViewWithTag("school-"+id).performClick();
    PageMotion motion=getField(a,"pageMotion");assertFalse("School switching must not animate the page",motion.isRunning());
    assertEquals(id,getField(a,"loginSchool"));assertFalse((Boolean)getField(a,"skipPageMotion"));
    View page=getField(a,"root");assertEquals("学校账号",page.getTag());assertNotNull(page.findViewWithTag("school-password-input"));
    android.widget.LinearLayout selector=page.findViewWithTag("select-school");android.widget.LinearLayout labels=(android.widget.LinearLayout)selector.getChildAt(1);
    assertEquals(id.equals("xzit")?"徐州工程学院":"宿迁学院",((android.widget.TextView)labels.getChildAt(0)).getText().toString());
    settle();content.measure(View.MeasureSpec.makeMeasureSpec(1080,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1920,View.MeasureSpec.EXACTLY));content.layout(0,0,1080,1920);
   }
  }
 }
 private static void settle(){shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(400));}
}
