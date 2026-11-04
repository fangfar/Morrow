package cn.courselens;

import static org.junit.Assert.*;
import java.time.LocalDate;
import org.json.JSONObject;
import org.junit.Test;

public class DeepSeekContextTest {
 @Test public void relativeWeekAndPeriodUseLocalDateAndTimetable()throws Exception{
  JSONObject context=DeepSeekClient.context(LocalDate.parse("2026-11-04"),"","[]","2026-08-31",10,"08:00-08:40\n08:50-09:30","");
  assertEquals("2026-11-13",context.getJSONArray("next_week_dates").getString(4));
  assertEquals("08:00",context.getJSONArray("periods").getJSONObject(0).getString("start"));
  JSONObject sunday=DeepSeekClient.context(LocalDate.parse("2026-11-08"),"","[]","2026-08-31",10,"08:00-08:40","");
  assertEquals("2026-11-13",sunday.getJSONArray("next_week_dates").getString(4));
 }
}
