package cn.courselens;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.view.*;
import android.widget.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import java.time.Duration;
import static org.junit.Assert.*;
import static org.robolectric.util.ReflectionHelpers.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34)
public class AiComposerTest {
 @Before public void animations(){org.robolectric.shadows.ShadowChoreographer.setPaused(true);org.robolectric.shadows.ShadowChoreographer.setFrameDelay(Duration.ofMillis(16));callStaticMethod(ValueAnimator.class,"setDurationScale",ClassParameter.from(float.class,1f));}
 private void layout(View v){org.robolectric.shadows.ShadowChoreographer.setPaused(false);org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();org.robolectric.shadows.ShadowChoreographer.setPaused(true);v.measure(View.MeasureSpec.makeMeasureSpec(390,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(844,View.MeasureSpec.AT_MOST));v.layout(0,0,390,v.getMeasuredHeight());}
 private AiComposer composer(Activity a){
  AiComposer composer=new AiComposer(a);composer.setOrientation(LinearLayout.VERTICAL);
  EditText message=new EditText(a);message.setTag("ai-message");message.setText("合成草稿 test");message.setSelection(3);composer.addView(message,new LinearLayout.LayoutParams(-1,-2));
  LinearLayout tools=new LinearLayout(a);tools.setTag("ai-tools");TextView strength=new TextView(a);strength.setTag("ai-strength");tools.addView(strength);composer.addView(tools,new LinearLayout.LayoutParams(-1,48));a.setContentView(composer);composer.expanded(false);layout(composer);message.requestFocus();return composer;
 }
 @Test public void expansionAndReversalKeepTheDraftFocusAndIntermediateGeometry(){
  try(ActivityController<Activity> c=Robolectric.buildActivity(Activity.class).setup().visible()){
   AiComposer composer=composer(c.get());EditText message=composer.findViewWithTag("ai-message");int compact=composer.getHeight(),padding=message.getPaddingLeft();
   composer.expanded(true);ValueAnimator animator=getField(composer.motion,"animator");assertNotNull(animator);animator.setCurrentPlayTime(65);layout(composer);int middle=composer.getHeight();assertTrue(middle>compact);assertTrue(message.getPaddingLeft()<padding);float interrupted=composer.motion.value;
   composer.expanded(false);assertEquals(interrupted,composer.motion.value,0);ValueAnimator reverse=getField(composer.motion,"animator");reverse.end();layout(composer);assertEquals(compact,composer.getHeight());assertSame(message,composer.findViewWithTag("ai-message"));assertEquals("合成草稿 test",message.getText().toString());assertEquals(3,message.getSelectionStart());assertTrue(message.hasFocus());assertEquals(View.GONE,composer.findViewWithTag("ai-strength").getVisibility());
   composer.expanded(true);((ValueAnimator)getField(composer.motion,"animator")).end();layout(composer);assertTrue(composer.getHeight()>middle);assertEquals(View.VISIBLE,composer.findViewWithTag("ai-strength").getVisibility());
  }
 }
 @Test public void disabledAnimationsAndDetachSettleWithoutLeavingAnOffset(){
  try(ActivityController<Activity> c=Robolectric.buildActivity(Activity.class).setup().visible()){
   AiComposer composer=composer(c.get());callStaticMethod(ValueAnimator.class,"setDurationScale",ClassParameter.from(float.class,0f));composer.expanded(true);assertEquals(1,composer.motion.value,0);assertFalse(composer.motion.running());composer.setTranslationY(80);composer.onDetachedFromWindow();assertEquals(0,composer.getTranslationY(),0);assertFalse(composer.motion.running());
  }
 }
 @Test public void composerFollowsTheKeyboardWithoutTranslatingTheWholePage(){
  try(ActivityController<Activity> c=Robolectric.buildActivity(Activity.class).setup().visible()){
   AiComposer composer=composer(c.get());WindowInsetsAnimation ime=new WindowInsetsAnimation(WindowInsets.Type.ime(),new android.view.animation.LinearInterpolator(),300);composer.dispatchWindowInsetsAnimationPrepare(ime);composer.offsetTopAndBottom(-200);composer.dispatchWindowInsetsAnimationStart(ime,new WindowInsetsAnimation.Bounds(android.graphics.Insets.NONE,android.graphics.Insets.of(0,0,0,200)));assertEquals(200,composer.getTranslationY(),.01f);ime.setFraction(.5f);composer.dispatchWindowInsetsAnimationProgress(new WindowInsets.Builder().build(),java.util.Collections.singletonList(ime));assertEquals(100,composer.getTranslationY(),.01f);composer.dispatchWindowInsetsAnimationEnd(ime);assertEquals(0,composer.getTranslationY(),0);
  }
 }
}
