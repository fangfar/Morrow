package cn.courselens;

import android.app.*;
import android.graphics.*;
import android.os.Looper;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.time.Duration;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.ReflectionHelpers.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class AiChatControlsTest {
 private void layout(MainActivity a){View v=a.findViewById(android.R.id.content);shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500));android.util.DisplayMetrics d=a.getResources().getDisplayMetrics();for(int i=0;i<3;i++){v.measure(View.MeasureSpec.makeMeasureSpec(d.widthPixels,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(d.heightPixels,View.MeasureSpec.EXACTLY));v.layout(0,0,d.widthPixels,d.heightPixels);}}
 @Test public void attachmentMenuTogglesAndClosesWhenTheKeyboardIsDismissed(){
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup().visible()){
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");View chat=getField(a,"root");layout(a);WindowInsets shown=new WindowInsets.Builder().setInsets(WindowInsets.Type.ime(),Insets.of(0,0,0,300)).setVisible(WindowInsets.Type.ime(),true).build();View stage=getField(a,"stage");callInstanceMethod(a,"applyStageInsets",ClassParameter.from(View.class,stage),ClassParameter.from(WindowInsets.class,shown));chat.findViewWithTag("ai-attach").performClick();MainActivity.NavigationOverlay menu=getField(a,"aiAttachmentMenu");assertNotNull(menu);assertTrue(menu.open);chat.findViewWithTag("ai-attach").performClick();assertFalse(menu.open);menu.close(false);
   chat.findViewWithTag("ai-attach").performClick();MainActivity.NavigationOverlay next=getField(a,"aiAttachmentMenu");WindowInsets hidden=new WindowInsets.Builder().setVisible(WindowInsets.Type.ime(),false).build();callInstanceMethod(a,"applyStageInsets",ClassParameter.from(View.class,stage),ClassParameter.from(WindowInsets.class,hidden));assertFalse(next.open);assertSame(chat,getField(a,"root"));
  }
 }
 @Test public void strengthNeedleChangesAndSettingsReturnToTheSameChat(){
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");View chat=getField(a,"root");EditText input=chat.findViewWithTag("ai-message");input.setText("保留草稿");input.setSelection(2);TextView strength=chat.findViewWithTag("ai-strength");strength.performClick();Dialog dialog=getField(a,"aiStrengthDialog");AiStrengthSlider slider=dialog.findViewById(android.R.id.content).findViewWithTag("ai-strength-slider");slider.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT,new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_RIGHT));assertEquals(1,((AiStrengthIcon)strength.getCompoundDrawables()[0]).level);dialog.dismiss();
   chat.findViewWithTag("ai-history-button").performClick();SideNavigation drawer=getField(a,"sideNavigation");drawer.findViewWithTag("drawer-settings").performClick();shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500));((Runnable)getField(a,"pageBack")).run();assertSame(chat,getField(a,"root"));assertEquals("保留草稿",input.getText().toString());assertEquals(2,input.getSelectionStart());assertEquals(3,(int)callInstanceMethod(a,"primaryTab"));
  }
 }
 @Test public void microphoneRequestsTheSystemPermissionOnFirstUse(){
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();shadowOf((Application)a.getApplication()).denyPermissions(android.Manifest.permission.RECORD_AUDIO);callInstanceMethod(a,"aiAssistant");((View)getField(a,"root")).findViewWithTag("ai-mic").performClick();org.robolectric.shadows.ShadowActivity.PermissionsRequest request=shadowOf(a).getLastRequestedPermission();assertNotNull(request);assertEquals(908,request.requestCode);assertArrayEquals(new String[]{android.Manifest.permission.RECORD_AUDIO},request.requestedPermissions);
  }
 }
 @Test public void editableKeyAndPairedActionsStaySideBySideInEveryAppearance()throws Exception{
  for(String variant:new String[]{"light","dark","large"}){
   RuntimeEnvironment.setQualifiers(variant.equals("large")?"w320dp-h850dp-notnight":"w400dp-h850dp-"+(variant.equals("dark")?"night":"notnight"));RuntimeEnvironment.setFontScale(variant.equals("large")?1.6f:1f);
   try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
    MainActivity a=c.get();AiProviders.Config config=new AiProviders.Config("synthetic","合成供应商","https://example.com/v1","synthetic-model","synthetic-editable-key",false,false);callInstanceMethod(a,"aiEditProvider",ClassParameter.from(AiProviders.Config.class,config));layout(a);View root=getField(a,"root");EditText key=root.findViewWithTag("ai-key");assertEquals(config.key,key.getText().toString());assertFalse(key.isSaveEnabled());View save=root.findViewWithTag("ai-key-save"),delete=root.findViewWithTag("ai-key-delete");assertSame(save.getParent(),delete.getParent());assertEquals(LinearLayout.HORIZONTAL,((LinearLayout)save.getParent()).getOrientation());assertEquals(save.getTop(),delete.getTop());assertTrue(delete.getLeft()>save.getLeft());
    ((ScrollView)((ViewGroup)root).getChildAt(1)).fullScroll(View.FOCUS_DOWN);shadowOf(Looper.getMainLooper()).idle();File dir=new File(System.getProperty("user.home"),".codex/morrow-ui-previews/providers");dir.mkdirs();View content=a.findViewById(android.R.id.content);Bitmap bitmap=Bitmap.createBitmap(content.getWidth(),content.getHeight(),Bitmap.Config.ARGB_8888);content.draw(new Canvas(bitmap));try(FileOutputStream out=new FileOutputStream(new File(dir,variant+".png"))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}bitmap.recycle();
   }
  }
 }
 @Test public void permanentlyDeniedMicrophoneOffersTheAppPermissionPage(){
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();shadowOf((Application)a.getApplication()).denyPermissions(android.Manifest.permission.RECORD_AUDIO);a.getSharedPreferences("appSettings",0).edit().putBoolean("aiMicrophoneAsked",true).commit();callInstanceMethod(a,"aiAssistant");((View)getField(a,"root")).findViewWithTag("ai-mic").performClick();AlertDialog dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();assertNotNull(dialog);dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();shadowOf(Looper.getMainLooper()).idle();android.content.Intent intent=shadowOf(a).getNextStartedActivity();assertEquals(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,intent.getAction());
  }
 }
 @Test public void aSystemBlockedRequestImmediatelyOffersPermissionRecovery(){
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();shadowOf((Application)a.getApplication()).denyPermissions(android.Manifest.permission.RECORD_AUDIO);callInstanceMethod(a,"aiAssistant");((View)getField(a,"root")).findViewWithTag("ai-mic").performClick();a.onRequestPermissionsResult(908,new String[]{android.Manifest.permission.RECORD_AUDIO},new int[]{android.content.pm.PackageManager.PERMISSION_DENIED});AlertDialog dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();assertNotNull(dialog);assertEquals("前往设置",dialog.getButton(AlertDialog.BUTTON_POSITIVE).getText().toString());
  }
 }
 @Test public void attachmentMenuCoversItsPlusAndKeepsTheCloseTargetInPlace(){
  for(String viewport:new String[]{"w400dp-h850dp-notnight","w320dp-h400dp-notnight"}){
   RuntimeEnvironment.setQualifiers(viewport);RuntimeEnvironment.setFontScale(viewport.startsWith("w320")?1.6f:1f);
   try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup().visible()){
    MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");((PageMotion)getField(a,"pageMotion")).cancel();layout(a);View chat=getField(a,"root"),plus=chat.findViewWithTag("ai-attach");plus.performClick();MainActivity.NavigationOverlay menu=getField(a,"aiAttachmentMenu");menu.dispatchApplyWindowInsets(new WindowInsets.Builder().setInsets(WindowInsets.Type.systemBars(),Insets.of(0,24,0,24)).build());layout(a);menu.motion.set(1);View toggle=menu.findViewWithTag("ai-attach-toggle");Rect anchor=new Rect(),panel=new Rect(),close=new Rect();plus.getGlobalVisibleRect(anchor);menu.panel.getGlobalVisibleRect(panel);toggle.getGlobalVisibleRect(close);assertTrue("Menu must cover the original plus",panel.contains(anchor));assertEquals(anchor.centerX(),close.centerX(),1);assertEquals(anchor.centerY(),close.centerY(),1);assertTrue(panel.top>=0);toggle.performClick();assertFalse(menu.open);
   }
  }
 }
}
