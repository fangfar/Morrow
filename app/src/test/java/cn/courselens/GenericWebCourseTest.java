package cn.courselens;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class GenericWebCourseTest {
 private String grid(String rows){return "<table><tr><th>时段</th><th>节次</th><th>周一<br>11/9</th><th>周二<br>11/10</th></tr>"+rows+"</table>";}
 private List<Course> parse(String html)throws Exception{return Course.parse(WebCourseParser.parse(html).courses);}
 private String block(String name,String weeks){return "<div><h4>"+name+"</h4><p>周次："+weeks+"</p><p>授课教师：合成教师</p><p>上课地点：A101</p></div>";}
 private void invalid(String html)throws Exception{try{parse(html);fail("Ambiguous data must not be imported");}catch(IllegalArgumentException expected){}}
 @Test public void anonymousGridUsesRowspanAndPositionWithoutSchoolIds()throws Exception {
  String html=grid("<tr><th rowspan='2'>上午</th><th>1</th><td rowspan='2'>"+block("合成数学","1-8周(单),10-12周(双)")+"</td><td>"+block("合成英语","1-16周")+"</td></tr><tr><th>2</th><td></td></tr>");
  List<Course> courses=parse(html);assertEquals(2,courses.size());Course math=courses.get(0);assertEquals("合成数学",math.name);assertEquals(1,math.day);assertEquals(1,math.start);assertEquals(2,math.end);assertEquals("1,3,5,7,10,12",math.weeks);assertEquals("合成教师",math.teacher);assertEquals("A101",math.room);
 }
 @Test public void transposedGridExpandsPeriodColspan()throws Exception {
  String html="<table><tr><th>星期</th><th>第1节</th><th>第2节</th><th>第3-4节</th></tr><tr><th>二</th><td colspan='2'>"+block("合成物理","1-8周")+"</td><td>"+block("合成实验","9-16周")+"</td></tr></table>";
  List<Course> courses=parse(html);assertEquals(2,courses.size());assertEquals(2,courses.get(0).day);assertEquals(2,courses.get(0).end);assertEquals(3,courses.get(1).start);assertEquals(4,courses.get(1).end);
 }
 @Test public void nestedCourseCardsRemainSeparate()throws Exception {
  String html=grid("<tr><th>上午</th><th>1-2</th><td><div>"+block("合成课程甲","1-8周")+block("合成课程乙","9-16周")+"</div></td><td></td></tr>");
  List<Course> courses=parse(html);assertEquals(2,courses.size());assertEquals("合成课程乙",courses.get(1).name);
 }
 @Test public void plainTextScheduleKeepsUnknownFieldsAsNotes()throws Exception {
  String html=grid("<tr><th>上午</th><th>3-4</th><td>合成课程<br>第1至8周(单)<br>合成教师未标注<br>未标注地点</td><td></td></tr>");
  Course course=parse(html).get(0);assertEquals("1,3,5,7",course.weeks);assertEquals("",course.teacher);assertEquals("",course.room);assertTrue(course.details.contains("合成教师未标注"));assertTrue(course.details.contains("未标注地点"));
 }
 @Test public void labelledInlineFieldsAndExplicitTimes()throws Exception {
  String html=grid("<tr><th>上午</th><th>1-2</th><td><div>课程名称：数学 教师：甲老师 教室：A101 周次：单周1-8 节次：1-2 星期：周一</div></td><td></td></tr>");
  Course course=parse(html).get(0);assertEquals("数学",course.name);assertEquals("甲老师",course.teacher);assertEquals("1,3,5,7",course.weeks);
 }
 @Test public void aliasesAndCombinedScheduleColumnDoNotNeedExactHeaders()throws Exception {
  String html="<table><tr><th>科目名称</th><th>授课老师</th><th>授课时间</th><th>地点</th></tr><tr><td>合成课程</td><td>合成教师</td><td>周二第3-4节；第1-8周(双)</td><td>合成教室</td></tr></table>";
  Course course=parse(html).get(0);assertEquals(2,course.day);assertEquals(3,course.start);assertEquals(4,course.end);assertEquals("2,4,6,8",course.weeks);assertEquals("合成教室",course.room);
 }
 @Test public void multilevelListHeaderAndRepeatedHeaderPreserveMergedCourseNames()throws Exception {
  String html="<table><tr><th rowspan='2'>科目</th><th colspan='3'>上课安排</th></tr><tr><th>教学周</th><th>星期几</th><th>课节</th></tr><tr><td rowspan='2'>合成课程</td><td>1-8单周</td><td>三</td><td>1-2</td></tr><tr><td>9-16双周</td><td>五</td><td>3-4</td></tr></table>";
  List<Course> courses=parse(html);assertEquals(2,courses.size());assertEquals("合成课程",courses.get(1).name);assertEquals(5,courses.get(1).day);
 }
 @Test public void listAndGridOnSamePageAreBothImported()throws Exception {
  String list="<table><tr><th>课程</th><th>时间安排</th></tr><tr><td>明细课程</td><td>周一第1节 第1-8周</td></tr></table>";
  String html=list+grid("<tr><th>下午</th><th>5-6</th><td>"+block("网格课程","1-8周")+"</td><td></td></tr>");assertEquals(2,parse(html).size());
 }
 @Test public void partialMissingWeeksAndPositionConflictsRejectWholeTable()throws Exception {
  String rows="<tr><th>上午</th><th>1-2</th><td>"+block("有效课程","1-8周")+"</td><td>"+block("问题课程","待定")+"</td></tr>";invalid(grid(rows));
  invalid(grid("<tr><th>上午</th><th>1-2</th><td><h4>冲突课程</h4><p>周次：1-8周</p><p>节次：5-6节</p></td><td></td></tr>"));
  invalid(grid("<tr><th>上午</th><th>1-2</th><td><h4>冲突课程</h4><p>周次：1-8周</p><p>星期：星期三</p></td><td></td></tr>"));
 }
 @Test public void mergedWeekdaysAndUndelimitedMultipleCoursesAreNotGuessed()throws Exception {
  invalid(grid("<tr><th>上午</th><th>1-2</th><td colspan='2'>"+block("跨天课程","1-8周")+"</td></tr>"));
  invalid(grid("<tr><th>上午</th><th>1-2</th><td>甲课程<br>1-8周<br>未知教师<br>乙课程<br>9-16周</td><td></td></tr>"));
 }
 @Test public void emptyTimetableAndNavigationDoNotBecomeCourses()throws Exception {
  assertTrue(parse(grid("<tr><th>上午</th><th>1-2</th><td>暂无课程</td><td>—</td></tr>")).isEmpty());assertTrue(parse("<table><tr><td>周一周二周三</td><td>返回</td></tr></table>").isEmpty());
 }
 @Test public void explicitPeriodBeyondOneCellAnchorAndRepeatedGridHeader()throws Exception {
  String html="<table><tr><th>节次</th><th>星期一</th><th>星期二</th></tr><tr><td>1</td><td><h4>数学</h4><p>(1-2节)1-8周</p></td><td></td></tr><tr><th>节次</th><th>星期一</th><th>星期二</th></tr></table>";Course c=parse(html).get(0);assertEquals(2,c.end);
 }
 @Test public void semanticCourseCardsOutsideTablesHaveExplicitSchedule()throws Exception {
  String html="<article data-course-name='合成数学'><h3 class='course-name'>合成数学</h3><p>星期：星期二</p><p>节次：3-4</p><p>周次：1-8周</p><p>教师：甲老师</p></article><article data-course-name='合成物理'><p>周五</p><p>第5-6节 第9-16周(双)</p></article>";
  List<Course> courses=parse(html);assertEquals(2,courses.size());assertEquals(2,courses.get(0).day);assertEquals(3,courses.get(0).start);assertEquals("合成物理",courses.get(1).name);assertEquals(5,courses.get(1).day);assertEquals("10,12,14,16",courses.get(1).weeks);
 }
 @Test public void incompleteCardDoesNotDisappearBehindValidCard()throws Exception {
  invalid("<article><h3 class='course-name'>有效课程</h3><p>周二第1-2节 第1-8周</p></article><article><h3 class='course-name'>缺少周次的课程</h3><p>周三第3-4节</p></article>");assertTrue(parse("<h3>新闻</h3><p>欢迎回来</p>").isEmpty());
 }
 @Test public void incompleteListIsNotHiddenByAnotherValidTable()throws Exception {
  invalid("<table><tr><th>课程名称</th><th>周次</th></tr><tr><td>缺少时间的课程</td><td>1-8周</td></tr></table>"+grid("<tr><th>上午</th><th>1-2</th><td>"+block("有效课程","1-8周")+"</td><td></td></tr>"));
 }
 @Test public void incompleteUnlabelledWeekSuffixIsNotSilentlyIgnored()throws Exception {
  invalid(grid("<tr><th>上午</th><th>1-2</th><td>合成课程<br>第1-8周，待定</td><td></td></tr>"));
 }
 @Test public void schoolLikeIdsDoNotOverrideSemanticStructure()throws Exception {
  String html=grid("<tr><th>上午</th><th>1-2</th><td>"+block("普通学校课程","1-8周")+"</td><td></td></tr>");
  for(String id:new String[]{"Table1","table1","kbtable"})assertEquals("普通学校课程",parse(html.replace("<table>","<table id='"+id+"'>")).get(0).name);
 }
}
