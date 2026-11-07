package cn.courselens;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

public class WebTimetableTest {
 private String table(String day,String units,String weeks){return "<table><tr><th>课程名称</th><th>星期</th><th>节次</th><th>周次</th><th>教师</th><th>教室</th></tr><tr><td>测试数学</td><td>"+day+"</td><td>"+units+"</td><td>"+weeks+"</td><td>测试教师</td><td>测试教室</td></tr></table>";}
 @Test public void listTablePreservesSchedule(){List<Course> courses=Course.parse(SchoolParser.webCourses(table("星期二","3-4节","1-8周（单）")));assertEquals(1,courses.size());Course c=courses.get(0);assertEquals(2,c.day);assertEquals(3,c.start);assertEquals(4,c.end);assertEquals("1,3,5,7",c.weeks);assertEquals("测试教师",c.teacher);assertEquals("测试教室",c.room);}
 @Test public void emptyAndImagePagesDoNotImport(){assertEquals("",SchoolParser.webCourses("<img src='schedule.png'>"));assertEquals("",SchoolParser.webCourses("<form><input type='password'></form>"));}
 @Test public void malformedTableFailsAsAWhole(){try{SchoolParser.webCourses(table("星期二","3-4节","1-8")+table("星期八","3-4节","1-8"));fail();}catch(IllegalArgumentException expected){}}
 @Test public void urlBoundary(){assertTrue(WebTimetableActivity.allowed("https://school.example.edu.cn/timetable"));for(String url:new String[]{"http://school.edu.cn","https://user:pass@school.edu.cn","file:///sdcard/test","javascript:alert(1)","intent://login","https://school.edu.cn:444/","https:///missing"})assertFalse(url,WebTimetableActivity.allowed(url));}
}
