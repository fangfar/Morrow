package cn.courselens;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;

/** App-wide light/dark choice; resources and programmatic surfaces share the same mode. */
final class Appearance {
 static final String PREFS="appSettings",KEY="themeMode";
 static final int SYSTEM=0,LIGHT=1,DARK=2;

 static final String[] ACCENT_NAMES={"蓝色","青色","绿色","青柠绿","黄色","橙色","粉色","洋红色","紫色","黑色"};
 static final int[] ACCENT_SWATCHES={0xff3563e9,0xff00a8d6,0xff00a98f,0xff90b900,0xfff5c634,0xfff27757,0xffee70ab,0xffcf50d5,0xff9f57ed,0xff171717};
 private static final int[] ACCENT_STYLES={R.style.AccentBlue,R.style.AccentCyan,R.style.AccentGreen,R.style.AccentLime,R.style.AccentYellow,R.style.AccentOrange,R.style.AccentPink,R.style.AccentMagenta,R.style.AccentPurple,R.style.AccentBlack};
 static int accent(Context context){try{int value=context.getSharedPreferences(PREFS,0).getInt("accentColor",6);return value>=0&&value<ACCENT_NAMES.length?value:6;}catch(RuntimeException e){return 6;}}
 static void accent(Context context,int value){context.getSharedPreferences(PREFS,0).edit().putInt("accentColor",value>=0&&value<ACCENT_NAMES.length?value:6).apply();}
 static int accentStyle(Context context){return ACCENT_STYLES[accent(context)];}
 static int themeColor(Context context,int attribute){android.util.TypedValue value=new android.util.TypedValue();if(!context.getTheme().resolveAttribute(attribute,value,true))throw new IllegalStateException("Missing app palette");return value.data;}

 private Appearance(){}

 static int mode(Context context){
  try{int value=context.getSharedPreferences(PREFS,0).getInt(KEY,SYSTEM);return value==LIGHT||value==DARK?value:SYSTEM;}
  catch(RuntimeException e){return SYSTEM;}
 }

 static void mode(Context context,int value){
  context.getSharedPreferences(PREFS,0).edit().putInt(KEY,value==LIGHT||value==DARK?value:SYSTEM).apply();
 }

 static boolean dark(Context context){
  int mode=mode(context);
  if(mode==DARK)return true;
  if(mode==LIGHT)return false;
  return (context.getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
 }

 static String label(Context context){
  int mode=mode(context);
  if(mode==DARK)return "深色";
  if(mode==LIGHT)return "浅色";
  return dark(context)?"跟随系统 · 当前深色":"跟随系统 · 当前浅色";
 }

 static Context wrap(Context base){
  int mode=mode(base);
  if(mode==SYSTEM)return base;
  Configuration configuration=new Configuration(base.getResources().getConfiguration());
  int night=mode==DARK?Configuration.UI_MODE_NIGHT_YES:Configuration.UI_MODE_NIGHT_NO;
  configuration.uiMode=(configuration.uiMode&~Configuration.UI_MODE_NIGHT_MASK)|night;
  return base.createConfigurationContext(configuration);
 }

 /** Enable edge-to-edge and let the app background paint beneath system bars. */
 static void edgeToEdge(Activity activity){
  Window window=activity.getWindow();
  androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window,false);
  systemBars(activity);
  if(Build.VERSION.SDK_INT>=29){
   try{window.setNavigationBarContrastEnforced(false);}catch(RuntimeException ignored){}
  }
 }

 /** Re-apply icon appearance after the page background changes. */
 static void systemBars(Activity activity){
  boolean light=!dark(activity);
  View decor=activity.getWindow().getDecorView();
  if(Build.VERSION.SDK_INT>=30){
   try{
    WindowInsetsController controller=activity.getWindow().getInsetsController();
    if(controller!=null){
     int mask=WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
     controller.setSystemBarsAppearance(light?mask:0,mask);
     return;
    }
   }catch(RuntimeException ignored){}
  }
  decor.setSystemUiVisibility(light?View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR:0);
 }
}
