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
 @Test public void historyDrawerAndStopControlWork()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");View page=getField(a,"root");page.findViewWithTag("ai-history-button").performClick();android.app.Dialog drawer=getField(a,"aiDrawer");assertTrue(drawer.isShowing());assertNotNull(drawer.findViewById(android.R.id.content).findViewWithTag("ai-history-search"));assertNotNull(drawer.findViewById(android.R.id.content).findViewWithTag("ai-history-new-project"));drawer.dismiss();DeepSeekClient.Call call=new DeepSeekClient.Call();setField(a,"aiCall",call);setField(a,"aiSending",true);page.findViewWithTag("ai-send").performClick();assertTrue(call.cancelled);
  }
 }
 @Test public void memoryToolPersistsProjectContext()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");AiChats store=callInstanceMethod(a,"aiStore");JSONObject context=new JSONObject().put("project",new JSONObject().put("id",store.project().getString("id")));AiModulePlan plan=AiModulePlan.build(new JSONObject("{\"type\":\"remember_project\",\"value\":{\"text\":\"偏好简洁回答\"}}"),context);callInstanceMethod(a,"executeAiModule",ClassParameter.from(AiModulePlan.class,plan),ClassParameter.from(TextView.class,new TextView(a)));assertEquals("偏好简洁回答",new AiChats(a).project().getString("memory"));
  }
 }
 @Test public void keyboardInsetsAreAppliedOnceAndTabsReturnAfterDismissal()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");View stage=getField(a,"stage"),nav=a.findViewById(android.R.id.content).findViewWithTag("bottom-navigation");
   android.view.WindowInsets shown=new android.view.WindowInsets.Builder().setInsets(android.view.WindowInsets.Type.ime(),android.graphics.Insets.of(0,0,0,600)).setVisible(android.view.WindowInsets.Type.ime(),true).build();callInstanceMethod(a,"applyStageInsets",ClassParameter.from(View.class,stage),ClassParameter.from(android.view.WindowInsets.class,shown));assertEquals(600,stage.getPaddingBottom());assertEquals(View.GONE,nav.getVisibility());
   android.view.WindowInsets hidden=new android.view.WindowInsets.Builder().setVisible(android.view.WindowInsets.Type.ime(),false).build();callInstanceMethod(a,"applyStageInsets",ClassParameter.from(View.class,stage),ClassParameter.from(android.view.WindowInsets.class,hidden));assertEquals(0,stage.getPaddingBottom());assertEquals(View.VISIBLE,nav.getVisibility());assertNotNull(((View)getField(a,"root")).findViewWithTag("ai-message"));
  }
 }
 @Test public void switchingTabsRetainsChatDraftAndTimetableView()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();View content=a.findViewById(android.R.id.content);View home=getField(a,"root");content.findViewWithTag("nav-ai").performClick();View chat=getField(a,"root");EditText draft=chat.findViewWithTag("ai-message");draft.setText("下周五第一节考英语");draft.setSelection(3);
   for(int i=0;i<3;i++){content.findViewWithTag("nav-timetable").performClick();assertSame(home,getField(a,"root"));assertTrue(content.findViewWithTag("nav-timetable").isSelected());content.findViewWithTag("nav-ai").performClick();assertSame(chat,getField(a,"root"));assertEquals("下周五第一节考英语",draft.getText().toString());assertEquals(3,draft.getSelectionStart());assertFalse(((PageMotion)getField(a,"pageMotion")).isRunning());}
   android.view.WindowInsets shown=new android.view.WindowInsets.Builder().setInsets(android.view.WindowInsets.Type.ime(),android.graphics.Insets.of(0,0,0,600)).setVisible(android.view.WindowInsets.Type.ime(),true).build();View stage=getField(a,"stage");callInstanceMethod(a,"applyStageInsets",ClassParameter.from(View.class,stage),ClassParameter.from(android.view.WindowInsets.class,shown));content.findViewWithTag("nav-timetable").performClick();callInstanceMethod(a,"applyStageInsets",ClassParameter.from(View.class,stage),ClassParameter.from(android.view.WindowInsets.class,shown));assertEquals(0,stage.getPaddingBottom());assertEquals(View.VISIBLE,content.findViewWithTag("bottom-navigation").getVisibility());
  }
 }
 @Test public void aiIsIndependentBottomTab()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();View content=a.findViewById(android.R.id.content);content.findViewWithTag("nav-ai").performClick();assertTrue(content.findViewWithTag("nav-ai").isSelected());assertEquals(View.VISIBLE,content.findViewWithTag("bottom-navigation").getVisibility());assertNotNull(content.findViewWithTag("ai-message"));content.findViewWithTag("nav-timetable").performClick();assertTrue(content.findViewWithTag("nav-timetable").isSelected());content.findViewWithTag("nav-ai").performClick();assertTrue(content.findViewWithTag("nav-ai").isSelected());
  }
 }
 @Test public void chatComposerStaysOutsideConversationAndMenuContainsActions()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");LinearLayout page=getField(a,"root");View input=page.findViewWithTag("ai-message"),send=page.findViewWithTag("ai-send");assertNotNull(input);assertNotNull(send);assertSame(input.getParent(),send.getParent());assertSame(page,input.getParent().getParent());assertTrue(page.getChildAt(1) instanceof ScrollView);
   page.findViewWithTag("ai-menu").performClick();View decor=a.findViewById(android.R.id.content);assertNotNull(decor.findViewWithTag("ai-settings"));assertNotNull(decor.findViewWithTag("ai-exams"));
  }
 }
 @Test public void examAppearsOnCorrectDayAndCanBeOpened()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();setField(a,"term","2026-08-31");a.getPreferences(0).edit().putString("aiExams","[{\"name\":\"英语\",\"date\":\"2026-09-04\",\"time\":\"\",\"room\":\"\"}]").commit();LinearLayout days=new LinearLayout(a);for(int i=0;i<8;i++)days.addView(new TextView(a));
   callInstanceMethod(a,"updateDays",ClassParameter.from(LinearLayout.class,days),ClassParameter.from(int.class,1));TextView friday=(TextView)days.getChildAt(5);assertTrue(friday.getText().toString().contains("考"));assertTrue(friday.performClick());assertTrue(org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog().isShowing());
   callInstanceMethod(a,"updateDays",ClassParameter.from(LinearLayout.class,days),ClassParameter.from(int.class,2));assertFalse(friday.getText().toString().contains("考"));assertFalse(friday.hasOnClickListeners());
  }
 }
 @Test public void confirmationAndStaleDataProtectRecords()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");View root=a.findViewById(android.R.id.content);assertNotNull(root.findViewWithTag("ai-message"));assertNotNull(root.findViewWithTag("ai-menu"));
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
