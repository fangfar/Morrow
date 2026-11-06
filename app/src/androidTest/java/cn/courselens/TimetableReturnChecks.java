package cn.courselens;

import android.app.Instrumentation;
import android.os.*;
import android.view.*;
import androidx.viewpager.widget.ViewPager;
import java.lang.reflect.Field;

final class TimetableReturnChecks {
 static void run(Instrumentation i,MainActivity a)throws Exception{
  AiChecks.main(i,()->{StringBuilder data=new StringBuilder();for(int day=1;day<=7;day++)for(int n=1;n<12;n+=2)data.append(day+"|"+n+"|"+(n+1)+"|Swipe "+day+"-"+n+"|A101|1-30\n");AiChecks.set(a,"data",data.toString());AiChecks.set(a,"week",6);AiChecks.set(a,"notes","");a.getPreferences(0).edit().putString("aiExams","[]").commit();AiChecks.call(a,"home");});i.waitForIdleSync();SystemClock.sleep(300);
  ViewPager pager=(ViewPager)AiChecks.get(a,"pager");Field first=ViewPager.class.getDeclaredField("mFirstLayout");first.setAccessible(true);
  for(int trial=0;trial<12;trial++){
   int direction=trial%2==0?1:-1;
   AiChecks.main(i,()->{pager.setCurrentItem(5,false);a.findViewById(android.R.id.content).findViewWithTag("nav-ai").performClick();a.findViewById(android.R.id.content).findViewWithTag("nav-timetable").performClick();});i.waitForIdleSync();SystemClock.sleep(200);
   Bundle before=new Bundle();before.putString("swipeBefore","firstLayout="+first.getBoolean(pager)+" width="+pager.getWidth()+" scrollX="+pager.getScrollX()+" item="+pager.getCurrentItem());i.sendStatus(0,before);AiChecks.check(!first.getBoolean(pager),"Returned pager still awaits layout before first horizontal gesture");
   int[] point=new int[2];AiChecks.main(i,()->pager.getLocationInWindow(point));float x=point[0]+pager.getWidth()*(direction>0?.8f:.2f),y=Math.max(point[1]+100,600);long down=SystemClock.uptimeMillis();event(i,a,down,MotionEvent.ACTION_DOWN,x,y);
   for(int step=1;step<=20;step++){SystemClock.sleep(16);event(i,a,down,MotionEvent.ACTION_MOVE,x-direction*pager.getWidth()*.6f*step/20,y);}
   event(i,a,down,MotionEvent.ACTION_UP,x-direction*pager.getWidth()*.6f,y);i.waitForIdleSync();SystemClock.sleep(650);i.waitForIdleSync();Bundle after=new Bundle();after.putString("swipeAfter","firstLayout="+first.getBoolean(pager)+" scrollX="+pager.getScrollX()+" item="+pager.getCurrentItem());i.sendStatus(0,after);View selected=pager.findViewWithTag("week-grid-"+(6+direction));AiChecks.check(pager.getCurrentItem()==5+direction&&selected!=null&&selected.getLeft()==pager.getScrollX(),"First swipe did not settle on the selected week");
  }
 }
 private static void event(Instrumentation i,MainActivity a,long down,int action,float x,float y){AiChecks.main(i,()->{MotionEvent event=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,x,y,0);try{a.getWindow().getDecorView().dispatchTouchEvent(event);}finally{event.recycle();}});}
}
