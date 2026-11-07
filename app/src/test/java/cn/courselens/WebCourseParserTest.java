package cn.courselens;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class WebCourseParserTest {
 private String grid(String id,String cells){return "<table id='"+id+"'><tr><th>节次</th><th>星期一</th><th>星期二</th><th>星期三</th><th>星期四</th><th>星期五</th><th>星期六</th><th>星期日</th></tr>"+cells+"</table>";}
 private List<Course> courses(String source)throws Exception{return Course.parse(WebCourseParser.parse(source).courses);}
 @Test public void newZhengfangPreservesSeparatedWeeksAndFields()throws Exception {
  String html=grid("table1","<tr><td class='festival'>1</td><td><div><span class='title'>合成数学</span><p title='教师'>合成教师</p><p title='上课地点'>合成教室</p><p title='节/周'>(1-2节)1-8周(单),10-12周(双)</p></div></td></tr>");
  Course c=courses(html).get(0);assertEquals("新正方",WebCourseParser.parse(html).system);assertEquals(1,c.day);assertEquals(1,c.start);assertEquals(2,c.end);assertEquals("1,3,5,7,10,12",c.weeks);assertEquals("合成教师",c.teacher);assertEquals("合成教室",c.room);
 }
 @Test public void qiangzhiMultipleCoursesInOneCellUseExplicitSections()throws Exception {
  String course="合成数学<br><font title='老师'>测试教师</font><br><font title='周次(节次)'>1-6(单周)[03-04节]</font><font title='教室'>测试楼</font>";
  List<Course> parsed=courses(grid("kbtable","<tr><th>第3-4节</th><td><div class='kbcontent'>"+course+"<br>----------<br>"+course.replace("合成数学","合成英语")+"</div></td></tr>"));
  assertEquals(2,parsed.size());assertEquals(3,parsed.get(0).start);assertEquals(4,parsed.get(0).end);assertEquals("1,3,5",parsed.get(0).weeks);assertEquals("合成英语",parsed.get(1).name);
 }
 @Test public void newZhengfangListAndIconLabels()throws Exception {
  String html="<table id='kblist_table'><tbody><tr><th>星期二</th></tr><tr><td>3-4</td><td><div><span class='title'>合成课程</span><p><span title='教师'></span>合成教师</p><p><font>上课地点：A101</font></p><p><font>周次：1-8周(单)</font></p></div></td></tr></tbody></table>";
  Course c=courses(html).get(0);assertEquals(2,c.day);assertEquals(3,c.start);assertEquals(4,c.end);assertEquals("合成教师",c.teacher);assertEquals("A101",c.room);assertEquals("1,3,5,7",c.weeks);
 }
 @Test public void oldZhengfangKeepsRowspanAndMultipleCourses()throws Exception {
  String html=grid("Table1","<tr><td>第1节</td><td rowspan='2'>数学<br>周一第1,2节{第1-8周}<br>甲老师<br>A101<br><br>英语<br>周一第1,2节{第9-16周}<br>乙老师<br>B202</td><td></td></tr><tr><td>第2节</td><td>物理<br>周二第2节{第1-16周}<br>C303</td></tr>");
  List<Course> parsed=courses(html);assertEquals(3,parsed.size());Course physics=parsed.get(2);assertEquals("物理",physics.name);assertEquals(2,physics.day);assertEquals(2,physics.start);assertEquals("",physics.teacher);
 }
 @Test public void urpListsExpandMergedNamesAndPeriodCount()throws Exception {
  String html="<table class='displayTag'><tr><th>课程名</th><th>教师</th><th>周次</th><th>星期</th><th>节次</th><th>节数</th><th>教学楼</th><th>教室</th></tr><tr><td rowspan='2'>合成课程</td><td rowspan='2'>测试教师</td><td>1-8单周</td><td>星期三</td><td>第3小节</td><td>2</td><td>A楼</td><td>101</td></tr><tr><td>10-16双周</td><td>四</td><td>5</td><td>3</td><td>B楼</td><td>202</td></tr></table>";
  List<Course> parsed=courses(html);assertEquals(2,parsed.size());assertEquals("合成课程",parsed.get(1).name);assertEquals(4,parsed.get(1).day);assertEquals(7,parsed.get(1).end);assertEquals("B楼202",parsed.get(1).room);
 }
 @Test public void nonContiguousPeriodsAreNotInvented()throws Exception {
  String html="<table><tr><th>课程名称</th><th>星期</th><th>节次</th><th>周次</th></tr><tr><td>合成课程</td><td>1</td><td>1,2,4</td><td>1-8</td></tr></table>";
  List<Course> parsed=courses(html);assertEquals(2,parsed.size());assertEquals(2,parsed.get(0).end);assertEquals(4,parsed.get(1).start);
 }
 @Test public void newUrpJsonWeekMaskHasNoDefaultWeeks()throws Exception {
  String json="{\"dateList\":[{\"selectCourseList\":[{\"courseName\":\"合成课程\",\"attendClassTeacher\":\"合成教师\",\"timeAndPlaceList\":[{\"classWeek\":\"101001\",\"classDay\":5,\"classSessions\":3,\"continuingSession\":2,\"classroomName\":\"A101\"}]}]}]}";
  Course c=courses(json).get(0);assertEquals("1,3,6",c.weeks);assertEquals(5,c.day);assertEquals(4,c.end);assertEquals(c.encode(),courses("<html><body><pre>"+json+"</pre></body></html>").get(0).encode());
 }
 @Test public void newZhengfangJsonReusesExistingParser()throws Exception {
  String json="{\"kbList\":[{\"jcs\":\"3-4\",\"zcd\":\"1-8(单)\",\"kcmc\":\"合成课程\",\"xqj\":2,\"xm\":\"合成教师\"}]}";Course c=courses(json).get(0);assertEquals(2,c.day);assertEquals("1,3,5,7",c.weeks);
 }
 @Test public void unknownPageAndImagesDoNotGenerateCourses()throws Exception {assertTrue(courses("<img src='schedule.png'>").isEmpty());}
 @Test public void invalidRecognizedCourseFailsWithoutPartialImport()throws Exception {
  String good="<div><span class='title'>课程</span><p title='节/周'>(1-2节)1-8周</p></div>";String html=grid("table1","<tr><td class='festival'>1</td><td>"+good+good.replace("1-8周","未知周次")+"</td></tr>");
  try{courses(html);fail();}catch(IllegalArgumentException expected){}
 }
 @Test public void mixedParityAndInvalidWeeks(){assertEquals("1,3,5,7,11",WebCourseParser.parseWeeks("1-8,10-12周(单)"));assertEquals("1,3,10,12",WebCourseParser.parseWeeks("第1-4周(单),10-12周(双)"));for(String invalid:new String[]{"","0-8","1-31","4-1","1,,2","1-4单,未知"}){try{WebCourseParser.parseWeeks(invalid);fail(invalid);}catch(IllegalArgumentException expected){}}}
}
