package cn.courselens;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;
import static org.robolectric.util.ReflectionHelpers.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34)
public class AccentAndProviderTest {
 @Test public void allAccentPalettesApplyToControlsAndKeepTheirSelection(){
  for(String qualifier:new String[]{"notnight","night"}){RuntimeEnvironment.setQualifiers(qualifier);for(int color=0;color<10;color++){
   Appearance.accent(RuntimeEnvironment.getApplication(),color);
   try(ActivityController<Activity> c=Robolectric.buildActivity(Activity.class).setup()){
    Activity a=c.get();a.setTheme(R.style.AppTheme);SecondaryUi ui=new SecondaryUi(a);assertEquals(color,Appearance.accent(a));assertEquals(Appearance.themeColor(a,R.attr.appAccent),ui.PRIMARY);assertEquals(Appearance.themeColor(a,R.attr.appOnAccent),ui.ON_PRIMARY);assertEquals(Appearance.themeColor(a,R.attr.appSelection),ui.AQUA);Button action=ui.action(new LinearLayout(a),"测试",()->{});assertEquals(ui.ON_PRIMARY,action.getCurrentTextColor());assertEquals(ui.PRIMARY,action.getBackgroundTintList().getDefaultColor());
   }
  }}Appearance.accent(RuntimeEnvironment.getApplication(),999);assertEquals(6,Appearance.accent(RuntimeEnvironment.getApplication()));
 }
 @Test public void appearanceRecreationKeepsThePageAndChatDraft(){
  Bundle state=new Bundle();
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"aiAssistant");EditText input=((View)getField(a,"root")).findViewWithTag("ai-message");input.setText("保留颜色切换草稿");input.setSelection(2,5);setField(a,"settingsFromAi",true);callInstanceMethod(a,"menu");callInstanceMethod(a,"appearanceSettings");assertNotNull(((View)getField(a,"root")).findViewWithTag("accent-setting"));Appearance.accent(a,0);c.saveInstanceState(state);
  }
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup(state)){
   MainActivity a=c.get();assertEquals("外观",((View)getField(a,"root")).getTag());callInstanceMethod(a,"backPage");callInstanceMethod(a,"backPage");EditText input=((View)getField(a,"root")).findViewWithTag("ai-message");assertNotNull(input);assertEquals("保留颜色切换草稿",input.getText().toString());assertEquals(2,input.getSelectionStart());assertEquals(5,input.getSelectionEnd());
  }
 }
 @Test public void domesticPresetsAndCustomUseTheSameEditableForm()throws Exception{
  assertEquals(6,AiProviders.presets().size());for(AiProviders.Config preset:AiProviders.presets())assertEquals(preset.base,AiProviders.base(preset.base));
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"menu");callInstanceMethod(a,"aiProviderChoices");View choices=getField(a,"root");assertNotNull(choices.findViewWithTag("ai-provider-custom"));choices.findViewWithTag("preset-qwen").performClick();View form=getField(a,"root");assertEquals("https://dashscope.aliyuncs.com/compatible-mode/v1",((EditText)form.findViewWithTag("ai-provider-base")).getText().toString());assertEquals("qwen-plus",((EditText)form.findViewWithTag("ai-provider-model")).getText().toString());assertEquals("",((EditText)form.findViewWithTag("ai-key")).getText().toString());assertNull(form.findViewWithTag("ai-key-delete"));callInstanceMethod(a,"aiProviderChoices");((View)getField(a,"root")).findViewWithTag("ai-provider-custom").performClick();form=getField(a,"root");assertEquals("",((EditText)form.findViewWithTag("ai-provider-base")).getText().toString());assertEquals("",((EditText)form.findViewWithTag("ai-provider-model")).getText().toString());
  }
 }
}
