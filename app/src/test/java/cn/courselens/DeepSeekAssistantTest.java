package cn.courselens;

import android.app.AlertDialog;
import android.view.View;
import android.widget.*;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;
import static org.robolectric.util.ReflectionHelpers.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34)
public class DeepSeekAssistantTest {
 @Test public void confirmationAndStaleDataProtectRecords()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");View root=a.findViewById(android.R.id.content);assertNotNull(root.findViewWithTag("ai-message"));assertNotNull(root.findViewWithTag("ai-exams"));
   String courses="1|1|2|高数|A101|1-16",exams="[]",term=getField(a,"term"),adjustments=getField(a,"adjustments");ClassTimes times=getField(a,"classTimes");setField(a,"data",courses);String id=Timetables.active(a);int screen=getField(a,"screen");TextView status=new TextView(a);LinearLayout history=new LinearLayout(a);
   DeepSeekPlan plan=DeepSeekPlan.build(new JSONObject("{\"type\":\"delete_course\",\"index\":1}"),courses,exams,times);
   show(a,plan,courses,exams,id,term,adjustments,times.encode(),screen,status,history);AlertDialog dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertEquals(courses,(String)getField(a,"data"));
   show(a,plan,courses,exams,id,term,adjustments,times.encode(),screen,status,history);dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();setField(a,"data",courses+"\n2|1|2|英语||1-16");dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertTrue(status.getText().toString().contains("数据已变化"));assertNotEquals("",(String)getField(a,"data"));
   setField(a,"data",courses);show(a,plan,courses,exams,id,term,adjustments,times.encode(),screen,status,history);dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertEquals("",(String)getField(a,"data"));assertEquals("",a.getPreferences(0).getString("courses","missing"));
  }
 }
 private static void show(MainActivity a,DeepSeekPlan plan,String courses,String exams,String id,String term,String adjustments,String times,int token,TextView status,LinearLayout history){
  callInstanceMethod(a,"confirmAiPlan",ClassParameter.from(DeepSeekPlan.class,plan),ClassParameter.from(String.class,courses),ClassParameter.from(String.class,exams),ClassParameter.from(String.class,id),ClassParameter.from(String.class,term),ClassParameter.from(String.class,adjustments),ClassParameter.from(String.class,times),ClassParameter.from(int.class,token),ClassParameter.from(TextView.class,status),ClassParameter.from(LinearLayout.class,history));
 }
}
