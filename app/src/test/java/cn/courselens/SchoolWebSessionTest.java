package cn.courselens;

import org.junit.Test;
import java.net.*;
import java.util.*;
import static org.junit.Assert.*;

public class SchoolWebSessionTest {
 @Test public void browserSessionUsesExistingJsonParserWithoutPassword()throws Exception {
  List<String> requests=new ArrayList<>();SchoolClient client=new SchoolClient(){
   @Override Response fetchPage(String url)throws Exception {
    assertTrue(SchoolClient.allowed(url));requests.add(url);
    if(url.contains("get-data?"))return new Response(url,"{\"lessonIds\":[9],\"lessons\":[{\"id\":9,\"course\":{\"nameZh\":\"合成数学\"}}]}");
    if(url.contains("print-data?"))return new Response(url,"[{\"lessonId\":9,\"weekday\":2,\"startUnit\":3,\"endUnit\":4,\"weekIndexes\":[1,3],\"roomName\":\"合成教室\",\"teachers\":[\"合成教师\"]}]");
    throw new AssertionError("Unexpected request");
   }
  };
  String html="<script>var currentSemester={\"id\":31};var bizTypeId=2;</script>";
  String result=client.syncWebPage(SchoolClient.TABLE+"/info/123",html,"session=synthetic=value; vpn=synthetic");Course course=Course.parse(result).get(0);
  assertEquals("合成数学",course.name);assertEquals("合成教师",course.teacher);assertEquals("1,3",course.weeks);assertEquals(2,requests.size());assertTrue(requests.get(0).contains("dataId=123"));
  var field=SchoolClient.class.getDeclaredField("cookies");field.setAccessible(true);CookieManager cookies=(CookieManager)field.get(client);assertTrue(cookies.get(URI.create(SchoolClient.TABLE),Collections.emptyMap()).toString().contains("session=synthetic=value"));assertTrue(cookies.get(URI.create("https://other.example/"),Collections.emptyMap()).get("Cookie").isEmpty());client.logout();assertTrue(cookies.getCookieStore().getCookies().isEmpty());
 }
 @Test public void rejectsOtherOriginsAndNonTimetablePages()throws Exception {
  SchoolClient client=new SchoolClient();for(String url:new String[]{"https://vpn.squ.edu.cn.evil.test/for-std/course-table","http://vpn.squ.edu.cn/for-std/course-table",SchoolClient.HOST+"/login",SchoolClient.TABLE+"-fake"}){assertFalse(SchoolClient.webPage(url));try{client.syncWebPage(url,"","secret=synthetic");fail();}catch(java.io.IOException expected){}}
 }
}
