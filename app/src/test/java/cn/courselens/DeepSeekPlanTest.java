package cn.courselens;

import static org.junit.Assert.*;
import org.junit.Test;
import org.json.*;

public class DeepSeekPlanTest {
 private static final String COURSE="1|1|2|高数|A101|1-16|张老师|实验|数学班";
 private static JSONObject json(String s)throws Exception{return new JSONObject(s);}
 @Test public void courseChangesPreserveOtherFieldsAndExams()throws Exception{
  String exams="[ {\"name\":\"英语\",\"date\":\"2026-12-20\",\"time\":\"\",\"room\":\"\"} ]";
  DeepSeekPlan p=DeepSeekPlan.build(json("{\"type\":\"update_course\",\"index\":1,\"value\":{\"room\":\"B201\"}}"),COURSE,exams,ClassTimes.DEFAULT);
  assertEquals("1|1|2|高数|B201|1-16|张老师|实验|数学班",p.courses);assertEquals(exams,p.exams);assertTrue(p.description.contains("A101"));assertTrue(p.description.contains("B201"));
  p=DeepSeekPlan.build(json("{\"type\":\"delete_course\",\"index\":1}"),COURSE,exams,ClassTimes.DEFAULT);assertEquals("",p.courses);assertEquals(exams,p.exams);
 }
 @Test public void examChangesPreserveCourseTextAndAllowUnknownTime()throws Exception{
  String raw=COURSE+"\n";DeepSeekPlan p=DeepSeekPlan.build(json("{\"type\":\"add_exam\",\"value\":{\"name\":\"高数\",\"date\":\"2026-12-20\"}}"),raw,"[]",ClassTimes.DEFAULT);
  assertEquals(raw,p.courses);assertEquals("",new JSONArray(p.exams).getJSONObject(0).getString("time"));assertTrue(p.description.contains("时间未提供"));
  DeepSeekPlan next=DeepSeekPlan.build(json("{\"type\":\"update_exam\",\"index\":1,\"value\":{\"time\":\"09:30\"}}"),raw,p.exams,ClassTimes.DEFAULT);assertEquals("09:30",new JSONArray(next.exams).getJSONObject(0).getString("time"));
  assertEquals(0,new JSONArray(DeepSeekPlan.build(json("{\"type\":\"delete_exam\",\"index\":1}"),raw,next.exams,ClassTimes.DEFAULT).exams).length());
 }
 @Test public void examEndTimeIsOptionalAndValidated()throws Exception{
  DeepSeekPlan plan=DeepSeekPlan.build(json("{type:add_exam,value:{name:Exam,date:'2026-12-20',time:'09:00',end_time:'11:00'}}"),COURSE,"[]",ClassTimes.DEFAULT);assertEquals("11:00",new JSONArray(plan.exams).getJSONObject(0).getString("end_time"));
  for(String value:new String[]{"{type:add_exam,value:{name:Exam,date:'2026-12-20',end_time:'11:00'}}","{type:add_exam,value:{name:Exam,date:'2026-12-20',time:'12:00',end_time:'11:00'}}"})assertThrows(RuntimeException.class,()->DeepSeekPlan.build(json(value),COURSE,"[]",ClassTimes.DEFAULT));
 }
 @Test public void rejectsInvalidOrAmbiguousPlans()throws Exception{
  String[] bad={"{\"type\":\"delete_course\",\"index\":0}","{\"type\":\"update_course\",\"index\":1,\"value\":{\"room\":\"A|B\"}}","{\"type\":\"add_course\",\"value\":{\"name\":\"课\",\"day\":1,\"start\":1,\"end\":30,\"weeks\":\"1-16\"}}","{\"type\":\"add_exam\",\"value\":{\"name\":\"考试\",\"date\":\"2026-02-30\"}}","{\"type\":\"delete_all\"}","{\"type\":\"update_course\",\"index\":1,\"value\":{\"unknown\":true}}"};
  for(String value:bad){try{DeepSeekPlan.build(json(value),COURSE,"[]",ClassTimes.DEFAULT);fail(value);}catch(RuntimeException expected){}}
 }
}
