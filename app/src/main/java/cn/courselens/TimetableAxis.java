package cn.courselens;

import java.time.LocalTime;import java.util.*;

final class TimetableAxis {
 static final class Exam {final int start,end;Exam(String start,String end){this.start=minute(start);this.end=end==null||end.trim().isEmpty()?this.start:minute(end);}}
 static final class Segment {final int begin,end;final float top,height;Segment(int begin,int end,float top,float height){this.begin=begin;this.end=end;this.top=top;this.height=height;}}
 private final List<Segment> segments=new ArrayList<>();final float[] starts,ends;final float height;private float extent;
 TimetableAxis(String[] start,String[] end,List<Exam> exams){starts=new float[start.length];ends=new float[start.length];int begin=minute(start[0]),finish=minute(end[end.length-1]);for(Exam exam:exams){begin=Math.min(begin,exam.start);finish=Math.max(finish,exam.end);}int cursor=begin;for(int i=0;i<start.length;i++){int a=minute(start[i]),b=minute(end[i]);gap(cursor,a,i==0);starts[i]=extent;segments.add(new Segment(a,b,extent,1));extent++;ends[i]=extent;cursor=b;}gap(cursor,finish,true);float total=extent;for(Exam exam:exams)total=Math.max(total,y(exam.start)+Math.max(1f,y(exam.end)-y(exam.start)));height=total;}
 private void gap(int begin,int end,boolean outside){if(end<=begin)return;float size=outside?Math.max(1f,Math.min(2f,(end-begin)/60f)):0;segments.add(new Segment(begin,end,extent,size));extent+=size;}

 float y(int minute){for(Segment segment:segments)if(minute>=segment.begin&&minute<segment.end)return segment.top+segment.height*(minute-segment.begin)/(segment.end-segment.begin);return minute<segments.get(0).begin?0:extent;}
 static int minute(String value){LocalTime time=LocalTime.parse(value.trim());return time.getHour()*60+time.getMinute();}

}
