package cn.courselens;

import java.util.*;

/** Three-way merge against the last unedited source snapshot. */
final class TimetableSync {
 static final String BASELINE="syncCourses",PROTECTED="syncProtectedCourses";
 static final class Result {
  final String courses;final Set<String> protectedNames;
  Result(String courses,Set<String> names){this.courses=courses;protectedNames=names;}
 }
 static Result merge(String baseline,String local,String remote,Set<String> protectedNames,String adjustments){
  Map<String,List<String>> current=groups(local),fresh=groups(remote),before=groups(baseline==null?remote:baseline);
  Set<String> keep=new HashSet<>(protectedNames),names=new HashSet<>(before.keySet());names.addAll(current.keySet());
  for(String name:names)if(!Objects.equals(before.get(name),current.get(name)))keep.add(name);
  for(CourseAdjustment adjustment:CourseAdjustment.parse(adjustments))keep.add(adjustment.original.name);
  if(baseline!=null){
   boolean protectedDisappeared=false;for(String name:before.keySet())if(keep.contains(name)&&!fresh.containsKey(name))protectedDisappeared=true;
   if(protectedDisappeared)for(String name:fresh.keySet())if(!before.containsKey(name)&&!current.containsKey(name))throw new IllegalArgumentException("来源课程名称变化，无法安全对应本地修改，请先手动核对课程。");
  }
  // Same-name arrangements have no stable school ID; preserve the whole course when any part was edited.
  List<String> rows=new ArrayList<>();
  for(Course course:Course.parse(local))if(keep.contains(course.name))rows.add(course.encode());
  for(Course course:Course.parse(remote))if(!keep.contains(course.name))rows.add(course.encode());
  return new Result(String.join("\n",rows),keep);
 }
 private static Map<String,List<String>> groups(String value){
  Map<String,List<String>> groups=new LinkedHashMap<>();
  for(Course course:Course.parse(value)){
   List<String> weeks=new ArrayList<>();for(int week=1;week<=30;week++)if(course.active(week))weeks.add(""+week);
   Course normalized=new Course(course.day,course.start,course.end,course.name,course.room,String.join(",",weeks),course.teacher,course.details,course.className);
   groups.computeIfAbsent(course.name,key->new ArrayList<>()).add(normalized.encode());
  }
  for(List<String> rows:groups.values())Collections.sort(rows);
  return groups;
 }
}
