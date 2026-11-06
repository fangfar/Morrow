package cn.courselens;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;
public class TimetableAxisTest {
 private final String[] start={"08:00","09:00","14:00"},end={"08:45","09:45","14:45"};
 @Test public void ordinaryPeriodsKeepOriginalRows(){TimetableAxis axis=new TimetableAxis(start,end,List.of());assertEquals(3f,axis.height,0);assertArrayEquals(new float[]{0,1,2},axis.starts,0);}
 @Test public void timesInsidePeriodsAndBreaksUseTheirActualPosition(){TimetableAxis axis=new TimetableAxis(start,end,List.of(new TimetableAxis.Exam("08:15","09:15"),new TimetableAxis.Exam("12:00","13:00")));assertEquals(1/3f,axis.y(495),.001);assertTrue(axis.y(720)>axis.ends[1]);assertTrue(axis.y(780)<axis.starts[2]);assertEquals(6f,axis.height,0);}
 @Test public void outsideHoursExtendAxisAndUnknownEndStaysPoint(){TimetableAxis.Exam point=new TimetableAxis.Exam("20:00","");assertEquals(point.start,point.end);TimetableAxis axis=new TimetableAxis(start,end,List.of(new TimetableAxis.Exam("07:00","07:30"),point));assertTrue(axis.starts[0]>0);assertTrue(axis.y(1200)>=axis.ends[2]);assertTrue(axis.height>=axis.y(1200)+1);}
}
