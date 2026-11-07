package cn.courselens;

import android.app.AlertDialog;
import android.view.View;
import android.widget.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;
import static org.robolectric.util.ReflectionHelpers.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34)
public class ManualExamAndControlsTest {
 private static final String EXAMS="[{\"name\":\"合成考试甲\",\"date\":\"2026-11-07\",\"time\":\"\",\"room\":\"\"},{\"name\":\"合成考试乙\",\"date\":\"2026-11-08\",\"time\":\"09:00\",\"room\":\"A101\"}]";
 private AlertDialog confirm(MainActivity a)throws Exception{callInstanceMethod(a,"examDetails",ClassParameter.from(JSONObject.class,new JSONArray(EXAMS).getJSONObject(0)));AlertDialog details=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();assertNotNull(details.getButton(AlertDialog.BUTTON_NEUTRAL));details.getButton(AlertDialog.BUTTON_NEUTRAL).performClick();org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();return org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();}
 @Test public void deletingOneExamRequiresConfirmationAndPreservesOtherRecords()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();a.getPreferences(0).edit().putString("aiExams",EXAMS).putString("courses","1|1|2|合成课程|A101|1-16").commit();confirm(a).getButton(AlertDialog.BUTTON_NEGATIVE).performClick();assertEquals(EXAMS,a.getPreferences(0).getString("aiExams",""));confirm(a).getButton(AlertDialog.BUTTON_POSITIVE).performClick();org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();JSONArray remaining=new JSONArray(a.getPreferences(0).getString("aiExams",""));assertEquals("Delete result: "+org.robolectric.shadows.ShadowToast.getTextOfLatestToast(),1,remaining.length());assertEquals("合成考试乙",remaining.getJSONObject(0).getString("name"));assertEquals("1|1|2|合成课程|A101|1-16",a.getPreferences(0).getString("courses",""));
  }
 }
 @Test public void changedExamSnapshotCannotBeDeletedByAnOldDialog()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();a.getPreferences(0).edit().putString("aiExams",EXAMS).commit();AlertDialog dialog=confirm(a);String newer=EXAMS.replace("合成考试甲","已更新的考试");a.getPreferences(0).edit().putString("aiExams",newer).commit();dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertEquals(newer,a.getPreferences(0).getString("aiExams",""));
  }
 }
 @Test public void examListAndDateMarkerBothReachTheDeleteDialog()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();a.getPreferences(0).edit().putString("aiExams",EXAMS).commit();callInstanceMethod(a,"aiExams");((View)getField(a,"root")).findViewWithTag("exam-record-0").performClick();assertNotNull(org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog().getButton(AlertDialog.BUTTON_NEUTRAL));callInstanceMethod(a,"examDayDetails",ClassParameter.from(String.class,"2026-11-07"));assertNotNull(org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog().getButton(AlertDialog.BUTTON_NEUTRAL));
  }
 }
 @Test public void weekButtonsHaveNoRaisedStateAndTaskCategoryHasAPlainArrow(){
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"courseForm",ClassParameter.from(Course.class,null),ClassParameter.from(int.class,-1),ClassParameter.from(Runnable.class,(Runnable)()->{}));View root=getField(a,"root");for(int i=0;i<4;i++){View button=root.findViewWithTag("course-weeks-"+i);assertNotNull(button);assertNull(button.getStateListAnimator());assertEquals(0,button.getElevation(),0);}
   for(int i=1;i<=30;i++){View button=root.findViewWithTag("course-week-"+i);assertNotNull(button);assertNull(button.getStateListAnimator());assertEquals(0,button.getElevation(),0);}
   callInstanceMethod(a,"tasks");TaskPage page=getField(a,"taskPage");LinearLayout body=new LinearLayout(a);callInstanceMethod(page,"categoryField",ClassParameter.from(LinearLayout.class,body));View arrow=body.findViewWithTag("task-category-open");assertTrue(arrow instanceof ImageButton);assertNull(arrow.getBackground());assertEquals("选择课程分类",arrow.getContentDescription());
  }
 }
}
