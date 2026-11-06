package cn.courselens;

import android.app.*;
import android.os.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.core.view.*;
import java.util.*;
import static cn.courselens.AiChecks.*;

final class ChatMotionChecks {
 private static void await(Instrumentation i,java.util.function.BooleanSupplier ready,String error){long until=SystemClock.uptimeMillis()+6000;while(SystemClock.uptimeMillis()<until){i.waitForIdleSync();if(ready.getAsBoolean())return;SystemClock.sleep(32);}throw new AssertionError(error);}
 static void run(Instrumentation i){
  Bundle result=new Bundle();int code=Activity.RESULT_CANCELED;MainActivity a=null;
  try{
   check(i.getTargetContext().getPackageName().equals("cn.courselens.qa"),"QA package required");a=NativeChecks.startQaActivity(i);MainActivity activity=a;await(i,activity::hasWindowFocus,"QA window did not gain focus");
   main(i,()->call(activity,"aiAssistant"));i.waitForIdleSync();View chat=(View)get(a,"root");AiComposer composer=chat.findViewWithTag("ai-composer");EditText message=chat.findViewWithTag("ai-message");int compact=composer.getHeight();
   List<Integer> heights=Collections.synchronizedList(new ArrayList<>());ViewTreeObserver.OnPreDrawListener record=()->{heights.add(composer.getHeight());return true;};main(i,()->{composer.getViewTreeObserver().addOnPreDrawListener(record);message.setText("合成动画草稿");message.setSelection(3);message.requestFocus();((InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE)).showSoftInput(message,InputMethodManager.SHOW_IMPLICIT);});
   await(i,()->{WindowInsetsCompat insets=ViewCompat.getRootWindowInsets(composer);return insets!=null&&insets.isVisible(WindowInsetsCompat.Type.ime())&&composer.motion.value==1&&composer.getTranslationY()==0;},"Composer/IME did not finish expanding");int expanded=composer.getHeight();check(expanded>compact,"Composer did not expand");check(new HashSet<>(heights).size()>2,"Composer has no intermediate height frames");
   main(i,()->{chat.findViewWithTag("ai-attach").performClick();PopupWindow popup=(PopupWindow)get(activity,"aiAttachmentMenu");check(popup.getContentView().findViewWithTag("ai-attach-plugin")==null,"Phone context remains in attachments");check(popup.getContentView().findViewWithTag("ai-attach-strength")!=null,"Reasoning strength missing");popup.dismiss();});
   android.graphics.Bitmap image=i.getUiAutomation().takeScreenshot();java.io.File directory=new java.io.File(a.getExternalFilesDir(null),"chat-motion");directory.mkdirs();try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(directory,"expanded.png"))){image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);}image.recycle();
   main(i,()->((InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(message.getWindowToken(),0));await(i,()->composer.motion.value==0&&composer.getTranslationY()==0&&composer.getHeight()==compact,"Composer did not settle after keyboard dismissal");i.waitForIdleSync();check(composer.getHeight()==compact,"Compact height changed");check(message.getText().toString().equals("合成动画草稿")&&message.getSelectionStart()==3,"Draft or selection changed");
   main(i,()->{composer.getViewTreeObserver().removeOnPreDrawListener(record);call(activity,"menu");});SystemClock.sleep(300);main(i,()->((View)get(activity,"root")).findViewWithTag("permission-settings").performClick());SystemClock.sleep(300);main(i,()->((View)get(activity,"root")).findViewWithTag("permission-ai-phone").performClick());SystemClock.sleep(300);check(((View)get(a,"root")).findViewWithTag("ai-screen-permission")!=null,"Permissions not reachable from Settings");
   result.putString("result","PASS: real IME expansion/collapse, intermediate composer heights, preserved draft/selection, clean attachment menu and Settings permissions");result.putString("geometry","compact="+compact+" expanded="+expanded+" distinctHeights="+new HashSet<>(heights).size());code=Activity.RESULT_OK;
  }catch(Throwable e){java.io.StringWriter trace=new java.io.StringWriter();e.printStackTrace(new java.io.PrintWriter(trace));result.putString("result","FAIL: "+trace);}
  finally{if(a!=null){MainActivity activity=a;main(i,activity::finish);}i.finish(code,result);}
 }
}
