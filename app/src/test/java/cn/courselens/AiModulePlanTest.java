package cn.courselens;
import org.junit.Test;import org.json.*;import static org.junit.Assert.*;
public class AiModulePlanTest {
 @Test public void phonePermissionRequestsRejectUnknownAndAlreadyAvailableSources()throws Exception{
  JSONObject op=new JSONObject("{type:request_phone_access,value:{source:calendar}}");JSONObject context=new JSONObject();assertNotNull(AiModulePlan.build(op,context));context.put("phone",new JSONObject().put("calendar",new JSONObject().put("available",true)));assertThrows(IllegalArgumentException.class,()->AiModulePlan.build(op,context));assertThrows(IllegalArgumentException.class,()->AiModulePlan.build(new JSONObject("{type:request_phone_access,value:{source:all}}"),context));assertNotNull(AiModulePlan.build(new JSONObject("{type:phone_action,value:{action:set_volume,percent:50}}"),context));
 }
 @Test public void memoryReplacementSupportsForgettingAndRejectsCredentials()throws Exception{JSONObject context=new JSONObject().put("project",new JSONObject().put("id","p").put("memory","旧内容"));assertNotNull(AiModulePlan.build(new JSONObject("{\"type\":\"remember_project\",\"value\":{\"mode\":\"replace\",\"text\":\"\"}}"),context));try{AiModulePlan.build(new JSONObject("{\"type\":\"remember_project\",\"value\":{\"mode\":\"replace\",\"text\":\"密码：abc123\"}}"),context);fail();}catch(IllegalArgumentException expected){}try{AiModulePlan.build(new JSONObject("{\"type\":\"remember_project\",\"value\":{\"mode\":\"invalid\",\"text\":\"test\"}}"),context);fail();}catch(IllegalArgumentException expected){}}
 @Test public void validatesTargetsAndKeepsSecretsOutOfPreview()throws Exception{
  JSONObject context=new JSONObject().put("tasks",new JSONArray().put(new JSONObject().put("title","英语作业").put("course","英语").put("deadline","截止时间未知").put("key","private-id"))).put("parcels",new JSONArray());
  AiModulePlan plan=AiModulePlan.build(new JSONObject("{\"type\":\"complete_task\",\"index\":1}"),context);assertEquals("打开设置",AiModulePlan.build(new JSONObject("{\"type\":\"open_page\",\"value\":{\"page\":\"settings\"}}"),context).description);assertTrue(plan.description.contains("英语作业"));assertFalse(plan.description.contains("private-id"));try{AiModulePlan.build(new JSONObject("{\"type\":\"complete_task\",\"index\":1,\"value\":{\"title\":\"其他任务\"}}"),context);fail();}catch(IllegalArgumentException expected){}
  try{AiModulePlan.build(new JSONObject("{\"type\":\"delete_task\",\"index\":2}"),context);fail();}catch(IllegalArgumentException expected){}
  try{AiModulePlan.build(new JSONObject("{\"type\":\"add_parcel\",\"value\":{\"code\":\"A|B\"}}"),context);fail();}catch(IllegalArgumentException expected){}
  assertNotNull(AiModulePlan.build(new JSONObject("{\"type\":\"add_task\",\"value\":{\"title\":\"复习\",\"deadline\":\"2026-11-20 19:00\"}}"),context));
 }
}
