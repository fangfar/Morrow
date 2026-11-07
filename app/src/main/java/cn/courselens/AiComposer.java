package cn.courselens;

import android.content.Context;
import android.view.View;
import android.widget.*;
import androidx.core.view.*;

/** Keep the editor attached while the compact field grows into the multiline composer. */
final class AiComposer extends LinearLayout {
 private EditText message;private LinearLayout tools;private View strength;private boolean expanded,initialized;
 final Motion.Progress motion=new Motion.Progress(0,this::render);
 AiComposer(Context context){
  super(context);
  ViewCompat.setWindowInsetsAnimationCallback(this,new WindowInsetsAnimationCompat.Callback(WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE){
   private int before;private float offset;
   @Override public void onPrepare(WindowInsetsAnimationCompat a){if((a.getTypeMask()&WindowInsetsCompat.Type.ime())!=0)before=getBottom();}
   @Override public WindowInsetsAnimationCompat.BoundsCompat onStart(WindowInsetsAnimationCompat a,WindowInsetsAnimationCompat.BoundsCompat bounds){if((a.getTypeMask()&WindowInsetsCompat.Type.ime())!=0){offset=before-getBottom();setTranslationY(offset);}return bounds;}
   @Override public WindowInsetsCompat onProgress(WindowInsetsCompat insets,java.util.List<WindowInsetsAnimationCompat> running){for(WindowInsetsAnimationCompat a:running)if((a.getTypeMask()&WindowInsetsCompat.Type.ime())!=0)setTranslationY(offset*(1-a.getInterpolatedFraction()));return insets;}
   @Override public void onEnd(WindowInsetsAnimationCompat a){if((a.getTypeMask()&WindowInsetsCompat.Type.ime())!=0)setTranslationY(0);}
  });
 }
 private int dp(float value){return Math.round(value*getResources().getDisplayMetrics().density);}
 void expanded(boolean next){
  if(initialized&&expanded==next)return;
  expanded=next;setTag(R.id.ai_composer_state,next);
  if(!initialized){message=findViewWithTag("ai-message");tools=findViewWithTag("ai-tools");strength=findViewWithTag("ai-strength");initialized=true;motion.set(next?1:0);}
  else motion.to(next?1:0,next?Motion.EXPAND:Motion.COLLAPSE,false,()->{});
 }
 private void render(float p){
  if(!initialized)return;
  boolean multiline=expanded||p>0;
  message.setMinLines(multiline?2:1);message.setMaxLines(multiline?4:1);message.setHorizontallyScrolling(!multiline);
  message.setPadding(dp(52-40*p),dp(12),dp(108-100*p),dp(12));
  strength.setVisibility(p>0?VISIBLE:GONE);strength.setAlpha(p);
  requestLayout();
 }
 @Override protected void onMeasure(int width,int height){
  if(initialized){
   float p=motion.value;int compact=Math.max(dp(48),message.getLineHeight()+message.getPaddingTop()+message.getPaddingBottom());
   message.measure(MeasureSpec.makeMeasureSpec(Math.max(0,MeasureSpec.getSize(width)-getPaddingLeft()-getPaddingRight()),MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED));
   ((LayoutParams)message.getLayoutParams()).height=Math.round(compact+(message.getMeasuredHeight()-compact)*p);
   LayoutParams row=(LayoutParams)tools.getLayoutParams();row.height=Math.round(compact+(dp(48)-compact)*p);row.topMargin=Math.round(-compact*(1-p));
  }
  super.onMeasure(width,height);
 }
 @Override protected void onDetachedFromWindow(){motion.set(expanded?1:0);setTranslationY(0);super.onDetachedFromWindow();}
}
