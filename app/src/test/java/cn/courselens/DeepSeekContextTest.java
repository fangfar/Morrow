package cn.courselens;

import static org.junit.Assert.*;
import java.time.LocalDate;
import org.json.JSONObject;
import org.junit.Test;

public class DeepSeekContextTest {
 @Test public void clockUsesDeviceZoneAndUpdatesAcrossMidnight()throws Exception{java.time.ZonedDateTime now=java.time.ZonedDateTime.parse("2026-11-07T00:01:02+08:00[Asia/Shanghai]");JSONObject clock=DeepSeekClient.clock(now);assertEquals("2026-11-07",clock.getString("date"));assertEquals("00:01:02",clock.getString("time"));assertEquals("Asia/Shanghai",clock.getString("timezone"));assertEquals(6,clock.getInt("weekday"));assertEquals("2026-11-06",DeepSeekClient.clock(now.withZoneSameInstant(java.time.ZoneOffset.UTC)).getString("date"));assertEquals(now.toInstant().toEpochMilli(),clock.getLong("epoch_ms"));}
 @Test public void attachmentsAreSentAsImagePartsAndDocumentText()throws Exception{org.json.JSONArray attachments=new org.json.JSONArray().put(new JSONObject().put("kind","image").put("name","截图.png").put("file_id","file-api-test").put("expires_at",200)).put(new JSONObject().put("kind","text").put("name","作业.txt").put("text","实验数据"));org.json.JSONArray history=new org.json.JSONArray().put(new JSONObject().put("role","user").put("content","请分析").put("attachments",attachments));org.json.JSONArray parts=DeepSeekClient.wireMessages(history,100).getJSONObject(0).getJSONArray("content");assertEquals("file-api-test",parts.getJSONObject(2).getString("file_id"));assertTrue(parts.getJSONObject(3).getString("text").contains("实验数据"));org.json.JSONArray expired=DeepSeekClient.wireMessages(history,201).getJSONObject(0).getJSONArray("content");assertEquals("text",expired.getJSONObject(1).getString("type"));assertTrue(expired.getJSONObject(1).getString("text").contains("过期"));assertTrue(history.getJSONObject(0).has("attachments"));}
 @Test public void relativeWeekAndPeriodUseLocalDateAndTimetable()throws Exception{
  JSONObject context=DeepSeekClient.context(LocalDate.parse("2026-11-04"),"","[]","2026-08-31",10,"08:00-08:40\n08:50-09:30","");
  assertEquals("2026-11-13",context.getJSONArray("next_week_dates").getString(4));
  assertEquals("08:00",context.getJSONArray("periods").getJSONObject(0).getString("start"));
  JSONObject sunday=DeepSeekClient.context(LocalDate.parse("2026-11-08"),"","[]","2026-08-31",10,"08:00-08:40","");
  assertEquals("2026-11-13",sunday.getJSONArray("next_week_dates").getString(4));
 }
}
