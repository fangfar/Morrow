package cn.courselens;
import android.app.Activity;
import android.animation.ValueAnimator;
import android.os.SystemClock;
import android.view.*;
import android.widget.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.util.ReflectionHelpers;
import java.time.Duration;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk={34})
public class MotionHandoffTest {
 @Before public void setup(){org.robolectric.shadows.ShadowChoreographer.setPaused(true);org.robolectric.shadows.ShadowChoreographer.setFrameDelay(Duration.ofMillis(16));ReflectionHelpers.callStaticMethod(ValueAnimator.class,"setDurationScale",ReflectionHelpers.ClassParameter.from(float.class,1f));}
 private void layout(View v){org.robolectric.shadows.ShadowChoreographer.setPaused(false);org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();org.robolectric.shadows.ShadowChoreographer.setPaused(true);v.measure(View.MeasureSpec.makeMeasureSpec(390,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(844,View.MeasureSpec.EXACTLY));v.layout(0,0,390,844);}
 private MotionEvent event(int action,float x,float y){long t=SystemClock.uptimeMillis();return MotionEvent.obtain(t,t,action,x,y,0);}
 private void advance(Motion.Progress p){p.to(1,340,false,()->{});ValueAnimator a=ReflectionHelpers.getField(p,"animator");assertNotNull(a);a.setCurrentPlayTime(100);}
 @Test public void sheetDragStartsFromTheCurrentAnimationFrame(){
  try(ActivityController<Activity> c=Robolectric.buildActivity(Activity.class).setup().visible()){
   Activity a=c.get();MotionOverlay o=new MotionOverlay(a);View shade=new View(a),panel=new View(a);o.addView(shade,new FrameLayout.LayoutParams(-1,-1));o.addView(panel,new FrameLayout.LayoutParams(-1,300,Gravity.BOTTOM));o.bindMotion(panel,shade,false,()->{});a.setContentView(o);layout(o);o.motion.set(.3f);
   MotionEvent down=event(MotionEvent.ACTION_DOWN,100,panel.getY()+20);o.onInterceptTouchEvent(down);advance(o.motion);float before=o.motion.value;float y=down.getY()+30;MotionEvent move=event(MotionEvent.ACTION_MOVE,100,y);assertTrue(o.onInterceptTouchEvent(move));o.onTouchEvent(move);float discontinuity=before-30f/300-o.motion.value;assertEquals("Drag must not jump back to the ACTION_DOWN progress",0,discontinuity,.001f);down.recycle();move.recycle();
  }
 }
 @Test public void drawerDragUsesTheCurrentFrameAndLeavesVerticalScrollingAlone(){
  try(ActivityController<Activity> c=Robolectric.buildActivity(Activity.class).setup().visible()){
   Activity a=c.get();SideNavigation d=new SideNavigation(a,new FrameLayout(a),0xffeeeeee,0xffffffff,0xff000000,i->{},()->{});a.setContentView(d);layout(d);d.open(0);Motion.Progress p=ReflectionHelpers.getField(d,"motion");p.set(.3f);MotionEvent down=event(MotionEvent.ACTION_DOWN,100,200);d.onInterceptTouchEvent(down);advance(p);float before=p.value;MotionEvent move=event(MotionEvent.ACTION_MOVE,130,200);assertTrue(d.onInterceptTouchEvent(move));d.onTouchEvent(move);int width=ReflectionHelpers.getField(d,"drawerWidth");float jump=before+30f/width-p.value;assertEquals("Drawer must retain the current animation frame",0,jump,.001f);down.recycle();move.recycle();
   p.set(1);MotionEvent down2=event(MotionEvent.ACTION_DOWN,100,200),vertical=event(MotionEvent.ACTION_MOVE,100+ViewConfiguration.get(a).getScaledTouchSlop()+1,360);d.onInterceptTouchEvent(down2);boolean intercept=d.onInterceptTouchEvent(vertical);assertFalse("Mostly vertical movement belongs to the list",intercept);MotionEvent later=event(MotionEvent.ACTION_MOVE,300,360);assertFalse("A vertical gesture must stay vertical",d.onInterceptTouchEvent(later));later.recycle();down2.recycle();vertical.recycle();
  }
 }
 @Test public void imeOffsetSurvivesPanelAnimationAndRelayout(){
  try(ActivityController<Activity> c=Robolectric.buildActivity(Activity.class).setup().visible()){
   Activity a=c.get();MotionOverlay o=new MotionOverlay(a);View shade=new View(a),panel=new View(a);o.addView(shade,new FrameLayout.LayoutParams(-1,-1));o.addView(panel,new FrameLayout.LayoutParams(-1,300,Gravity.BOTTOM));o.bindMotion(panel,shade,false,()->{});a.setContentView(o);layout(o);o.motion.set(.4f);
   WindowInsetsAnimation ime=new WindowInsetsAnimation(WindowInsets.Type.ime(),new android.view.animation.LinearInterpolator(),300);o.dispatchWindowInsetsAnimationPrepare(ime);o.layout(0,0,390,644);WindowInsetsAnimation.Bounds bounds=new WindowInsetsAnimation.Bounds(android.graphics.Insets.NONE,android.graphics.Insets.of(0,0,0,200));o.dispatchWindowInsetsAnimationStart(ime,bounds);ime.setFraction(.5f);o.dispatchWindowInsetsAnimationProgress(new WindowInsets.Builder().build(),java.util.Collections.singletonList(ime));assertEquals(280,panel.getTranslationY(),.01f);
   o.motion.set(.6f);assertEquals(220,panel.getTranslationY(),.01f);o.layout(0,0,390,640);assertEquals(220,panel.getTranslationY(),.01f);
   o.dispatchWindowInsetsAnimationEnd(new WindowInsetsAnimation(WindowInsets.Type.navigationBars(),null,100));assertEquals(220,panel.getTranslationY(),.01f);o.dispatchWindowInsetsAnimationEnd(ime);assertEquals(120,panel.getTranslationY(),.01f);
  }
 }
}