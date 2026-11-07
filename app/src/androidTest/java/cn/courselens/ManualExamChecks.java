package cn.courselens;

import android.app.*;
import android.content.SharedPreferences;
import android.os.*;
import android.view.View;
import org.json.*;
import static cn.courselens.AiChecks.*;

final class ManualExamChecks {
 static void run(NativeChecks i){
  Bundle result=new Bundle();int code=Activity.RESULT_CANCELED;MainActivity activity=null;SharedPreferences preferences=null;String original=null;boolean existed=false;
  try{
   check(i.getTargetContext().getPackageName().equals("cn.courselens.qa"),"Isolated QA package required");activity=NativeChecks.startQaActivity(i);MainActivity a=activity;preferences=a.getPreferences(0);SharedPreferences prefs=preferences;existed=prefs.contains("aiExams");original=prefs.getString("aiExams",null);String courses=prefs.getString("courses","");String exams="[{\"name\":\"合成考试甲\",\"date\":\"2026-11-07\",\"time\":\"\",\"room\":\"\"},{\"name\":\"合成考试乙\",\"date\":\"2026-11-08\",\"time\":\"09:00\",\"room\":\"A101\"}]";
   check(prefs.edit().putString("aiExams",exams).commit(),"Cannot prepare synthetic exam fixture");main(i,()->{call(a,"aiExams");((View)get(a,"root")).findViewWithTag("exam-record-0").performClick();});i.waitForIdleSync();click(i,"删除考试");click(i,"取消");check(exams.equals(prefs.getString("aiExams","")),"Cancel changed exam records");
   main(i,()->((View)get(a,"root")).findViewWithTag("exam-record-0").performClick());click(i,"删除考试");click(i,"删除");long until=SystemClock.uptimeMillis()+5000;while(SystemClock.uptimeMillis()<until&&new JSONArray(prefs.getString("aiExams","[]")).length()!=1){i.waitForIdleSync();SystemClock.sleep(32);}JSONArray remaining=new JSONArray(prefs.getString("aiExams","[]"));check(remaining.length()==1&&remaining.getJSONObject(0).getString("name").equals("合成考试乙"),"Manual exam deletion removed the wrong record");check(courses.equals(prefs.getString("courses","")),"Exam deletion changed courses");result.putString("result","PASS: native exam detail delete, cancellation, selected-record removal, remaining exam and courses preserved");code=Activity.RESULT_OK;
  }catch(Throwable e){java.io.StringWriter trace=new java.io.StringWriter();e.printStackTrace(new java.io.PrintWriter(trace));result.putString("result","FAIL: "+trace);}
  finally{if(preferences!=null){SharedPreferences.Editor edit=preferences.edit();if(existed)edit.putString("aiExams",original);else edit.remove("aiExams");if(!edit.commit()){result.putString("result","FAIL: exam fixture restore failed");code=Activity.RESULT_CANCELED;}}if(activity!=null){MainActivity a=activity;main(i,a::finish);}i.finish(code,result);}
 }
}
