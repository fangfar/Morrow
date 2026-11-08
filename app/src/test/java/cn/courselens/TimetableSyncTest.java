package cn.courselens;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class TimetableSyncTest {
 private static String course(String name,String room){return "1|1|2|"+name+"|"+room+"|1-8";}
 @Test public void updatesUntouchedCoursesAndKeepsEditsDeletionsAndManualAdditions(){
  String original=course("A","旧")+"\n"+course("B","旧")+"\n"+course("C","旧");
  String local=course("A","自选")+"\n"+course("C","旧")+"\n"+course("手动","自选");
  String remote=course("A","新")+"\n"+course("B","新")+"\n"+course("C","新")+"\n"+course("D","新");
  TimetableSync.Result result=TimetableSync.merge(original,local,remote,Set.of(),"");
  assertEquals(course("A","自选")+"\n"+course("手动","自选")+"\n"+course("C","新")+"\n"+course("D","新"),result.courses);
  assertTrue(result.protectedNames.containsAll(Set.of("A","B","手动")));
 }
 @Test public void deletionStaysDeletedEvenAfterSchoolRemovesAndReintroducesIt(){
  String b=course("B","新");TimetableSync.Result first=TimetableSync.merge(course("A","旧")+"\n"+b,b,course("A","新")+"\n"+b,Set.of(),"");
  TimetableSync.Result removed=TimetableSync.merge(course("A","新")+"\n"+b,first.courses,b,first.protectedNames,"");
  TimetableSync.Result again=TimetableSync.merge(course("B","新"),removed.courses,course("A","更新")+"\n"+course("B","新"),removed.protectedNames,"");
  assertEquals(course("B","新"),again.courses);
 }
 @Test public void missingLegacySnapshotOnlyEstablishesBaseline(){
  String local=course("自改","自选"),remote=course("原课","学校");
  assertEquals(local,TimetableSync.merge(null,local,remote,Set.of(),"").courses);
 }
 @Test public void equivalentWeekFormattingAndOrderDoNotCountAsEdits(){
  String a=course("A","旧"),b=course("B","旧");
  assertEquals(course("A","新"),TimetableSync.merge(a+"\n"+b,b+"\n"+a.replace("1-8","1,2,3,4,5,6,7,8"),course("A","新"),Set.of(),"").courses);
 }
 @Test public void oneTimeAdjustmentsProtectTheirUnderlyingCourse(){
  Course original=Course.parse(course("A","旧")).get(0);
  String changes=new CourseAdjustment("2026-09-01",1,original,null).encode();
  assertEquals(original.encode(),TimetableSync.merge(original.encode(),original.encode(),course("A","新"),Set.of(),changes).courses);
 }
 @Test public void invalidRemoteNeverProducesMergedData(){
  try{TimetableSync.merge(course("A","旧"),course("A","旧"),"invalid",Set.of(),"");fail();}catch(IllegalArgumentException expected){}
 }
 @Test public void ambiguousSchoolRenameCannotResurrectDeletedCourse(){
  try{TimetableSync.merge(course("旧名称","旧"),"",course("新名称","新"),Set.of(),"");fail();}catch(IllegalArgumentException expected){}
 }
 @Test public void deletingOneOfDuplicateArrangementsIsStillALocalChange(){
  String a=course("A","旧");assertEquals(a,TimetableSync.merge(a+"\n"+a,a,course("A","新"),Set.of(),"").courses);
 }
}
