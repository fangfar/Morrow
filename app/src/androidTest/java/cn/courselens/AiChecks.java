package cn.courselens;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.json.*;

final class AiChecks {
 interface Work {void run()throws Exception;}
 static void main(Instrumentation i,Work work){AtomicReference<Throwable> failure=new AtomicReference<>();i.runOnMainSync(()->{try{work.run();}catch(Throwable t){failure.set(t);}});if(failure.get()!=null)throw new RuntimeException(failure.get());}
 static Object get(MainActivity a,String name)throws Exception{Field f=MainActivity.class.getDeclaredField(name);f.setAccessible(true);return f.get(a);}
 static void set(MainActivity a,String name,Object value)throws Exception{Field f=MainActivity.class.getDeclaredField(name);f.setAccessible(true);f.set(a,value);}
 static void call(MainActivity a,String name)throws Exception{Method m=MainActivity.class.getDeclaredMethod(name);m.setAccessible(true);m.invoke(a);}
 static void check(boolean ok,String label){if(!ok)throw new AssertionError(label);}
 static void click(Instrumentation i,String label){i.waitForIdleSync();AccessibilityNodeInfo root=i.getUiAutomation().getRootInActiveWindow();check(root!=null,"No active dialog");List<AccessibilityNodeInfo> nodes=root.findAccessibilityNodeInfosByText(label);for(AccessibilityNodeInfo n:nodes)if(label.contentEquals(n.getText())&&n.performAction(AccessibilityNodeInfo.ACTION_CLICK)){i.waitForIdleSync();return;}throw new AssertionError("Button not clickable: "+label);}
 static void show(MainActivity a,DeepSeekPlan plan,String courses,String exams,String id,String term,String adjust,String times,int token,TextView status,LinearLayout history)throws Exception{Method m=MainActivity.class.getDeclaredMethod("confirmAiPlan",DeepSeekPlan.class,String.class,String.class,String.class,String.class,String.class,String.class,int.class,TextView.class,LinearLayout.class);m.setAccessible(true);m.invoke(a,plan,courses,exams,id,term,adjust,times,token,status,history);}
 static void run(NativeChecks i){Bundle result=new Bundle();int code=Activity.RESULT_CANCELED;try{
  check(i.getTargetContext().getPackageName().equals("cn.courselens.qa.aitest"),"Refusing to run outside isolated QA package");MainActivity a=NativeChecks.startQaActivity(i);i.waitForIdleSync();DeepSeekKey key=new DeepSeekKey(a);check(!key.configured(),"Expected clean isolated key store");key.save("synthetic-test-key");check(key.configured()&&key.load().equals("synthetic-test-key"),"Keystore roundtrip failed");key.clear();check(!key.configured(),"Key removal failed");
  String course="1|1|2|合成高数|A101|1-16",exams="[]";TextView[] status={null};LinearLayout[] history={null};String[] state=new String[4];int[] token={0};
  main(i,()->{set(a,"data",course);check(a.getPreferences(0).edit().putString("courses",course).putString("aiExams",exams).commit(),"Fixture save failed");call(a,"aiAssistant");View root=(View)get(a,"root");check(root.findViewWithTag("ai-message")!=null&&root.findViewWithTag("ai-menu")!=null,"Assistant controls missing");state[0]=Timetables.active(a);state[1]=(String)get(a,"term");state[2]=(String)get(a,"adjustments");state[3]=((ClassTimes)get(a,"classTimes")).encode();token[0]=(int)get(a,"screen");status[0]=new TextView(a);history[0]=new LinearLayout(a);});
  DeepSeekPlan remove=DeepSeekPlan.build(new JSONObject("{\"type\":\"delete_course\",\"index\":1}"),course,exams,ClassTimes.parse(state[3]));
  main(i,()->show(a,remove,course,exams,state[0],state[1],state[2],state[3],token[0],status[0],history[0]));click(i,"取消");check(a.getPreferences(0).getString("courses","").equals(course),"Cancel changed course");
  main(i,()->{show(a,remove,course,exams,state[0],state[1],state[2],state[3],token[0],status[0],history[0]);set(a,"data",course+"\n2|1|2|合成英语||1-16");});click(i,"确认保存");check(a.getPreferences(0).getString("courses","").equals(course),"Stale request changed data");check(status[0].getText().toString().contains("数据已变化"),"No stale warning");
  DeepSeekPlan update=DeepSeekPlan.build(new JSONObject("{\"type\":\"update_course\",\"index\":1,\"value\":{\"room\":\"B202\"}}"),course,exams,ClassTimes.parse(state[3]));main(i,()->{set(a,"data",course);show(a,update,course,exams,state[0],state[1],state[2],state[3],token[0],status[0],history[0]);});click(i,"确认保存");check(Course.parse(a.getPreferences(0).getString("courses","")).get(0).room.equals("B202"),"Confirmed course update not saved");
  String updated=update.courses;DeepSeekPlan add=DeepSeekPlan.build(new JSONObject("{\"type\":\"add_course\",\"value\":{\"name\":\"合成英语\",\"day\":2,\"start\":1,\"end\":2,\"weeks\":\"1-16\"}}"),updated,exams,ClassTimes.parse(state[3]));main(i,()->show(a,add,updated,exams,state[0],state[1],state[2],state[3],token[0],status[0],history[0]));click(i,"确认保存");check(Course.parse(a.getPreferences(0).getString("courses","")).size()==2,"Confirmed course add not saved");
  String withBoth=add.courses;DeepSeekPlan delete=DeepSeekPlan.build(new JSONObject("{\"type\":\"delete_course\",\"index\":2}"),withBoth,exams,ClassTimes.parse(state[3]));main(i,()->show(a,delete,withBoth,exams,state[0],state[1],state[2],state[3],token[0],status[0],history[0]));click(i,"确认保存");check(Course.parse(a.getPreferences(0).getString("courses","")).size()==1,"Confirmed course delete not saved");main(i,()->{set(a,"data",course);check(a.getPreferences(0).edit().putString("courses",course).commit(),"Fixture restore failed");});
  DeepSeekPlan exam=DeepSeekPlan.build(new JSONObject("{\"type\":\"add_exam\",\"value\":{\"name\":\"合成考试\",\"date\":\"2026-12-20\",\"time\":\"09:00\"}}"),course,exams,ClassTimes.parse(state[3]));main(i,()->{set(a,"data",course);show(a,exam,course,exams,state[0],state[1],state[2],state[3],token[0],status[0],history[0]);});click(i,"确认保存");check(new JSONArray(a.getPreferences(0).getString("aiExams","[]")).getJSONObject(0).getString("name").equals("合成考试"),"Exam not saved");check(a.getPreferences(0).getString("courses","").equals(course),"Exam changed course");
  main(i,()->{call(a,"aiExams");View root=(View)get(a,"root");check(contains(root,"合成考试"),"Saved exam not visible");});
  main(i,()->{set(a,"term","2026-12-14");set(a,"week",1);a.getPreferences(0).edit().putString("term","2026-12-14").putInt("week",1).commit();call(a,"home");});i.waitForIdleSync();
  main(i,()->{View content=a.findViewById(android.R.id.content);View home=(View)get(a,"root"),grid=content.findViewWithTag("week-grid-1");check(grid!=null&&grid.findViewWithTag("exam-card")!=null,"Exam card absent from timetable");check(contains(grid.findViewWithTag("exam-card"),"合成考试"),"Exam title absent");content.findViewWithTag("nav-ai").performClick();View chat=(View)get(a,"root");EditText draft=chat.findViewWithTag("ai-message");draft.setText("合成聊天草稿");for(int n=0;n<4;n++){content.findViewWithTag("nav-timetable").performClick();check(get(a,"root")==home,"Timetable recreated on tab switch");content.findViewWithTag("nav-ai").performClick();check(get(a,"root")==chat,"Chat recreated on tab switch");check(draft.getText().toString().equals("合成聊天草稿"),"Chat draft lost");}content.findViewWithTag("nav-timetable").performClick();});
  TimetableReturnChecks.run(i,a);
  result.putString("result","PASS: isolated key encryption and removal; assistant entry; cancel and stale guards; confirmed course add/update/delete and exam persistence and timetable cards; repeated AI/timetable switches and draft retention; first left/right swipes after 12 returns");code=Activity.RESULT_OK;
 }catch(Throwable t){result.putString("error",android.util.Log.getStackTraceString(t));}finally{i.finish(code,result);}}
 static boolean contains(View v,String s){if(v instanceof TextView&&((TextView)v).getText().toString().contains(s))return true;if(v instanceof ViewGroup)for(int n=0;n<((ViewGroup)v).getChildCount();n++)if(contains(((ViewGroup)v).getChildAt(n),s))return true;return false;}
}
