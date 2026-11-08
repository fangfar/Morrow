package cn.courselens;

import org.json.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.time.Duration;
import java.util.concurrent.*;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34)
public class WebTimetableSyncTest {
 private JSONObject page(String url)throws Exception{return new JSONObject().put("url",url).put("html","<table><tr><th>课程名称</th><th>星期</th><th>节次</th><th>周次</th></tr><tr><td>合成数学</td><td>星期二</td><td>3-4节</td><td>1-8周</td></tr></table>");}
 @Test public void acceptsOnlyBoundOriginAndDeduplicatesFrames()throws Exception {
  JSONArray pages=new JSONArray().put(page("https://school.example/course")).put(page("https://school.example/frame")).put(page("https://other.example/course"));
  assertEquals(1,Course.parse(WebTimetableSync.parsePages(pages,"https://school.example/")).size());
  try{WebTimetableSync.parsePages(pages,"https://missing.example/");fail();}catch(java.io.IOException expected){}
 }
 @Test public void timeoutAndCancellationReleaseHiddenBrowser()throws Exception {
  ExecutorService worker=Executors.newSingleThreadExecutor();int[] calls={0};
  try{
   WebTimetableSync sync=new WebTimetableSync(RuntimeEnvironment.getApplication(),"https://school.example/course",worker,(courses,times,error)->{assertNull(courses);assertNotNull(error);calls[0]++;});
   shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofSeconds(31));assertEquals(1,calls[0]);sync.cancel();
   WebTimetableSync cancelled=new WebTimetableSync(RuntimeEnvironment.getApplication(),"https://school.example/course",worker,(courses,times,error)->fail("Cancelled read completed"));cancelled.cancel();shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofSeconds(31));assertEquals(1,calls[0]);
  }finally{worker.shutdownNow();}
 }
}
