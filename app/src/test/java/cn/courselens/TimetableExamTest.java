package cn.courselens;

import android.view.*;
import android.widget.*;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;
import static org.robolectric.util.ReflectionHelpers.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34)
public class TimetableExamTest {
 @Test public void examsAppearInDateColumnsWithoutCoveringCourses()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();setField(a,"term","2026-08-31");setField(a,"week",1);setField(a,"data","5|1|2|英语|A101|1-16");a.getPreferences(0).edit().putString("aiExams","[{\"name\":\"英语\",\"date\":\"2026-09-04\",\"time\":\"08:00\",\"room\":\"B201\"},{\"name\":\"数学\",\"date\":\"2026-09-04\",\"time\":\"\",\"room\":\"\"},{\"name\":\"下周考试\",\"date\":\"2026-09-07\",\"time\":\"19:00\",\"room\":\"\"}]").commit();callInstanceMethod(a,"home");View content=a.findViewById(android.R.id.content);layout(content);ViewGroup grid=content.findViewWithTag("week-grid-1");assertNotNull(grid);List<View> exams=tagged(grid,"exam-card");assertEquals(1,exams.size());assertNotNull(exams.get(0).findViewWithTag("exam-badge"));assertTrue(exams.get(0).getContentDescription().toString().contains("B201"));assertTrue(allText(exams.get(0)).contains("08:00"));assertTrue(allText(exams.get(0)).contains("结束待定"));FrameLayout.LayoutParams examBounds=(FrameLayout.LayoutParams)exams.get(0).getLayoutParams();assertTrue(examBounds.leftMargin>grid.getWidth()/2);View course=grid.findViewWithTag("course-card");assertNotNull(course);FrameLayout.LayoutParams courseBounds=(FrameLayout.LayoutParams)course.getLayoutParams();assertEquals(courseBounds.topMargin,examBounds.topMargin);assertTrue(courseBounds.leftMargin+courseBounds.width<=examBounds.leftMargin||examBounds.leftMargin+examBounds.width<=courseBounds.leftMargin);assertTrue(allText(content).contains("五考"));exams.get(0).performClick();assertTrue(org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog().isShowing());
   callInstanceMethod(a,"changeWeek",ClassParameter.from(int.class,2));layout(content);assertEquals(1,tagged(content.findViewWithTag("week-grid-2"),"exam-card").size());
  }
 }
 @Test public void examsRefreshAfterChatEvenWhenNoCoursesExist()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();setField(a,"term","2026-08-31");setField(a,"week",1);setField(a,"data","");callInstanceMethod(a,"home");View old=getField(a,"root"),content=a.findViewById(android.R.id.content);content.findViewWithTag("nav-ai").performClick();a.getPreferences(0).edit().putString("aiExams","[{\"name\":\"英语\",\"date\":\"2026-09-04\",\"time\":\"\",\"room\":\"\"}]").commit();content.findViewWithTag("nav-timetable").performClick();assertNotSame(old,getField(a,"root"));layout(content);View grid=content.findViewWithTag("week-grid-1");assertEquals(0,tagged(grid,"exam-card").size());assertTrue(allText(content).contains("五考"));assertNull(grid.findViewWithTag("timetable-empty-message"));
   content.findViewWithTag("nav-ai").performClick();a.getPreferences(0).edit().putString("aiExams","[]").commit();content.findViewWithTag("nav-timetable").performClick();layout(content);grid=content.findViewWithTag("week-grid-1");assertTrue(tagged(grid,"exam-card").isEmpty());assertNotNull(grid.findViewWithTag("timetable-empty-message"));
  }
 }
 private static void layout(View v){v.measure(View.MeasureSpec.makeMeasureSpec(1080,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1920,View.MeasureSpec.EXACTLY));v.layout(0,0,1080,1920);}
 private static List<View> tagged(View v,String tag){List<View> result=new ArrayList<>();if(tag.equals(v.getTag()))result.add(v);if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++)result.addAll(tagged(((ViewGroup)v).getChildAt(i),tag));return result;}
 private static String allText(View v){StringBuilder result=new StringBuilder();if(v instanceof TextView)result.append(((TextView)v).getText());if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++)result.append(allText(((ViewGroup)v).getChildAt(i)));return result.toString();}
}
