package cn.courselens;

import android.app.AlertDialog;
import android.view.View;
import android.widget.*;
import org.json.JSONObject;
import org.json.JSONArray;
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
   callInstanceMethod(a,"rememberAiResponse",ClassParameter.from(String.class,"删除高数"),ClassParameter.from(String.class,"请核对要删除的课程"),ClassParameter.from(DeepSeekPlan.class,plan));assertTrue(last(a).contains("请核对要删除的课程")&&last(a).contains("高数")&&last(a).contains("尚未执行"));
   show(a,plan,courses,exams,id,term,adjustments,times.encode(),screen,status,history);AlertDialog dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertEquals(courses,(String)getField(a,"data"));assertTrue(last(a).contains("取消")&&last(a).contains("未执行"));
   show(a,plan,courses,exams,id,term,adjustments,times.encode(),screen,status,history);dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();setField(a,"data",courses+"\n2|1|2|英语||1-16");dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertTrue(status.getText().toString().contains("数据已变化"));assertTrue(last(a).contains("数据已变化")&&last(a).contains("未执行"));assertNotEquals("",(String)getField(a,"data"));
   setField(a,"data",courses);show(a,plan,courses,exams,id,term,adjustments,times.encode(),screen,status,history);dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertEquals("",(String)getField(a,"data"));assertEquals("",a.getPreferences(0).getString("courses","missing"));assertTrue(last(a).contains("操作已保存"));
  }
 }
 @Test public void conversationSurvivesSubpageAndDismissalRecordsNoChange()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");callInstanceMethod(a,"rememberAi",ClassParameter.from(String.class,"user"),ClassParameter.from(String.class,"把课程改到明天"));callInstanceMethod(a,"rememberAi",ClassParameter.from(String.class,"assistant"),ClassParameter.from(String.class,"请告诉我具体课程"));
   callInstanceMethod(a,"aiExams");callInstanceMethod(a,"aiAssistant");JSONArray chat=getField(a,"aiChat");assertEquals(2,chat.length());assertEquals("请告诉我具体课程",chat.getJSONObject(1).getString("content"));
   String courses="1|1|2|高数|A101|1-16",term=getField(a,"term"),adjustments=getField(a,"adjustments");ClassTimes times=getField(a,"classTimes");setField(a,"data",courses);String id=Timetables.active(a);int screen=getField(a,"screen");DeepSeekPlan plan=DeepSeekPlan.build(new JSONObject("{\"type\":\"delete_course\",\"index\":1}"),courses,"[]",times);
   show(a,plan,courses,"[]",id,term,adjustments,times.encode(),screen,new TextView(a),new LinearLayout(a));org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog().cancel();org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertTrue(last(a).contains("未执行"));assertEquals(courses,(String)getField(a,"data"));
  }
 }
 private static String last(MainActivity a)throws Exception{JSONArray chat=getField(a,"aiChat");return chat.getJSONObject(chat.length()-1).getString("content");}
 private static void show(MainActivity a,DeepSeekPlan plan,String courses,String exams,String id,String term,String adjustments,String times,int token,TextView status,LinearLayout history){
  callInstanceMethod(a,"confirmAiPlan",ClassParameter.from(DeepSeekPlan.class,plan),ClassParameter.from(String.class,courses),ClassParameter.from(String.class,exams),ClassParameter.from(String.class,id),ClassParameter.from(String.class,term),ClassParameter.from(String.class,adjustments),ClassParameter.from(String.class,times),ClassParameter.from(int.class,token),ClassParameter.from(TextView.class,status),ClassParameter.from(LinearLayout.class,history));
 }
}
