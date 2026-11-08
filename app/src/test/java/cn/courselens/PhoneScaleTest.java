package cn.courselens;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;
import static org.robolectric.util.ReflectionHelpers.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class PhoneScaleTest {
 @Before public void systemTheme(){Appearance.mode(RuntimeEnvironment.getApplication(),Appearance.SYSTEM);}
 @Test public void portraitPhoneContextsUseTheSameReferenceWidthWithoutChangingSystemResources(){
  for(String screen:new String[]{"w360dp-h800dp-560dpi","w380dp-h844dp-xxhdpi","w400dp-h890dp-xxhdpi","w411dp-h891dp-xxhdpi"}){
   RuntimeEnvironment.setQualifiers(screen);RuntimeEnvironment.setFontScale(1.3f);Context base=RuntimeEnvironment.getApplication();Configuration original=new Configuration(base.getResources().getConfiguration());DisplayMetrics metrics=base.getResources().getDisplayMetrics();float density=metrics.density;int pixels=metrics.widthPixels;
   Context adapted=Appearance.wrap(base);Configuration actual=adapted.getResources().getConfiguration();assertEquals(400,actual.screenWidthDp);assertEquals(Math.round(original.densityDpi*original.screenWidthDp/400f),actual.densityDpi);assertEquals(original.fontScale,actual.fontScale,0);assertEquals(pixels,adapted.getResources().getDisplayMetrics().widthPixels);assertEquals(original,base.getResources().getConfiguration());assertEquals(density,base.getResources().getDisplayMetrics().density,0);
  }
 }
 @Test public void tabletsLandscapeAndSmallWindowsKeepTheirNativeScale(){
  for(String screen:new String[]{"w800dp-h1280dp-xhdpi","w800dp-h360dp-land-xhdpi","w320dp-h640dp-xhdpi"}){
   RuntimeEnvironment.setQualifiers(screen);Context base=RuntimeEnvironment.getApplication();assertSame(base,Appearance.wrap(base));
  }
 }
 @Test public void allThreeActivitiesUseTheAdaptedScale(){
  RuntimeEnvironment.setQualifiers("w360dp-h800dp-560dpi");
  try(ActivityController<MainActivity> main=Robolectric.buildActivity(MainActivity.class).setup();ActivityController<ParcelActivity> parcels=Robolectric.buildActivity(ParcelActivity.class).setup();ActivityController<WebTimetableActivity> web=Robolectric.buildActivity(WebTimetableActivity.class).setup()){
   for(android.app.Activity activity:new android.app.Activity[]{main.get(),parcels.get(),web.get()})assertEquals(400,activity.getResources().getConfiguration().screenWidthDp);
  }
 }
 @Test public void aiHeaderAndBottomNavigationKeepTheSameProportionAcrossPhoneDensities(){
  for(String screen:new String[]{"w360dp-h800dp-560dpi","w400dp-h889dp-480dpi"}){
   RuntimeEnvironment.setQualifiers(screen);RuntimeEnvironment.setFontScale(1f);
   try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup()){
    MainActivity a=controller.get();callInstanceMethod(a,"aiAssistant");org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(java.time.Duration.ofMillis(600));View content=a.findViewById(android.R.id.content);DisplayMetrics dm=a.getResources().getDisplayMetrics();content.measure(View.MeasureSpec.makeMeasureSpec(dm.widthPixels,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(dm.heightPixels,View.MeasureSpec.EXACTLY));content.layout(0,0,dm.widthPixels,dm.heightPixels);View menu=content.findViewWithTag("ai-menu"),title=content.findViewWithTag("primary-page-title");assertSame(title.getParent(),menu.getParent());assertEquals(48f/400,menu.getWidth()/(float)dm.widthPixels,.001f);assertEquals(48f/400,content.findViewWithTag("nav-ai").getHeight()/(float)dm.widthPixels,.001f);
   }
  }
 }
 @Test public void themeAndDraftSurviveRecreationAtTheAdaptedScale(){
  RuntimeEnvironment.setQualifiers("w360dp-h800dp-560dpi");RuntimeEnvironment.setFontScale(2f);
  try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=controller.get();callInstanceMethod(a,"aiAssistant");EditText input=((View)getField(a,"root")).findViewWithTag("ai-message");input.setText("合成草稿保留");input.setSelection(2,4);Appearance.mode(a,Appearance.DARK);controller.recreate();a=controller.get();input=((View)getField(a,"root")).findViewWithTag("ai-message");assertEquals("合成草稿保留",input.getText().toString());assertEquals(2,input.getSelectionStart());assertEquals(4,input.getSelectionEnd());assertEquals(400,a.getResources().getConfiguration().screenWidthDp);assertEquals(2f,a.getResources().getConfiguration().fontScale,0);assertTrue(Appearance.dark(a));
  }
 }
}
