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
 // Exercise camera grants independently of Android FileProvider path handling on the Windows host.
 @org.robolectric.annotation.Implements(value=androidx.core.content.FileProvider.class,isInAndroidSdk=false)
 public static class FileUriShadow {
  @org.robolectric.annotation.Implementation public static android.net.Uri getUriForFile(android.content.Context context,String authority,java.io.File file){return android.net.Uri.parse("content://"+authority+"/ai-capture/"+file.getName());}
 }
 @Test public void historyDrawerAndStopControlWork()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");View page=getField(a,"root");page.findViewWithTag("ai-history-button").performClick();SideNavigation drawer=getField(a,"sideNavigation");assertTrue(drawer.isOpen());assertNotNull(drawer.findViewWithTag("drawer-timetable"));assertNull(drawer.findViewWithTag("ai-history-search"));assertNotNull(drawer.findViewWithTag("ai-history-new-chat"));LinearLayout list=drawer.findViewWithTag("ai-history-list");assertTrue(list.getChildCount()>0);drawer.close(false);DeepSeekClient.Call call=new DeepSeekClient.Call();setField(a,"aiCall",call);setField(a,"aiSending",true);page.findViewWithTag("ai-send").performClick();assertTrue(call.cancelled);
  }
 }
 @Test public void composerStrengthAndTemporaryChatAreIndependentOfHistory()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");View page=getField(a,"root");assertNull(page.findViewWithTag("ai-new-chat"));TextView title=page.findViewWithTag("primary-page-title");assertFalse(title.hasOnClickListeners());View strength=page.findViewWithTag("ai-strength");assertNotSame(title,strength);page.findViewWithTag("ai-temporary-chat").performClick();AiChats store=callInstanceMethod(a,"aiStore");store.messages(new JSONArray().put(new JSONObject().put("role","user").put("content","临时内容")));assertEquals(0,new AiChats(a).chat().getJSONArray("messages").length());((View)getField(a,"root")).findViewWithTag("ai-temporary-chat").performClick();assertEquals(0,((JSONArray)getField(a,"aiChat")).length());
  }
 }
 @Test public void aiBackExitsAndTimetableMenuHasNoAiEntry()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"menu");callInstanceMethod(a,"aiAssistant");assertNull(getField(a,"pageBack"));a.onBackPressed();assertTrue(a.isFinishing());callInstanceMethod(a,"timetableSettings");View content=a.findViewById(android.R.id.content);assertNull(content.findViewWithTag("timetable-ai"));assertNotNull(content.findViewWithTag("timetable-add-course"));
  }
 }
 @Test public void sendAndStopUseCenteredIconsWithAccessibleLabels()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");ImageButton send=((View)getField(a,"root")).findViewWithTag("ai-send");assertNotNull(send.getDrawable());assertEquals(ImageView.ScaleType.CENTER,send.getScaleType());assertEquals(send.getPaddingLeft(),send.getPaddingRight());assertEquals("发送消息",send.getContentDescription());callInstanceMethod(a,"aiSendState",ClassParameter.from(ImageButton.class,send),ClassParameter.from(boolean.class,true));assertEquals("停止生成",send.getContentDescription());assertNotNull(send.getDrawable());
  }
 }
 @Test public void calendarAccessRequestsSystemPermissionWithoutNavigatingAway()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");View page=getField(a,"root");callInstanceMethod(a,"aiRequestPhoneAccess",ClassParameter.from(String.class,"calendar"));assertSame(page,getField(a,"root"));assertTrue(AiPhoneContext.settings(a).getBoolean("calendar",false));assertEquals(android.content.pm.PackageManager.PERMISSION_DENIED,a.checkSelfPermission(android.Manifest.permission.READ_CALENDAR));
   org.robolectric.shadows.ShadowActivity.PermissionsRequest requested=org.robolectric.Shadows.shadowOf(a).getLastRequestedPermission();assertNotNull(requested);assertEquals(910,requested.requestCode);assertArrayEquals(new String[]{android.Manifest.permission.READ_CALENDAR},requested.requestedPermissions);
  }
 }
 @Test public void reasoningSliderPersistsSelectedLevelAndDismissesSeparately()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");View page=getField(a,"root");page.findViewWithTag("ai-strength").performClick();android.app.Dialog dialog=getField(a,"aiStrengthDialog");assertTrue(dialog.isShowing());AiStrengthSlider slider=dialog.findViewById(android.R.id.content).findViewWithTag("ai-strength-slider");assertEquals(3000,slider.getMax());slider.onKeyDown(android.view.KeyEvent.KEYCODE_DPAD_RIGHT,new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN,android.view.KeyEvent.KEYCODE_DPAD_RIGHT));AiChats store=callInstanceMethod(a,"aiStore");assertEquals(slider.level(),store.chat().getInt("strength"));assertTrue(slider.level()>0);org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(2));assertEquals(slider.level()*1000,slider.getProgress());dialog.dismiss();assertSame(page,getField(a,"root"));assertFalse(a.isFinishing());
  }
 }
 @Test public void attachmentMenuAndHeaderMenuKeepComposerHeight()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");View page=getField(a,"root");EditText input=page.findViewWithTag("ai-message");input.requestFocus();callInstanceMethod(a,"aiComposerExpanded",ClassParameter.from(boolean.class,true));int lines=input.getMinLines();page.findViewWithTag("ai-attach").performClick();MainActivity.NavigationOverlay popup=getField(a,"aiAttachmentMenu");assertTrue("attachment menu open",popup.open);assertTrue("editor focus preserved",input.hasFocus());for(String kind:new String[]{"camera","photo","file","strength"})assertNotNull(popup.body.findViewWithTag("ai-attach-"+kind));assertNull(popup.body.findViewWithTag("ai-attach-plugin"));popup.close(false);android.view.WindowInsets shown=new android.view.WindowInsets.Builder().setInsets(android.view.WindowInsets.Type.ime(),android.graphics.Insets.of(0,0,0,600)).setVisible(android.view.WindowInsets.Type.ime(),true).build();View stage=getField(a,"stage");callInstanceMethod(a,"applyStageInsets",ClassParameter.from(View.class,stage),ClassParameter.from(android.view.WindowInsets.class,shown));page.findViewWithTag("ai-menu").performClick();callInstanceMethod(a,"applyStageInsets",ClassParameter.from(View.class,stage),ClassParameter.from(android.view.WindowInsets.class,shown));assertEquals(600,stage.getPaddingBottom());assertEquals("editor line state preserved",lines,input.getMinLines());assertTrue(input.getMinLines()>=2);
  }
 }
 @Test public void cameraPermissionAndFilePickerAreScoped()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");callInstanceMethod(a,"aiOpenAttachment",ClassParameter.from(int.class,2));android.content.Intent file=org.robolectric.Shadows.shadowOf(a).getNextStartedActivity();assertEquals(android.content.Intent.ACTION_OPEN_DOCUMENT,file.getAction());assertEquals("*/*",file.getType());org.robolectric.Shadows.shadowOf((android.app.Application)a.getApplication()).denyPermissions(android.Manifest.permission.CAMERA);callInstanceMethod(a,"aiOpenAttachment",ClassParameter.from(int.class,0));assertEquals(909,org.robolectric.Shadows.shadowOf(a).getLastRequestedPermission().requestCode);assertNull(getField(a,"aiCamera"));
  }
 }
 @Test public void longPressDeletionRequiresConfirmation()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");AiChats store=callInstanceMethod(a,"aiStore");String id=store.chat().getString("id");store.memory("项目记忆");callInstanceMethod(a,"aiHistory");SideNavigation drawer=getField(a,"sideNavigation");LinearLayout list=drawer.findViewWithTag("ai-history-list");View row=((LinearLayout)list.getChildAt(1)).getChildAt(0);assertTrue(row.performLongClick());AlertDialog dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertEquals(id,store.chat().getString("id"));row.performLongClick();org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog().getButton(AlertDialog.BUTTON_POSITIVE).performClick();org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertNotEquals(id,store.chat().getString("id"));assertEquals("项目记忆",store.project().getString("memory"));
  }
 }
 @Test public void projectFlowCreatesOnlyNamedProjects()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"aiCreateProject");View root=getField(a,"root");Button create=root.findViewWithTag("ai-project-create");EditText name=root.findViewWithTag("ai-project-name");assertFalse(create.isEnabled());name.setText("  ");assertFalse(create.isEnabled());name.setText("电路复习");assertTrue(create.isEnabled());create.performClick();AiChats store=callInstanceMethod(a,"aiStore");assertEquals("电路复习",store.project().getString("name"));assertNotNull(((View)getField(a,"root")).findViewWithTag("ai-message"));
  }
 }
 @Test public void memoryToolPersistsProjectContext()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");AiChats store=callInstanceMethod(a,"aiStore");JSONObject context=new JSONObject().put("project",new JSONObject().put("id",store.project().getString("id")).put("memory",store.project().getString("memory")));AiModulePlan plan=AiModulePlan.build(new JSONObject("{\"type\":\"remember_project\",\"value\":{\"text\":\"偏好简洁回答\"}}"),context);callInstanceMethod(a,"executeAiModule",ClassParameter.from(AiModulePlan.class,plan),ClassParameter.from(TextView.class,new TextView(a)));assertEquals("偏好简洁回答",new AiChats(a).project().getString("memory"));
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
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");LinearLayout page=getField(a,"root");View input=page.findViewWithTag("ai-message"),send=page.findViewWithTag("ai-send");assertNotNull(input);assertNotNull(send);assertSame(input.getParent(),send.getParent().getParent());assertSame(page.findViewWithTag("ai-chat-viewport"),input.getParent().getParent());assertNotNull(page.findViewWithTag("ai-conversation-scroll"));
   page.findViewWithTag("ai-menu").performClick();View decor=a.findViewById(android.R.id.content);assertNotNull(decor.findViewWithTag("ai-settings"));assertNotNull(decor.findViewWithTag("ai-exams"));
  }
 }
 @Test public void examAppearsOnCorrectDayAndCanBeOpened()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();setField(a,"term","2026-08-31");a.getPreferences(0).edit().putString("aiExams","[{\"name\":\"英语\",\"date\":\"2026-09-04\",\"time\":\"\",\"room\":\"\"}]").commit();LinearLayout days=new LinearLayout(a);for(int i=0;i<8;i++)days.addView(new TextView(a));
   callInstanceMethod(a,"updateDays",ClassParameter.from(LinearLayout.class,days),ClassParameter.from(int.class,1));TextView friday=(TextView)days.getChildAt(5);assertTrue(friday.getContentDescription().toString().contains("有考试"));assertEquals("五考\n9/4",friday.getText().toString());assertTrue(friday.performClick());assertTrue(org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog().isShowing());
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
 @Test public void phonePermissionsLiveInSettingsAndReturnToThePermissionPage(){
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");View chat=getField(a,"root");chat.findViewWithTag("ai-menu").performClick();assertNull(a.findViewById(android.R.id.content).findViewWithTag("ai-phone"));
   callInstanceMethod(a,"menu");View settings=getField(a,"root");settings.findViewWithTag("permission-settings").performClick();View permissions=getField(a,"root");permissions.findViewWithTag("permission-ai-phone").performClick();View phone=getField(a,"root");for(String tag:new String[]{"ai-calendar-permission","ai-notification-permission","ai-screen-permission"})assertNotNull(phone.findViewWithTag(tag));
   assertFalse(AiPhoneContext.settings(a).getBoolean("calendar",false));assertFalse(AiPhoneContext.settings(a).getBoolean("notifications",false));((Runnable)getField(a,"pageBack")).run();assertNotNull(((View)getField(a,"root")).findViewWithTag("permission-ai-phone"));
  }
 }
}
