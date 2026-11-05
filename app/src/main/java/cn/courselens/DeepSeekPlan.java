package cn.courselens;

import java.time.*;
import java.util.*;
import org.json.*;

final class DeepSeekPlan {
 final String courses,exams,description;
 AiModulePlan module;
 static DeepSeekPlan module(AiModulePlan module,String courses,String exams){DeepSeekPlan plan=new DeepSeekPlan(courses,exams,module.description);plan.module=module;return plan;}
 private DeepSeekPlan(String courses,String exams,String description){this.courses=courses;this.exams=exams;this.description=description;}
 static String clean(String value){if(value==null||value.length()>200||value.contains("|")||value.contains("\n")||value.contains("\r"))throw new IllegalArgumentException("内容过长或包含无效字符");return value.trim();}
 static String str(JSONObject o,String key,String fallback)throws JSONException{if(!o.has(key))return fallback;Object v=o.get(key);if(!(v instanceof String))throw new IllegalArgumentException("字段格式不正确："+key);return clean((String)v);}
 static int integer(JSONObject o,String key,int fallback)throws JSONException{if(!o.has(key))return fallback;Object n=o.get(key);if(!(n instanceof Number)||((Number)n).doubleValue()!=((Number)n).intValue())throw new IllegalArgumentException("数字格式不正确："+key);return ((Number)n).intValue();}
 static void fields(JSONObject value,String... allowed){Set<String> names=new HashSet<>(Arrays.asList(allowed));Iterator<String> keys=value.keys();while(keys.hasNext())if(!names.contains(keys.next()))throw new IllegalArgumentException("操作包含未知字段，请重新描述");}
 static JSONArray examList(String raw)throws JSONException{JSONArray list=new JSONArray(raw);if(list.length()>500)throw new IllegalArgumentException("考试记录过多");for(int i=0;i<list.length();i++)exam(list.getJSONObject(i),null);return list;}
 static JSONObject exam(JSONObject value,JSONObject old)throws JSONException{
  fields(value,"name","date","time","room");String name=str(value,"name",old==null?"":old.getString("name")),date=str(value,"date",old==null?"":old.getString("date")),time=str(value,"time",old==null?"":old.getString("time")),room=str(value,"room",old==null?"":old.getString("room"));
  if(name.isEmpty()||!date.matches("\\d{4}-\\d{2}-\\d{2}"))throw new IllegalArgumentException("考试需要名称和完整日期");LocalDate.parse(date);if(!time.isEmpty()){if(!time.matches("\\d{2}:\\d{2}"))throw new IllegalArgumentException("考试时间应为 HH:mm");LocalTime.parse(time);}return new JSONObject().put("name",name).put("date",date).put("time",time).put("room",room);
 }
 static String examText(JSONObject x)throws JSONException{return x.getString("name")+" · "+x.getString("date")+(x.getString("time").isEmpty()?" · 时间未提供":" "+x.getString("time"))+(x.getString("room").isEmpty()?"":" · "+x.getString("room"));}
 static String courseText(Course c){return c.name+" · 星期"+"一二三四五六日".charAt(c.day-1)+" · 第 "+c.start+"–"+c.end+" 节 · 第 "+c.weeks+" 周"+(c.room.isEmpty()?"":" · "+c.room)+(c.teacher.isEmpty()?"":" · "+c.teacher)+(c.details.isEmpty()?"":" · "+c.details)+(c.className.isEmpty()?"":" · "+c.className);}
 static DeepSeekPlan build(JSONObject operation,String raw,String examRaw,ClassTimes times)throws Exception{
  fields(operation,"type","index","value");String type=operation.getString("type");List<Course> rows=Course.parse(raw);JSONArray exams=examList(examRaw);int index=integer(operation,"index",-1);JSONObject value=operation.optJSONObject("value");String before="",after="";
  if(type.equals("add_course")||type.equals("update_course")||type.equals("delete_course")){
   Course old=null;if(!type.equals("add_course")){if(index<1||index>rows.size())throw new IllegalArgumentException("课程已不存在，请重新提问");old=rows.get(index-1);before=courseText(old);}
   if(type.equals("delete_course")){if(value!=null)throw new IllegalArgumentException("删除操作格式不正确");rows.remove(index-1);}else{
    if(value==null)throw new IllegalArgumentException("课程信息不完整");fields(value,"name","day","start","end","weeks","room","teacher","details");
    String name=str(value,"name",old==null?"":old.name),weeks=str(value,"weeks",old==null?"":old.weeks),room=str(value,"room",old==null?"":old.room),teacher=str(value,"teacher",old==null?"":old.teacher),details=str(value,"details",old==null?"":old.details);
    if(old!=null&&old.schoolAdjusted()&&!Arrays.asList(details.split("；")).contains(Course.SCHOOL_ADJUSTED))details=Course.optionalText(details+"；"+Course.SCHOOL_ADJUSTED);
    Course c=new Course(integer(value,"day",old==null?0:old.day),integer(value,"start",old==null?0:old.start),integer(value,"end",old==null?0:old.end),name,room,weeks,teacher,details,old==null?"":old.className);Course.parse(c.encode());times.validate(Collections.singletonList(c));after=courseText(c);if(old==null)rows.add(c);else rows.set(index-1,c);
   }
  }else if(type.equals("add_exam")||type.equals("update_exam")||type.equals("delete_exam")){
   JSONObject old=null;if(!type.equals("add_exam")){if(index<1||index>exams.length())throw new IllegalArgumentException("考试记录已不存在，请重新提问");old=exams.getJSONObject(index-1);before=examText(old);}
   if(type.equals("delete_exam")){if(value!=null)throw new IllegalArgumentException("删除操作格式不正确");exams.remove(index-1);}else{if(value==null)throw new IllegalArgumentException("考试信息不完整");JSONObject next=exam(value,old);after=examText(next);if(old==null)exams.put(next);else exams.put(index-1,next);}
  }else throw new IllegalArgumentException("不支持此操作");
  List<String> encoded=new ArrayList<>();for(Course c:rows)encoded.add(c.encode());String title=type.startsWith("add_")?"添加":type.startsWith("update_")?"修改":"删除";return new DeepSeekPlan(type.endsWith("course")?String.join("\n",encoded):raw,type.endsWith("exam")?exams.toString():examRaw,title+(type.endsWith("course")?"课程时段":"考试记录")+(before.isEmpty()?"":"\n原内容："+before)+(after.isEmpty()?"":"\n新内容："+after));
 }
 static JSONArray courseContext(String raw)throws Exception{JSONArray out=new JSONArray();List<Course> rows=Course.parse(raw);for(int i=0;i<rows.size();i++){Course c=rows.get(i);out.put(new JSONObject().put("index",i+1).put("name",c.name).put("day",c.day).put("start",c.start).put("end",c.end).put("weeks",c.weeks).put("room",c.room).put("teacher",c.teacher).put("details",c.details));}return out;}
 static JSONArray examContext(String raw)throws Exception{JSONArray exams=examList(raw),out=new JSONArray();for(int i=0;i<exams.length();i++){JSONObject e=new JSONObject(exams.getJSONObject(i).toString());e.put("index",i+1);out.put(e);}return out;}
}
