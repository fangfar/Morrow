package cn.courselens;

import android.app.*;
import android.os.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.content.*;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.PagerAdapter;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
 private String aiRestoredDraft,aiRestoredDraftChat;private int aiRestoredStart,aiRestoredEnd;private SecondaryUi secondaryUi;private AiCameraPanel aiCamera;private boolean aiSettingsFromMenu,aiMemoryFromMenu;
 private ClassTimes classTimes=ClassTimes.DEFAULT;private String[] START_TIMES=classTimes.starts,END_TIMES=classTimes.ends;private String schoolId="squ",loginSchool="squ";
 private int INK,MUTED,BLUE,PRIMARY,SURFACE,AQUA,SOFT,NAVY,CARD,DIVIDER,DANGER,DISABLED,ON_PRIMARY,HANDLE,COURSE_BORDER,NAV_MUTED,WARNING;
 private boolean dark;
 private AlertDialog widgetDialog,unsavedDialog;
 private AlertDialog remoteDialog;private RemoteConfigClient remote;private TextView remoteStatus;private final Handler remoteHandler=new Handler(Looper.getMainLooper());
 private boolean remoteResumed,touching,manualUpdatePending;private int pagerState,manualCheckScreen=-1;private long interactionAt;private final Runnable remoteNotice=this::showRemoteNotice;private final RemoteConfigClient.Listener remoteListener=this::remoteChanged;
 private final ViewTreeObserver.OnScrollChangedListener remoteScroll=()->{interactionAt=SystemClock.uptimeMillis();scheduleRemoteNotice();};
 private static final String ADD_WIDGET="cn.courselens.ADD_WIDGET";
 static final String OPEN_TAB="cn.courselens.OPEN_TAB",OPEN_SETTINGS="cn.courselens.OPEN_SETTINGS";
 private Set<String> displayChoices=new HashSet<>();
 private String adjustments="";
 private String cachedData,cachedAdjustments,cachedTerm;private List<Course> cachedCourses;
 private Map<String,CourseAdjustment> cachedMoves=Collections.emptyMap();
 private Map<Course,String> cachedCardText=new HashMap<>();
 private HomePage homePage;
 private TaskPage taskPage;private Bundle taskState;private ParcelPage parcelPage;private Bundle parcelState;private boolean tasksVisible,parcelsVisible,switchingPrimaryTab,pageBackwards;
 private FrameLayout windowContent;private AppNavigation bottomNavigation;private SideNavigation sideNavigation;
 private DeepSeekClient.Call aiCall;private Dialog aiStrengthDialog;private AiChats aiStore,aiTemporaryStore;private final ArrayList<AiAttachment> aiPendingAttachments=new ArrayList<>();private LinearLayout aiPendingView;private String aiPendingChat,aiPendingPhoneQuestion,aiPendingPhoneSource;private int aiPhoneWaits;private long aiPendingPhoneAt;private View aiAttachmentPage,aiPendingPhonePage;private java.io.File aiCaptureFile;private NavigationOverlay aiAttachmentMenu;private boolean aiKeyboardVisible;private boolean aiSending;private boolean aiVisible;private LinearLayout aiPage;private final org.json.JSONArray aiChat=new org.json.JSONArray();private String aiChatTimetable;private LinearLayout settingsPage;private int settingsScrollY;private boolean settingsFromParcel,settingsFromParcelActivity,settingsFromAi;
 private ScrollView timetableScroll;private int timetableScrollY;
 private float backPageX;
 private final PageMotion pageMotion=new PageMotion();private Object systemBack;private boolean backRegistered;
 private ViewPager pager;private TextView weekLabel;private FrameLayout daysBar; private LinearLayout root;private FrameLayout stage;private boolean returningPage,skipPageMotion;private int week=6;private String data="",notes="",term="2026-08-31";
 private SchoolClient school=new SchoolClient();private final ExecutorService worker=Executors.newSingleThreadExecutor();private boolean busy=false,showingDetail=false;private int screen=0;private Runnable pageBack;private CourseChoiceSheet choiceSheet;private NavigationOverlay navigationOverlay,exitingOverlay;
 private EditText account,password,captcha;private TextView loginStatus;private ProgressBar loginProgress;private Button loginButton;private LinearLayout captchaBox;private ImageView captchaImage;private SchoolAccount schoolAccount;private WebTimetableSync webTimetableSync;private String loginAccountPrefill;
 private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
 private void applyPalette(){secondaryUi=new SecondaryUi(this);INK=secondaryUi.INK;MUTED=secondaryUi.MUTED;BLUE=secondaryUi.BLUE;PRIMARY=secondaryUi.PRIMARY;SURFACE=secondaryUi.SURFACE;AQUA=secondaryUi.AQUA;SOFT=secondaryUi.SOFT;NAVY=secondaryUi.NAVY;CARD=secondaryUi.CARD;DIVIDER=secondaryUi.DIVIDER;DANGER=secondaryUi.DANGER;DISABLED=secondaryUi.DISABLED;ON_PRIMARY=secondaryUi.ON_PRIMARY;HANDLE=secondaryUi.HANDLE;COURSE_BORDER=secondaryUi.COURSE_BORDER;NAV_MUTED=secondaryUi.NAV_MUTED;WARNING=secondaryUi.WARNING;dark=secondaryUi.dark;}
 @Override protected void attachBaseContext(Context base){super.attachBaseContext(Appearance.wrap(base));}
 @Override public void onCreate(Bundle state){super.onCreate(state);if(state!=null){aiRestoredDraft=state.getString("aiDraft");aiRestoredDraftChat=state.getString("aiDraftChat");aiRestoredStart=state.getInt("aiDraftStart");aiRestoredEnd=state.getInt("aiDraftEnd");}applyPalette();schoolAccount=new SchoolAccount(this);Appearance.edgeToEdge(this);setTimes(ClassTimes.parse(getPreferences(0).getString("classTimes","")));schoolId=getPreferences(0).getString("school","squ");loginSchool=schoolId;school=schoolClient(schoolId);data=getPreferences(0).getString("courses","");notes=getPreferences(0).getString("notes","");if((getApplicationInfo().flags&android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE)!=0)school.onDiagnostic=summary->{try(java.io.FileOutputStream out=openFileOutput("sync-diagnostic.txt",0)){out.write(summary.getBytes(java.nio.charset.StandardCharsets.UTF_8));}catch(Exception ignored){}};else deleteFile("sync-diagnostic.txt");week=getPreferences(0).getInt("week",6);term=getPreferences(0).getString("term",term);displayChoices=new HashSet<>(getPreferences(0).getStringSet("displayChoices",Collections.emptySet()));adjustments=getPreferences(0).getString("courseAdjustments","");home();openWidget(getIntent());widgetShortcut();
  if(state!=null){taskState=state.getBundle("taskPage");parcelState=state.getBundle("parcelPage");boolean skip=CourseWidget.OPEN.equals(getIntent().getAction())||ADD_WIDGET.equals(getIntent().getAction());if(state.getBoolean("parcelsVisible")&&!skip)parcels();else if(state.getBoolean("tasksVisible")&&!skip)tasks();}
  if(state!=null&&state.getBoolean("appSettingsVisible")){settingsFromParcel=state.getBoolean("settingsFromParcel");settingsFromParcelActivity=state.getBoolean("settingsFromParcelActivity");settingsFromAi=state.getBoolean("settingsFromAi");menu();if(state.getBoolean("appearanceVisible"))appearanceSettings();else if(state.getBoolean("dataManagementVisible"))dataManagement();else if(state.getBoolean("timetableManagementVisible"))timetableManagement();}else if(state!=null&&state.getBoolean("timetableSettingsVisible"))timetableSettings();if(state!=null&&state.getBoolean("aiVisible")&&!state.getBoolean("appSettingsVisible"))aiAssistant();openPrimaryTab(getIntent());
  View decor=getWindow().getDecorView();decor.getViewTreeObserver().addOnScrollChangedListener(remoteScroll);
  decor.getViewTreeObserver().addOnDrawListener(new ViewTreeObserver.OnDrawListener(){boolean drawn;public void onDraw(){if(drawn)return;drawn=true;decor.post(()->{if(decor.getViewTreeObserver().isAlive())decor.getViewTreeObserver().removeOnDrawListener(this);remoteHandler.postDelayed(()->{if(!isDestroyed()&&!isFinishing()){remote=RemoteConfigClient.get(MainActivity.this);remote.listen(remoteListener);if(state==null||!remote.loaded)remote.opened();refreshRemoteStatus();}},1500);Looper.myQueue().addIdleHandler(()->{prepareTasks();prepareParcels();return false;});});}});
 }
 @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);boolean appearance="外观".equals(root.getTag())||(navigationOverlay!=null&&"外观".equals(navigationOverlay.underneath.getTag()));state.putBoolean("appearanceVisible",appearance);if(aiPage!=null){EditText draft=aiPage.findViewWithTag("ai-message");if(draft!=null)try{state.putString("aiDraft",draft.getText().toString());state.putString("aiDraftChat",aiStore().chat().getString("id"));state.putInt("aiDraftStart",draft.getSelectionStart());state.putInt("aiDraftEnd",draft.getSelectionEnd());}catch(Exception ignored){}}state.putBoolean("aiVisible",aiVisible);state.putBoolean("tasksVisible",tasksVisible);state.putBoolean("parcelsVisible",parcelsVisible);state.putBoolean("dataManagementVisible","数据管理".equals(root.getTag()));state.putBoolean("timetableManagementVisible","课表管理".equals(root.getTag()));state.putBoolean("appSettingsVisible",appearance||(root==settingsPage&&settingsPage!=null)||"数据管理".equals(root.getTag())||"课表管理".equals(root.getTag()));state.putBoolean("timetableSettingsVisible",navigationOverlay!=null&&"课表设置".equals(navigationOverlay.panel.getTag()));state.putBoolean("settingsFromAi",settingsFromAi);state.putBoolean("settingsFromParcel",settingsFromParcel);state.putBoolean("settingsFromParcelActivity",settingsFromParcelActivity);Bundle page=new Bundle();if(taskPage!=null)taskPage.saveState(page);else if(taskState!=null)page.putAll(taskState);state.putBundle("taskPage",page);Bundle parcel=new Bundle();if(parcelPage!=null)parcelPage.saveState(parcel);else if(parcelState!=null)parcel.putAll(parcelState);state.putBundle("parcelPage",parcel);}
 @Override public SharedPreferences getPreferences(int mode){return Timetables.preferences(this);}
 private void setTimes(ClassTimes value){classTimes=value;START_TIMES=value.starts;END_TIMES=value.ends;}
 private SchoolClient schoolClient(String id){return id.equals("xzit")?new XzitClient():new SchoolClient();}
 private void saveWeek(){getPreferences(0).edit().putInt("week",week).apply();}
 private void save(){getPreferences(0).edit().putString("courses",data).putString("notes",notes).putInt("week",week).putString("term",term).putString("classTimes",classTimes.encode()).putString("school",schoolId).apply();CourseWidget.updateAll(this);}
 private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
 private GradientDrawable bg(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));return g;}
 private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setIncludeFontPadding(false);return t;}
 private void applyStageInsets(View view,WindowInsets insets){
  androidx.core.view.WindowInsetsCompat bars=androidx.core.view.WindowInsetsCompat.toWindowInsetsCompat(insets,view);boolean chat=aiPage!=null&&root==aiPage;for(NavigationOverlay overlay=navigationOverlay;!chat&&overlay!=null;overlay=overlay.previous)chat=overlay.underneath==aiPage;boolean keyboard=bars.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime());int bottom=chat?(Build.VERSION.SDK_INT>=30&&keyboard?bars.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime()).bottom:0):(bottomNavigation!=null&&bottomNavigation.getVisibility()==View.VISIBLE?0:bars.getInsets(androidx.core.view.WindowInsetsCompat.Type.navigationBars()).bottom);view.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),bottom);if(chat&&bottomNavigation!=null)bottomNavigation.setVisibility(keyboard?View.GONE:View.VISIBLE);if(chat){if(aiKeyboardVisible&&!keyboard&&aiAttachmentMenu!=null)aiAttachmentMenu.close(true);aiKeyboardVisible=keyboard;aiComposerExpanded(keyboard);}
 }
 private void base(boolean secondary){base(secondary,null);}
 private void base(boolean secondary,LinearLayout retained){
  getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
  manualUpdatePending=false;pagerState=ViewPager.SCROLL_STATE_IDLE;if(remoteDialog!=null)remoteDialog.dismiss();scheduleRemoteNotice();
  pageMotion.cancel();if(sideNavigation!=null&&!sideNavigation.isRouting())sideNavigation.close(false);
  if(unsavedDialog!=null){unsavedDialog.dismiss();unsavedDialog=null;}
  while(navigationOverlay!=null)navigationOverlay.close(true);
  if(root==settingsPage&&settingsPage!=null)settingsScrollY=((ScrollView)settingsPage.getChildAt(1)).getScrollY();
  if(timetableScroll!=null){timetableScrollY=timetableScroll.getScrollY();timetableScroll=null;}
  if(choiceSheet!=null){choiceSheet.close(false);if(choiceSheet.getParent() instanceof ViewGroup)((ViewGroup)choiceSheet.getParent()).removeView(choiceSheet);choiceSheet=null;}
  LinearLayout previous=root;boolean backwards=returningPage||(!secondary&&pageBackwards);
  if(aiAttachmentMenu!=null)aiAttachmentMenu.close(true);pageBack=null;updateSystemBack();showingDetail=false;screen++;pager=null;
  if(previous==parcelPage&&retained!=parcelPage)parcelPage.pause();
  if(stage==null){stage=new FrameLayout(this){@Override public boolean dispatchTouchEvent(MotionEvent event){return pageMotion.isRunning()||super.dispatchTouchEvent(event);}};stage.setBackgroundColor(SURFACE);stage.setOnApplyWindowInsetsListener((view,insets)->{applyStageInsets(view,insets);return insets;});LinearLayout shell=new LinearLayout(this);shell.setOrientation(LinearLayout.VERTICAL);shell.addView(stage,new LinearLayout.LayoutParams(-1,0,1));bottomNavigation=createBottomNavigation();bottomNavigation.setOnApplyWindowInsetsListener((view,insets)->{view.setPadding(0,0,0,androidx.core.view.WindowInsetsCompat.toWindowInsetsCompat(insets,view).getInsets(androidx.core.view.WindowInsetsCompat.Type.navigationBars()).bottom);return insets;});shell.addView(bottomNavigation,new LinearLayout.LayoutParams(-1,-2));windowContent=new FrameLayout(this);windowContent.addView(shell,new FrameLayout.LayoutParams(-1,-1));sideNavigation=new SideNavigation(this,windowContent,SURFACE,CARD,INK,this::selectPrimaryPage,()->{settingsFromParcel=parcelsVisible;settingsFromAi=aiVisible;settingsFromParcelActivity=false;menu();});setContentView(sideNavigation);}
  bottomNavigation.setVisibility(secondary?View.GONE:View.VISIBLE);updateBottomNavigation();
  for(int i=0;i<stage.getChildCount();i++)stage.getChildAt(i).animate().cancel();
  stage.removeAllViews();
  root=retained!=null?retained:new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setClickable(true);
  root.setBackgroundColor(SURFACE);
  boolean animate=!skipPageMotion&&!switchingPrimaryTab&&!sideNavigation.isRouting()&&previous!=null&&previous!=root&&previous.getWidth()>0&&android.animation.ValueAnimator.areAnimatorsEnabled();
  if(animate){
   stage.addView(previous,new FrameLayout.LayoutParams(-1,-1));
  }
  LinearLayout incoming=root;stage.addView(incoming,new FrameLayout.LayoutParams(-1,-1));
  if(incoming==parcelPage)parcelPage.resume();stage.post(()->stage.requestApplyInsets());
  if(!animate)return;
  pageMotion.start(stage,previous,incoming,backwards,stage.getWidth());
 }
 private void backPage(){Runnable back=pageBack;if(back==null)return;returningPage=true;try{back.run();}finally{returningPage=false;}}
 private void updateSystemBack(){
  if(Build.VERSION.SDK_INT<33)return;
  boolean needed=(aiAttachmentMenu!=null&&aiAttachmentMenu.open)||tasksVisible||parcelsVisible||navigationOverlay!=null||exitingOverlay!=null||pageBack!=null||(choiceSheet!=null&&choiceSheet.getVisibility()==View.VISIBLE)||busy;
  if(needed==backRegistered)return;
  if(needed)systemBack=MotionBack.register(getWindow(),0,this::startBackMotion,this::progressBackMotion,this::cancelBackMotion,this::onBackPressed);
  else {MotionBack.unregister(getWindow(),systemBack);systemBack=null;}
  backRegistered=needed;
 }
 private void startBackMotion(){if(pageMotion.isReturning()||exitingOverlay!=null&&exitingOverlay.isAttachedToWindow())return;if(pageMotion.isRunning())pageMotion.beginBack();else if(navigationOverlay!=null)navigationOverlay.beginBack();else if(choiceSheet!=null&&choiceSheet.getVisibility()==View.VISIBLE)choiceSheet.beginBack();else{root.animate().cancel();backPageX=root.getTranslationX();}}
 private void progressBackMotion(float progress){if(pageMotion.isReturning()||exitingOverlay!=null&&exitingOverlay.isAttachedToWindow())return;if(pageMotion.isRunning())pageMotion.backProgress(progress);else if(navigationOverlay!=null)navigationOverlay.backProgress(progress);else if(choiceSheet!=null&&choiceSheet.getVisibility()==View.VISIBLE)choiceSheet.backProgress(progress);else if(pageBack!=null&&!busy){root.setTranslationX(backPageX+dp(24)*progress);root.setAlpha(1);}}
 private void cancelBackMotion(){if(pageMotion.isReturning()||exitingOverlay!=null&&exitingOverlay.isAttachedToWindow())return;if(pageMotion.isRunning())pageMotion.cancelBack();else if(navigationOverlay!=null)navigationOverlay.cancelBack();else if(choiceSheet!=null&&choiceSheet.getVisibility()==View.VISIBLE)choiceSheet.cancelBack();else if(root.getTranslationX()!=0||root.getAlpha()!=1){root.animate().translationX(0).alpha(1).setDuration(Motion.PAGE_OUT).setInterpolator(Motion.ENTER).start();}}
 private void focusOutline(View view){secondaryUi.focusOutline(view);}

 private void selectPrimaryPage(int selected){
  if(aiVisible&&selected!=3)((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(stage.getWindowToken(),0);
  switchingPrimaryTab=true;
  try{
   if(selected==0){if(tasksVisible||parcelsVisible||aiVisible)home();}
   else if(selected==1){if(!tasksVisible)tasks();}
   else if(selected==2){if(!parcelsVisible)parcels();}else if(selected==3&&(!aiVisible||root!=aiPage))showAiTab();
  }finally{switchingPrimaryTab=false;}
 }
 private void showAiTab(){
  if(aiPage==null||!Timetables.active(this).equals(aiChatTimetable)){aiAssistant();return;}
  aiVisible=true;tasksVisible=false;parcelsVisible=false;base(false,aiPage);root.setBackgroundColor(SURFACE);pageBack=null;updateSystemBack();if(Build.VERSION.SDK_INT>=30)getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);stage.requestApplyInsets();
 }
 private int primaryTab(){return aiVisible?3:parcelsVisible?2:tasksVisible?1:0;}
 void openSideMenu(){if(busy)return;pageMotion.cancel();sideNavigation.setExtra(aiDrawerContent());sideNavigation.open(primaryTab());}
 private void selectBottomPage(int selected){selectPrimaryPage(selected);}
 private AppNavigation createBottomNavigation(){return new AppNavigation(this,SURFACE,INK,NAV_MUTED,primaryTab(),this::selectBottomPage);}
 private void updateBottomNavigation(){
  if(bottomNavigation!=null)bottomNavigation.select(primaryTab());if(sideNavigation!=null)sideNavigation.select(primaryTab());
 }
 private void openPrimaryTab(Intent intent){
  if(OPEN_SETTINGS.equals(intent.getAction())){intent.setAction(Intent.ACTION_MAIN);settingsFromParcel=false;settingsFromParcelActivity=true;menu();return;}
  if(!OPEN_TAB.equals(intent.getAction()))return;settingsFromParcel=false;int selected=intent.getIntExtra("primaryTab",0);intent.setAction(Intent.ACTION_MAIN);
  boolean active=selected==0?(homePage!=null&&root==homePage.view&&!tasksVisible&&!parcelsVisible):selected==1?(tasksVisible&&root==taskPage):selected==2?(parcelsVisible&&root==parcelPage):(aiVisible&&"AI 助手".equals(root.getTag()));
  if(active){updateBottomNavigation();return;}
  selectPrimaryPage(selected);}
 // Prepare cached task content after the first home frame; due refreshes run on the task worker.
 private void prepareTasks(){
  if(isDestroyed()||isFinishing()||taskPage!=null)return;
  taskPage=new TaskPage(this,taskState);int width=stage.getWidth(),height=stage.getHeight();
  if(width>0&&height>0){taskPage.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));taskPage.layout(0,0,width,height);}
 }
 boolean taskRefreshActive(){return remoteResumed&&navigationOverlay==null&&(sideNavigation==null||!sideNavigation.isOpen());}
 void tasks(){aiVisible=false;tasksVisible=true;parcelsVisible=false;pageBackwards=returningPage;if(taskPage==null)taskPage=new TaskPage(this,taskState);taskPage.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);base(false,taskPage);root.setBackgroundColor(SURFACE);}
 private void parcels(){
  aiVisible=false;tasksVisible=false;parcelsVisible=true;
  if(parcelPage==null)parcelPage=new ParcelPage(this,parcelState,this::openSideMenu);
  parcelPage.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);base(false,parcelPage);root.setBackgroundColor(SURFACE);
 }
 private void prepareParcels(){
  if(isDestroyed()||isFinishing()||parcelPage!=null)return;
  parcelPage=new ParcelPage(this,parcelState,this::openSideMenu);int width=stage.getWidth(),height=stage.getHeight();
  if(width>0&&height>0){parcelPage.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));parcelPage.layout(0,0,width,height);}
 }

 private Calendar date(){Calendar c=Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));c.set(Calendar.HOUR_OF_DAY,12);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);return c;}
 private Calendar monday(){Calendar c=date();String[] p=term.split("-");c.set(Integer.parseInt(p[0]),Integer.parseInt(p[1])-1,Integer.parseInt(p[2]));return c;}
 private int currentWeek(){return Math.max(1,Math.min(30,(int)((date().getTimeInMillis()-monday().getTimeInMillis())/86400000L/7)+1));}
 private TextView icon(int drawable,String description,Runnable action){TextView view=text("",22,INK);view.setGravity(Gravity.CENTER);view.setContentDescription(description);view.setTooltipText(description);view.setOnClickListener(v->action.run());view.setFocusable(true);secondaryUi.styleIcon(view,drawable);return view;}
 private void updateOverlayAppearance(){
  boolean choosing=choiceSheet!=null&&choiceSheet.open,bottomSheet=choosing;for(NavigationOverlay overlay=navigationOverlay;overlay!=null;overlay=overlay.previous)if(!overlay.corner)bottomSheet=true;Appearance.systemBars(this);bottomNavigation.setImportantForAccessibility(navigationOverlay!=null||exitingOverlay!=null||choosing?View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS:View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
 }

 private void changeWeek(int next){if(next<1||next>30)return;if(navigationOverlay!=null)navigationOverlay.close(true);if(pager!=null&&pager.isAttachedToWindow()){pager.setCurrentItem(next-1,true);}else{week=next;saveWeek();home();}}
 LinearLayout secondary(String title,String description,Runnable back){base(true);root.setTag(title);pageBack=back;updateSystemBack();root.setBackgroundColor(SURFACE);Appearance.systemBars(this);LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPadding(dp(12),dp(4),dp(12),dp(4));TextView close=icon(R.drawable.ic_back,"返回",()->{if(busy){toast("正在同步，请稍候");return;}backPage();});bar.addView(close,new LinearLayout.LayoutParams(dp(48),dp(48)));TextView heading=text(title,18,INK);heading.setTypeface(null,Typeface.BOLD);if(Build.VERSION.SDK_INT>=28)heading.setAccessibilityHeading(true);heading.setGravity(Gravity.CENTER);bar.addView(heading,new LinearLayout.LayoutParams(0,-2,1));View spacer=new View(this);spacer.setTag("header-spacer");spacer.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);bar.addView(spacer,new LinearLayout.LayoutParams(dp(48),dp(48)));secondaryUi.headerStyle(bar);root.addView(bar);ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setClipToPadding(false);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(16),dp(8),dp(16),dp(20));scroll.addView(body);if(!description.isEmpty()){TextView intro=text(description,14,MUTED);intro.setLineSpacing(dp(4),1);intro.setPadding(dp(4),0,dp(4),dp(20));body.addView(intro);}return body;}
 private LinearLayout bottomSheet(String title,String description,Runnable back){
  if(returningPage&&navigationOverlay==null)back.run();
  NavigationOverlay overlay=new NavigationOverlay(false,title);LinearLayout body=overlay.body;
  if(!description.isEmpty()){TextView intro=text(description,14,MUTED);intro.setLineSpacing(dp(4),1);intro.setPadding(0,0,0,dp(16));body.addView(intro);}
  return body;
 }
 private void quickMenu(){
  if(navigationOverlay!=null&&navigationOverlay.corner){navigationOverlay.close(true);return;}
  NavigationOverlay overlay=new NavigationOverlay(true,"更多操作");
  String[] labels={"课程管理","学校登录","切换课表","新增课表"};
  Runnable[] actions={()->edit(),()->login(),()->timetablePicker(),()->newTimetable()};int[] tags={0,2,3,4};
  for(int i=0;i<labels.length;i++)menuItem(overlay,labels[i],"quick-action-"+tags[i],actions[i]);
  if(getPreferences(0).getBoolean("schoolImported",notes.startsWith("已从学校同步")))menuItem(overlay,"同步课表","refresh-timetable",this::refreshTimetable).setEnabled(!busy);
 }
 TextView menuItem(NavigationOverlay overlay,String label,String tag,Runnable run){TextView item=secondaryUi.popupItem(overlay.body,label,()->{overlay.close(true);run.run();});item.setTag(tag);item.setTextColor(new android.content.res.ColorStateList(new int[][]{new int[]{-android.R.attr.state_enabled},new int[]{}},new int[]{DISABLED,INK}));return item;}

 private NavigationOverlay sheetMenu(String title){return new NavigationOverlay(true,title);}
 class NavigationOverlay extends MotionOverlay {
  final boolean corner;final View anchor;View returnFocus;final LinearLayout panel,body;final View shade;final LinearLayout underneath;final Runnable underneathBack;final NavigationOverlay previous;
  final int accessibility;final boolean underneathDetail;boolean anchorClosing,fitContent;boolean open=true;int menuWidth=SecondaryUi.MENU_WIDTH;int preferredMenuTop,menuAnchorHeight=dp(48);
  NavigationOverlay(boolean corner,String title){this(corner,title,null);}
  NavigationOverlay(boolean corner,String title,View anchor){
   super(MainActivity.this);pageMotion.cancel();this.corner=corner;this.anchor=anchor;underneath=root;underneathBack=pageBack;underneathDetail=showingDetail;previous=navigationOverlay;accessibility=underneath.getImportantForAccessibility();
   underneath.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
   setFocusableInTouchMode(true);setClickable(true);setTag(corner?"quick-menu":"navigation-sheet");
   shade=new View(MainActivity.this);shade.setBackgroundColor(corner?Color.TRANSPARENT:0x26000000);shade.setTag("navigation-shade");shade.setOnClickListener(v->requestClose());addView(shade,new FrameLayout.LayoutParams(-1,-1));
   panel=new LinearLayout(MainActivity.this);panel.setOrientation(LinearLayout.VERTICAL);panel.setTag(title);panel.setBackground(corner?secondaryUi.popupSurface():secondaryUi.sheetSurface());panel.setClipToOutline(true);panel.setClickable(true);panel.setElevation(0);
   FrameLayout.LayoutParams bounds=new FrameLayout.LayoutParams(-1,-2,corner?Gravity.TOP|Gravity.END:Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);bounds.setMargins(corner?dp(12):0,corner?dp(56):0,corner?dp(12):0,0);if(corner){View trigger=anchor!=null?anchor:underneath.findViewWithTag("sheet-action-button");if(trigger==null)trigger=underneath.findViewWithTag("quick-menu-button");if(trigger!=null&&trigger.getHeight()>0){int[] at=new int[2],stageAt=new int[2];trigger.getLocationInWindow(at);stage.getLocationInWindow(stageAt);bounds.topMargin=at[1]-stageAt[1]+trigger.getHeight();menuAnchorHeight=trigger.getHeight();}}preferredMenuTop=bounds.topMargin;addView(panel,bounds);
   if(!corner){View handle=new View(MainActivity.this);handle.setBackground(bg(HANDLE,2));handle.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);LinearLayout.LayoutParams grip=new LinearLayout.LayoutParams(dp(36),dp(4));grip.gravity=Gravity.CENTER_HORIZONTAL;grip.setMargins(0,dp(10),0,dp(4));panel.addView(handle,grip);}
   if(!corner){LinearLayout bar=new LinearLayout(MainActivity.this);bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPadding(dp(16),0,dp(8),0);TextView heading=text(title,18,INK);heading.setTag("navigation-title");heading.setTypeface(null,Typeface.BOLD);bar.addView(heading,new LinearLayout.LayoutParams(0,-2,1));TextView close=icon(R.drawable.ic_more,title+"操作",()->{});close.setTag("sheet-action-button");close.setVisibility(View.GONE);bar.addView(close,new LinearLayout.LayoutParams(dp(48),dp(48)));panel.addView(bar);}
   ScrollView scroll=new ScrollView(MainActivity.this);panel.addView(scroll,new LinearLayout.LayoutParams(-1,corner?-2:0,corner?0:1));body=new LinearLayout(MainActivity.this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(corner?dp(4):dp(16),corner?dp(SecondaryUi.MENU_VERTICAL_PADDING):dp(8),corner?dp(4):dp(16),corner?dp(SecondaryUi.MENU_VERTICAL_PADDING):dp(8));scroll.addView(body);
   navigationOverlay=this;updateOverlayAppearance();root=panel;pageBack=null;showingDetail=corner&&underneathDetail;windowContent.addView(this,new FrameLayout.LayoutParams(-1,-1));updateSystemBack();requestFocus();if(Build.VERSION.SDK_INT>=28)setAccessibilityPaneTitle(title);
   bindMotion(panel,shade,corner,this::requestClose);if(previous!=null&&!corner)above(previous);showMotion();
  }
  @Override public boolean dispatchTouchEvent(MotionEvent event){
   if(overlapsAnchor&&anchor!=null){if(event.getActionMasked()==MotionEvent.ACTION_DOWN){int[] at=new int[2];anchor.getLocationOnScreen(at);anchorClosing=event.getRawX()>=at[0]&&event.getRawX()<at[0]+anchor.getWidth()&&event.getRawY()>=at[1]&&event.getRawY()<at[1]+anchor.getHeight();}if(anchorClosing){int action=event.getActionMasked();if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL){anchorClosing=false;if(action==MotionEvent.ACTION_UP)close(true);}return true;}}
   return super.dispatchTouchEvent(event);
  }
  // Keep readable text; long sheets scroll instead of shrinking their contents.
  @Override protected void onMeasure(int widthSpec,int heightSpec){
   FrameLayout.LayoutParams bounds=(FrameLayout.LayoutParams)panel.getLayoutParams();int height=MeasureSpec.getSize(heightSpec)-getPaddingBottom();bounds.bottomMargin=0;
   bounds.width=Math.max(1,MeasureSpec.getSize(widthSpec)-(corner?dp(24):0));bounds.height=-2;
   if(corner){
    bounds.width=Math.min(bounds.width,dp(menuWidth));
    if(overlapsAnchor&&anchor!=null){
     LinearLayout.LayoutParams content=(LinearLayout.LayoutParams)panel.getChildAt(0).getLayoutParams();content.height=-2;content.weight=0;
     int[] at=new int[2],origin=new int[2];anchor.getLocationInWindow(at);getLocationInWindow(origin);placeMenuOverAnchor(MeasureSpec.getSize(widthSpec),height,new Rect(at[0]-origin[0],at[1]-origin[1],at[0]-origin[0]+anchor.getWidth(),at[1]-origin[1]+anchor.getHeight()));
     content.height=0;content.weight=1;
    }else placeMenu(height,preferredMenuTop,menuAnchorHeight);
   }
   else if(fitContent){panel.measure(MeasureSpec.makeMeasureSpec(bounds.width,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED));bounds.height=Math.min(panel.getMeasuredHeight(),Math.max(1,height-Math.max(dp(24),safeTop())));}
   else bounds.height=Math.max(1,height/2);
   super.onMeasure(widthSpec,heightSpec);
  }

  Runnable dismissRequest;
  void requestClose(){if(dismissRequest!=null){dismissRequest.run();if(open)cancelBack();}else close(true);}
  void close(boolean animate){
   scheduleRemoteNotice();if(animate&&!open)return;open=false;exitingOverlay=this;underneath.setImportantForAccessibility(accessibility);
   if(navigationOverlay==this){navigationOverlay=previous;root=underneath;pageBack=underneathBack;showingDetail=underneathDetail;if(previous!=null)previous.setVisibility(VISIBLE);updateSystemBack();}
   setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
   hideMotion(animate,()->{windowContent.removeView(this);if(aiAttachmentMenu==this)aiAttachmentMenu=null;if(exitingOverlay==this)exitingOverlay=null;View focus=returnFocus!=null?returnFocus:corner?(anchor!=null?anchor:underneath.findViewWithTag("sheet-action-button")):underneath;if(focus==null)focus=underneath;if(navigationOverlay==previous&&focus.isAttachedToWindow())focus.requestFocus();updateOverlayAppearance();updateSystemBack();});
  }
 }
 LinearLayout panel(LinearLayout body){
  LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(16),dp(16),dp(16),dp(16));card.setBackground(bg(CARD,20));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(12);body.addView(card,lp);return card;
 }
 private Button action(LinearLayout body,String label,Runnable run){return secondaryUi.action(body,label,run);}
 LinearLayout configurationActions(LinearLayout body){LinearLayout actions=secondaryUi.actionRow(body);body.removeView(actions);LinearLayout.LayoutParams footer=new LinearLayout.LayoutParams(-1,-2);footer.setMargins(dp(16),dp(8),dp(16),dp(12));root.addView(actions,footer);return actions;}
 LinearLayout option(LinearLayout body,int drawable,String title,String description,Runnable run){
  LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER_VERTICAL);row.setMinimumHeight(dp(56));row.setPadding(dp(16),dp(12),dp(16),dp(12));boolean grouped="settings-group".equals(body.getTag());if(grouped&&body.getChildCount()>0){View line=new View(this);line.setBackgroundColor(DIVIDER);LinearLayout.LayoutParams divider=new LinearLayout.LayoutParams(-1,dp(1));divider.leftMargin=dp(16);divider.rightMargin=dp(16);body.addView(line,divider);}row.setBackground(bg(CARD,grouped?0:22));LinearLayout.LayoutParams rowParams=new LinearLayout.LayoutParams(-1,-2);rowParams.bottomMargin=grouped?0:dp(10);body.addView(row,rowParams);
  ImageView symbol=new ImageView(this);symbol.setImageResource(drawable);symbol.setImageTintList(android.content.res.ColorStateList.valueOf(INK));symbol.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);LinearLayout.LayoutParams glyph=new LinearLayout.LayoutParams(dp(22),dp(22));glyph.rightMargin=dp(14);row.addView(symbol,glyph);
  LinearLayout copy=new LinearLayout(this);copy.setOrientation(LinearLayout.VERTICAL);TextView heading=text(title,16,INK);copy.addView(heading);if(!description.isEmpty()){TextView note=text(description,13,MUTED);note.setPadding(0,dp(3),0,0);copy.addView(note);}row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));TextView arrow=text("›",20,MUTED);arrow.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);arrow.setPadding(dp(12),0,0,0);row.addView(arrow);
  row.setFocusable(true);row.setContentDescription(description.isEmpty()?title:title+"，"+description);row.setOnClickListener(v->run.run());focusOutline(row);return row;
 }
 private void weekPicker(){weekPicker(()->home());}
 private void weekPicker(Runnable back){
  LinearLayout body=bottomSheet("选择教学周","当前教学周：第 "+currentWeek()+" 周 · 点选即可查看",back);
  for(int start=1;start<=30;start+=5){LinearLayout row=new LinearLayout(this);LinearLayout.LayoutParams rowParams=new LinearLayout.LayoutParams(-1,-2);rowParams.bottomMargin=dp(8);body.addView(row,rowParams);for(int w=start;w<start+5;w++){final int chosen=w;TextView cell=text(""+w,16,NAVY);cell.setTag("week-choice-"+w);cell.setContentDescription("查看第 "+w+" 周"+(w==currentWeek()?"，当前教学周":""));cell.setGravity(Gravity.CENTER);cell.setMinHeight(dp(48));cell.setSelected(w==week);cell.setTypeface(null,w==week?Typeface.BOLD:Typeface.NORMAL);cell.setBackground(bg(w==week?AQUA:SURFACE,16));cell.setFocusable(true);cell.setOnClickListener(v->changeWeek(chosen));focusOutline(cell);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(48),1);if(w>start)lp.leftMargin=dp(8);row.addView(cell,lp);}}
  action(body,"回到本周",()->changeWeek(currentWeek())).setTag("week-current");
  root.findViewWithTag("sheet-action-button").setVisibility(View.VISIBLE);root.findViewWithTag("sheet-action-button").setOnClickListener(v->{NavigationOverlay menu=sheetMenu("教学周操作");for(int w=1;w<=30;w++){final int chosen=w;secondaryUi.popupChecked(menuItem(menu,"第 "+w+" 周","week-"+w,()->changeWeek(chosen)),w==week);}});
 }
 private void home(){
  settingsPage=null;pageBackwards=true;
  Appearance.systemBars(this);
  boolean fromTasks=tasksVisible,fromParcels=parcelsVisible,fromAi=aiVisible;aiVisible=false;tasksVisible=false;parcelsVisible=false;
  if(homePage!=null&&(pageBack!=null||fromTasks||fromParcels||fromAi)&&navigationOverlay==null&&homePage.matches()){
   HomePage page=homePage;page.view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);base(false,page.view);
   pager=page.pages;timetableScroll=page.scroll;daysBar=page.days;weekLabel=page.label;
   pager.setCurrentItem(week-1,false);updateHeader();timetableScroll.scrollTo(0,timetableScrollY);return;
  }
  base(false);LinearLayout header=new LinearLayout(this);header.setTag("home-header");header.setOrientation(LinearLayout.VERTICAL);LinearLayout toolbar=new LinearLayout(this);toolbar.setGravity(Gravity.CENTER_VERTICAL);toolbar.setPadding(dp(16),dp(8),dp(16),dp(6));header.addView(toolbar);secondaryUi.menuAction(toolbar,this::openSideMenu);toolbar.addView(new View(this),new LinearLayout.LayoutParams(0,1));
  LinearLayout dates=new LinearLayout(this);dates.setOrientation(LinearLayout.VERTICAL);dates.setGravity(Gravity.CENTER);Calendar today=date();TextView title=text((today.get(Calendar.MONTH)+1)+"月"+today.get(Calendar.DAY_OF_MONTH)+"日",24,INK);title.setGravity(Gravity.CENTER);title.setTypeface(null,Typeface.BOLD);title.setTag("primary-page-title");dates.addView(title);TextView sub=text(weekCaption(),14,MUTED);sub.setGravity(Gravity.CENTER);sub.setSingleLine(true);sub.setEllipsize(android.text.TextUtils.TruncateAt.END);weekLabel=sub;LinearLayout.LayoutParams subp=new LinearLayout.LayoutParams(-1,-2);subp.topMargin=dp(4);dates.addView(sub,subp);dates.setTag("home-date");dates.setMinimumHeight(dp(48));dates.setFocusable(true);dates.setContentDescription(title.getText()+"，"+sub.getText()+"，点击回到本周");dates.setTooltipText("回到本周");dates.setOnClickListener(v->changeWeek(currentWeek()));focusOutline(dates);toolbar.addView(dates,new LinearLayout.LayoutParams(0,-2,1));
  TextView moduleSettings=icon(R.drawable.ic_more,"课表设置",this::timetableSettings);moduleSettings.setTag("timetable-settings");LinearLayout.LayoutParams settingsSize=new LinearLayout.LayoutParams(dp(48),dp(48));settingsSize.leftMargin=dp(8);toolbar.addView(moduleSettings,settingsSize);secondaryUi.headerStyle(toolbar);
  daysBar=new FrameLayout(this);daysBar.setTag("week-days-viewport");header.addView(daysBar,new LinearLayout.LayoutParams(-1,dp(55)));
  TimetableScrollView scroll=new TimetableScrollView(this);timetableScroll=scroll;scroll.setFillViewport(false);scroll.setVerticalScrollBarEnabled(false);LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);scroll.addView(content);
   pager=scroll.new WeekPager(this);pager.setOffscreenPageLimit(1);pager.setAdapter(new PagerAdapter(){public int getCount(){return 30;}public boolean isViewFromObject(View v,Object o){return v==o;}public Object instantiateItem(ViewGroup container,int position){WeekGrid grid=new WeekGrid();grid.shownWeek=position+1;grid.setTag("week-grid-"+grid.shownWeek);grid.days=weekDays(grid.shownWeek);daysBar.addView(grid.days,new FrameLayout.LayoutParams(-1,dp(55)));daysBar.post(daysBar::requestLayout);grid.addOnLayoutChangeListener((v,left,top,right,bottom,oldLeft,oldTop,oldRight,oldBottom)->syncWeekDays());container.addView(grid);syncWeekDays();return grid;}public void destroyItem(ViewGroup container,int position,Object object){daysBar.removeView(((WeekGrid)object).days);container.removeView((View)object);}});pager.setCurrentItem(week-1,false);pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){public void onPageScrolled(int position,float offset,int pixels){syncWeekDays();}public void onPageSelected(int position){week=position+1;saveWeek();updateHeader();}public void onPageScrollStateChanged(int state){pagerState=state;scheduleRemoteNotice();}});content.addView(pager,new LinearLayout.LayoutParams(-1,timetableGridHeight(week)));
  if(!notes.isEmpty()){TextView note=text(notes,12,MUTED);note.setPadding(dp(16),dp(16),dp(16),dp(24));content.addView(note);}
  final int restoreY=timetableScrollY;scroll.addOnLayoutChangeListener(new View.OnLayoutChangeListener(){public void onLayoutChange(View v,int l,int t,int r,int b,int oldL,int oldT,int oldR,int oldB){scroll.removeOnLayoutChangeListener(this);scroll.scrollTo(0,restoreY);}});
  root.addView(new ScrollHeader(this,header,scroll,SURFACE,scroll),new LinearLayout.LayoutParams(-1,0,1));
  homePage=new HomePage();
 }
 @Override protected void onStart(){super.onStart();if(remote!=null)remote.opened();}
 @Override protected void onResume(){super.onResume();getWindow().getDecorView().postDelayed(this::aiResumePhoneQuestion,500);AiScreenService.morrowVisible=true;remoteResumed=true;if(taskPage!=null)taskPage.scheduleRefresh();if(parcelPage!=null&&root==parcelPage)parcelPage.resume();scheduleRemoteNotice();if(pager!=null&&pager.isAttachedToWindow())updateHeader();Context context=getApplicationContext();worker.execute(()->CourseWidget.updateAll(context));getWindow().getDecorView().postDelayed(()->checkWidgetPin(),250);}
 @Override protected void onPause(){AiScreenService.morrowVisible=false;if(aiCamera!=null)aiCamera.dismiss();remoteResumed=false;if(taskPage!=null)taskPage.pauseRefresh();if(parcelPage!=null)parcelPage.pause();touching=false;remoteHandler.removeCallbacks(remoteNotice);super.onPause();}
 @Override protected void onStop(){if(remoteDialog!=null)remoteDialog.dismiss();super.onStop();}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==920){if(result==RESULT_OK&&data!=null)acceptWebTimetable(data);return;}if(request==911){if(result==RESULT_OK&&data!=null){Intent service=new Intent(this,AiScreenService.class).putExtra("consent",data);startForegroundService(service);toast("屏幕共享已开启。打开要看的其他 App，再返回 AI 发送问题。");}else{aiPendingPhoneQuestion=null;aiPendingPhoneSource=null;toast("未开启屏幕共享");}return;}if(request>=900&&request<=903){aiAttachmentResult(request,result,data);return;}}

 @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] grants){super.onRequestPermissionsResult(request,permissions,grants);if(request==909){if(grants.length>0&&grants[0]==android.content.pm.PackageManager.PERMISSION_GRANTED&&aiAttachmentPage==aiPage&&aiPage==root)aiOpenCamera();else toast("未允许相机，可改用照片或文件添加。");return;}if(request==910){if(grants.length==0||grants[0]!=android.content.pm.PackageManager.PERMISSION_GRANTED){aiPendingPhoneQuestion=null;aiPendingPhoneSource=null;toast("没有日历权限，AI 不会读取日历。");}else getWindow().getDecorView().postDelayed(this::aiResumePhoneQuestion,300);if("手机信息与权限".equals(root.getTag()))aiPhoneSettings();return;}if(parcelPage!=null)parcelPage.onRequestPermissionsResult(request,permissions,grants);}
 @Override public boolean dispatchTouchEvent(MotionEvent event){if(event.getActionMasked()==MotionEvent.ACTION_DOWN)touching=true;interactionAt=SystemClock.uptimeMillis();try{return super.dispatchTouchEvent(event);}finally{if(event.getActionMasked()==MotionEvent.ACTION_UP||event.getActionMasked()==MotionEvent.ACTION_CANCEL){touching=false;scheduleRemoteNotice();}}}
 private String weekCaption(){String name=Timetables.names(this).get(Timetables.active(this));return "第 "+week+" 周"+(week==currentWeek()?"":" · 本周第 "+currentWeek()+" 周")+(Timetables.names(this).size()>1?" · "+name:"");}
 private void updateHeader(){if(pager.getLayoutParams()!=null&&pager.getLayoutParams().height!=timetableGridHeight(week)){pager.getLayoutParams().height=timetableGridHeight(week);pager.requestLayout();}Calendar today=date();((TextView)((LinearLayout)weekLabel.getParent()).getChildAt(0)).setText(String.format(Locale.ROOT,"%d月%d日",today.get(Calendar.MONTH)+1,today.get(Calendar.DAY_OF_MONTH)));weekLabel.setText(weekCaption());((View)weekLabel.getParent()).setContentDescription(((TextView)((LinearLayout)weekLabel.getParent()).getChildAt(0)).getText()+"，"+weekLabel.getText()+"，点击回到本周");for(int i=0;i<pager.getChildCount();i++)if(pager.getChildAt(i) instanceof WeekGrid){WeekGrid grid=(WeekGrid)pager.getChildAt(i);updateDays(grid.days,grid.shownWeek);}syncWeekDays();}
 private LinearLayout weekDays(int shownWeek){
  LinearLayout days=new LinearLayout(this);days.setTag("week-days-"+shownWeek);days.setPadding(0,dp(3),0,dp(10));
  for(int i=0;i<8;i++){TextView cell=text("",i==0?14:12,i==0?INK:MUTED);cell.setGravity(Gravity.CENTER);if(i==0)cell.setTypeface(null,Typeface.BOLD);else cell.setLineSpacing(dp(5),1);days.addView(cell,i==0?new LinearLayout.LayoutParams(dp(34),dp(42)):new LinearLayout.LayoutParams(0,dp(42),1));}
  updateDays(days,shownWeek);return days;
 }
 private void updateDays(LinearLayout days,int shownWeek){Calendar start=monday(),today=date();start.add(Calendar.DATE,(shownWeek-1)*7);((TextView)days.getChildAt(0)).setText((start.get(Calendar.MONTH)+1)+"\n月");String[] labels={"一","二","三","四","五","六","日"};for(int i=0;i<7;i++){TextView d=(TextView)days.getChildAt(i+1);d.setText(labels[i]+"\n"+(start.get(Calendar.MONTH)+1)+"/"+start.get(Calendar.DAY_OF_MONTH));boolean isToday=start.get(Calendar.YEAR)==today.get(Calendar.YEAR)&&start.get(Calendar.DAY_OF_YEAR)==today.get(Calendar.DAY_OF_YEAR);d.setTextColor(isToday?BLUE:MUTED);d.setTypeface(null,isToday?Typeface.BOLD:Typeface.NORMAL);String day=String.format(Locale.ROOT,"%04d-%02d-%02d",start.get(Calendar.YEAR),start.get(Calendar.MONTH)+1,start.get(Calendar.DAY_OF_MONTH));StringBuilder exams=new StringBuilder();try{org.json.JSONArray records=DeepSeekPlan.examList(getPreferences(0).getString("aiExams","[]"));for(int n=0;n<records.length();n++){org.json.JSONObject exam=records.getJSONObject(n);if(day.equals(exam.getString("date"))){if(exams.length()>0)exams.append("\n");exams.append(DeepSeekPlan.examText(exam));}}}catch(Exception ignored){}if(exams.length()>0){android.text.SpannableString caption=new android.text.SpannableString(labels[i]+"考\n"+(start.get(Calendar.MONTH)+1)+"/"+start.get(Calendar.DAY_OF_MONTH));caption.setSpan(new android.text.style.RelativeSizeSpan(.55f),1,2,android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);caption.setSpan(new android.text.style.SuperscriptSpan(),1,2,android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);caption.setSpan(new android.text.style.ForegroundColorSpan(PRIMARY),1,2,android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);d.setText(caption);d.setContentDescription(day+"有考试，点击查看");d.setOnClickListener(v->examDayDetails(day));}else{d.setOnClickListener(null);d.setContentDescription(null);}start.add(Calendar.DATE,1);}}
 private void syncWeekDays(){if(pager==null)return;for(int i=0;i<pager.getChildCount();i++)if(pager.getChildAt(i) instanceof WeekGrid){WeekGrid grid=(WeekGrid)pager.getChildAt(i);grid.days.setTranslationX(grid.getLeft()-pager.getScrollX());}}
 private List<org.json.JSONObject> weekExams(int shownWeek){
  List<org.json.JSONObject> result=new ArrayList<>();java.time.LocalDate start=java.time.LocalDate.parse(term).plusWeeks(shownWeek-1);try{org.json.JSONArray records=DeepSeekPlan.examList(getPreferences(0).getString("aiExams","[]"));for(int i=0;i<records.length();i++){org.json.JSONObject exam=records.getJSONObject(i);long day=java.time.temporal.ChronoUnit.DAYS.between(start,java.time.LocalDate.parse(exam.getString("date")));if(day>=0&&day<7)result.add(exam);}}catch(Exception ignored){}return result;
 }
 private int examDay(org.json.JSONObject exam,int shownWeek){return (int)java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.parse(term).plusWeeks(shownWeek-1),java.time.LocalDate.parse(exam.optString("date")));}
 private TimetableAxis timetableAxis(int shownWeek){List<TimetableAxis.Exam> exams=new ArrayList<>();for(org.json.JSONObject exam:weekExams(shownWeek))if(!exam.optString("time").trim().isEmpty())exams.add(new TimetableAxis.Exam(exam.optString("time"),exam.optString("end_time")));return new TimetableAxis(START_TIMES,END_TIMES,exams);}
 private int timetableGridHeight(int shownWeek){return Math.round(timetableAxis(shownWeek).height*dp(66));}
 private void examDayDetails(String day){try{ArrayList<org.json.JSONObject> matches=new ArrayList<>();org.json.JSONArray records=DeepSeekPlan.examList(getPreferences(0).getString("aiExams","[]"));for(int n=0;n<records.length();n++)if(day.equals(records.getJSONObject(n).getString("date")))matches.add(records.getJSONObject(n));if(matches.size()==1){examDetails(matches.get(0));return;}if(matches.isEmpty()){toast("考试记录已变化，请重新查看。");return;}String[] titles=new String[matches.size()];for(int n=0;n<titles.length;n++)titles[n]=DeepSeekPlan.examText(matches.get(n));AlertDialog dialog=new AlertDialog.Builder(this,R.style.SecondaryDialog).setTitle("考试安排").setItems(titles,(d,index)->examDetails(matches.get(index))).setNegativeButton("取消",null).create();dialog.show();styleDialog(dialog,false);}catch(Exception e){toast("考试记录无法读取。");}}
 private void examDetails(org.json.JSONObject exam){try{String timetable=Timetables.active(this),baseline=getPreferences(0).getString("aiExams","[]");AlertDialog dialog=new AlertDialog.Builder(this,R.style.SecondaryDialog).setTitle("考试安排").setMessage(DeepSeekPlan.examText(exam)).setPositiveButton("知道了",null).setNeutralButton("删除考试",(d,w)->confirmDeleteExam(exam,timetable,baseline)).create();dialog.show();styleDialog(dialog,false);dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(DANGER);}catch(org.json.JSONException invalid){toast("考试记录格式异常，请检查数据。");}}
 private void confirmDeleteExam(org.json.JSONObject exam,String timetable,String baseline){AlertDialog dialog=new AlertDialog.Builder(this,R.style.SecondaryDialog).setTitle("删除这条考试？").setMessage(exam.optString("name")+" · "+exam.optString("date")+"\n删除后无法撤销。").setNegativeButton("取消",null).setPositiveButton("删除",(d,w)->{if(!timetable.equals(Timetables.active(this))||!baseline.equals(getPreferences(0).getString("aiExams","[]"))){toast("考试记录已变化，请重新打开后删除。");return;}try{org.json.JSONArray records=DeepSeekPlan.examList(baseline);int index=-1;for(int n=0;n<records.length();n++)if(records.getJSONObject(n).toString().equals(exam.toString())){index=n;break;}if(index<0){toast("考试记录已不存在。");return;}records.remove(index);if(!getPreferences(0).edit().putString("aiExams",records.toString()).commit()){getPreferences(0).edit().putString("aiExams",baseline).commit();toast("删除失败，请重试。");return;}homePage=null;if("考试记录".equals(root.getTag()))aiExams();else if(primaryTab()==0)home();CourseWidget.updateAll(this);toast("考试已删除。");}catch(Exception e){toast("删除失败，原记录已保留。");}}).create();dialog.show();styleDialog(dialog,true);}

 private static final class GridEvent {final View view;final int day,top,height;int lane;GridEvent(View view,int day,int top,int height){this.view=view;this.day=day;this.top=top;this.height=height;}}
 private void buildExamCards(int shownWeek,TimetableAxis timeline,List<GridEvent> events){
  for(org.json.JSONObject exam:weekExams(shownWeek)){String time=exam.optString("time"),end=exam.optString("end_time"),room=exam.optString("room");if(time.trim().isEmpty())continue;LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setTag("exam-card");GradientDrawable background=bg(courseColor(exam.optString("name")),5);background.setStroke(dp(1.5f),PRIMARY);tile.setBackground(background);String details=time+(end.isEmpty()?"\n结束待定":"–"+end)+(room.isEmpty()?"":"\n@"+room);addCourseBadge(tile,"考",exam.optString("name"),"exam-badge");TextView badge=text(exam.optString("name")+"\n"+details,12,Color.WHITE);badge.setPadding(dp(3),dp(4),dp(2),dp(3));badge.setLineSpacing(dp(1),1);badge.setEllipsize(TextUtils.TruncateAt.END);badge.setTypeface(null,Typeface.BOLD);badge.setTag("exam-content");tile.addView(badge,new LinearLayout.LayoutParams(-1,0,1));tile.setContentDescription("考试，"+exam.optString("name")+"，"+exam.optString("date")+"，"+details);tile.setFocusable(true);focusOutline(tile);tile.setOnClickListener(v->examDetails(exam));float top=timeline.y(TimetableAxis.minute(time)),bottom=end.isEmpty()?top:timeline.y(TimetableAxis.minute(end));events.add(new GridEvent(tile,examDay(exam,shownWeek),Math.round(top*dp(66))+dp(2),Math.round(Math.max(1,bottom-top)*dp(66))-dp(4)));}
 }
 private void placeGridEvents(WeekGrid grid,List<GridEvent> events,int axis,float column){
  for(int day=0;day<7;day++){List<GridEvent> items=new ArrayList<>();for(GridEvent item:events)if(item.day==day)items.add(item);items.sort(java.util.Comparator.comparingInt((GridEvent item)->item.top).thenComparingInt(item->-item.height));int start=0;while(start<items.size()){int end=start+1,bottom=items.get(start).top+items.get(start).height;while(end<items.size()&&items.get(end).top<bottom){bottom=Math.max(bottom,items.get(end).top+items.get(end).height);end++;}List<Integer> lanes=new ArrayList<>();for(int i=start;i<end;i++){GridEvent item=items.get(i);int lane=0;while(lane<lanes.size()&&lanes.get(lane)>item.top)lane++;if(lane==lanes.size())lanes.add(0);lanes.set(lane,item.top+item.height);item.lane=lane;}float laneWidth=column/lanes.size();for(int i=start;i<end;i++){GridEvent item=items.get(i);FrameLayout.LayoutParams bounds=new FrameLayout.LayoutParams(Math.max(1,(int)laneWidth-dp(3)),item.height);bounds.leftMargin=axis+Math.round(day*column+item.lane*laneWidth)+dp(1);bounds.topMargin=item.top;grid.addView(item.view,bounds);}start=end;}}
 }
 private void buildGrid(WeekGrid grid,int shownWeek){grid.shownWeek=shownWeek;int week=shownWeek;grid.removeAllViews();List<Course> courses;try{courses=effectiveCourses();}catch(Exception e){toast("课表或调整记录格式异常，请检查课程与调整记录。");return;}int row=dp(66),axis=dp(34),width=grid.contentWidth>0?grid.contentWidth:grid.getWidth();float column=(width-axis)/7f;grid.contentWidth=width;TimetableAxis timeline=timetableAxis(shownWeek);List<GridEvent> events=new ArrayList<>();buildExamCards(shownWeek,timeline,events);
  String[] begin=START_TIMES,end=END_TIMES;
  for(int i=0;i<START_TIMES.length;i++){LinearLayout label=new LinearLayout(this);label.setOrientation(LinearLayout.VERTICAL);label.setGravity(Gravity.CENTER_HORIZONTAL);TextView n=text(""+(i+1),14,INK);n.setGravity(Gravity.CENTER_HORIZONTAL);label.addView(n);TextView time=text(begin[i]+"\n"+end[i],10,INK);time.setGravity(Gravity.CENTER);LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2);tp.topMargin=dp(4);label.addView(time,tp);FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(axis,row);lp.topMargin=Math.round(timeline.starts[i]*row)+dp(8);grid.addView(label,lp);}
  List<CourseDisplay.Group> groups=CourseDisplay.groups(courses,week,displayChoices);
  if(groups.isEmpty()&&weekExams(shownWeek).isEmpty()){LinearLayout empty=new LinearLayout(this);empty.setOrientation(LinearLayout.VERTICAL);empty.setGravity(Gravity.CENTER);empty.setPadding(dp(16),0,dp(16),0);TextView msg=text(data.trim().isEmpty()?"还没有导入课表":"行到水穷处，坐看云起时",data.trim().isEmpty()?17:21,MUTED);msg.setTag("timetable-empty-message");msg.setGravity(Gravity.CENTER);msg.setMinHeight(row);if(!data.trim().isEmpty()){msg.setTypeface(Typeface.create("serif",Typeface.NORMAL));msg.setLetterSpacing(.04f);msg.setLineSpacing(dp(4),1);}empty.addView(msg);if(data.trim().isEmpty()){Button link=action(empty,"登录学校账号",this::login);link.setTag("timetable-import");}FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(width-axis,-2);lp.leftMargin=axis;lp.topMargin=3*row;grid.addView(empty,lp);return;}
  for(CourseDisplay.Group group:groups){Course c=group.selected;String content=c.displayDetails();boolean conflict=group.courses.size()>1,adjusted=c.schoolAdjusted()||movedAdjustment(c,week)!=null;
   String cardText=cachedCardText.computeIfAbsent(c,key->SchoolParser.clean(c.name)+"\n@"+SchoolParser.clean(c.room)+(c.teacher.isEmpty()?"":"\n"+SchoolParser.clean(c.teacher))+(content.isEmpty()?"":"\n"+SchoolParser.clean(content)));
   TextView card=text(cardText,12,Color.WHITE);card.setTypeface(null,Typeface.BOLD);card.setLineSpacing(dp(1),1);card.setPadding(dp(3),dp(4),dp(2),dp(3));card.setEllipsize(TextUtils.TruncateAt.END);card.setMaxLines(Math.max(2,(c.end-c.start+1)*4-(conflict||adjusted?1:0)));
   View tile=card;
   if(conflict||adjusted){LinearLayout stack=new LinearLayout(this);stack.setOrientation(LinearLayout.VERTICAL);addCourseBadge(stack,conflict?(adjusted?"冲突 · 调":"冲突"):"调",c.name,conflict?"conflict-badge":"adjustment-badge");stack.addView(card,new LinearLayout.LayoutParams(-1,0,1));tile=stack;}
   GradientDrawable background=bg(courseColor(c.name),5);background.setStroke(dp(1.2f),COURSE_BORDER);tile.setBackground(background);focusOutline(tile);tile.setContentDescription(c.name+"，星期"+c.day+"，第"+c.start+"到"+c.end+"节，"+c.room+"，"+c.teacher+(content.isEmpty()?"":"，"+content)+(adjusted?"，调课":"")+(conflict?"，有冲突，共 "+group.courses.size()+" 门，点击更换显示课程":""));tile.setFocusable(true);tile.setTag("course-card");tile.setOnClickListener(v->{if(conflict)conflictPicker(group,week);else detail(c,week,()->home());});
   events.add(new GridEvent(tile,c.day-1,Math.round(timeline.starts[c.start-1]*row)+dp(2),Math.round((timeline.ends[c.end-1]-timeline.starts[c.start-1])*row)-dp(4)));
  }
  placeGridEvents(grid,events,axis,column);
 }
 private void conflictPicker(CourseDisplay.Group group,int shownWeek){conflictPicker(group,shownWeek,group.courses.indexOf(group.selected));}
 private void conflictPicker(CourseDisplay.Group group,int shownWeek,int selected){
  String baseline=data,adjustmentBaseline=adjustments;int[] choice={selected};Course c=group.courses.get(selected);
  detail(c,shownWeek,()->home());NavigationOverlay sheet=navigationOverlay;sheet.panel.setTag("冲突课程");
  Runnable reopen=()->conflictPicker(group,shownWeek,choice[0]),conflictDetails=()->conflictComparison(group,shownWeek,choice[0]);detailActions(c,shownWeek,reopen,conflictDetails);
  java.util.function.IntConsumer pick=index->{
   if(!data.equals(baseline)||!adjustments.equals(adjustmentBaseline)){toast("课表已更新，请重新选择");home();return;}
   Course next=group.courses.get(index);choice[0]=index;CourseDisplay.prefer(next,displayChoices);getPreferences(0).edit().putStringSet("displayChoices",new HashSet<>(displayChoices)).apply();CourseWidget.updateAll(this);
   if(pager!=null&&pager.isAttachedToWindow())for(int i=0;i<pager.getChildCount();i++)if(pager.getChildAt(i) instanceof WeekGrid){WeekGrid grid=(WeekGrid)pager.getChildAt(i);buildGrid(grid,grid.shownWeek);}
   sheet.body.removeAllViews();courseInformation(sheet.body,next,shownWeek);detailActions(next,shownWeek,reopen,conflictDetails);
  };
  TextView change=icon(R.drawable.ic_swap,"切换冲突课程；点击循环切换",()->pick.accept((choice[0]+1)%group.courses.size()));change.setTag("conflict-switch");
  LinearLayout bar=(LinearLayout)sheet.panel.findViewWithTag("sheet-action-button").getParent();LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(48),dp(48));lp.rightMargin=dp(8);bar.addView(change,bar.getChildCount()-1,lp);
 }
 private void conflictComparison(CourseDisplay.Group group,int shownWeek,int selected){
  LinearLayout body=bottomSheet("冲突课程对比","",()->conflictPicker(group,shownWeek,selected));
  for(int i=0;i<group.courses.size();i++){Course c=group.courses.get(i);LinearLayout card=panel(body);card.setPadding(dp(12),dp(8),dp(12),dp(8));card.setTag("conflict-comparison-"+i);TextView name=text(SchoolParser.clean(c.name),16,INK);name.setTypeface(null,Typeface.BOLD);card.addView(name);if(i==selected)card.addView(text("当前显示",12,MUTED));
   courseInformationRows(card,c,shownWeek);
  }
 }
 private int courseColor(String name){if(name.contains("数据结构")||name.contains("马克思"))return dark?0xff8f4d60:0xffd9809a;if(name.contains("模拟电子"))return dark?0xff9a5a4b:0xffdb947f;if(name.contains("电路"))return dark?0xff6b5ba8:0xffa597df;if(name.contains("英语"))return dark?0xff4f6f93:0xff7d9fc5;if(name.contains("复变"))return dark?0xff8a7146:0xffd7b984;if(name.contains("物理")||name.contains("体育")||name.contains("羽毛球"))return dark?0xff4f6da8:0xff87a6e6;return (dark?new int[]{0xff457a70,0xff7e5a79,0xff51758f,0xff6a6396}:new int[]{0xff87cabe,0xffc598ba,0xff84a9c8,0xffa29bce})[Math.floorMod(name.hashCode(),4)];}
 private void addCourseBadge(LinearLayout stack,String label,String name,String tag){TextView badge=text(label,10,courseBadgeColor(name));badge.setTag(tag);badge.setTypeface(null,Typeface.BOLD);badge.setPadding(dp(3),dp(1),dp(3),dp(1));badge.setBackground(bg(CARD,3));LinearLayout.LayoutParams bounds=new LinearLayout.LayoutParams(-2,-2);bounds.setMargins(dp(3),dp(4),dp(3),0);stack.addView(badge,bounds);}
 private int courseBadgeColor(String name){int color=courseColor(name);return dark?androidx.core.graphics.ColorUtils.blendARGB(color,Color.WHITE,.55f):color;}
 private void detail(Course c){detail(c,()->home());}
 private void detail(Course c,Runnable back){detail(c,week,back);}
 private void detail(Course c,int shownWeek,Runnable back){
  LinearLayout body=bottomSheet("课程详情","",back);showingDetail=true;
  courseInformation(body,c,shownWeek);detailActions(c,shownWeek,()->detail(c,shownWeek,back),null);
 }
 private void courseInformation(LinearLayout body,Course c,int shownWeek){
  TextView title=root.findViewWithTag("navigation-title");title.setText(SchoolParser.clean(c.name));title.setTextSize(18);title.setTypeface(null,Typeface.BOLD);
  courseInformationRows(body,c,shownWeek);
 }
 private void courseInformationRows(LinearLayout body,Course c,int shownWeek){
  String content=c.displayDetails();
  TextView when=text("第 "+shownWeek+" 周 · 周"+"一二三四五六日".charAt(c.day-1)+" · 第 "+c.start+"–"+c.end+" 节（"+START_TIMES[c.start-1]+"–"+END_TIMES[c.end-1]+"）",12,MUTED);when.setPadding(0,0,0,dp(8));body.addView(when);
  courseInfoRow(body,R.drawable.ic_calendar,"教学周次",CourseDisplay.prettyWeeks(c)+(c.active(shownWeek)?"":" · 本周无课"));
  courseInfoRow(body,R.drawable.ic_person,"任课老师",SchoolParser.clean(c.teacher));
  courseInfoRow(body,R.drawable.ic_location,"上课教室",SchoolParser.clean(c.room));
  if(!content.isEmpty())courseInfoRow(body,R.drawable.ic_notes,"实验内容",SchoolParser.clean(content));
 }
 private void courseInfoRow(LinearLayout body,int drawable,String label,String value){
  if(body.getTag()!=null&&body.getTag().toString().startsWith("conflict-comparison-")){sheetRow(body,label,value);return;}
  View divider=new View(this);divider.setBackgroundColor(DIVIDER);body.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));
  LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(10),0,dp(10));
  ImageView symbol=new ImageView(this);symbol.setImageResource(drawable);symbol.setImageTintList(android.content.res.ColorStateList.valueOf(INK));symbol.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);LinearLayout.LayoutParams iconBounds=new LinearLayout.LayoutParams(dp(22),dp(22));iconBounds.rightMargin=dp(12);symbol.setTag("course-info-icon");row.addView(symbol,iconBounds);
  LinearLayout copy=new LinearLayout(this);copy.setOrientation(LinearLayout.VERTICAL);copy.addView(text(label,11,MUTED));TextView valueView=text(value,14,INK);valueView.setPadding(0,dp(2),0,0);valueView.setContentDescription(label+"，"+value);copy.addView(valueView);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));body.addView(row);
 }
 private void detailActions(Course c,int shownWeek,Runnable reopen,Runnable conflictDetails){
  CourseAdjustment moved=movedAdjustment(c,shownWeek);Course source=moved==null?originalCourse(c,shownWeek):moved.original;int sourceWeek=moved==null?shownWeek:moved.week;
  root.findViewWithTag("sheet-action-button").setVisibility(View.VISIBLE);root.findViewWithTag("sheet-action-button").setOnClickListener(v->{NavigationOverlay actions=sheetMenu("课程操作");
  if(conflictDetails!=null)menuItem(actions,"冲突详情","conflict-course-details",conflictDetails);
  if(source!=null&&source.active(sourceWeek)){menuItem(actions,"本次调课","adjust-course",()->adjustmentForm(source,sourceWeek,false,reopen));menuItem(actions,"本次停课","cancel-course",()->adjustmentForm(source,sourceWeek,true,reopen));}
  menuItem(actions,"校对课程","edit-course",()->{List<Course> courses=Course.parse(data);int index=-1;for(int i=0;i<courses.size();i++)if(source!=null&&courses.get(i).encode().equals(source.encode())){index=i;break;}if(index<0){toast("课程已更新，请重新打开详情");home();return;}courseForm(source,index,reopen);});
  });
 }
 private void sheetRow(LinearLayout card,String label,String value){
  if(card.getChildCount()>0){View divider=new View(this);divider.setBackgroundColor(DIVIDER);card.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));}
  LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(7),0,dp(7));row.addView(text(label,12,MUTED),new LinearLayout.LayoutParams(dp(92),-2));TextView content=text(value.isEmpty()?"学校暂未提供":value,14,INK);row.addView(content,new LinearLayout.LayoutParams(0,-2,1));card.addView(row);
 }
 private void detailRow(LinearLayout card,String label,String value){if(card.getChildCount()>0){View divider=new View(this);divider.setBackgroundColor(DIVIDER);card.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));}LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(0,dp(9),0,dp(9));row.addView(text(label,12,MUTED));TextView content=text(value.isEmpty()?"学校暂未提供":value,16,INK);content.setTextIsSelectable(true);content.setLineSpacing(dp(2),1);content.setPadding(0,dp(3),0,0);row.addView(content);card.addView(row);}
 private LinearLayout settingsGroup(LinearLayout body,String title){
  if(!title.isEmpty()){TextView label=text(title,13,MUTED);if(Build.VERSION.SDK_INT>=28)label.setAccessibilityHeading(true);label.setPadding(dp(4),dp(4),0,dp(6));body.addView(label);}
  LinearLayout group=new LinearLayout(this);group.setOrientation(LinearLayout.VERTICAL);group.setTag("settings-group");group.setBackground(bg(CARD,18));group.setClipToOutline(true);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(12);body.addView(group,lp);return group;
 }
 private void closeSettings(){boolean parcel=settingsFromParcel,host=settingsFromParcelActivity,chat=settingsFromAi;settingsFromParcel=false;settingsFromParcelActivity=false;settingsFromAi=false;if(chat){pageBackwards=true;showAiTab();}else if(parcel){pageBackwards=true;parcels();}else if(tasksVisible)tasks();else home();if(host)startActivity(new Intent(this,ParcelActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));}
 private void menu(){
  if(returningPage&&settingsPage!=null){
   settingsPage.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);base(true,settingsPage);
   root.setBackgroundColor(SURFACE);Appearance.systemBars(this);
   pageBack=this::closeSettings;updateSystemBack();remoteStatus=root.findViewWithTag("update-status");refreshRemoteStatus();
   ((ScrollView)root.getChildAt(1)).scrollTo(0,settingsScrollY);return;
  }
  LinearLayout body=secondary("设置","",this::closeSettings);
  LinearLayout timetable=settingsGroup(body,"课表");
  option(timetable,R.drawable.ic_calendar,"课表管理","",this::timetableManagement).setTag("timetable-management");
  option(timetable,R.drawable.ic_calendar,"开学日期","",this::termPicker).setTag("settings-term");
  option(timetable,R.drawable.ic_calendar,"作息时间","",this::timesForm).setTag("settings-times");
  option(timetable,R.drawable.ic_person,"学校账号","",()->login(this::menu)).setTag("school-account");
  LinearLayout chat=settingsGroup(body,"AI 助手");
  option(chat,R.drawable.ic_settings,"API 配置","",this::aiSettings).setTag("settings-api");
  option(chat,R.drawable.ic_notes,"项目记忆","",this::aiMemory).setTag("settings-ai-memory");
  option(chat,R.drawable.ic_info,"数据与说明","",this::aiDataInfo).setTag("settings-ai-info");
  LinearLayout appearance=settingsGroup(body,"外观与桌面");
  option(appearance,R.drawable.ic_palette,"外观","",this::appearanceSettings).setTag("dark-mode-setting");
  option(appearance,R.drawable.ic_widget,"桌面小组件","",this::widgetAddPage).setTag("add-home-widget");
  LinearLayout privacy=settingsGroup(body,"隐私与数据");
  option(privacy,R.drawable.ic_settings,"权限管理","",this::openPermissionSettings).setTag("permission-settings");
  option(privacy,R.drawable.ic_notes,"数据管理","",this::dataManagement).setTag("data-management");
  LinearLayout about=settingsGroup(body,"关于");
  remoteStatus=null;option(about,R.drawable.ic_info,"关于软件","",this::about).setTag("about-app");
  settingsPage=root;
 }
 private void timetableManagement(){
  LinearLayout body=secondary("课表管理","",this::menu);LinearLayout courses=settingsGroup(body,"");
  option(courses,R.drawable.ic_calendar,"课程管理","",()->edit(this::timetableManagement)).setTag("manage-courses");
  LinearLayout list=settingsGroup(body,"切换课表");String current=Timetables.active(this);
  for(var entry:Timetables.names(this).entrySet()){String id=entry.getKey();boolean selected=id.equals(current);LinearLayout row=option(list,R.drawable.ic_calendar,entry.getValue(),(selected?"当前课表 · ":"")+Timetables.sourceLabel(this,id),()->{if(busy){toast("请等待同步结束");return;}if(selected)return;try{Timetables.select(this,id);reloadTimetable(this::timetableManagement);}catch(Exception e){toast(e.getMessage());}});row.setTag("timetable-"+id);row.setSelected(selected);TextView mark=(TextView)row.getChildAt(row.getChildCount()-1);mark.setText("");if(selected){secondaryUi.styleIcon(mark,R.drawable.ic_check);mark.setBackground(null);}row.setTooltipText("长按修改课表名称");row.setOnLongClickListener(v->{renameTimetable(id,this::timetableManagement);return true;});}
  option(list,R.drawable.ic_add,"新增课表","",()->newTimetable(this::timetableManagement)).setTag("add-timetable");
 }
 private void dataManagement(){
  LinearLayout body=secondary("数据管理","",this::menu);
  LinearLayout timetableData=settingsGroup(body,"课表");option(timetableData,R.drawable.ic_notes,"清空本课表考试记录","删除手动保存的考试记录",this::clearAiExams).setTag("clear-ai-exams");
  option(timetableData,R.drawable.ic_calendar,"清空当前课表","清空课程、备注和单次调整，退出学校账号",()->confirmClear()).setTag("clear-timetable");
  LinearLayout browser=settingsGroup(body,"网页导入");option(browser,R.drawable.ic_notes,"清理网页数据","清除所有网页导入站点的登录状态、网站存储和缓存",this::clearWebData).setTag("clear-web-data");
  LinearLayout tasks=settingsGroup(body,"任务");
  option(tasks,R.drawable.ic_tasks,"任务恢复","恢复已删除或本机标记完成的任务",()->{prepareTasks();taskPage.history();}).setTag("task-deletion-history");
  option(tasks,R.drawable.ic_tasks,"删除全部任务数据","删除手动任务、所有学习通缓存和删除历史，退出学习通账号",()->{prepareTasks();taskPage.clearData();}).setTag("clear-tasks");
  LinearLayout parcels=settingsGroup(body,"快递");option(parcels,R.drawable.ic_parcel,"快递恢复","",()->{prepareParcels();parcelPage.history();}).setTag("parcel-deletion-history");
  option(parcels,R.drawable.ic_parcel,"删除全部快递数据","删除记录和设置，关闭通知识别与提醒",()->{prepareParcels();parcelPage.clearData();}).setTag("clear-parcels");
 }
 private void clearWebData(){
  AlertDialog dialog=new AlertDialog.Builder(this,R.style.SecondaryDialog).setTitle("清理网页数据？").setMessage("将清除所有网页导入站点的登录状态、网站存储和缓存，下次使用需要重新登录。已导入的课表、单独保存的学校账号和学习通账号会保留。").setNegativeButton("取消",null).setPositiveButton("清理",null).create();dialog.show();styleDialog(dialog,true);
  dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);dialog.setCancelable(false);
   try{android.webkit.WebStorage.getInstance().deleteAllData();android.webkit.WebViewDatabase.getInstance(this).clearHttpAuthUsernamePassword();android.webkit.WebView browser=new android.webkit.WebView(this);try{browser.clearCache(true);browser.clearFormData();}finally{browser.destroy();}android.webkit.CookieManager.getInstance().removeAllCookies(removed->{android.webkit.CookieManager.getInstance().flush();if(!isDestroyed()){dialog.dismiss();toast("网页数据已清理，下次使用需要重新登录。");}});}catch(Exception e){dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);dialog.setCancelable(true);toast("网页数据未能全部清理，请重试。");}
  });
 }
 private void timetableSettings(){
  if(navigationOverlay!=null)navigationOverlay.close(false);if(pageBack!=null)home();
  View anchor=root.findViewWithTag("timetable-settings");NavigationOverlay menu=new NavigationOverlay(true,"课表设置",anchor);
  menu.menuWidth=264;TextView current=text("",14,INK);current.setText(timetableCaption(Timetables.active(this)));current.setTypeface(null,Typeface.NORMAL);current.setPadding(dp(16),dp(8),dp(16),dp(8));current.setMinHeight(dp(48));current.setTag("timetable-source-summary");current.setContentDescription("当前课表，"+current.getText());menu.body.addView(current);secondaryUi.popupDivider(menu.body);
  menuItem(menu,"切换课表","timetable-switch",this::timetablePicker).setEnabled(!busy);
  menuItem(menu,"添加课程","timetable-add-course",()->courseForm(null,-1,this::home));
  menuItem(menu,"课程调整恢复","adjustment-manager",this::adjustmentManager);
  menuItem(menu,"学校登录","timetable-login",this::login);
  if(getPreferences(0).getBoolean("schoolImported",notes.startsWith("已从学校同步")||notes.startsWith("已从网页导入")))menuItem(menu,"同步课表","refresh-timetable",this::refreshTimetable).setEnabled(!busy);
 }
 private void clearAiExams(){
  String timetable=Timetables.active(this),baseline=getPreferences(0).getString("aiExams","[]");AlertDialog dialog=new AlertDialog.Builder(this,R.style.SecondaryDialog).setTitle("清空考试记录？").setMessage("将删除当前课表中手动保存的所有考试记录。课程不会被删除。").setNegativeButton("取消",null).setPositiveButton("清空",(d,w)->{if(!timetable.equals(Timetables.active(this))||!baseline.equals(getPreferences(0).getString("aiExams","[]"))){toast("考试记录已变化，请重试。");return;}if(!getPreferences(0).edit().remove("aiExams").commit()){getPreferences(0).edit().putString("aiExams",baseline).commit();toast("清空失败，请重试。");return;}homePage=null;toast("考试记录已清空。");}).create();dialog.show();styleDialog(dialog,true);
 }
 private void aiSettings(){
  if("设置".equals(root.getTag()))aiSettingsFromMenu=true;else if(root==aiPage)aiSettingsFromMenu=false;
  if(aiSending){toast("请先停止当前回复");return;}
  LinearLayout body=secondary("API 配置","选择本次聊天使用的供应商。支持 DeepSeek 和兼容 OpenAI Chat Completions 的服务；对话、附件和已授权的信息会发送到所选 API 地址。",aiSettingsFromMenu?this::menu:this::showAiTab);
  try{
   AiProviders providers=new AiProviders(this);AiProviders.Config selected=providers.active();LinearLayout list=settingsGroup(body,"供应商");
   for(AiProviders.Config config:providers.all()){
    boolean active=selected!=null&&selected.id.equals(config.id);LinearLayout row=option(list,R.drawable.ic_settings,config.name,config.model+(active?" · 正在使用":""),()->{try{new AiProviders(this).select(config.id);skipPageMotion=true;try{aiSettings();}finally{skipPageMotion=false;}}catch(Exception e){toast("未能切换配置，请重试。");}});row.setTag("ai-provider-"+config.id);row.setSelected(active);
    TextView edit=(TextView)row.getChildAt(row.getChildCount()-1);edit.setText("");secondaryUi.styleIcon(edit,R.drawable.ic_calendar_edit);edit.setLayoutParams(new LinearLayout.LayoutParams(dp(48),dp(48)));edit.setTag("ai-provider-edit-"+config.id);edit.setContentDescription("编辑 "+config.name);edit.setFocusable(true);edit.setOnClickListener(v->aiEditProvider(config));
   }
   if(providers.all().isEmpty())body.addView(text("尚未添加 API 配置",15,MUTED));
   action(body,"添加供应商",this::aiProviderChoices).setTag("ai-provider-add");
  }catch(Exception e){body.addView(text("配置暂时无法解密，原数据已保留。请重试或检查系统安全设置。",14,DANGER));}
 }
 private void aiProviderChoices(){LinearLayout body=secondary("添加供应商","",this::aiSettings),group=settingsGroup(body,"");for(AiProviders.Config preset:AiProviders.presets())option(group,R.drawable.ic_settings,preset.name,preset.model,()->aiEditProvider(null,preset)).setTag(preset.id);option(settingsGroup(body,""),R.drawable.ic_add,"自定义配置","自行填写地址与模型",()->aiEditProvider(null,AiProviders.custom())).setTag("ai-provider-custom");}
 private void aiEditProvider(AiProviders.Config existing){aiEditProvider(existing,null);}
 private void aiEditProvider(AiProviders.Config existing,AiProviders.Config template){
  if(aiSending){toast("请先停止当前回复");return;}AiProviders.Config initial=existing==null?(template==null?AiProviders.defaultConfig(""):template):existing;String id=existing==null?UUID.randomUUID().toString():existing.id;
  LinearLayout body=secondary(existing==null?"添加供应商":"编辑供应商","密钥加密保存在本机。填写供应商提供的 HTTPS 基础地址与支持工具调用的模型名称。",existing==null?this::aiProviderChoices:this::aiSettings),card=panel(body);
  EditText name=courseField(card,"名称",initial.name,"供应商名称","ai-provider-name");
  Spinner protocol=courseChoice(card,"API 协议",new String[]{"DeepSeek","OpenAI 兼容"},initial.deepseek?0:1,"ai-provider-protocol");
  EditText base=courseField(card,"API 地址",initial.base,"https://api.example.com/v1","ai-provider-base"),model=courseField(card,"模型",initial.model,"供应商的模型 ID","ai-provider-model");
  EditText key=courseField(card,"API 密钥",initial.key,"API 密钥","ai-key");key.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);key.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());key.setImeOptions(key.getImeOptions()|android.view.inputmethod.EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING);key.setSaveEnabled(false);key.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
  CheckBox reveal=new CheckBox(this);reveal.setText("显示密钥");reveal.setTextColor(INK);reveal.setMinHeight(dp(48));reveal.setOnCheckedChangeListener((button,checked)->{int start=key.getSelectionStart(),end=key.getSelectionEnd();key.setTransformationMethod(checked?null:android.text.method.PasswordTransformationMethod.getInstance());key.setSelection(Math.max(0,start),Math.max(0,end));});card.addView(reveal);
  CheckBox reasoning=new CheckBox(this);reasoning.setText("模型支持推理强度");reasoning.setTextColor(INK);reasoning.setChecked(initial.reasoning);reasoning.setMinHeight(dp(48));reasoning.setTag("ai-provider-reasoning");card.addView(reasoning);
  protocol.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> parent){}public void onItemSelected(android.widget.AdapterView<?> parent,View view,int position,long value){if(existing==null&&position==1&&base.getText().toString().equals("https://api.deepseek.com")){name.setText("OpenAI 兼容");base.setText("https://api.openai.com/v1");model.setText("");reasoning.setChecked(false);}}});
  TextView error=text("",13,DANGER);error.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);body.addView(error);LinearLayout actions=configurationActions(body);
  action(actions,"保存",()->{try{new AiProviders(this).save(new AiProviders.Config(id,name.getText().toString(),base.getText().toString(),model.getText().toString(),key.getText().toString(),protocol.getSelectedItemPosition()==0,reasoning.isChecked()));key.getText().clear();pageBack=this::aiSettings;backPage();}catch(Exception e){error.setText(e instanceof IllegalArgumentException?e.getMessage():"配置未保存，请检查输入或稍后重试。");}}).setTag("ai-key-save");
  if(existing!=null){Button delete=action(actions,"删除",()->{AlertDialog dialog=new AlertDialog.Builder(this,R.style.SecondaryDialog).setTitle("删除此供应商？").setMessage("将删除本机保存的这份配置和密钥，聊天记录会保留。").setNegativeButton("取消",null).setPositiveButton("删除",(d,w)->{try{new AiProviders(this).delete(id);key.getText().clear();backPage();}catch(Exception e){error.setText("删除未完成，请重试。");}}).create();dialog.show();styleDialog(dialog,true);});delete.setTextColor(DANGER);delete.setBackgroundTintList(android.content.res.ColorStateList.valueOf(SOFT));delete.setTag("ai-key-delete");}
 }
 private void aiPhoneSettings(){
  if(aiSending){toast("请先停止当前回复");return;}
  LinearLayout body=secondary("手机信息与权限","当前时间和时区自动更新。启用后的日历、当前通知和最近共享画面，会在发送消息时提供给所选供应商。屏幕共享只在本机暂存最近画面，可在此处或系统通知中随时停止。受系统保护的画面可能无法读取。",this::openPermissionSettings);
  body.addView(text(java.time.ZonedDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss VV")),16,INK));android.content.SharedPreferences preferences=AiPhoneContext.settings(this);boolean calendar=preferences.getBoolean("calendar",false),notifications=preferences.getBoolean("notifications",false);
  action(body,calendar?"不再向 AI 提供日历":"允许 AI 读取系统日历",()->{preferences.edit().putBoolean("calendar",!calendar).apply();if(!calendar&&checkSelfPermission(android.Manifest.permission.READ_CALENDAR)!=android.content.pm.PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{android.Manifest.permission.READ_CALENDAR},910);else aiPhoneSettings();}).setTag("ai-calendar-permission");
  action(body,notifications?"不再向 AI 提供通知":"允许 AI 读取当前通知",()->{preferences.edit().putBoolean("notifications",!notifications).apply();if(!notifications)try{startActivity(new Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));}catch(RuntimeException e){toast("无法打开通知访问设置，请从系统设置中开启。");}aiPhoneSettings();}).setTag("ai-notification-permission");
  action(body,AiScreenService.active?"停止屏幕共享":"开启屏幕共享",()->{if(AiScreenService.active){stopService(new Intent(this,AiScreenService.class));getWindow().getDecorView().postDelayed(()->{if("手机信息与权限".equals(root.getTag()))aiPhoneSettings();},300);}else startActivityForResult(getSystemService(android.media.projection.MediaProjectionManager.class).createScreenCaptureIntent(),911);}).setTag("ai-screen-permission");
  body.addView(text("日历范围：昨天起未来 30 天，最多 100 条；通知：当前最多 20 条，识别到的验证码正文不提供。共享画面带捕获时间，不代表实时画面或完整手机访问。",13,MUTED));
 }
 private void aiRequestPhoneAccess(String source){
  try{android.content.SharedPreferences prefs=AiPhoneContext.settings(this);boolean already=source.equals("calendar")?checkSelfPermission(android.Manifest.permission.READ_CALENDAR)==android.content.pm.PackageManager.PERMISSION_GRANTED:source.equals("notifications")&&AiNotificationListener.ready();if(!source.equals("screen")&&already&&!prefs.getBoolean(source,false)){AlertDialog dialog=new AlertDialog.Builder(this,R.style.SecondaryDialog).setTitle("允许 AI 使用"+(source.equals("calendar")?"日历":"当前通知")+"？").setMessage("内容会随本次及后续提问提供给所选供应商，可随时在权限管理中停用。").setNegativeButton("取消",(d,w)->{aiPendingPhoneQuestion=null;aiPendingPhoneSource=null;}).setPositiveButton("允许",(d,w)->{prefs.edit().putBoolean(source,true).apply();aiRequestPhoneAccess(source);}).create();dialog.show();styleDialog(dialog,false);return;}
   if(source.equals("calendar")){prefs.edit().putBoolean("calendar",true).apply();if(checkSelfPermission(android.Manifest.permission.READ_CALENDAR)!=android.content.pm.PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{android.Manifest.permission.READ_CALENDAR},910);else aiResumePhoneQuestion();}
   else if(source.equals("notifications")){prefs.edit().putBoolean("notifications",true).apply();if(AiNotificationListener.ready())aiResumePhoneQuestion();else{Intent permission=new Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);if(Build.VERSION.SDK_INT>=30)permission=new Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).putExtra(android.provider.Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,new ComponentName(this,AiNotificationListener.class).flattenToString());try{startActivity(permission);}catch(ActivityNotFoundException e){startActivity(new Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));}}}
   else if(source.equals("screen")){if(!AiScreenService.active)startActivityForResult(getSystemService(android.media.projection.MediaProjectionManager.class).createScreenCaptureIntent(),911);else{toast("打开要看的其他 App，再返回 AI 继续提问。");aiResumePhoneQuestion();}}
   else throw new IllegalArgumentException();getWindow().getDecorView().postDelayed(this::aiResumePhoneQuestion,300);
  }catch(RuntimeException e){aiPendingPhoneQuestion=null;aiPendingPhoneSource=null;toast("无法打开系统授权，请从系统权限设置中开启。");}
 }
 private void aiResumePhoneQuestion(){
  if(isDestroyed()||!remoteResumed||!hasWindowFocus()||aiSending||aiPendingPhoneQuestion==null||root!=aiPage)return;if(aiPage!=aiPendingPhonePage){aiPendingPhoneQuestion=null;aiPendingPhoneSource=null;return;}if(System.currentTimeMillis()-aiPendingPhoneAt>600000){aiPendingPhoneQuestion=null;aiPendingPhoneSource=null;toast("原问题已过期，请重新提问。");return;}boolean ready=false;try{if("calendar".equals(aiPendingPhoneSource))ready=AiPhoneContext.settings(this).getBoolean("calendar",false)&&checkSelfPermission(android.Manifest.permission.READ_CALENDAR)==android.content.pm.PackageManager.PERMISSION_GRANTED;else if("notifications".equals(aiPendingPhoneSource))ready=AiPhoneContext.settings(this).getBoolean("notifications",false)&&AiNotificationListener.ready();else if("screen".equals(aiPendingPhoneSource))ready=AiScreenService.frame()!=null;}catch(RuntimeException ignored){}
  if(!ready){if("notifications".equals(aiPendingPhoneSource)&&aiPhoneWaits++<10)getWindow().getDecorView().postDelayed(this::aiResumePhoneQuestion,500);return;}String question=aiPendingPhoneQuestion;aiPendingPhoneQuestion=null;aiPendingPhoneSource=null;EditText input=aiPage.findViewWithTag("ai-message");if(input.length()>0){toast("权限已开启，可以继续提问。");return;}input.setText(question);aiPage.findViewWithTag("ai-send").performClick();
 }
 private AiChats savedAiStore()throws Exception{if(aiStore==null)aiStore=new AiChats(this);return aiStore;}
 private AiChats aiStore()throws Exception{return aiTemporaryStore==null?savedAiStore():aiTemporaryStore;}
 private void aiTemporaryChat(){if(aiSending){toast("请先停止当前回复");return;}try{aiTemporaryStore=aiTemporaryStore==null?new AiChats(this,true):null;aiChatTimetable=null;aiAssistant();}catch(Exception e){toast("临时聊天暂时无法打开。");}}
 private void loadAiChat()throws Exception{org.json.JSONArray messages=aiStore().chat().getJSONArray("messages");while(aiChat.length()>0)aiChat.remove(0);for(int i=0;i<messages.length();i++)aiChat.put(messages.getJSONObject(i));}
 private void rememberAi(String role,String value){try{aiChat.put(new org.json.JSONObject().put("role",role).put("content",value));aiStore().messages(aiChat);}catch(Exception e){toast("聊天记录未能保存，请重试。");}}
 private void rememberAiResponse(String input,String content,DeepSeekPlan plan){if(aiChat.length()==0||!aiChat.optJSONObject(aiChat.length()-1).optString("role").equals("user")||!aiChat.optJSONObject(aiChat.length()-1).optString("content").equals(input))rememberAi("user",input);rememberAi("assistant",plan==null?content:"建议操作（尚未执行）："+plan.description+"\n"+content);}
 private void aiNewChat(){if(aiSending){toast("请先停止当前回复");return;}try{savedAiStore().newChat();aiTemporaryStore=null;loadAiChat();aiAssistant();}catch(Exception e){toast("新聊天未能创建，请重试。");}}
 private void aiHistory(){openSideMenu();}
 private View aiDrawerContent(){
  LinearLayout section=new LinearLayout(this);section.setOrientation(LinearLayout.VERTICAL);section.setPadding(dp(8),dp(12),dp(8),0);
  View divider=new View(this);divider.setBackgroundColor(SOFT);section.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));
  TextView heading=text("聊天",14,MUTED);heading.setPadding(dp(8),dp(16),0,dp(8));section.addView(heading);
  sideNavigation.setChatTools(this::aiNewChat);
  LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);list.setTag("ai-history-list");section.addView(list);
  Runnable render=()->{list.removeAllViews();try{org.json.JSONArray projects=savedAiStore().projects();String current=savedAiStore().chat().getString("id");for(int i=0;i<projects.length();i++){org.json.JSONObject project=projects.getJSONObject(i);String pid=project.getString("id"),name=project.getString("name");org.json.JSONArray chats=project.getJSONArray("chats");LinearLayout rows=new LinearLayout(this);rows.setOrientation(LinearLayout.VERTICAL);for(int j=chats.length()-1;j>=0;j--){org.json.JSONObject chat=chats.getJSONObject(j);String cid=chat.getString("id"),caption=chat.getString("title");TextView row=text(caption,15,INK);row.setSingleLine(true);row.setEllipsize(TextUtils.TruncateAt.END);row.setMinHeight(dp(52));row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(12),dp(8),dp(8),dp(8));row.setBackground(cid.equals(current)?bg(SOFT,12):null);row.setOnClickListener(v->{if(aiSending){toast("可以先停止当前回复，再切换聊天");return;}try{savedAiStore().select(pid,cid);aiTemporaryStore=null;sideNavigation.route(()->{loadAiChatSafely();aiAssistant();});}catch(Exception e){toast("聊天暂时无法打开。");}});row.setOnLongClickListener(v->{aiDeleteChat(pid,cid,caption);return true;});rows.addView(row);}if(rows.getChildCount()>0){TextView title=text(name,13,MUTED);title.setPadding(dp(8),dp(16),0,dp(4));list.addView(title);list.addView(rows);}}}catch(Exception e){list.addView(text("聊天记录暂时无法读取。",14,MUTED));}};
  render.run();return section;
 }
 private void aiDeleteChat(String projectId,String chatId,String caption){
  if(aiSending){toast("请先停止当前回复");return;}AlertDialog dialog=new AlertDialog.Builder(this,R.style.SecondaryDialog).setTitle("删除聊天？").setMessage("删除“"+caption+"”及其消息，项目记忆会保留。此操作无法撤销。").setNegativeButton("取消",null).setPositiveButton("删除",(d,w)->{if(aiSending){toast("请先停止当前回复");return;}try{boolean active=aiTemporaryStore==null&&savedAiStore().chat().getString("id").equals(chatId);savedAiStore().deleteChat(projectId,chatId);if(active)sideNavigation.route(()->{loadAiChatSafely();aiAssistant();});else sideNavigation.setExtra(aiDrawerContent());}catch(Exception e){toast("聊天未能删除，原数据已保留。");}}).create();dialog.show();styleDialog(dialog,true);
 }
 private void aiProjects(){
  if(aiSending){toast("请先停止当前回复");return;}
  LinearLayout body=secondary("项目","不同项目分别保存聊天与项目记忆。",this::aiAssistant);
  action(body,"新建项目",this::aiCreateProject).setTag("ai-history-new-project");
  try{org.json.JSONArray projects=aiStore().projects();for(int i=0;i<projects.length();i++){org.json.JSONObject project=projects.getJSONObject(i);String id=project.getString("id");option(body,R.drawable.ic_notes,project.getString("name"),project.getJSONArray("chats").length()+" 个聊天",()->{try{savedAiStore().select(id,null);aiTemporaryStore=null;loadAiChat();aiAssistant();}catch(Exception e){toast("项目暂时无法打开。");}});}}catch(Exception e){body.addView(text("项目暂时无法读取。",14,MUTED));}
 }
 private void aiCreateProject(){
  LinearLayout body=secondary("新建项目","将同一主题的聊天放在一起，共享项目记忆。",this::aiProjects);
  EditText name=field("项目名称",false);name.setTag("ai-project-name");name.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(40)});body.addView(name);
  Button create=action(body,"创建项目",()->{if(aiSending){toast("请先停止当前回复");return;}try{savedAiStore().newProject(name.getText().toString());aiTemporaryStore=null;loadAiChat();aiAssistant();}catch(Exception e){name.setError("未能创建，请检查名称或稍后重试。");}});create.setTag("ai-project-create");create.setEnabled(false);
  name.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence t,int st,int c,int a){}public void onTextChanged(CharSequence t,int st,int b,int c){create.setEnabled(!t.toString().trim().isEmpty());}public void afterTextChanged(android.text.Editable t){}});
 }
 private void loadAiChatSafely(){try{loadAiChat();}catch(Exception e){toast("聊天记录无法读取，原数据已保留。");}}
 private void aiScroll(ScrollView scroll){View content=scroll.getChildAt(0);if(content!=null&&content.getHeight()-scroll.getHeight()-scroll.getScrollY()<dp(160))scroll.post(()->scroll.fullScroll(View.FOCUS_DOWN));}
 private void aiMemory(){if("设置".equals(root.getTag()))aiMemoryFromMenu=true;else if(root==aiPage)aiMemoryFromMenu=false;if(aiSending){toast("请等待本次回复结束");return;}try{AiChats store=aiStore();LinearLayout body=secondary("项目记忆","该项目的聊天会使用这些记忆。",aiMemoryFromMenu?this::menu:this::showAiTab);EditText memory=field("输入项目记忆",false);memory.setTag("ai-memory-editor");memory.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);memory.setSingleLine(false);memory.setGravity(Gravity.TOP|Gravity.START);memory.setPadding(dp(16),dp(16),dp(16),dp(16));memory.setLineSpacing(dp(4),1);memory.setMinLines(8);memory.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(8000)});memory.setText(store.project().getString("memory"));body.addView(memory);LinearLayout actions=secondaryUi.actionRow(body);body.removeView(actions);actions.setPadding(dp(16),dp(8),dp(16),dp(16));root.addView(actions,new LinearLayout.LayoutParams(-1,-2));if(store.project().has("memory_previous"))action(actions,"撤销上次记忆修改",()->{try{store.undoMemory();aiMemory();}catch(Exception e){toast("记忆未能撤销，请重试。");}});action(actions,"保存",()->{try{store.memory(memory.getText().toString());backPage();}catch(Exception e){toast("记忆未能保存，内容最多8000字。");}});}catch(Exception e){toast("项目记忆无法读取。");}}
 private void aiStrengthState(TextView button,int level){AiStrengthIcon icon=new AiStrengthIcon(level,PRIMARY);icon.setBounds(0,0,dp(24),dp(24));button.setCompoundDrawables(icon,null,null,null);button.setContentDescription("推理强度："+new String[]{"即时","轻度","深入","最高"}[Math.max(0,Math.min(3,level))]);}
 private void aiStrength(TextView label){
  if(aiStrengthDialog!=null)aiStrengthDialog.dismiss();Dialog dialog=new Dialog(this,R.style.SecondaryDialog);aiStrengthDialog=dialog;dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);FrameLayout mask=new FrameLayout(this);mask.setBackgroundColor(dark?0x99000000:0xC0FFFFFF);mask.setOnClickListener(v->dialog.dismiss());LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(24),dp(16),dp(24),dp(16));body.setClickable(true);mask.addView(body,new FrameLayout.LayoutParams(-1,-2,Gravity.CENTER_VERTICAL));String[] levels={"即时","轻度","深入","最高"};TextView title=text("",20,INK);title.setGravity(Gravity.CENTER);title.setTypeface(null,Typeface.BOLD);title.setPadding(0,0,0,dp(20));body.addView(title);
  int selected;try{selected=aiStore().chat().optInt("strength",0);}catch(Exception e){toast("强度无法读取。");dialog.dismiss();return;}LinearLayout capsule=new LinearLayout(this);capsule.setGravity(Gravity.CENTER_VERTICAL);capsule.setBackground(bg(CARD,36));body.addView(capsule,new LinearLayout.LayoutParams(-1,dp(72)));
  java.util.function.IntConsumer describe=level->{android.text.SpannableString value=new android.text.SpannableString(levels[level]+"推理强度");value.setSpan(new android.text.style.ForegroundColorSpan(PRIMARY),0,levels[level].length(),android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);title.setText(value);};describe.accept(selected);
  AiStrengthSlider slider=new AiStrengthSlider(this,selected,PRIMARY,CARD,MUTED,level->{try{aiStore().strength(level);aiStrengthState(label,level);describe.accept(level);return true;}catch(Exception e){toast("强度未能保存。");return false;}});slider.setTag("ai-strength-slider");slider.setContentDescription("选择思考强度，当前"+levels[selected]);capsule.addView(slider,new LinearLayout.LayoutParams(-1,dp(64)));
  dialog.setContentView(mask);dialog.setOnDismissListener(d->{if(aiStrengthDialog==dialog)aiStrengthDialog=null;});Window window=dialog.getWindow();window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);window.setWindowAnimations(android.animation.ValueAnimator.areAnimatorsEnabled()?R.style.CenterPopupMotion:0);dialog.show();window.setLayout(-1,-1);window.setGravity(Gravity.TOP);body.setVisibility(View.INVISIBLE);mask.post(()->{int[] anchor=new int[2],origin=new int[2];label.getLocationOnScreen(anchor);mask.getLocationOnScreen(origin);FrameLayout.LayoutParams position=(FrameLayout.LayoutParams)body.getLayoutParams();position.gravity=Gravity.TOP;position.topMargin=Math.max(dp(16),Math.min(mask.getHeight()-body.getHeight()-dp(16),anchor[1]+label.getHeight()/2-origin[1]-body.getHeight()+body.getPaddingBottom()+dp(36)));body.setLayoutParams(position);body.setVisibility(View.VISIBLE);});
 }
 private void aiWelcome(LinearLayout history){LinearLayout welcome=new LinearLayout(this);welcome.setTag("ai-welcome");welcome.setOrientation(LinearLayout.VERTICAL);welcome.setGravity(Gravity.CENTER);welcome.setPadding(dp(16),dp(96),dp(16),dp(32));TextView title=text("有什么可以帮你？",24,INK);title.setGravity(Gravity.CENTER);title.setTypeface(null,Typeface.BOLD);welcome.addView(title);TextView hint=text(aiTemporaryStore==null?"可以直接聊天，也可以让我管理你的安排。":"临时聊天不保留历史，关闭后清除。",14,MUTED);hint.setGravity(Gravity.CENTER);hint.setPadding(0,dp(16),0,dp(24));welcome.addView(hint);TextView privacy=text("API · 数据与费用说明",12,MUTED);privacy.setGravity(Gravity.CENTER);privacy.setMinHeight(dp(48));privacy.setOnClickListener(v->{AlertDialog dialog=new AlertDialog.Builder(this,R.style.SecondaryDialog).setTitle("数据与费用").setMessage(AI_DATA_INFO).setPositiveButton("知道了",null).create();dialog.show();styleDialog(dialog,false);});welcome.addView(privacy);history.addView(welcome,new LinearLayout.LayoutParams(-1,-2));}
 private TextView aiMessage(LinearLayout history,String content,boolean user){TextView line=text("",16,INK);line.setTag(user?"ai-user-message":"ai-assistant-message");line.setText(user?content:AiText.format(content));line.setLineSpacing(dp(6),1);line.setPadding(user?dp(16):0,dp(12),user?dp(16):0,dp(12));line.setTextIsSelectable(true);if(user)line.setBackground(bg(AQUA,24));LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(user?-2:-1,-2);size.gravity=user?Gravity.END:Gravity.START;size.topMargin=dp(user?20:12);if(user)line.setMaxWidth(Math.round(getResources().getDisplayMetrics().widthPixels*.8f));history.addView(line,size);return line;}
 private void aiComposerExpanded(boolean expanded){
  if(aiPage!=null){AiComposer composer=aiPage.findViewWithTag("ai-composer");if(composer!=null)composer.expanded(expanded);}
 }
 private void aiChatChrome(LinearLayout page,ScrollView scroll,LinearLayout composer){
  View header=page.getChildAt(0);page.removeView(header);page.removeView(scroll);page.removeView(composer);FrameLayout viewport=new FrameLayout(this);viewport.setTag("ai-chat-viewport");page.addView(viewport,new LinearLayout.LayoutParams(-1,0,1));scroll.setTag("ai-conversation-scroll");scroll.setClipToPadding(false);scroll.setVerticalScrollBarEnabled(false);viewport.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
  View top=new View(this),bottom=new View(this);top.setTag("ai-top-fade");bottom.setTag("ai-bottom-fade");for(View fade:new View[]{top,bottom}){fade.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);fade.setFocusable(false);fade.setClickable(false);}top.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{(SURFACE&0x00ffffff)|0xf5000000,(SURFACE&0x00ffffff)|0xe6000000,SURFACE&0x00ffffff}));bottom.setBackground(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{(SURFACE&0x00ffffff)|0xeb000000,(SURFACE&0x00ffffff)|0x80000000,SURFACE&0x00ffffff}));viewport.addView(top,new FrameLayout.LayoutParams(-1,dp(96),Gravity.TOP));viewport.addView(bottom,new FrameLayout.LayoutParams(-1,dp(88),Gravity.BOTTOM));viewport.addView(header,new FrameLayout.LayoutParams(-1,-2,Gravity.TOP));FrameLayout.LayoutParams input=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM);input.setMargins(dp(16),0,dp(16),dp(12));viewport.addView(composer,input);
  View.OnLayoutChangeListener fit=(v,l,t,r,b,ol,ot,or,ob)->{int start=header.getHeight()+dp(8),end=composer.getHeight()+dp(32);if(scroll.getPaddingTop()!=start||scroll.getPaddingBottom()!=end)scroll.setPadding(0,start,0,end);if(top.getLayoutParams().height!=start+dp(32)){top.getLayoutParams().height=start+dp(32);top.requestLayout();}if(bottom.getLayoutParams().height!=end+dp(24)){bottom.getLayoutParams().height=end+dp(24);bottom.requestLayout();}};header.addOnLayoutChangeListener(fit);composer.addOnLayoutChangeListener(fit);aiComposerExpanded(false);
 }

 private void aiAttachments(View anchor,TextView strength){
  if(aiAttachmentMenu!=null){aiAttachmentMenu.close(true);return;}
  View focus=aiPage==null?getCurrentFocus():aiPage.findFocus();NavigationOverlay menu=new NavigationOverlay(true,"添加附件",anchor);aiAttachmentMenu=menu;menu.menuWidth=264;menu.overlapsAnchor=true;menu.panel.setTag("ai-attachment-menu");menu.body.setPadding(dp(12),dp(8),dp(12),dp(8));
  String[] names={"拍照","照片","文件","推理强度"},tags={"camera","photo","file","strength"};int[] icons={R.drawable.ic_ai_camera,R.drawable.ic_ai_photo,R.drawable.ic_ai_attach,R.drawable.ic_strength};
  for(int i=0;i<names.length;i++){int item=i;LinearLayout row=new LinearLayout(this);row.setTag("ai-attach-"+tags[i]);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(4),dp(4),dp(8),dp(4));row.setMinimumHeight(dp(60));row.setFocusable(true);ImageView icon=new ImageView(this);icon.setImageResource(icons[i]);icon.setColorFilter(i==3?PRIMARY:INK);icon.setPadding(dp(12),dp(12),dp(12),dp(12));icon.setBackground(bg(SOFT,24));icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);row.addView(icon,new LinearLayout.LayoutParams(dp(44),dp(44)));TextView name=text(names[i],18,i==3?PRIMARY:INK);name.setTypeface(null,Typeface.BOLD);name.setPadding(dp(12),0,0,0);row.addView(name,new LinearLayout.LayoutParams(0,-2,1));if(i==3)row.addView(text("✓",20,PRIMARY));row.setContentDescription(names[i]+(i==0||i==1?"，添加给云端 AI 查看":i==2?"，支持图片、文本、PDF 和 DOCX":""));row.setOnClickListener(v->{menu.close(true);if(item==3)aiStrength(strength);else aiOpenAttachment(item);});focusOutline(row);menu.body.addView(row,new LinearLayout.LayoutParams(-1,-2));}

  if(focus instanceof EditText){menu.returnFocus=focus;focus.requestFocus();}
 }
 private void aiOpenAttachment(int item){
  aiAttachmentPage=aiPage;try{if(item==0){aiOpenCamera();}else if(item==1)ImagePicker.open(this,901);else if(item==2)startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*").putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/*","text/*","application/pdf","application/vnd.openxmlformats-officedocument.wordprocessingml.document"}),902);}catch(Exception e){aiClearCapture();toast("无法打开，请检查相应系统应用后重试。");}
 }
 private void aiStageAttachment(android.net.Uri uri,boolean capture){View page=aiAttachmentPage;worker.execute(()->{try{java.util.List<AiAttachment> files=AiAttachment.read(this,uri);runOnUiThread(()->{if(page!=root||page!=aiPage){toast("聊天已切换，请重新添加。");return;}if(aiPendingAttachments.size()+files.size()>4){toast("一次最多添加 4 个附件或 PDF 页面。");return;}aiPendingAttachments.addAll(files);aiRenderAttachments();});}catch(Exception e){runOnUiThread(()->toast(e instanceof java.io.IOException?e.getMessage():"附件无法读取，请重试。"));}finally{if(capture)runOnUiThread(this::aiClearCapture);}});}
 private void aiAttachmentResult(int request,int result,Intent data){
  if(result!=RESULT_OK){aiClearCapture();return;}if(aiAttachmentPage==null||aiAttachmentPage!=root||root!=aiPage){aiClearCapture();toast("聊天已切换，请重新添加。");return;}
  if(request==900){if(aiCaptureFile!=null)aiStageAttachment(androidx.core.content.FileProvider.getUriForFile(this,getPackageName()+".ocr.files",aiCaptureFile),true);return;}
  if(request==901||request==902)aiStageAttachment(data==null?null:data.getData(),false);
 }
 private void aiRenderAttachments(){
  if(aiPendingView==null)return;aiPendingView.removeAllViews();((View)aiPendingView.getParent()).setVisibility(aiPendingAttachments.isEmpty()?View.GONE:View.VISIBLE);
  for(AiAttachment file:aiPendingAttachments){LinearLayout item=new LinearLayout(this);item.setGravity(Gravity.CENTER_VERTICAL);item.setBackground(bg(SOFT,16));TextView chip=text(file.name,13,INK);chip.setMinHeight(dp(48));chip.setGravity(Gravity.CENTER);chip.setMaxWidth(dp(140));chip.setSingleLine(true);chip.setEllipsize(TextUtils.TruncateAt.END);chip.setPadding(dp(12),0,0,0);chip.setContentDescription(file.image==null?file.name:"预览图片 "+file.name);if(file.image!=null)chip.setOnClickListener(v->aiPreviewImage(file));if(file.image!=null){BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(file.image,0,file.image.length,options);options.inSampleSize=1;while(Math.max(options.outWidth,options.outHeight)/options.inSampleSize>160)options.inSampleSize*=2;options.inJustDecodeBounds=false;Bitmap thumbnail=BitmapFactory.decodeByteArray(file.image,0,file.image.length,options);ImageView photo=new ImageView(this);photo.setImageBitmap(thumbnail);photo.setScaleType(ImageView.ScaleType.CENTER_CROP);photo.setContentDescription("预览图片 "+file.name);photo.setOnClickListener(v->aiPreviewImage(file));item.addView(photo,new LinearLayout.LayoutParams(dp(64),dp(64)));}else item.addView(chip);TextView remove=icon(R.drawable.ic_close,"移除附件 "+file.name,()->{aiPendingAttachments.remove(file);aiRenderAttachments();});item.addView(remove,new LinearLayout.LayoutParams(dp(48),dp(48)));aiPendingView.addView(item);}
 }
 private void aiPreviewImage(AiAttachment file){
  try{Bitmap bitmap;if(Build.VERSION.SDK_INT>=28){android.graphics.ImageDecoder.Source source=android.graphics.ImageDecoder.createSource(java.nio.ByteBuffer.wrap(file.image));bitmap=android.graphics.ImageDecoder.decodeBitmap(source,(decoder,info,src)->{int largest=Math.max(info.getSize().getWidth(),info.getSize().getHeight());decoder.setTargetSampleSize(Math.max(1,(largest+1599)/1600));});}else{BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(file.image,0,file.image.length,options);options.inSampleSize=1;while(Math.max(options.outWidth,options.outHeight)/options.inSampleSize>1600)options.inSampleSize*=2;options.inJustDecodeBounds=false;bitmap=BitmapFactory.decodeByteArray(file.image,0,file.image.length,options);}if(bitmap==null)throw new java.io.IOException();Dialog dialog=new Dialog(this,R.style.SecondaryDialog);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);FrameLayout content=new FrameLayout(this);content.setBackgroundColor(android.graphics.Color.BLACK);content.addView(new AiImagePreview(this,bitmap),new FrameLayout.LayoutParams(-1,-1));TextView close=icon(R.drawable.ic_close,"关闭图片预览",dialog::dismiss);close.getCompoundDrawables()[0].setTint(android.graphics.Color.WHITE);close.setBackground(bg(0xcc202020,24));content.addView(close,new FrameLayout.LayoutParams(dp(48),dp(48),Gravity.TOP|Gravity.END));dialog.setContentView(content);dialog.show();if(dialog.getWindow()!=null)dialog.getWindow().setLayout(-1,-1);}catch(Exception e){toast("图片无法预览，请重新添加。");}
 }

 private void aiClearCapture(){if(aiCaptureFile!=null){aiCaptureFile.delete();aiCaptureFile=null;}}
 private static final String AI_DATA_INFO="聊天与项目记忆保存在本机；临时聊天不保存。\n\n发送时，对话、项目记忆、课表、考试、任务、快递和所选附件会发往当前 API；已启用的日历、通知和共享画面也会发送。费用与附件保存期限由供应商决定。\n\n应用数据变更需你确认，保存成功后生效。";
 private void aiDataInfo(){LinearLayout body=secondary("数据与说明","",this::menu);TextView info=text(AI_DATA_INFO,15,INK);info.setLineSpacing(dp(5),1);info.setPadding(dp(4),dp(8),dp(4),dp(20));body.addView(info);}
 private void aiAppPermissions(){try{startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:"+getPackageName())));}catch(ActivityNotFoundException e){toast("无法打开权限设置。");}}
 private void aiOpenCamera(){
  if(checkSelfPermission(android.Manifest.permission.CAMERA)!=android.content.pm.PackageManager.PERMISSION_GRANTED){if(getSharedPreferences("appSettings",0).getBoolean("aiCameraAsked",false)&&!shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)){new AlertDialog.Builder(this,R.style.SecondaryDialog).setTitle("开启相机权限").setMessage("在应用权限中允许相机，或使用照片添加图片。").setNegativeButton("取消",null).setPositiveButton("前往设置",(d,w)->aiAppPermissions()).show();return;}getSharedPreferences("appSettings",0).edit().putBoolean("aiCameraAsked",true).apply();requestPermissions(new String[]{android.Manifest.permission.CAMERA},909);return;}
  aiCamera=new AiCameraPanel(this,file->{aiCaptureFile=file;aiStageAttachment(androidx.core.content.FileProvider.getUriForFile(this,getPackageName()+".ocr.files",file),true);});aiCamera.show();
 }
 private void aiSendState(ImageButton button,boolean sending){
  button.setImageResource(sending?R.drawable.ic_ai_stop:R.drawable.ic_ai_send);button.setScaleType(ImageView.ScaleType.CENTER);button.setPadding(0,0,0,0);button.setImageTintList(android.content.res.ColorStateList.valueOf(ON_PRIMARY));button.setBackgroundTintList(null);button.setBackground(new android.graphics.drawable.InsetDrawable(bg(PRIMARY,20),dp(4)));button.setContentDescription(sending?"停止生成":"发送消息");
 }
 private void aiAssistant(){
  aiVisible=true;tasksVisible=false;parcelsVisible=false;
  String active=Timetables.active(this);try{loadAiChat();if(aiChatTimetable!=null&&!aiChatTimetable.equals(active))rememberAi("system","当前课表已切换，请以最新快照为准。");}catch(Exception e){toast("聊天记录无法读取，原数据已保留。");}aiChatTimetable=active;try{String chatId=aiStore().chat().getString("id");if(!chatId.equals(aiPendingChat)){aiPendingAttachments.clear();aiPendingChat=chatId;}}catch(Exception ignored){}
  LinearLayout body=secondary("AI 助手","",null);pageBack=null;updateSystemBack();final LinearLayout chatPage=root;aiPage=chatPage;LinearLayout bar=(LinearLayout)root.getChildAt(0);root.setBackgroundColor(SURFACE);bar.setPadding(dp(16),dp(8),dp(16),dp(6));bar.removeView(bar.findViewWithTag("header-spacer"));bar.addView(new View(this),1,new LinearLayout.LayoutParams(dp(56),1));TextView historyButton=(TextView)bar.getChildAt(0);secondaryUi.styleIcon(historyButton,R.drawable.ic_menu);historyButton.setContentDescription("打开导航和聊天侧栏");historyButton.setTag("ai-history-button");historyButton.setOnClickListener(v->aiHistory());TextView fresh=icon(R.drawable.ic_temporary,"切换临时聊天",this::aiTemporaryChat);fresh.setTag("ai-temporary-chat");fresh.setSelected(aiTemporaryStore!=null);bar.addView(fresh,new LinearLayout.LayoutParams(dp(48),dp(48)));TextView more=icon(R.drawable.ic_more,"AI 设置",()->{NavigationOverlay menu=new NavigationOverlay(true,"AI 助手菜单",chatPage.findViewWithTag("ai-menu"));menuItem(menu,"项目","ai-projects",this::aiProjects);menuItem(menu,"项目记忆","ai-memory",this::aiMemory);menuItem(menu,"API 配置","ai-settings",this::aiSettings);menuItem(menu,"考试记录","ai-exams",this::aiExams);});more.setTag("ai-menu");LinearLayout.LayoutParams menuBounds=new LinearLayout.LayoutParams(dp(48),dp(48));menuBounds.leftMargin=dp(8);bar.addView(more,menuBounds);LinearLayout.LayoutParams temporaryBounds=(LinearLayout.LayoutParams)fresh.getLayoutParams();temporaryBounds.leftMargin=dp(8);secondaryUi.headerStyle(bar);
  bottomNavigation.setVisibility(View.VISIBLE);updateBottomNavigation();if(Build.VERSION.SDK_INT>=30)getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);stage.requestApplyInsets();ScrollView conversationScroll=(ScrollView)root.getChildAt(1);LinearLayout history=new LinearLayout(this);history.setOrientation(LinearLayout.VERTICAL);body.addView(history,new LinearLayout.LayoutParams(-1,-2));history.setTag("ai-history");if(aiChat.length()==0)aiWelcome(history);for(int i=0;i<aiChat.length();i++){org.json.JSONObject entry=aiChat.optJSONObject(i);if(entry==null)continue;String role=entry.optString("role"),content=entry.optString("content");org.json.JSONArray files=entry.optJSONArray("attachments");if(files!=null)for(int j=0;j<files.length();j++)content+="\n[附件："+files.optJSONObject(j).optString("name")+"]";aiMessage(history,content,role.equals("user"));}TextView status=text("",13,DANGER);status.setTag("ai-status");status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);body.addView(status);AiComposer composer=new AiComposer(this);composer.setTag("ai-composer");composer.setOrientation(LinearLayout.VERTICAL);composer.setGravity(Gravity.BOTTOM);composer.setPadding(dp(8),dp(4),dp(4),dp(4));composer.setBackground(bg(CARD,28));LinearLayout.LayoutParams composerSize=new LinearLayout.LayoutParams(-1,-2);composerSize.setMargins(dp(16),dp(8),dp(16),dp(12));root.addView(composer,composerSize);EditText message=field("消息",false);message.setBackground(null);message.setPadding(dp(12),dp(12),dp(8),dp(12));message.setTag("ai-message");message.setContentDescription("输入消息");message.setSingleLine(false);message.setMinLines(2);message.setMaxLines(4);message.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(2000)});if(aiRestoredDraft!=null){try{if(aiStore().chat().getString("id").equals(aiRestoredDraftChat)){message.setText(aiRestoredDraft);message.setSelection(Math.max(0,Math.min(message.length(),aiRestoredStart)),Math.max(0,Math.min(message.length(),aiRestoredEnd)));}}catch(Exception ignored){}aiRestoredDraft=null;aiRestoredDraftChat=null;}composer.addView(message,new LinearLayout.LayoutParams(-1,-2));HorizontalScrollView pendingScroll=new HorizontalScrollView(this);pendingScroll.setHorizontalScrollBarEnabled(false);aiPendingView=new LinearLayout(this);pendingScroll.addView(aiPendingView);composer.addView(pendingScroll,0,new LinearLayout.LayoutParams(-1,-2));aiRenderAttachments();
  TextView title=(TextView)bar.getChildAt(2);title.setTag("primary-page-title");title.setTextSize(24);title.setText(aiTemporaryStore==null?"Morrow AI":"临时聊天");title.setOnClickListener(null);title.setContentDescription(aiTemporaryStore==null?"Morrow AI":"临时聊天，关闭后不保留记录");
  LinearLayout tools=new LinearLayout(this);tools.setTag("ai-tools");tools.setGravity(Gravity.CENTER_VERTICAL);composer.addView(tools,new LinearLayout.LayoutParams(-1,dp(48)));
  TextView add=icon(R.drawable.ic_add,"添加附件",()->{});add.setTag("ai-attach");add.setBackground(null);add.setOnClickListener(v->aiAttachments(add,(TextView)chatPage.findViewWithTag("ai-strength")));tools.addView(add,new LinearLayout.LayoutParams(dp(48),dp(48)));tools.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));
  TextView strength=icon(R.drawable.ic_strength,"选择思考强度",()->{});strength.setTag("ai-strength");strength.setBackground(null);try{aiStrengthState(strength,aiStore().chat().optInt("strength",0));}catch(Exception ignored){aiStrengthState(strength,0);}strength.setOnClickListener(v->aiStrength(strength));tools.addView(strength,new LinearLayout.LayoutParams(dp(48),dp(48)));
  ImageButton[] send={null};
  send[0]=new ImageButton(this);tools.addView(send[0]);send[0].setOnClickListener(v->{
   if(aiSending){if(aiCall!=null)aiCall.cancel();status.setText("正在停止…");return;}String typed=message.getText().toString().trim();final String input=typed.isEmpty()&&!aiPendingAttachments.isEmpty()?"请查看附件。":typed;if(input.isEmpty()){message.setError("请输入消息");return;}final AiProviders.Config provider;try{provider=new AiProviders(this).active();if(provider==null){toast("请先在右上角菜单添加 API 配置");return;}}catch(Exception e){toast("API 配置无法读取，原数据已保留。");return;}if(busy||aiSending){toast("请等待本次操作结束");return;}
   final String snapshot=data,examSnapshot=getPreferences(0).getString("aiExams","[]"),timetable=Timetables.active(this),termSnapshot=term,adjustmentSnapshot=adjustments,timesSnapshot=classTimes.encode();final int current=currentWeek();org.json.JSONArray request=new org.json.JSONArray();try{for(int i=Math.max(0,aiChat.length()-32);i<aiChat.length();i++)request.put(aiChat.getJSONObject(i));request.put(new org.json.JSONObject().put("role","user").put("content",input));}catch(Exception e){status.setText("无法准备消息，请重试。");return;}
   aiPendingPhoneQuestion=null;aiPendingPhoneSource=null;aiPhoneWaits=0;final ArrayList<AiAttachment> manualPending=new ArrayList<>(aiPendingAttachments);final ArrayList<AiAttachment> pending=new ArrayList<>(manualPending);byte[] screenBytes=AiScreenService.frame();if(screenBytes!=null)pending.add(AiAttachment.screen(screenBytes,AiScreenService.capturedAt));final java.util.concurrent.atomic.AtomicBoolean attachmentsSaved=new java.util.concurrent.atomic.AtomicBoolean(false);final int userEntryIndex=aiChat.length();final int selectedStrength;final org.json.JSONObject projectContext;try{selectedStrength=aiStore().chat().optInt("strength",0);projectContext=new org.json.JSONObject().put("id",aiStore().project().getString("id")).put("name",aiStore().project().getString("name")).put("memory",aiStore().project().getString("memory")).put("temporary",aiTemporaryStore!=null);}catch(Exception failed){status.setText("聊天记录暂时无法保存，请重新打开后重试。");return;}rememberAi("user",input);aiPendingAttachments.clear();aiRenderAttachments();View welcome=history.findViewWithTag("ai-welcome");if(welcome!=null)history.removeView(welcome);StringBuilder filesShown=new StringBuilder(input);for(AiAttachment file:pending)filesShown.append("\n[附件：").append(file.name).append("]");aiMessage(history,filesShown.toString(),true);message.setText("");TextView streaming=aiMessage(history,pending.isEmpty()?"正在思考…":"正在上传附件…",false);conversationScroll.post(()->conversationScroll.fullScroll(View.FOCUS_DOWN));aiSending=true;final DeepSeekClient.Call call=new DeepSeekClient.Call();aiCall=call;aiSendState(send[0],true);org.json.JSONArray taskSnapshot=new org.json.JSONArray();boolean tasksReady=false;prepareTasks();try{taskSnapshot=taskPage.aiContext();tasksReady=true;}catch(Exception ignored){}final org.json.JSONArray taskData=taskSnapshot;final boolean tasksAvailable=tasksReady;send[0].setEnabled(true);status.setText("");worker.execute(()->{try{if(!pending.isEmpty()){org.json.JSONArray uploaded=new org.json.JSONArray();for(AiAttachment file:pending)uploaded.put(file.upload(provider,call));request.getJSONObject(request.length()-1).put("attachments",uploaded);java.util.concurrent.FutureTask<Void> saveAttachments=new java.util.concurrent.FutureTask<>(()->{if(aiPage!=chatPage||call.cancelled)throw new java.io.IOException("已停止回复");org.json.JSONObject entry=aiChat.getJSONObject(userEntryIndex);if(!entry.optString("role").equals("user")||!entry.optString("content").equals(input))throw new IllegalStateException("聊天已变化");entry.put("attachments",uploaded);try{aiStore().messages(aiChat);}catch(Exception failed){entry.remove("attachments");throw failed;}attachmentsSaved.set(true);return null;});runOnUiThread(saveAttachments);saveAttachments.get();}org.json.JSONObject modules=new org.json.JSONObject().put("tasks",taskData).put("tasks_available",tasksAvailable).put("project",projectContext);try{modules.put("phone",AiPhoneContext.read(this));}catch(Exception e){modules.put("phone",new org.json.JSONObject().put("available",false));}org.json.JSONArray parcels=new org.json.JSONArray();try{for(ParcelRecord p:ParcelDatabase.get(this).parcels().all())parcels.put(AiModulePlan.parcel(p));modules.put("parcels",parcels).put("parcels_available",true);}catch(Exception unreadable){modules.put("parcels",new org.json.JSONArray()).put("parcels_available",false);}org.json.JSONObject reply=null;DeepSeekPlan plan=null;String answerContent="";long[] streamAt={0};for(int attempt=0;attempt<2;attempt++){reply=DeepSeekClient.request(provider,request,snapshot,examSnapshot,termSnapshot,current,timesSnapshot,adjustmentSnapshot,modules,selectedStrength,partial->{long now=SystemClock.uptimeMillis();if(now-streamAt[0]<50)return;streamAt[0]=now;runOnUiThread(()->{if(aiPage==chatPage&&!isDestroyed()){streaming.setText(AiText.format(partial));aiScroll(conversationScroll);}});},call);try{answerContent=reply.getString("reply").trim();if(answerContent.isEmpty()||answerContent.length()>64000)throw new IllegalArgumentException("回复内容不完整");Object proposed=reply.opt("operation");if(proposed!=null&&proposed!=org.json.JSONObject.NULL){if(!(proposed instanceof org.json.JSONObject))throw new IllegalArgumentException("操作信息不完整");org.json.JSONObject operation=(org.json.JSONObject)proposed;plan=AiModulePlan.supports(operation.getString("type"))?DeepSeekPlan.module(AiModulePlan.build(operation,modules),snapshot,examSnapshot):DeepSeekPlan.build(operation,snapshot,examSnapshot,ClassTimes.parse(timesSnapshot));}break;}catch(IllegalArgumentException|java.time.DateTimeException|org.json.JSONException invalid){if(attempt==1){plan=null;answerContent="我还需要确认具体安排，请补充要操作的对象、日期或时间；也可以继续和我讨论。";break;}request.put(new org.json.JSONObject().put("role","assistant").put("content",reply.toString()));request.put(new org.json.JSONObject().put("role","system").put("content","应用未执行操作：信息不完整或不符合课表规则。请根据已有对话纠正操作；若缺少必要信息，用自然语言具体询问用户，不要猜测。直接自然回答；需要应用操作才调用工具。"));}}final String content=answerContent;final String reasoning=reply==null?"":reply.optString("reasoning_content");final DeepSeekPlan ready=plan;runOnUiThread(()->{aiSending=false;if(isDestroyed()||aiPage!=chatPage)return;send[0].setEnabled(true);aiSendState(send[0],false);if(call.cancelled){streaming.setText("已停止回复。");rememberAi("assistant","已停止回复，未执行任何操作。");return;}if(busy||!timetable.equals(Timetables.active(this))||!data.equals(snapshot)||!adjustments.equals(adjustmentSnapshot)||!term.equals(termSnapshot)||!classTimes.encode().equals(timesSnapshot)||!getPreferences(0).getString("aiExams","[]").equals(examSnapshot)){status.setText("数据已变化，请重新发送请求。");return;}status.setText("");boolean automaticMemory=ready!=null&&ready.module!=null&&(ready.module.operation.optString("type").equals("remember_project")||ready.module.operation.optString("type").equals("request_phone_access"));String shown=ready==null||automaticMemory?content:content+(root==chatPage?"\n\n请核对操作后确认。":"\n\n操作尚未确认；请重新提出该操作后确认保存。");streaming.setText(AiText.format(shown));aiScroll(conversationScroll);rememberAi("assistant",ready==null||automaticMemory?content:"建议操作（尚未执行）："+ready.description+"\n"+content);try{if(!reasoning.isEmpty()){aiChat.getJSONObject(aiChat.length()-1).put("reasoning_content",reasoning);aiStore().messages(aiChat);}}catch(Exception ignored){}if(ready!=null&&ready.module!=null&&ready.module.operation.optString("type").equals("request_phone_access")){try{aiPendingPhoneQuestion=input;aiPendingPhonePage=chatPage;aiPendingPhoneAt=System.currentTimeMillis();aiPendingPhoneSource=ready.module.operation.getJSONObject("value").getString("source");aiPhoneWaits=0;aiRequestPhoneAccess(aiPendingPhoneSource);}catch(Exception e){status.setText("系统授权未能启动，请重试。");}return;}if(ready!=null&&ready.module!=null&&ready.module.operation.optString("type").equals("remember_project")){executeAiModule(ready.module,status);return;}if(ready!=null&&root==chatPage)confirmAiPlan(ready,snapshot,examSnapshot,timetable,termSnapshot,adjustmentSnapshot,timesSnapshot,screen,status,history);});}catch(Exception e){runOnUiThread(()->{aiSending=false;if(isDestroyed()||aiPage!=chatPage)return;send[0].setEnabled(true);aiSendState(send[0],false);String reason=call.cancelled?"已停止回复":e instanceof java.io.IOException?e.getMessage():"这次没有完成，请再试一次；你也可以换一种说法继续聊。";if(call.cancelled){String partial=streaming.getText().toString();if(!partial.equals("正在思考…")&&!partial.isEmpty())reason=partial+"\n\n已停止回复。";}streaming.setText(AiText.format(reason));rememberAi("assistant",reason);if(!call.cancelled&&message.getText().length()==0)message.setText(input);if(!attachmentsSaved.get()&&!manualPending.isEmpty()){aiPendingAttachments.addAll(manualPending);aiRenderAttachments();}status.setText("");});}});
  });send[0].setTag("ai-send");send[0].setStateListAnimator(null);focusOutline(send[0]);aiSendState(send[0],false);LinearLayout.LayoutParams sendSize=new LinearLayout.LayoutParams(dp(48),dp(48));sendSize.leftMargin=dp(8);send[0].setLayoutParams(sendSize);secondaryUi.fitHeader(bar,title);aiChatChrome(chatPage,conversationScroll,composer);
 }
 private void aiExams(){
  LinearLayout body=secondary("考试记录","考试信息由你提供，保存在当前课表中。点击记录查看详情或删除。",this::aiAssistant);try{org.json.JSONArray records=DeepSeekPlan.examList(getPreferences(0).getString("aiExams","[]"));if(records.length()==0){body.addView(text("暂无考试记录。",15,MUTED));return;}LinearLayout group=settingsGroup(body,"");for(int i=0;i<records.length();i++){org.json.JSONObject exam=records.getJSONObject(i);TextView row=text(DeepSeekPlan.examText(exam),15,INK);row.setTag("exam-record-"+i);row.setFocusable(true);row.setOnClickListener(v->examDetails(exam));row.setPadding(dp(16),dp(16),dp(16),dp(16));group.addView(row);}}catch(Exception e){body.addView(text("考试记录格式异常，请检查数据。",14,DANGER));}
 }
 private void confirmAiPlan(DeepSeekPlan plan,String snapshot,String examSnapshot,String timetable,String termSnapshot,String adjustmentSnapshot,String timesSnapshot,int token,TextView status,LinearLayout history){
  String detail=plan.description;try{int lost=0;List<Course> before=Course.parse(snapshot),after=Course.parse(plan.courses);for(CourseAdjustment adjustment:CourseAdjustment.parse(adjustmentSnapshot))if(adjustment.applies(before,termSnapshot)&&!adjustment.applies(after,termSnapshot))lost++;if(lost>0)detail+="\n\n此操作会使 "+lost+" 条单次课程调整不再生效。";}catch(Exception e){status.setText("课程调整记录异常，未执行操作。");rememberAi("system","应用结果：课程调整记录异常，建议操作未执行。");return;}
  boolean[] recorded={false};AlertDialog dialog=new AlertDialog.Builder(this,R.style.SecondaryDialog).setTitle("确认操作？").setMessage(detail).setNegativeButton("取消",(d,w)->{recorded[0]=true;status.setText("已取消操作。");rememberAi("system","应用结果：用户取消了以上建议操作，未执行。");}).setPositiveButton("确认保存",(d,w)->{
   if(screen!=token||busy||!timetable.equals(Timetables.active(this))||!data.equals(snapshot)||!adjustments.equals(adjustmentSnapshot)||!term.equals(termSnapshot)||!classTimes.encode().equals(timesSnapshot)||!getPreferences(0).getString("aiExams","[]").equals(examSnapshot)){recorded[0]=true;status.setText("数据已变化，请重新发送请求。");rememberAi("system","应用结果：数据已变化，以上建议操作未执行。");return;}
   if(plan.module!=null){recorded[0]=true;executeAiModule(plan.module,status);return;}SharedPreferences.Editor edit=getPreferences(0).edit();if(!plan.courses.equals(snapshot))edit.putString("courses",plan.courses);if(!plan.exams.equals(examSnapshot))edit.putString("aiExams",plan.exams);if(!edit.commit()){getPreferences(0).edit().putString("courses",snapshot).putString("aiExams",examSnapshot).commit();recorded[0]=true;status.setText("保存失败，请重试。");rememberAi("system","应用结果：保存失败，操作没有确认成功，请用户重新核对数据。");return;}data=plan.courses;if(!plan.exams.equals(examSnapshot))homePage=null;CourseWidget.updateAll(this);TextView result=text("已保存："+plan.description,14,PRIMARY);result.setPadding(0,dp(12),0,0);history.addView(result);recorded[0]=true;status.setText("操作已保存。");rememberAi("system","应用结果：用户已确认，以上建议操作已保存。");
  }).create();dialog.setOnDismissListener(d->{if(!recorded[0])rememberAi("system","应用结果：确认窗口已关闭，以上建议操作未执行。");});dialog.show();styleDialog(dialog,false);
 }
 void aiTaskResult(boolean saved){rememberAi("system",saved?"任务操作已成功保存。":"任务操作保存失败，未确认成功，请重新查询核对。");}
 private void executeAiModule(AiModulePlan plan,TextView status){
  try{String type=plan.operation.getString("type");if(type.equals("phone_action")){if(plan.operation.getJSONObject("value").optString("action").equals("set_alarm")&&!AiPhoneActions.describe(plan.operation.getJSONObject("value")).equals(plan.description))throw new IllegalStateException("闹钟时间已变化，请重新确认");AiPhoneActions.execute(this,plan.operation.getJSONObject("value"));rememberAi("system","手机操作已交给系统处理，请以系统或对应应用显示的结果为准。");status.setText("已交给系统处理，请核对对应应用的结果。");return;}if(type.equals("remember_project")){if(aiTemporaryStore!=null){status.setText("临时聊天未保存长期记忆。");return;}if(!aiStore().project().getString("id").equals(plan.expected.getString("id")))throw new IllegalStateException();String previous=plan.expected.getString("memory"),value=plan.operation.getJSONObject("value").getString("text").trim();if(!plan.operation.getJSONObject("value").optString("mode","append").equals("replace")){java.util.LinkedHashSet<String> lines=new java.util.LinkedHashSet<>(java.util.Arrays.asList((previous+"\n"+value).split("\n")));lines.remove("");value=String.join("\n",lines);}aiStore().organizeMemory(previous,value);rememberAi("system","项目记忆已自动整理，可在项目记忆页查看、编辑或撤销。");status.setText("已自动整理项目记忆 · 可在菜单中查看或撤销");return;}if(type.equals("open_page")){String page=plan.operation.getJSONObject("value").getString("page");rememberAi("system","应用已打开所请求的页面。");switch(page){case "tasks":tasks();break;case "parcels":parcels();break;case "settings":menu();break;case "phone":aiPhoneSettings();break;case "exams":aiExams();break;case "term":termPicker();break;case "times":timesForm();break;case "timetables":timetableManagement();break;default:home();}return;}
   if(type.endsWith("_task")){prepareTasks();taskPage.aiApply(plan.operation,plan.expected);rememberAi("system","操作已提交到任务模块，保存结果在任务页显示；尚不能声称保存成功。");tasks();return;}
   ParcelRepository.IO.execute(()->{try{ParcelDatabase db=ParcelDatabase.get(this);ParcelRepository repository=new ParcelRepository(this);db.runInTransaction(()->{try{ParcelRecord current=plan.expected==null?null:db.parcels().get(plan.expected.getLong("id"));if(plan.expected!=null&&(current==null||!AiModulePlan.parcel(current).toString().equals(plan.expected.toString())))throw new IllegalStateException("快递已变化，请重新提问");if(type.equals("delete_parcel")){repository.delete(current.id);return;}ParcelRecord next=AiModulePlan.record(plan.expected,plan.operation.optJSONObject("value"));if(type.equals("complete_parcel"))next.state=ParcelRecord.DONE;if(current!=null){current.code=next.code;current.station=next.station;current.trackingNumber=next.trackingNumber;current.state=next.state;next=current;}repository.save(next);}catch(Exception e){throw new IllegalStateException(e);}});runOnUiThread(()->{rememberAi("system","快递操作已保存。");status.setText("快递操作已保存。");if(parcelPage!=null){parcelPage.close();parcelPage=null;if(parcelsVisible)parcels();}toast("快递操作已保存");});}catch(Exception e){runOnUiThread(()->{rememberAi("system","快递操作未保存，请重新查询并核对。");status.setText("快递操作未保存，记录可能已变化或重复，请重新核对。");});}});
  }catch(Exception e){status.setText("模块尚未就绪或数据已变化，请稍后重试。");rememberAi("system","操作未执行：模块尚未就绪或数据已变化。");}
 }
 private void appearanceSettings(){
  LinearLayout body=secondary("外观","",()->menu());
  LinearLayout group=settingsGroup(body,"");int selected=Appearance.mode(this);
  String[] labels={"跟随系统","浅色","深色"};
  int[] icons={R.drawable.ic_phone,R.drawable.ic_sun,R.drawable.ic_dark_mode};
  for(int i=0;i<3;i++){final int value=i;LinearLayout row=option(group,icons[i],labels[i],"",()->chooseAppearance(value));row.setTag("appearance-"+i);row.setSelected(i==selected);TextView mark=(TextView)row.getChildAt(row.getChildCount()-1);mark.setText("");if(i==selected){secondaryUi.styleIcon(mark,R.drawable.ic_check);mark.setBackground(null);}}
  LinearLayout accents=settingsGroup(body,"");LinearLayout accent=option(accents,R.drawable.ic_palette,"强调色",Appearance.ACCENT_NAMES[Appearance.accent(this)],()->accentChoices(root.findViewWithTag("accent-setting")));accent.setTag("accent-setting");TextView label=(TextView)accent.getChildAt(2);GradientDrawable dot=bg(Appearance.ACCENT_SWATCHES[Appearance.accent(this)],6);dot.setBounds(0,0,dp(12),dp(12));label.setCompoundDrawables(dot,null,null,null);label.setCompoundDrawablePadding(dp(8));
 }
 private void accentChoices(View anchor){
  NavigationOverlay menu=new NavigationOverlay(true,"强调色",anchor);menu.menuWidth=208;
  for(int i=0;i<Appearance.ACCENT_NAMES.length;i++){final int value=i;TextView row=secondaryUi.popupItem(menu.body,Appearance.ACCENT_NAMES[i],()->{menu.close(false);if(Appearance.accent(this)!=value){Appearance.accent(this,value);recreate();}});row.setTag("accent-"+i);row.setTextSize(17);boolean selected=i==Appearance.accent(this);row.setSelected(selected);GradientDrawable dot=bg(Appearance.ACCENT_SWATCHES[i],8);dot.setBounds(0,0,dp(16),dp(16));android.graphics.drawable.Drawable check=selected?getDrawable(R.drawable.ic_check).mutate():null;if(check!=null){check.setTint(INK);check.setBounds(0,0,dp(20),dp(20));}row.setCompoundDrawables(dot,null,check,null);row.setCompoundDrawablePadding(dp(16));}
 }
 private void chooseAppearance(int mode){
  if(Appearance.mode(this)==mode){toast("当前已是"+Appearance.label(this));return;}
  Appearance.mode(this,mode);CourseWidget.updateAll(this);ParcelWidget.updateAll(this);recreate();
 }

 private String appVersion(){try{return getPackageManager().getPackageInfo(getPackageName(),0).versionName;}catch(android.content.pm.PackageManager.NameNotFoundException e){throw new IllegalStateException(e);}}
 private long appVersionCode(){try{android.content.pm.PackageInfo info=getPackageManager().getPackageInfo(getPackageName(),0);return Build.VERSION.SDK_INT>=28?info.getLongVersionCode():info.versionCode;}catch(android.content.pm.PackageManager.NameNotFoundException e){throw new IllegalStateException(e);}}
 private String versionLabel(){return "当前版本 "+appVersion();}
 private void about(){
  LinearLayout body=secondary("关于软件","",this::menu);
  LinearLayout brand=new LinearLayout(this);brand.setOrientation(LinearLayout.VERTICAL);brand.setGravity(Gravity.CENTER);brand.setPadding(0,dp(24),0,dp(32));ImageView logo=new ImageView(this);logo.setImageResource(R.mipmap.ic_launcher);logo.setContentDescription("Morrow App 图标");logo.setTag("about-app-icon");brand.addView(logo,new LinearLayout.LayoutParams(dp(80),dp(80)));TextView name=text("Morrow",23,NAVY);name.setTypeface(null,Typeface.BOLD);name.setPadding(0,dp(20),0,dp(10));brand.addView(name);body.addView(brand,new LinearLayout.LayoutParams(-1,-2));
  LinearLayout updates=settingsGroup(body,"");option(updates,R.drawable.ic_sync,"软件更新",versionLabel(),this::updateSettings).setTag("update-settings");
  LinearLayout links=settingsGroup(body,"");option(links,R.drawable.ic_notes,"官网","morrow.xvyan.cn",()->openRemoteLink("https://morrow.xvyan.cn/")).setTag("official-website");option(links,R.drawable.ic_notes,"GitHub 仓库","fangfar/Morrow",()->openRemoteLink(RemoteConfig.REPOSITORY)).setTag("public-repository");
  String feedbackGroup="1135218563",feedbackLink="https://qun.qq.com/universal-share/share?ac=1&authKey=qF9e8dvcJ7nEJObP6TFs8LMcYxpXsUTi%2Bp5w7p4i4Uz9t5KKpSFJiojPkJruxuXK&busi_data=eyJncm91cENvZGUiOiIxMTM1MjE4NTYzIiwidG9rZW4iOiI1N0hSUCs2VTBuWEZlYmxaQW5tSFFhU1BYVlNWRDdleENLQVQwQU9iRVlSWUxGNTQ3UXZ2M1VlTm01K3NjUHh4IiwidWluIjoiNDk4NDYyNDE5In0%3D&data=LzelMJC9i4cQgPLZipJckbXjhtabq0AgxwzqiZDO_e7RmulW4iNAunDsuo8qqhc8HC9FYsRYUouttgYOr-05Hg&svctype=4&tempid=h5_group_info";
  LinearLayout feedback=option(links,R.drawable.ic_person,"QQ 群反馈",feedbackGroup,()->openRemoteLink(feedbackLink));feedback.setTag("feedback-qq-group");feedback.setTooltipText("长按复制群号");feedback.setOnLongClickListener(v->{((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("反馈 QQ 群",feedbackGroup));toast("反馈群号已复制");return true;});
  remoteStatus=null;
 }

 private void updateSettings(){
  LinearLayout body=secondary("软件更新","",this::about);LinearLayout automatic=settingsGroup(body,"");LinearLayout row=option(automatic,R.drawable.ic_sync,"自动检查更新","",()->{});row.removeViewAt(row.getChildCount()-1);row.removeViewAt(0);row.setPadding(dp(16),dp(4),dp(16),dp(4));row.setContentDescription(null);row.setFocusable(false);
  Switch toggle=new Switch(this);toggle.setTag("auto-check-updates");toggle.setContentDescription("自动检查更新");toggle.setMinHeight(dp(48));toggle.setSwitchMinWidth(dp(52));toggle.setShowText(false);toggle.setSplitTrack(false);toggle.setThumbTintList(null);toggle.setTrackTintList(null);toggle.setThumbDrawable(getDrawable(R.drawable.settings_switch_thumb));toggle.setTrackDrawable(getDrawable(R.drawable.settings_switch_track));toggle.setChecked(autoCheckUpdates());row.addView(toggle);row.setOnClickListener(v->toggle.setChecked(!toggle.isChecked()));toggle.setOnCheckedChangeListener((button,enabled)->{getSharedPreferences("appSettings",0).edit().putBoolean("autoCheckUpdates",enabled).apply();scheduleRemoteNotice();});
  TextView explanation=text("每次打开软件时检查新版本。关闭后，重大更新仍会提醒。",13,MUTED);explanation.setPadding(dp(16),0,dp(16),dp(16));explanation.setLineSpacing(dp(3),1);body.addView(explanation);
  LinearLayout manual=settingsGroup(body,"");option(manual,R.drawable.ic_sync,"立即检查更新",versionLabel(),this::checkRemoteUpdates).setTag("check-app-update");remoteStatus=text("",13,MUTED);remoteStatus.setTag("update-status");remoteStatus.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);remoteStatus.setPadding(dp(16),0,dp(16),dp(12));body.addView(remoteStatus);refreshRemoteStatus();
 }
 private void openRemoteLink(String url){if(url==null||url.isEmpty()){toast("暂未配置下载地址，请检查更新后重试");return;}if(!RemoteConfig.validLink(url)){toast("下载地址无效，请稍后重试");return;}try{startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(url)));}catch(ActivityNotFoundException|SecurityException e){toast("没有可打开此链接的浏览器");}}
 private void checkRemoteUpdates(){if(remote==null){remote=RemoteConfigClient.get(this);remote.listen(remoteListener);}manualCheckScreen=screen;remote.check(true);refreshRemoteStatus();}
 private boolean autoCheckUpdates(){return getSharedPreferences("appSettings",0).getBoolean("autoCheckUpdates",true);}
 private void refreshRemoteStatus(){if(remoteStatus==null||!remoteStatus.isAttachedToWindow())return;String status=remote==null||!remote.loaded?"尚未检查更新":remote.checking?"正在检查更新…":remote.config==null?"暂无可用更新信息":remote.config.newerThan(appVersionCode())?"发现新版本 "+remote.config.versionName:versionLabel();if(remote!=null&&remote.config!=null){String time=new java.text.SimpleDateFormat("MM-dd HH:mm",Locale.CHINA).format(new Date(remote.validatedAt));status+=" · 缓存于 "+time;if(!remote.fresh(System.currentTimeMillis()))status+="（已过期）";}remoteStatus.setText(status);remoteStatus.setTextColor(remote!=null&&remote.config!=null&&remote.config.newerThan(appVersionCode())?PRIMARY:MUTED);}
 private void remoteChanged(String error,boolean manual){if(isDestroyed()||isFinishing())return;refreshRemoteStatus();if(manual&&remoteResumed&&screen==manualCheckScreen){if(error!=null)toast(error);else if(remote.config==null)toast("暂无可用更新信息");else if(remote.config.newerThan(appVersionCode()))manualUpdatePending=true;else toast("已是最新版本");}scheduleRemoteNotice();}
 private void scheduleRemoteNotice(){remoteHandler.removeCallbacks(remoteNotice);if(remoteResumed)remoteHandler.postDelayed(remoteNotice,1500);}
 private boolean canShowRemote(){return (aiAttachmentMenu==null||!aiAttachmentMenu.open)&&(aiStrengthDialog==null||!aiStrengthDialog.isShowing())&&remoteResumed&&hasWindowFocus()&&!isFinishing()&&!isDestroyed()&&!touching&&SystemClock.uptimeMillis()-interactionAt>=1400&&pagerState==ViewPager.SCROLL_STATE_IDLE&&!pageMotion.isRunning()&&!busy&&!aiSending&&navigationOverlay==null&&(sideNavigation==null||!sideNavigation.isOpen())&&!showingDetail&&(choiceSheet==null||choiceSheet.getVisibility()!=View.VISIBLE)&&(unsavedDialog==null||!unsavedDialog.isShowing())&&(widgetDialog==null||!widgetDialog.isShowing())&&(remoteDialog==null||!remoteDialog.isShowing());}
 private void showRemoteNotice(){
  if(remote==null||remote.config==null||remote.checking)return;if(pageMotion.isRunning()){scheduleRemoteNotice();return;}if(!canShowRemote())return;RemoteConfig config=remote.config;
  if(manualUpdatePending){manualUpdatePending=false;if(config.newerThan(appVersionCode()))showUpdate(config);return;}
  if(pageBack!=null||!remote.fresh(System.currentTimeMillis()))return;
  String updateKey="update:"+config.versionCode;if((autoCheckUpdates()||config.forcePrompt)&&config.newerThan(appVersionCode())&&remote.sessionShown.add(updateKey)){showUpdate(config);return;}
  for(RemoteConfig.Announcement item:config.announcements)if(item.enabled&&!remote.readIds.contains(item.id)&&remote.sessionShown.add("announcement:"+item.id)){showAnnouncement(item);return;}
 }
 private AlertDialog remoteDialog(String title,String content,String date,int icon){
  LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(24),dp(20),dp(24),dp(8));LinearLayout heading=new LinearLayout(this);heading.setGravity(Gravity.CENTER_VERTICAL);ImageView image=new ImageView(this);image.setImageResource(icon);image.setColorFilter(PRIMARY);image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);heading.addView(image,new LinearLayout.LayoutParams(dp(24),dp(24)));TextView label=text(title,20,INK);label.setTypeface(null,Typeface.BOLD);label.setMaxLines(3);label.setEllipsize(TextUtils.TruncateAt.END);label.setPadding(dp(12),0,0,0);heading.addView(label,new LinearLayout.LayoutParams(0,-2,1));box.addView(heading);
  if(!date.isEmpty()){TextView time=text(date,12,MUTED);time.setPadding(0,dp(10),0,0);box.addView(time);}ScrollView scroll=new ScrollView(this){@Override protected void onMeasure(int width,int height){int max=Math.max(dp(80),(int)(getResources().getDisplayMetrics().heightPixels*.4));super.onMeasure(width,MeasureSpec.makeMeasureSpec(max,MeasureSpec.AT_MOST));}};TextView message=text(content,15,INK);message.setTextIsSelectable(true);message.setLineSpacing(dp(4),1);message.setPadding(0,dp(16),0,dp(8));message.setTag("remote-message");scroll.addView(message);box.addView(scroll,new LinearLayout.LayoutParams(-1,-2));return new AlertDialog.Builder(this,R.style.SecondaryDialog).setView(box).create();
 }
 private void styleDialog(AlertDialog dialog,boolean destructive){secondaryUi.styleDialog(dialog,destructive);}

 private void finishRemoteDialog(AlertDialog dialog){remoteDialog=dialog;dialog.setOnDismissListener(value->{if(remoteDialog==dialog)remoteDialog=null;scheduleRemoteNotice();});dialog.show();styleDialog(dialog,false);}
 private void showAnnouncement(RemoteConfig.Announcement item){AlertDialog dialog=remoteDialog(item.title,item.body,item.publishedAt.isEmpty()?"":item.publishedAt.replace('T',' ').replace("Z"," UTC"),R.drawable.ic_notes);dialog.setButton(AlertDialog.BUTTON_POSITIVE,"我知道了",(d,w)->remote.read(item.id));finishRemoteDialog(dialog);}
 private void showUpdate(RemoteConfig config){remote.sessionShown.add("update:"+config.versionCode);AlertDialog dialog=remoteDialog(config.promptTitle.isEmpty()?"发现新版本 "+config.versionName:config.promptTitle,config.promptMessage.isEmpty()?(config.changelog.isEmpty()?"新版 Morrow 已发布，可前往下载。":config.changelog):config.promptMessage,"",R.drawable.ic_sync);dialog.setButton(AlertDialog.BUTTON_NEGATIVE,"取消",(d,w)->{});dialog.setButton(AlertDialog.BUTTON_POSITIVE,"立即更新",(d,w)->openRemoteLink(config.downloadUrl(false)));finishRemoteDialog(dialog);}

 private AlertDialog confirmClear(){Runnable destination="数据管理".equals(root.getTag())?()->{}:this::home;if(busy){toast("请等待同步结束");return null;}AlertDialog dialog=new AlertDialog.Builder(this,R.style.SecondaryDialog).setTitle("清空当前课表？").setMessage("当前课表的课程、备注和单次调整会被清空。需要再次导入或手动添加课程。").setNegativeButton("保留课表",null).setPositiveButton("清空",(d,w)->{if(busy){toast("请等待同步结束");return;}school.logout();boolean cleared=true;try{schoolAccount.remove(Timetables.active(this));}catch(Exception e){cleared=false;}loginAccountPrefill=null;data="";notes="";adjustments="";getPreferences(0).edit().remove("courseAdjustments").remove("schoolImported").remove(TimetableSync.BASELINE).remove(TimetableSync.PROTECTED).remove("syncClassTimes").remove("syncSource").apply();displayChoices.clear();getPreferences(0).edit().remove("displayChoices").apply();save();destination.run();toast(cleared?"已退出并清空当前课表。":"课表已清空，但已保存的学校账号未能删除。");}).create();dialog.show();styleDialog(dialog,true);return dialog;}
 private void termPicker(){
  LinearLayout body=secondary("开学日期","选择第 1 周的开始日期。",this::menu);LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER_HORIZONTAL);card.setBackground(bg(CARD,20));card.setPadding(0,dp(20),0,dp(16));body.addView(card,new LinearLayout.LayoutParams(-1,-2));
  TextView label=text("第一周开始于",13,MUTED);label.setPadding(dp(16),0,dp(16),0);card.addView(label);TextView selected=text("",22,INK);selected.setTag("term-date-summary");selected.setGravity(Gravity.CENTER);selected.setPadding(0,dp(8),0,dp(16));card.addView(selected,new LinearLayout.LayoutParams(-1,-2));
  DatePicker picker=new DatePicker(new android.view.ContextThemeWrapper(this,R.style.SecondaryPicker));Calendar c=monday();DatePicker.OnDateChangedListener changed=(view,year,month,day)->selected.setText(String.format(Locale.CHINA,"%d年%d月%d日",year,month+1,day));picker.init(c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH),changed);changed.onDateChanged(picker,picker.getYear(),picker.getMonth(),picker.getDayOfMonth());picker.setTag("term-date");card.addView(picker,new LinearLayout.LayoutParams(-1,-2));
  Button button=action(body,"保存开学日期",()->{picker.clearFocus();term=String.format(Locale.ROOT,"%04d-%02d-%02d",picker.getYear(),picker.getMonth()+1,picker.getDayOfMonth());save();returningPage=true;try{menu();}finally{returningPage=false;}});button.setTag("save-term");saveHeader(body,button);
 }

 private void edit(){edit(()->home());}
 @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)scheduleRemoteNotice();if(focused&&ADD_WIDGET.equals(getIntent().getAction())){getIntent().setAction(Intent.ACTION_MAIN);getWindow().getDecorView().post(()->pinWidget());}else if(focused)getWindow().getDecorView().postDelayed(()->checkWidgetPin(),250);}
 private void widgetShortcut(){
  android.content.pm.ShortcutManager manager=getSystemService(android.content.pm.ShortcutManager.class);
  manager.addDynamicShortcuts(Collections.singletonList(new android.content.pm.ShortcutInfo.Builder(this,"add-widget").setShortLabel("添加小部件").setIcon(android.graphics.drawable.Icon.createWithResource(this,R.drawable.ic_widget)).setIntent(new Intent(this,MainActivity.class).setAction(ADD_WIDGET)).build()));
 }
 private android.content.SharedPreferences widgetAddPrefs(){return getSharedPreferences("widget_add",MODE_PRIVATE);}
 private boolean isXiaomi(){return Build.MANUFACTURER.equalsIgnoreCase("Xiaomi");}
 private void widgetAddPage(){
  LinearLayout body=secondary("桌面小组件","",this::menu);
  action(body,"添加到桌面",()->pinWidget()).setTag("pin-home-widget");
  option(body,R.drawable.ic_grid,"手动添加","从桌面选择 2×2、4×2 或 4×4",()->widgetManualGuide());
  if(isXiaomi())option(body,R.drawable.ic_notes,"权限教程","一键添加没有反应时查看",()->widgetPermissionGuide());
 }
 private void pinWidget(){
  if(isXiaomi()&&!widgetAddPrefs().getBoolean("guideSeen",false)){widgetAddPage();widgetPermissionGuide();return;}
  android.appwidget.AppWidgetManager manager=android.appwidget.AppWidgetManager.getInstance(this);
  if(manager.isRequestPinAppWidgetSupported())try{
   String token=UUID.randomUUID().toString();
   widgetAddPrefs().edit().putString("requestToken",token).putInt("pendingCount",CourseWidget.count(this)).putLong("requestedAt",SystemClock.elapsedRealtime()).apply();
   Intent callback=new Intent(this,WidgetPinReceiver.class).setAction("cn.courselens.WIDGET_PINNED").setData(android.net.Uri.parse("courselens-widget:"+token)).putExtra("requestToken",token);
   PendingIntent success=PendingIntent.getBroadcast(this,0,callback,PendingIntent.FLAG_ONE_SHOT|(Build.VERSION.SDK_INT>=31?PendingIntent.FLAG_MUTABLE:0));
   Bundle preview=new Bundle();preview.putParcelable(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_PREVIEW,CourseWidget.views(this,0,java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Shanghai"))));
   if(manager.requestPinAppWidget(new ComponentName(this,CourseWidget.class),preview,success)){
    getWindow().getDecorView().postDelayed(()->{if(token.equals(widgetAddPrefs().getString("requestToken","")))checkWidgetPin();},1500);return;
   }
   success.cancel();
  }catch(IllegalArgumentException|IllegalStateException|SecurityException e){/* Launcher did not accept this request. */}
  widgetAddPrefs().edit().remove("pendingCount").apply();widgetGuide();
 }
 private void checkWidgetPin(){
  if(!hasWindowFocus()||isFinishing()||isDestroyed())return;
  android.content.SharedPreferences prefs=widgetAddPrefs();String token=prefs.getString("requestToken","");
  boolean confirmed=!token.isEmpty()&&token.equals(prefs.getString("confirmedToken",""))&&!token.equals(prefs.getString("consumedToken",""));
  int before=prefs.getInt("pendingCount",-1);
  if(confirmed||(before>=0&&CourseWidget.count(this)>before)){
   prefs.edit().remove("pendingCount").putString("consumedToken",token).apply();if(widgetDialog!=null)widgetDialog.dismiss();toast("已添加到手机桌面");return;
  }
  if(before<0)return;
  long remaining=1500-(SystemClock.elapsedRealtime()-prefs.getLong("requestedAt",0));
  if(remaining>0){getWindow().getDecorView().postDelayed(()->checkWidgetPin(),remaining);return;}
  prefs.edit().remove("pendingCount").apply();widgetGuide();
 }
 private String widgetManualPath(){return isXiaomi()?"长按桌面空白处 → 小部件 → 全部应用 → 安卓小部件，找到“Morrow”，选择 2×2、4×2 或 4×4。":"长按桌面空白处，打开小组件列表，找到“Morrow”，选择 2×2、4×2 或 4×4。";}
 private void widgetManualGuide(){
  if(widgetDialog!=null)widgetDialog.dismiss();
  widgetDialog=new AlertDialog.Builder(this,R.style.SecondaryDialog).setTitle("手动添加小组件").setMessage(widgetManualPath()+"\n\n手动添加无需授予创建桌面快捷方式权限。").setPositiveButton("知道了",null).show();styleDialog(widgetDialog,false);
 }
 private void widgetPermissionGuide(){
  widgetAddPrefs().edit().putBoolean("guideSeen",true).apply();if(widgetDialog!=null)widgetDialog.dismiss();
  widgetDialog=new AlertDialog.Builder(this,R.style.SecondaryDialog).setTitle("桌面添加说明").setMessage("部分手机的一键添加可能需要“创建桌面快捷方式”权限。\n\n添加没有反应时，可到应用详情 → 权限管理（或其他权限）开启“创建桌面快捷方式 / 桌面快捷方式”。返回后可再次尝试。\n\n也可不授权，从桌面手动添加。").setPositiveButton("前往设置",(d,w)->widgetPermissionSettings()).setNegativeButton("手动添加",(d,w)->widgetManualGuide()).setNeutralButton("继续添加",(d,w)->pinWidget()).show();styleDialog(widgetDialog,false);
 }
 private void widgetPermissionSettings(){
  toast("权限管理（或其他权限）→ 创建桌面快捷方式；返回后可再次添加");
  // No public shortcut-permission settings action; use the supported application details page.
  try{startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:"+getPackageName())));}
  catch(ActivityNotFoundException|SecurityException e){toast("无法打开应用详情，请在系统设置中找到 Morrow；也可以手动添加小组件");}
 }
 private void openPermissionSettings(){
  LinearLayout body=secondary("权限管理","",this::menu);
  LinearLayout ai=settingsGroup(body,"AI 助手");
  option(ai,R.drawable.ic_settings,"手机信息与权限","日历、通知与屏幕共享",this::aiPhoneSettings).setTag("permission-ai-phone");
  option(ai,R.drawable.ic_ai_camera,"相机",checkSelfPermission(android.Manifest.permission.CAMERA)==android.content.pm.PackageManager.PERMISSION_GRANTED?"已允许 · 点击管理":"拍照时申请，也可在系统设置中管理",this::aiAppPermissions).setTag("permission-ai-camera");
  LinearLayout system=settingsGroup(body,"系统权限");
  option(system,R.drawable.ic_notifications,"通知","允许应用发送通知",this::openNotificationSettings).setTag("permission-notification");
  option(system,R.drawable.ic_battery,"电池优化","允许后台运行，关闭省电限制",this::openBatteryOptimizationSettings).setTag("permission-battery");
  option(system,R.drawable.ic_sync,"自启动","允许开机自动启动应用",this::openAutoStartSettings).setTag("permission-autostart");
  option(system,R.drawable.ic_settings,"通知读取","允许读取其他应用的通知",this::openNotificationAccessSettings).setTag("permission-notification-access");
 }
 private void openNotificationSettings(){
  try{
   Intent intent=new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
   intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,getPackageName());
   startActivity(intent);
  }catch(ActivityNotFoundException|SecurityException e){toast("无法打开通知设置");}
 }
 private void openBatteryOptimizationSettings(){
  try{
   Intent intent=new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
   intent.setData(android.net.Uri.parse("package:"+getPackageName()));
   startActivity(intent);
  }catch(ActivityNotFoundException|SecurityException e){
   try{startActivity(new Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));}
   catch(ActivityNotFoundException|SecurityException e2){toast("无法打开电池设置");}
  }
 }
 private void openAutoStartSettings(){
  toast("请在应用详情中找到「自启动管理」并开启");
  try{startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:"+getPackageName())));}
  catch(ActivityNotFoundException|SecurityException e){toast("无法打开应用详情");}
 }
 private void openNotificationAccessSettings(){
  try{startActivity(new Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));}
  catch(ActivityNotFoundException|SecurityException e){toast("无法打开通知访问设置");}
 }
 private void widgetGuide(){
  if(widgetDialog!=null)widgetDialog.dismiss();
  if(isXiaomi()&&!widgetAddPrefs().getBoolean("guideSeen",false)){widgetPermissionGuide();return;}
  AlertDialog.Builder guide=new AlertDialog.Builder(this,R.style.SecondaryDialog).setTitle("还未确认添加到桌面").setMessage("未收到添加确认。可能已取消、桌面空间不足或启动器限制了一键添加。"+(isXiaomi()?"\n可在权限教程中查看“创建桌面快捷方式”设置。":"")).setPositiveButton("重新添加",(d,w)->pinWidget()).setNegativeButton("手动添加",(d,w)->widgetManualGuide());
  if(isXiaomi())guide.setNeutralButton("权限教程",(d,w)->{widgetAddPage();widgetPermissionGuide();});
  widgetDialog=guide.show();styleDialog(widgetDialog,false);
 }
 private void edit(Runnable back){
  LinearLayout body=secondary("课程管理","",back);LinearLayout bar=(LinearLayout)root.getChildAt(0);bar.removeView(bar.findViewWithTag("header-spacer"));bar.addView(new View(this),1,new LinearLayout.LayoutParams(dp(56),1));TextView add=icon(R.drawable.ic_add,"添加课程",()->courseForm(null,-1,()->edit(back)));add.setTag("manage-add-course");bar.addView(add,new LinearLayout.LayoutParams(dp(48),dp(48)));
  List<Course> courses=Course.parse(data);Map<String,List<Course>> groups=new LinkedHashMap<>();for(Course c:courses)groups.computeIfAbsent(c.name,key->new ArrayList<>()).add(c);
  if(courses.isEmpty()){body.setGravity(Gravity.CENTER);TextView empty=text("暂无课程",18,INK);empty.setGravity(Gravity.CENTER);body.addView(empty);action(body,"添加课程",()->courseForm(null,-1,()->edit(back))).setTag("manage-empty-add");return;}
  LinearLayout list=settingsGroup(body,"");for(List<Course> group:groups.values()){Course first=group.get(0);option(list,R.drawable.ic_notes,SchoolParser.clean(first.name),group.size()>1?group.size()+" 个上课时段":"",()->courseForm(first,courses.indexOf(first),()->edit(back))).setTag("manage-course-"+courses.indexOf(first));}
 }

 private List<Course> effectiveCourses(){
  if(cachedCourses!=null&&data.equals(cachedData)&&adjustments.equals(cachedAdjustments)&&term.equals(cachedTerm))return cachedCourses;
  List<Course> raw=Course.parse(data);List<CourseAdjustment> changes=CourseAdjustment.parse(adjustments);
  List<Course> effective=Collections.unmodifiableList(CourseAdjustment.apply(raw,term,changes));Map<String,CourseAdjustment> moves=new HashMap<>();
  for(CourseAdjustment a:changes)if(a.replacement!=null&&a.applies(raw,term))for(int w=1;w<=30;w++)if(a.replacement.active(w))moves.putIfAbsent(CourseDisplay.key(w,a.replacement),a);
  cachedData=data;cachedAdjustments=adjustments;cachedTerm=term;cachedMoves=moves;cachedCardText.clear();cachedCourses=effective;return effective;
 }
 private CourseAdjustment movedAdjustment(Course course,int shownWeek){
  effectiveCourses();return cachedMoves.get(CourseDisplay.key(shownWeek,course));
 }
 private Course originalCourse(Course course,int shownWeek){for(Course c:Course.parse(data))if(c.encode().equals(course.encode())||(c.active(shownWeek)&&CourseDisplay.key(shownWeek,c).equals(CourseDisplay.key(shownWeek,course))))return c;return null;}
 private String arrangement(Course c,int shownWeek){return "第 "+shownWeek+" 周 · 星期"+"一二三四五六日".charAt(c.day-1)+" · 第 "+c.start+"–"+c.end+" 节 · "+(c.room.isEmpty()?"地点未填写":SchoolParser.clean(c.room));}
 private void storeAdjustment(CourseAdjustment next){
  List<CourseAdjustment> rows=CourseAdjustment.parse(adjustments);rows.removeIf(a->a.key().equals(next.key()));rows.add(next);List<String> encoded=new ArrayList<>();for(CourseAdjustment a:rows)encoded.add(a.encode());
  String value=String.join("\n",encoded);if(!getPreferences(0).edit().putString("courseAdjustments",value).commit())throw new IllegalStateException("本机保存失败，请重试");adjustments=value;CourseWidget.updateAll(this);
 }
 private void adjustmentForm(Course original,int sourceWeek,boolean cancel,Runnable back){
  String baseline=data,adjustmentBaseline=adjustments,termBaseline=term;
  LinearLayout body=secondary(cancel?"本次停课":"本次调课","仅调整下面这一次课程，其他教学周和原始课表保留。重新同步后，原课程未改变的调整继续生效。",back);
  LinearLayout card=panel(body);card.addView(text(SchoolParser.clean(original.name),20,INK));detailRow(card,"原安排",arrangement(original,sourceWeek));
  CourseAdjustment previous=null;for(CourseAdjustment a:CourseAdjustment.parse(adjustments))if(a.key().equals(term+":"+CourseDisplay.key(sourceWeek,original)))previous=a;
  Course initial=previous!=null&&previous.replacement!=null?previous.replacement:original;
  Spinner targetWeek=null,day=null,start=null,end=null;EditText room=null;
  if(!cancel){
   String[] weeks=new String[30];for(int i=0;i<30;i++)weeks[i]="第 "+(i+1)+" 周";
   targetWeek=courseChoice(card,"目标教学周",weeks,previous!=null&&previous.replacement!=null?Integer.parseInt(initial.weeks)-1:sourceWeek-1,"adjustment-week");
   if(initial!=null&&initial.teacher.isEmpty()&&!initial.room.isEmpty()){TextView legacy=text("旧课表的老师可能包含在地点信息中，可在此手动分开填写。",12,MUTED);legacy.setPadding(0,dp(12),0,0);card.addView(legacy);}
   day=courseChoice(card,"星期",new String[]{"星期一","星期二","星期三","星期四","星期五","星期六","星期日"},initial.day-1,"adjustment-day");String[] units=new String[START_TIMES.length];for(int i=0;i<units.length;i++)units[i]="第 "+(i+1)+" 节";
   start=courseChoice(card,"开始节次",units,initial.start-1,"adjustment-start");end=courseChoice(card,"结束节次",units,initial.end-1,"adjustment-end");room=courseField(card,"上课教室",SchoolParser.clean(initial.room),"教室 / 线上（选填）","adjustment-room");
  }else detailRow(card,"停课范围","仅取消原安排这一次；可在设置的调整记录中恢复。");
  Spinner chosenWeek=targetWeek,chosenDay=day,chosenStart=start,chosenEnd=end;EditText chosenRoom=room;
  TextView error=text("",13,DANGER);error.setTag("adjustment-error");error.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);body.addView(error);
  Button submit=action(body,cancel?"确认本次停课":"保存本次调课",()->{
   if(!data.equals(baseline)||!adjustments.equals(adjustmentBaseline)||!term.equals(termBaseline)){error.setText("课表或调整记录已更新，请返回后重新打开");return;}
   try{
    Course replacement=cancel?null:new Course(chosenDay.getSelectedItemPosition()+1,chosenStart.getSelectedItemPosition()+1,chosenEnd.getSelectedItemPosition()+1,original.name,SchoolParser.clean(chosenRoom.getText().toString()),""+(chosenWeek.getSelectedItemPosition()+1),original.teacher,original.details,original.className);
    CourseAdjustment next=new CourseAdjustment(term,sourceWeek,original,replacement);if(!next.applies(Course.parse(data),term))throw new IllegalArgumentException("原课程已更新，请重新打开");storeAdjustment(next);
    ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(root.getWindowToken(),0);home();toast(cancel?"本次课程已停课":"本次课程已调课");
   }catch(Exception e){error.setText("未保存："+e.getMessage());}
  });submit.setTag("save-adjustment");if(cancel){body.removeView(submit);LinearLayout.LayoutParams footer=new LinearLayout.LayoutParams(-2,-2);footer.gravity=Gravity.CENTER_HORIZONTAL;footer.setMargins(dp(20),dp(8),dp(20),dp(16));root.addView(submit,footer);}else saveHeader(body,submit);
  if(!cancel){choiceSheet=new CourseChoiceSheet();windowContent.addView(choiceSheet,new FrameLayout.LayoutParams(-1,-1));}
 }
 private void adjustmentManager(){
  LinearLayout body=secondary("课程调整","调整仅保存在本机。恢复后按原课表显示；原课程或开学日期变化的记录不生效。",this::timetableSettings);List<CourseAdjustment> rows;
  try{rows=CourseAdjustment.parse(adjustments);}catch(Exception e){body.addView(text("调整记录格式异常，原记录保留，请先修复数据。",14,DANGER));return;}
  if(rows.isEmpty()){body.addView(text("暂无单次调整。点击课表中的课程，在详情中选择调课或停课。",14,MUTED));return;}
  List<Course> courses=Course.parse(data);
  for(int i=0;i<rows.size();i++){CourseAdjustment a=rows.get(i);boolean valid=a.applies(courses,term);LinearLayout card=panel(body);card.setTag("adjustment-record-"+i);card.addView(text(SchoolParser.clean(a.original.name)+" · "+(a.replacement==null?"停课":"调课"),18,INK));detailRow(card,"原安排",arrangement(a.original,a.week));if(a.replacement!=null)detailRow(card,"调至",arrangement(a.replacement,Integer.parseInt(a.replacement.weeks)));if(!valid)detailRow(card,"状态","原课程或学期已变更，本记录不生效");
   if(valid)action(card,"修改为调课",()->adjustmentForm(a.original,a.week,false,()->adjustmentManager())).setTag("edit-adjustment-"+i);
   String snapshot=adjustments;action(card,valid?"恢复原安排":"移除失效记录",()->{
    if(!adjustments.equals(snapshot)){toast("调整记录已更新，请重新打开");adjustmentManager();return;}
    List<String> remaining=new ArrayList<>();for(CourseAdjustment other:rows)if(!other.key().equals(a.key()))remaining.add(other.encode());String value=String.join("\n",remaining);if(!getPreferences(0).edit().putString("courseAdjustments",value).commit()){toast("本机保存失败，请重试");return;}adjustments=value;CourseWidget.updateAll(this);home();toast(valid?"已恢复原安排":"已移除失效记录");
   }).setTag("restore-adjustment-"+i);
  }
 }
 private void formValues(View view,List<Object> values){
  if(view instanceof DatePicker){DatePicker date=(DatePicker)view;values.add(date.getYear()+"-"+date.getMonth()+"-"+date.getDayOfMonth());}
  if(view instanceof EditText)values.add(((EditText)view).getText().toString());
  else if(view instanceof Spinner)values.add(((Spinner)view).getSelectedItemPosition());
  else if(view instanceof CompoundButton)values.add(((CompoundButton)view).isChecked());
  if(view.getTag()!=null&&view.getTag().toString().startsWith("course-slot-"))values.add(view.getTag());
  if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++)formValues(group.getChildAt(i),values);}
 }
 private void saveHeader(LinearLayout body,Button button){
  body.removeView(button);button.setText("保存");button.setContentDescription("保存");button.setMinWidth(0);button.setMinimumWidth(0);button.setMinHeight(dp(48));button.setMinimumHeight(dp(48));button.setPadding(dp(12),0,dp(12),0);
  LinearLayout bar=(LinearLayout)root.getChildAt(0);bar.removeView(bar.findViewWithTag("header-spacer"));bar.addView(new View(this),1,new LinearLayout.LayoutParams(dp(56),1));button.setSingleLine(true);bar.addView(button,new LinearLayout.LayoutParams(-2,-2));
  List<Object> original=new ArrayList<>();formValues(body,original);Runnable back=pageBack;
  Runnable discard=()->{returningPage=true;try{back.run();}finally{returningPage=false;}};
  pageBack=()->{
   List<Object> current=new ArrayList<>();formValues(body,current);
   if(current.equals(original)){back.run();return;}
   if(unsavedDialog!=null&&unsavedDialog.isShowing())return;
   AlertDialog prompt=new AlertDialog.Builder(this,R.style.SecondaryDialog).setTitle("修改尚未保存").setMessage("退出前是否保存？").setPositiveButton("保存并退出",null).setNegativeButton("不保存退出",null).create();
   unsavedDialog=prompt;prompt.setOnDismissListener(dialog->{if(unsavedDialog==prompt)unsavedDialog=null;});prompt.show();styleDialog(prompt,false);
   prompt.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{prompt.dismiss();button.performClick();});
   prompt.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v->{prompt.dismiss();discard.run();});
  };
 }
 EditText courseField(LinearLayout card,String label,String value,String hint,String tag){TextView caption=text(label,14,MUTED);caption.setPadding(0,dp(16),0,dp(8));card.addView(caption);EditText input=field(hint,false);input.setText(value);input.setContentDescription(label);input.setTag(tag);card.addView(input,new LinearLayout.LayoutParams(-1,-2));return input;}
 private CourseChoiceSheet courseChoicePicker(Spinner choice,String label,String[] values){
  if(choiceSheet==null){choiceSheet=new CourseChoiceSheet();windowContent.addView(choiceSheet,new FrameLayout.LayoutParams(-1,-1));}
  ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(choice.getWindowToken(),0);
  choiceSheet.open(choice,label,values);return choiceSheet;
 }
 private class CourseChoiceSheet extends MotionOverlay {
  final View shade;final LinearLayout panel;final TextView title,hint;final ScrollView scroll;final LinearLayout rows;final RadioButton[] options=new RadioButton[30];
  Spinner selected;boolean open;int generation;final int formAccessibility=root.getImportantForAccessibility();
  CourseChoiceSheet(){
   super(MainActivity.this);setVisibility(INVISIBLE);setFocusableInTouchMode(true);setClickable(true);
   shade=new View(MainActivity.this);shade.setBackgroundColor(0x26000000);shade.setTag("choice-shade");addView(shade,new FrameLayout.LayoutParams(-1,-1));shade.setOnClickListener(v->close(true));
   panel=new LinearLayout(MainActivity.this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(dp(16),dp(12),dp(16),dp(12));panel.setBackground(secondaryUi.sheetSurface());panel.setClipToOutline(true);panel.setClickable(true);FrameLayout.LayoutParams panelBounds=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);addView(panel,panelBounds);
   View handle=new View(MainActivity.this);handle.setBackground(bg(HANDLE,2));handle.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);LinearLayout.LayoutParams grip=new LinearLayout.LayoutParams(dp(36),dp(4));grip.gravity=Gravity.CENTER_HORIZONTAL;grip.bottomMargin=dp(16);panel.addView(handle,grip);
   title=text("",18,NAVY);title.setTypeface(null,Typeface.BOLD);panel.addView(title);
   hint=text("",14,MUTED);hint.setPadding(0,dp(8),0,dp(16));panel.addView(hint);
   scroll=new ScrollView(MainActivity.this);scroll.setClipToPadding(false);panel.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));rows=new LinearLayout(MainActivity.this);rows.setOrientation(LinearLayout.VERTICAL);scroll.addView(rows);
   for(int i=0;i<options.length;i++){
    final int position=i;RadioButton option=new RadioButton(MainActivity.this);options[i]=option;option.setButtonDrawable(null);option.setGravity(Gravity.CENTER);option.setTextSize(15);option.setPadding(dp(8),dp(10),dp(8),dp(10));option.setTag("choice-option-"+i);option.setTextColor(NAVY);option.setLineSpacing(dp(3),1);option.setOnClickListener(v->{if(!open)return;selected.setSelection(position);close(true);});
   }
   Button cancel=action(panel,"取消",()->close(true));cancel.setTag("choice-cancel");cancel.setTextColor(NAVY);cancel.setBackgroundTintList(android.content.res.ColorStateList.valueOf(SURFACE));LinearLayout.LayoutParams footer=(LinearLayout.LayoutParams)cancel.getLayoutParams();footer.topMargin=dp(16);footer.bottomMargin=0;
   bindMotion(panel,shade,false,()->close(true));
  }
  @Override protected void onMeasure(int widthSpec,int heightSpec){
   FrameLayout.LayoutParams lp=(FrameLayout.LayoutParams)panel.getLayoutParams();lp.width=Math.max(1,MeasureSpec.getSize(widthSpec));lp.height=Math.max(1,(MeasureSpec.getSize(heightSpec)-getPaddingBottom())/2);super.onMeasure(widthSpec,heightSpec);
  }
  void open(Spinner choice,String label,String[] values){
   if(open&&selected==choice)return;int token=++generation;open=true;selected=choice;motion.stop();title.setText("选择"+label);hint.setText("当前："+values[choice.getSelectedItemPosition()]+" · 点选后返回");
   boolean units=label.endsWith("节次"),weeks=values.length==30&&!units;int columns=weeks?5:2;
   for(RadioButton option:options)if(option.getParent()!=null)((ViewGroup)option.getParent()).removeView(option);rows.removeAllViews();
   LinearLayout row=null;
   for(int i=0;i<options.length;i++){
    RadioButton option=options[i];option.setVisibility(i<values.length?VISIBLE:GONE);if(i>=values.length)continue;
    if(i%columns==0){row=new LinearLayout(MainActivity.this);LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.bottomMargin=dp(8);rows.addView(row,rp);}
    boolean current=i==choice.getSelectedItemPosition();String caption=weeks?""+(i+1):values[i];if(units)caption+="\n"+START_TIMES[i]+"–"+END_TIMES[i];if(current)caption+="  ✓";option.setText(caption);option.setChecked(current);option.setTypeface(null,current?Typeface.BOLD:Typeface.NORMAL);option.setMinHeight(dp(weeks?48:units?72:56));GradientDrawable surface=bg(current?SOFT:SURFACE,16);if(current)surface.setStroke(dp(1.5f),PRIMARY);option.setBackground(surface);focusOutline(option);option.setContentDescription(label+"，"+values[i]+(units?"，"+START_TIMES[i]+"至"+END_TIMES[i]:"")+(current?"，已选中":""));LinearLayout.LayoutParams cell=new LinearLayout.LayoutParams(0,-2,1);if(i%columns>0)cell.leftMargin=dp(8);row.addView(option,cell);
   }
   if(values.length%columns!=0)for(int i=values.length%columns;i<columns;i++){Space space=new Space(MainActivity.this);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,1,1);lp.leftMargin=dp(8);row.addView(space,lp);}
   root.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);setVisibility(VISIBLE);updateSystemBack();updateOverlayAppearance();requestFocus();if(Build.VERSION.SDK_INT>=28)setAccessibilityPaneTitle("选择"+label);requestLayout();if(motion.value==0)motion.set(0);
   getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(){public boolean onPreDraw(){getViewTreeObserver().removeOnPreDrawListener(this);if(token!=generation||!open||!isAttachedToWindow())return true;RadioButton option=options[choice.getSelectedItemPosition()];android.graphics.Rect rect=new android.graphics.Rect(0,0,option.getWidth(),option.getHeight());rows.offsetDescendantRectToMyCoords(option,rect);scroll.scrollTo(0,Math.max(0,rect.top-dp(60)));showMotion();return true;}});
  }

  void close(boolean animate){
   scheduleRemoteNotice();
   if(animate&&!open)return;open=false;generation++;hideMotion(animate,()->{setVisibility(INVISIBLE);updateSystemBack();root.setImportantForAccessibility(formAccessibility);if(selected!=null&&selected.isAttachedToWindow())selected.requestFocus();updateOverlayAppearance();});
  }
 }

 private Spinner courseChoice(LinearLayout card,String label,String[] values,int selected,String tag){TextView caption=text(label,14,MUTED);caption.setPadding(0,dp(16),0,dp(8));card.addView(caption);Spinner choice=new Spinner(this){@Override public boolean performClick(){courseChoicePicker(this,label,values);return true;}};ArrayAdapter<String> adapter=new ArrayAdapter<>(this,R.layout.course_choice,values);choice.setAdapter(adapter);choice.setSelection(selected);choice.setBackgroundResource(R.drawable.form_field);choice.setContentDescription(label);choice.setTag(tag);choice.setMinimumHeight(dp(SecondaryUi.CONTROL_HEIGHT));card.addView(choice,new LinearLayout.LayoutParams(-1,-2));return choice;}
 private final class CourseSlot {
  final Course original;final int number;final LinearLayout card;final EditText teacher,room,details;final Spinner day,start,end;final ToggleButton[] selected=new ToggleButton[30];
  CourseSlot(LinearLayout body,Course initial,int number){
   original=initial;this.number=number;String suffix=number==1?"":"-slot-"+number;card=panel(body);card.setTag("course-slot-"+number);card.addView(text("上课时段 "+number,18,INK));
   room=courseField(card,"上课教室",initial==null?"":SchoolParser.clean(initial.room),"教室 / 线上（选填）","course-room"+suffix);
   teacher=courseField(card,"任课老师",initial==null?"":SchoolParser.clean(initial.teacher),"老师（选填）","course-teacher"+suffix);
   details=courseField(card,"课程内容 / 实验项目",initial==null?"":SchoolParser.clean(initial.displayDetails()),"具体内容（选填）","course-details"+suffix);
   if(initial!=null&&initial.teacher.isEmpty()&&!initial.room.isEmpty()){TextView legacy=text("旧课表的老师可能包含在地点信息中，可在此手动分开填写。",12,MUTED);legacy.setPadding(0,dp(12),0,0);card.addView(legacy);}
   day=courseChoice(card,"星期",new String[]{"星期一","星期二","星期三","星期四","星期五","星期六","星期日"},initial==null?0:initial.day-1,"course-day"+suffix);
   String[] units=new String[START_TIMES.length];for(int i=0;i<units.length;i++)units[i]="第 "+(i+1)+" 节";
   start=courseChoice(card,"开始节次",units,initial==null?0:initial.start-1,"course-start"+suffix);end=courseChoice(card,"结束节次",units,initial==null?1:initial.end-1,"course-end"+suffix);
   LinearLayout weeks=card;weeks.addView(text("具体周次 · 可多选",18,INK));TextView summary=text("",13,MUTED);summary.setTag("course-weeks-summary"+suffix);summary.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);summary.setPadding(0,dp(12),0,dp(8));weeks.addView(summary);Runnable update=()->{StringBuilder value=new StringBuilder();for(int w=1;w<=30;w++)if(selected[w-1]!=null&&selected[w-1].isChecked()){if(value.length()>0)value.append(",");value.append(w);}summary.setText(value.length()==0?"请选择至少一个教学周":CourseDisplay.prettyWeeks(new Course(1,1,1,"","",value.toString())));summary.setTextColor(value.length()==0?DANGER:MUTED);};LinearLayout quick=new LinearLayout(MainActivity.this);weeks.addView(quick);String[] shortcuts={"全选","单周","双周","清空"};for(int i=0;i<4;i++){final int mode=i;Button button=new Button(MainActivity.this);button.setText(shortcuts[i]);button.setStateListAnimator(null);button.setElevation(0);button.setTranslationZ(0);button.setBackgroundTintList(null);button.setTextSize(12);button.setAllCaps(false);button.setPadding(0,0,0,0);button.setMinWidth(0);button.setTextColor(PRIMARY);button.setBackgroundResource(R.drawable.form_field);button.setTag("course-weeks-"+i+suffix);button.setOnClickListener(v->{for(int w=1;w<=30;w++)selected[w-1].setChecked(mode==0||(mode==1&&w%2==1)||(mode==2&&w%2==0));update.run();});LinearLayout.LayoutParams qp=new LinearLayout.LayoutParams(0,dp(48),1);if(i>0)qp.leftMargin=dp(6);quick.addView(button,qp);}for(int first=1;first<=30;first+=5){LinearLayout row=new LinearLayout(MainActivity.this);LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.topMargin=dp(8);weeks.addView(row,rp);for(int w=first;w<first+5;w++){ToggleButton button=new ToggleButton(MainActivity.this);selected[w-1]=button;button.setStateListAnimator(null);button.setElevation(0);button.setTranslationZ(0);button.setBackgroundTintList(null);button.setTextOn(""+w);button.setTextOff(""+w);button.setTextColor(INK);button.setTextSize(14);button.setMinWidth(0);button.setPadding(0,0,0,0);button.setTag("course-week-"+w+suffix);button.setContentDescription("第"+w+"周");button.setBackgroundResource(R.drawable.form_field);button.setOnCheckedChangeListener((v,on)->{if(on)button.setBackground(bg(PRIMARY,10));else button.setBackgroundResource(R.drawable.form_field);button.setTextColor(on?ON_PRIMARY:INK);update.run();});button.setChecked(initial==null?w==week:initial.active(w));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dp(48),1);if(w>first)bp.leftMargin=dp(6);row.addView(button,bp);}}update.run();
  }
  Course value(String name){
   if(end.getSelectedItemPosition()<start.getSelectedItemPosition())throw new IllegalArgumentException("结束节次不能早于开始节次");
   StringJoiner weeks=new StringJoiner(",");for(int w=1;w<=30;w++)if(selected[w-1].isChecked())weeks.add(""+w);
   if(weeks.length()==0)throw new IllegalArgumentException("请选择至少一个教学周");
   String content=SchoolParser.clean(details.getText().toString()),classes=original==null?"":original.className;
   if(original!=null&&content.equals(original.displayDetails())){
    content=original.details;
    if(!name.equals(original.name)){List<String> visible=new ArrayList<>();for(String part:content.split("；"))if(Course.isTeachingClassName(original.name,part))classes=Course.optionalText(classes+"；"+part);else visible.add(part);content=String.join("；",visible);}
   }
   if(original!=null&&original.schoolAdjusted()&&!Arrays.asList(content.split("；")).contains(Course.SCHOOL_ADJUSTED))content=Course.optionalText(content+"；"+Course.SCHOOL_ADJUSTED);
   Course next=new Course(day.getSelectedItemPosition()+1,start.getSelectedItemPosition()+1,end.getSelectedItemPosition()+1,name,SchoolParser.clean(room.getText().toString()),weeks.toString(),SchoolParser.clean(teacher.getText().toString()),content,classes);
   Course.parse(next.encode());return next;
  }
 }
 private void courseForm(Course initial,int index,Runnable back){
  String baseline=data;List<Course> courses=Course.parse(baseline);
  if(initial!=null&&(index<0||index>=courses.size()||!courses.get(index).encode().equals(initial.encode()))){toast("课程已更新，请重新打开课程");home();return;}
  LinearLayout body=secondary(initial==null?"添加课程":"校对课程","同一课程可有多个上课时段，分别设置教室、节次与周次。重新同步会覆盖本机修改。",back);
  LinearLayout info=panel(body);EditText name=courseField(info,"课程名称 · 必填",initial==null?"":SchoolParser.clean(initial.name),"例如：数据结构","course-name");
  LinearLayout schedules=new LinearLayout(this);schedules.setOrientation(LinearLayout.VERTICAL);body.addView(schedules);List<CourseSlot> slots=new ArrayList<>();int[] serial={0};
  java.util.function.Consumer<Course> addSlot=source->{CourseSlot slot=new CourseSlot(schedules,source,++serial[0]);slots.add(slot);Button remove=action(slot.card,"移除此时段",()->{if(slots.size()==1){toast("请至少保留一个上课时段");return;}slots.remove(slot);schedules.removeView(slot.card);});remove.setTag("remove-course-slot-"+serial[0]);};
  if(initial==null)addSlot.accept(null);else for(Course c:courses)if(c.name.equals(initial.name))addSlot.accept(c);
  Button add=action(body,"＋ 添加上课时段",()->addSlot.accept(null));add.setTag("add-course-slot");
  TextView error=text("",13,DANGER);error.setTag("course-error");error.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);body.addView(error);
  Button saveButton=action(body,initial==null?"添加并保存":"保存修改",()->{
   if(name.getText().toString().trim().isEmpty()){name.setError("请填写课程名称");name.requestFocus();return;}
   if(!data.equals(baseline)){error.setText("课表已更新，请返回后重新打开课程");return;}
   try{
    List<Course> next=new ArrayList<>();for(int i=0;i<slots.size();i++)try{next.add(slots.get(i).value(SchoolParser.clean(name.getText().toString())));}catch(IllegalArgumentException e){throw new IllegalArgumentException("时段 "+slots.get(i).number+"："+e.getMessage());}
    List<String> rows=new ArrayList<>();for(Course c:courses){if(initial!=null&&c.name.equals(initial.name)){for(int i=0;i<slots.size();i++)if(slots.get(i).original==c)rows.add(next.get(i).encode());}else rows.add(c.encode());}
    for(int i=0;i<slots.size();i++)if(slots.get(i).original==null)rows.add(next.get(i).encode());
    String updated=String.join("\n",rows);Course.parse(updated);data=updated;save();((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(name.getWindowToken(),0);home();toast(initial==null?"课程已添加":"课程已更新");
   }catch(Exception e){error.setText("课程未保存："+e.getMessage());}
  });
  saveButton.setTag("save-course");saveHeader(body,saveButton);choiceSheet=new CourseChoiceSheet();windowContent.addView(choiceSheet,new FrameLayout.LayoutParams(-1,-1));
 }
 private void acceptImport(String value,SchoolClient client,String source){acceptImport(value,client,source,"");}
 private void acceptImport(String value,SchoolClient client,String source,String suffix){
  client.classTimes.validate(Course.parse(value));if(source.equals(schoolId)&&getPreferences(0).getBoolean("schoolImported",false)){try{applyTimetableSync(value,client.classTimes,source);if(password!=null)password.getText().clear();if(primaryTab()!=0)home();}catch(Exception e){toast("同步失败，原课表已保留。\n"+e.getMessage());}return;}getPreferences(0).edit().putString(TimetableSync.BASELINE,value).putString("syncClassTimes",client.classTimes.encode()).putString("syncSource",source).remove(TimetableSync.PROTECTED).apply();setTimes(client.classTimes);schoolId=source;data=value;notes="已从学校同步 · "+Course.parse(value).size()+" 条课程\n"+client.courseNotes;getPreferences(0).edit().putBoolean("schoolImported",true).apply();week=currentWeek();homePage=null;save();if(password!=null)password.getText().clear();home();toast("课表同步成功"+suffix);
 }
 private void openWebTimetable(){
  if(busy){toast("请等待同步结束");return;}startActivityForResult(new Intent(this,WebTimetableActivity.class).putExtra("timetable",Timetables.active(this)).putExtra("baseline",data).putExtra("times",classTimes.encode()).putExtra("url",getPreferences(0).getString("webTimetableUrl","")),920);
 }
 private void acceptWebTimetable(Intent result){
  if(!Timetables.active(this).equals(result.getStringExtra("timetable"))||!data.equals(result.getStringExtra("baseline"))){toast("课表已切换或修改，请重新导入");return;}
  try{String value=result.getStringExtra("courses");List<Course> courses=Course.parse(value);if(courses.isEmpty())throw new IllegalArgumentException("未识别到课程");ClassTimes times=ClassTimes.parse(result.getStringExtra("times"));times.validate(courses);String url=result.getStringExtra("url");if(!WebTimetableActivity.allowed(url))throw new IllegalArgumentException("网页地址无效");
   String syncUrl=result.getStringExtra("syncUrl");if(syncUrl==null)syncUrl=url;
   if(schoolId.equals("web")&&getPreferences(0).getBoolean("schoolImported",false)){schoolAccount.saveWebSource(Timetables.active(this),syncUrl);applyTimetableSync(value,times,"web");return;}
   if(!getPreferences(0).edit().putString("courses",value).putString("classTimes",times.encode()).putString("school","web").putString(TimetableSync.BASELINE,value).putString("syncSource","web").putString("syncClassTimes",times.encode()).remove(TimetableSync.PROTECTED).putBoolean("schoolImported",true).putString("webTimetableUrl",url).putString("notes","已从网页导入 · "+courses.size()+" 条课程").commit())throw new IllegalStateException("本机保存失败");
   try{schoolAccount.remove(Timetables.active(this));}catch(Exception e){toast("旧学校账号未能删除，请在账号管理中重新配置");}try{schoolAccount.saveWebSource(Timetables.active(this),syncUrl);}catch(Exception e){toast("课表已导入，但同步来源未能加密保存，请重新导入后同步。");}reloadTimetable();toast("网页课表已导入");
  }catch(Exception e){toast("导入失败："+e.getMessage());}
 }
 private void refreshTimetable(){
  if(busy){toast("同步正在进行中");return;}if(schoolId.equals("web")){refreshWebTimetable();return;}if(!loginSchool.equals(schoolId)){loginSchool=schoolId;school=schoolClient(schoolId);}SchoolClient client=school;String source=schoolId,id=Timetables.active(this),baseline=data,oldTimes=classTimes.encode(),oldTerm=term,oldAdjustments=adjustments;int token=screen;
  busy=true;updateSystemBack();View refresh=root.findViewWithTag("refresh-timetable");if(refresh!=null){refresh.setEnabled(false);refresh.setContentDescription("正在刷新课表");}toast("正在刷新课表…");
  worker.execute(()->{
   try{
    try{client.prepare();}catch(SchoolClient.AuthRequired ignored){}
    String next=client.cachedCourses;
    if(next.isEmpty()){
     SchoolAccount.Saved saved;
     try{saved=schoolAccount.load(id);}catch(Exception e){String message=e.getMessage()==null?"保存的学校账号无法读取，请重新登录后刷新。":e.getMessage();runOnUiThread(()->{finishTimetableRefresh();if(isDestroyed()||screen!=token||!id.equals(Timetables.active(this)))return;toast(message);});return;}
     if(saved==null||!source.equals(saved.school)){runOnUiThread(()->{finishTimetableRefresh();if(isDestroyed()||screen!=token||!id.equals(Timetables.active(this)))return;toast("请在学校账号中重新登录后同步课表");});return;}
     if(client.captchaRequired)throw new SchoolClient.AuthRequired("学校要求验证码，请登录后刷新课表。");
     next=client.login(saved.account,saved.password,"");
    }
    if(next.isEmpty())throw new IllegalArgumentException("学校未返回课程，已保留原课表。");
    List<Course> courses=Course.parse(next);if(courses.isEmpty())throw new IllegalArgumentException("学校未返回课程，已保留原课表。");client.classTimes.validate(courses);
    String ready=next;
    runOnUiThread(()->{finishTimetableRefresh();if(isDestroyed()||screen!=token||!id.equals(Timetables.active(this)))return;if(!data.equals(baseline)||!term.equals(oldTerm)||!classTimes.encode().equals(oldTimes)||!adjustments.equals(oldAdjustments)){toast("课表已修改，请重新刷新");return;}try{applyTimetableSync(ready,client.classTimes,source);}catch(Exception e){toast("同步失败，原课表已保留。\n"+e.getMessage());}});
   }catch(SchoolClient.AuthRequired e){
    runOnUiThread(()->{finishTimetableRefresh();if(isDestroyed()||screen!=token||!id.equals(Timetables.active(this)))return;toast("请在学校账号中重新登录后同步。\n"+e.getMessage());});
   }catch(Exception e){
    runOnUiThread(()->{finishTimetableRefresh();if(!isDestroyed()&&screen==token)toast("刷新失败，已保留原课表。\n"+e.getMessage());});
   }
  });
 }
 private void applyTimetableSync(String remoteCourses,ClassTimes remoteTimes,String source){
  if(Course.parse(remoteCourses).isEmpty())throw new IllegalArgumentException("来源未返回课程");
  SharedPreferences prefs=getPreferences(0);if(!source.equals(schoolId))throw new IllegalStateException("课表来源已变化");
  if(prefs.contains("syncSource")&&!source.equals(prefs.getString("syncSource","")))throw new IllegalStateException("同步来源不匹配，请重新导入");
  String baseline=prefs.getString(TimetableSync.BASELINE,null);TimetableSync.Result merged=TimetableSync.merge(baseline,data,remoteCourses,prefs.getStringSet(TimetableSync.PROTECTED,Collections.emptySet()),adjustments);
  ClassTimes nextTimes=remoteTimes!=null&&baseline!=null&&classTimes.encode().equals(prefs.getString("syncClassTimes",""))?remoteTimes:classTimes;
  nextTimes.validate(Course.parse(merged.courses));for(CourseAdjustment change:CourseAdjustment.parse(adjustments))if(change.replacement!=null)nextTimes.validate(Collections.singletonList(change.replacement));
  boolean changed=!data.equals(merged.courses)||!classTimes.encode().equals(nextTimes.encode());Map<String,?> before=prefs.getAll();
  if(!prefs.edit().putString("courses",merged.courses).putString("classTimes",nextTimes.encode()).putString(TimetableSync.BASELINE,remoteCourses).putStringSet(TimetableSync.PROTECTED,merged.protectedNames).putString("syncClassTimes",remoteTimes==null?classTimes.encode():remoteTimes.encode()).putString("syncSource",source).putBoolean("schoolImported",true).commit()){
   SharedPreferences.Editor rollback=prefs.edit();for(String key:new String[]{"courses","classTimes",TimetableSync.BASELINE,TimetableSync.PROTECTED,"syncClassTimes","syncSource","schoolImported"}){Object value=before.get(key);if(value==null)rollback.remove(key);else if(value instanceof String)rollback.putString(key,(String)value);else if(value instanceof Boolean)rollback.putBoolean(key,(Boolean)value);else if(value instanceof Set)rollback.putStringSet(key,(Set<String>)value);}rollback.commit();throw new IllegalStateException("本机保存失败");
  }
  data=merged.courses;setTimes(nextTimes);homePage=null;cachedData=null;CourseWidget.updateAll(this);if(primaryTab()==0)home();
  toast(baseline==null?"已建立来源基线，现有课程保留，下次同步将检查来源更新。":changed?"课表已同步，本地修改和删除已保留。":"来源课表没有可应用的更新。");
 }
 private void refreshWebTimetable(){
  String url;try{url=schoolAccount.webSource(Timetables.active(this));}catch(Exception e){toast("同步来源无法解密，请重新网页导入。");return;}
  if(url==null){toast("请先重新网页导入一次，以绑定此课表的同步来源。");return;}
  String id=Timetables.active(this),baseline=data,oldTimes=classTimes.encode(),oldTerm=term,oldAdjustments=adjustments;int token=screen;
  busy=true;updateSystemBack();toast("正在后台同步课表…");
  try{webTimetableSync=new WebTimetableSync(this,url,worker,(courses,times,error)->{
   webTimetableSync=null;finishTimetableRefresh();if(isDestroyed()||screen!=token||!id.equals(Timetables.active(this)))return;
   if(error!=null){toast(error+" 原课表已保留。");return;}
   if(!data.equals(baseline)||!term.equals(oldTerm)||!classTimes.encode().equals(oldTimes)||!adjustments.equals(oldAdjustments)){toast("课表已修改，请重新同步");return;}
   try{applyTimetableSync(courses,times,"web");}catch(Exception e){toast("同步失败，原课表已保留。\n"+e.getMessage());}
  });}catch(Exception e){finishTimetableRefresh();toast("无法读取同步来源，原课表已保留。");}
 }
 private void finishTimetableRefresh(){busy=false;if(isDestroyed())return;updateSystemBack();View refresh=root.findViewWithTag("refresh-timetable");if(refresh!=null){refresh.setEnabled(true);refresh.setContentDescription("刷新课表");}}
 private void schoolPicker(Runnable back){
  if(busy){toast("请等待同步结束");return;}NavigationOverlay menu=new NavigationOverlay(true,"选择学校",root.findViewWithTag("select-school"));String[] ids={"squ","xzit"},names={"宿迁学院","徐州工程学院"};
  menuItem(menu,"其他学校（网页导入）","school-web",this::openWebTimetable);
  for(int i=0;i<ids.length;i++){String id=ids[i];TextView item=menuItem(menu,names[i],"school-"+id,()->{if(password!=null)password.getText().clear();if(account!=null)account.getText().clear();loginAccountPrefill=null;SchoolClient previous=school;worker.execute(previous::logout);loginSchool=id;school=schoolClient(id);skipPageMotion=true;try{login(back);}finally{skipPageMotion=false;}});secondaryUi.popupChecked(item,id.equals(loginSchool));}
 }
 private void reloadTimetable(){reloadTimetable(this::home);}
 private void reloadTimetable(Runnable destination){
  if(password!=null)password.getText().clear();account=null;password=null;loginAccountPrefill=null;SchoolClient previous=school;worker.execute(previous::logout);SharedPreferences prefs=getPreferences(0);
  data=prefs.getString("courses","");notes=prefs.getString("notes","");term=prefs.getString("term","2026-08-31");week=prefs.getInt("week",1);adjustments=prefs.getString("courseAdjustments","");displayChoices=new HashSet<>(prefs.getStringSet("displayChoices",Collections.emptySet()));setTimes(ClassTimes.parse(prefs.getString("classTimes","")));schoolId=prefs.getString("school","squ");loginSchool=schoolId;school=schoolClient(schoolId);homePage=null;cachedData=null;timetableScrollY=0;settingsScrollY=0;settingsPage=null;destination.run();CourseWidget.updateAll(this);
 }
 private void timetablePicker(){
  if(busy){toast("请等待同步结束");return;}View anchor=root.findViewWithTag("timetable-settings");NavigationOverlay menu=new NavigationOverlay(true,"切换课表",anchor);menu.menuWidth=264;String current=Timetables.active(this);
  for(var entry:Timetables.names(this).entrySet()){String id=entry.getKey();menuItem(menu,entry.getValue(),"timetable-"+id,()->{try{Timetables.select(this,id);reloadTimetable();}catch(Exception e){toast(e.getMessage());}});TextView item=menu.body.findViewWithTag("timetable-"+id);item.setText(timetableCaption(id));item.setContentDescription(entry.getValue()+"，"+Timetables.sourceLabel(this,id)+(id.equals(current)?"，当前课表":""));secondaryUi.popupChecked(item,id.equals(current));item.setTooltipText("长按修改课表名称");item.setOnLongClickListener(v->{menu.close(true);renameTimetable(id);return true;});}
  secondaryUi.popupDivider(menu.body);menuItem(menu,"新增课表","new-timetable",this::newTimetable);
 }
 private CharSequence timetableCaption(String id){
  String name=Timetables.names(this).get(id),source=Timetables.sourceLabel(this,id);android.text.SpannableString caption=new android.text.SpannableString(name+"\n"+source);
  caption.setSpan(new android.text.style.StyleSpan(Typeface.BOLD),0,name.length(),0);caption.setSpan(new android.text.style.RelativeSizeSpan(12f/14f),name.length()+1,caption.length(),0);caption.setSpan(new android.text.style.ForegroundColorSpan(MUTED),name.length()+1,caption.length(),0);return caption;
 }
 private void newTimetable(){newTimetable(this::home);}
 private void newTimetable(Runnable back){
  if(busy){toast("请等待同步结束");return;}LinearLayout body=secondary("新增课表","每份课表独立保存，新增后仍可切回已有课表。",back);LinearLayout card=panel(body);EditText name=courseField(card,"课表名称","","例如：大二上学期","timetable-name");Spinner source=courseChoice(card,"创建方式",new String[]{"手动创建","宿迁学院导入","徐州工程学院导入","其他学校（网页导入）"},0,"timetable-source");TextView error=text("",13,DANGER);error.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);card.addView(error);
  Button create=action(body,"创建课表",()->{try{int selected=source.getSelectedItemPosition();String id=selected==3?"web":selected==2?"xzit":"squ";Timetables.add(this,name.getText().toString(),id,selected==2?ClassTimes.XZIT:ClassTimes.DEFAULT,term);reloadTimetable(this::home);if(selected==3)openWebTimetable();else if(selected>0)login();}catch(Exception e){error.setText(e.getMessage());}});create.setTag("create-timetable");saveHeader(body,create);choiceSheet=new CourseChoiceSheet();windowContent.addView(choiceSheet,new FrameLayout.LayoutParams(-1,-1));
 }
 private void renameTimetable(String id){renameTimetable(id,()->{home();timetablePicker();});}
 private void renameTimetable(String id,Runnable back){
  if(busy){toast("请等待同步结束");return;}
  LinearLayout body=secondary("修改课表名称","名称只用于区分课表，不影响课程和作息时间。",back);EditText name=courseField(panel(body),"课表名称",Timetables.names(this).get(id),"1–40 个字","rename-timetable-name");
  Button save=action(body,"保存课表名称",()->{try{Timetables.rename(this,id,name.getText().toString());back.run();}catch(Exception e){name.setError(e.getMessage());}});save.setTag("save-timetable-name");saveHeader(body,save);
 }
 private void timesForm(){
  LinearLayout body=secondary("作息时间","点击时间调整上课和下课时刻；按节次顺序排列，最多 30 节。学校导入时会更新当前课表的作息。",this::menu);
  LinearLayout card=panel(body);TextView summary=text("每天 "+START_TIMES.length+" 节",18,INK);summary.setTypeface(null,Typeface.BOLD);summary.setPadding(0,0,0,dp(12));card.addView(summary);
  LinearLayout headings=new LinearLayout(this);headings.setPadding(0,0,0,dp(8));TextView periodHeading=text("节次",13,MUTED);periodHeading.setGravity(Gravity.CENTER);headings.addView(periodHeading,new LinearLayout.LayoutParams(dp(52),-2));for(String label:new String[]{"上课时间","下课时间"}){TextView heading=text(label,13,MUTED);heading.setGravity(Gravity.CENTER);headings.addView(heading,new LinearLayout.LayoutParams(0,-2,1));}card.addView(headings);
  LinearLayout rows=new LinearLayout(this);rows.setOrientation(LinearLayout.VERTICAL);rows.setTag("class-times");card.addView(rows);for(int i=0;i<START_TIMES.length;i++)addClassTimeRow(rows,START_TIMES[i],END_TIMES[i]);
  TextView error=text("",13,DANGER);error.setTag("class-times-error");error.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);card.addView(error,1);
  LinearLayout actions=new LinearLayout(this);card.addView(actions);
  Button add=action(actions,"新增一节",()->{try{int count=rows.getChildCount();if(count>=ClassTimes.MAX_PERIODS)throw new IllegalArgumentException("每天最多 30 节课。");LinearLayout last=(LinearLayout)rows.getChildAt(count-1);java.time.LocalTime end=java.time.LocalTime.parse(((EditText)last.getChildAt(2)).getText());if(end.isAfter(java.time.LocalTime.of(23,4)))throw new IllegalArgumentException("当天已没有足够时间，请先调整末节时间。");addClassTimeRow(rows,end.plusMinutes(10).toString(),end.plusMinutes(55).toString());error.setText("");}catch(Exception e){error.setText(e.getMessage());error.post(()->error.requestRectangleOnScreen(new Rect(0,0,error.getWidth(),error.getHeight()),false));}});add.setTag("add-class-time");
  Button remove=action(actions,"移除末节",()->{rows.removeViewAt(rows.getChildCount()-1);error.setText("");});remove.setTag("remove-class-time");
  for(Button control:new Button[]{add,remove}){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(dp(4),dp(12),dp(4),0);actions.updateViewLayout(control,lp);}
  Runnable update=()->{int count=rows.getChildCount();summary.setText("每天 "+count+" 节");add.setEnabled(count<ClassTimes.MAX_PERIODS);remove.setEnabled(count>1);};update.run();rows.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener(){public void onChildViewAdded(View parent,View child){update.run();}public void onChildViewRemoved(View parent,View child){update.run();}});
  Button button=action(body,"保存作息时间",()->{try{String[] starts=new String[rows.getChildCount()],ends=new String[starts.length];for(int i=0;i<starts.length;i++){LinearLayout row=(LinearLayout)rows.getChildAt(i);starts[i]=((EditText)row.getChildAt(1)).getText().toString();ends[i]=((EditText)row.getChildAt(2)).getText().toString();}ClassTimes next=new ClassTimes(starts,ends);next.validate(Course.parse(data));for(CourseAdjustment change:CourseAdjustment.parse(adjustments))if(change.replacement!=null)next.validate(Collections.singletonList(change.replacement));setTimes(next);homePage=null;save();menu();}catch(Exception e){error.setText(e.getMessage());error.post(()->error.requestRectangleOnScreen(new Rect(0,0,error.getWidth(),error.getHeight()),false));}});button.setTag("save-class-times");saveHeader(body,button);
 }
 private void addClassTimeRow(LinearLayout rows,String start,String end){
  int number=rows.getChildCount()+1;LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);TextView label=text(String.valueOf(number),13,MUTED);label.setGravity(Gravity.CENTER);label.setContentDescription("第 "+number+" 节");row.addView(label,new LinearLayout.LayoutParams(dp(52),-2));
  for(int i=0;i<2;i++){EditText time=field("HH:mm",false);time.setText(i==0?start:end);time.setTag("class-time-"+number+(i==0?"-start":"-end"));time.setContentDescription("第 "+number+" 节"+(i==0?"上课时间":"下课时间"));time.setGravity(Gravity.CENTER);time.setPadding(dp(4),0,dp(4),0);time.setInputType(InputType.TYPE_NULL);time.setKeyListener(null);time.setFocusable(true);time.setShowSoftInputOnFocus(false);time.setCursorVisible(false);time.setOnClickListener(v->pickClassTime(time));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(52),1);lp.setMargins(dp(4),dp(4),dp(4),dp(4));row.addView(time,lp);}rows.addView(row);
 }
 private TimePickerDialog pickClassTime(EditText value){
  java.time.LocalTime current=java.time.LocalTime.parse(value.getText());TimePickerDialog dialog=new TimePickerDialog(this,R.style.SecondaryDialog,(picker,hour,minute)->value.setText(String.format(Locale.ROOT,"%02d:%02d",hour,minute)),current.getHour(),current.getMinute(),true);dialog.setTitle(value.getContentDescription());dialog.show();styleDialog(dialog,false);return dialog;
 }
 private EditText field(String hint,boolean secret){EditText e=new EditText(this);e.setSingleLine();e.setTextSize(16);e.setHint(hint);e.setTextColor(INK);e.setHintTextColor(MUTED);e.setPadding(dp(16),dp(8),dp(16),dp(8));e.setMinHeight(dp(SecondaryUi.CONTROL_HEIGHT));e.setBackgroundResource(R.drawable.form_field);e.setInputType(secret?InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD:InputType.TYPE_CLASS_TEXT);e.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);if(secret)e.setSaveEnabled(false);return e;}
 private void login(){login(this::home);}
 private void login(Runnable back){String prefill=loginAccountPrefill;loginAccountPrefill=null;String schoolName=loginSchool.equals("web")?"其他学校（网页导入）":loginSchool.equals("xzit")?"徐州工程学院":"宿迁学院";String filledAccount=account!=null&&account.isAttachedToWindow()?account.getText().toString():(prefill==null?"":prefill),filledPassword=password!=null&&password.isAttachedToWindow()?password.getText().toString():"";if(busy){toast("同步正在进行中");return;}LinearLayout body=secondary("学校账号",loginSchool.equals("web")?"通过教务网页登录并导入当前课表。":"登录成功后加密保存账号，用于同步当前课表；同步失败时保留原课程。",()->{if(password!=null)password.getText().clear();back.run();});
  option(settingsGroup(body,""),R.drawable.ic_person,schoolName,"选择学校",()->schoolPicker(back)).setTag("select-school");
  if(loginSchool.equals("web")){panel(body).addView(text("此课表通过教务网页导入，请在网页中登录学校账号。",15,MUTED));action(body,"打开教务网页",this::openWebTimetable).setTag("school-web-login");return;}
  LinearLayout card=panel(body);account=courseField(card,"学校账号",filledAccount,"学号 / 学校账号","school-account-input");password=courseField(card,"登录密码",filledPassword,"请输入密码","school-password-input");password.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);password.setSaveEnabled(false);password.setContentDescription("学校登录密码");CheckBox show=new CheckBox(this);show.setText("显示密码");show.setTextColor(INK);show.setMinHeight(dp(48));show.setOnCheckedChangeListener((b,on)->{int start=password.getSelectionStart(),end=password.getSelectionEnd();password.setTransformationMethod(on?null:android.text.method.PasswordTransformationMethod.getInstance());password.setSelection(Math.max(0,start),Math.max(0,end));});card.addView(show);
  captchaBox=new LinearLayout(this);captchaBox.setGravity(Gravity.CENTER_VERTICAL);captchaBox.setVisibility(View.GONE);captcha=field("验证码",false);captcha.setContentDescription("学校验证码");captchaImage=new ImageView(this);captchaImage.setContentDescription("点击刷新学校验证码");captchaImage.setFocusable(true);captchaImage.setOnClickListener(v->refreshCaptcha());captchaBox.addView(captcha,new LinearLayout.LayoutParams(0,-2,1));LinearLayout.LayoutParams cip=new LinearLayout.LayoutParams(dp(96),dp(SecondaryUi.CONTROL_HEIGHT));cip.leftMargin=dp(8);captchaBox.addView(captchaImage,cip);card.addView(captchaBox);
  loginStatus=text("正在连接学校…",13,MUTED);loginStatus.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);loginStatus.setPadding(0,dp(14),0,dp(8));card.addView(loginStatus);loginProgress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);loginProgress.setIndeterminate(true);loginProgress.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(BLUE));loginProgress.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);card.addView(loginProgress,new LinearLayout.LayoutParams(-1,dp(4)));loginButton=action(configurationActions(body),"登录并同步课表",()->submitLogin());loginButton.setEnabled(false);LinearLayout privacy=panel(body);privacy.addView(text("仅用于学校认证",15,INK));TextView note=text("登录成功后，账号和密码使用系统密钥加密保存在本机，仅用于本课表的自动刷新。\n验证码登录仍需手动完成；同步失败时会保留原课表，已导入的课程可以离线查看。",13,MUTED);note.setLineSpacing(dp(4),1);note.setPadding(0,dp(10),0,0);privacy.addView(note);if(loginSchool.equals("xzit")){TextView transport=text("学校当前使用 HTTP 连接。密码按学校方式加密，但账号和登录会话的传输未加密。",13,MUTED);transport.setPadding(0,dp(12),0,0);privacy.addView(transport);}
  SchoolClient client=school;String source=loginSchool;int token=screen;worker.execute(()->{try{client.prepare();if(!client.cachedCourses.isEmpty()){String synced=client.cachedCourses;client.classTimes.validate(Course.parse(synced));runOnUiThread(()->{if(screen!=token||isDestroyed())return;acceptImport(synced,client,source);});return;}byte[] image=client.captchaRequired?client.captcha():null;runOnUiThread(()->{if(screen!=token)return;loginProgress.setVisibility(View.GONE);loginStatus.setText("学校连接正常，请输入账号密码。");loginButton.setEnabled(true);if(image!=null){captchaBox.setVisibility(View.VISIBLE);captchaImage.setImageBitmap(BitmapFactory.decodeByteArray(image,0,image.length));}});}catch(Exception e){runOnUiThread(()->{if(screen!=token)return;loginProgress.setVisibility(View.GONE);loginStatus.setText(e.getMessage());loginStatus.setTextColor(DANGER);loginButton.setEnabled(true);loginButton.setText("重新连接");loginButton.setOnClickListener(v->login(back));});}});
 }
 private void refreshCaptcha(){SchoolClient client=school;int token=screen;worker.execute(()->{try{byte[] b=client.captcha();runOnUiThread(()->{if(screen==token)captchaImage.setImageBitmap(BitmapFactory.decodeByteArray(b,0,b.length));});}catch(Exception e){runOnUiThread(()->{if(screen==token)loginStatus.setText("验证码加载失败，请重试。");});}});}
 private void submitLogin(){String user=account.getText().toString().trim(),pass=password.getText().toString(),code=captcha.getText().toString().trim();if(user.isEmpty()){account.setError("请输入学校账号");return;}if(pass.isEmpty()){password.setError("请输入密码");return;}if(school.captchaRequired&&code.isEmpty()){captcha.setError("请输入图片验证码");return;}busy=true;loginProgress.setVisibility(View.VISIBLE);loginButton.setEnabled(false);loginStatus.setText("正在认证并同步课程…");((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(password.getWindowToken(),0);SchoolClient client=school;String source=loginSchool,timetable=Timetables.active(this);int token=screen;worker.execute(()->{try{String next=client.login(user,pass,code);client.classTimes.validate(Course.parse(next));int count=Course.parse(next).size();if(count==0)throw new IllegalArgumentException("学校未返回课程，已保留原课表。");String suffix="";if(timetable.equals(Timetables.active(this)))try{schoolAccount.save(timetable,source,user,pass);}catch(Exception saveFailure){suffix="，但自动刷新账号未能保存";}final String savedSuffix=suffix;runOnUiThread(()->{busy=false;if(isDestroyed()||screen!=token)return;acceptImport(next,client,source,savedSuffix);});}catch(Exception e){runOnUiThread(()->{busy=false;if(screen!=token){toast(e.getMessage());return;}loginProgress.setVisibility(View.GONE);loginButton.setEnabled(true);if(SchoolClient.passwordRejected(e.getMessage())){password.getText().clear();password.setError("密码错误，请重新输入");}loginStatus.setText(e.getMessage());loginStatus.setTextColor(DANGER);if(school.captchaRequired){captchaBox.setVisibility(View.VISIBLE);refreshCaptcha();}});}});}
 @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);openPrimaryTab(intent);openWidget(intent);if(ADD_WIDGET.equals(intent.getAction())&&hasWindowFocus()){intent.setAction(Intent.ACTION_MAIN);getWindow().getDecorView().post(()->pinWidget());}}
 private void openWidget(Intent intent){
  if(intent==null||!CourseWidget.OPEN.equals(intent.getAction()))return;
  if(busy){toast("正在同步，请稍候再打开课程");return;}
  try{
   if(intent.hasExtra("widgetTimetable")&&!Timetables.active(this).equals(intent.getStringExtra("widgetTimetable"))){home();toast("当前课表已切换，请刷新小组件");return;}
   int shownWeek=intent.getIntExtra("widgetWeek",0);String key=intent.getStringExtra("widgetCourse");
   if(key==null){home();return;}
   if(shownWeek>=1&&shownWeek<=30&&term.equals(intent.getStringExtra("widgetTerm")))for(CourseDisplay.Group group:CourseDisplay.groups(effectiveCourses(),shownWeek,displayChoices))if(CourseDisplay.key(shownWeek,group.selected).equals(key)){week=shownWeek;saveWeek();if(group.courses.size()>1)conflictPicker(group,shownWeek);else detail(group.selected,shownWeek,()->home());return;}
   home();toast("课程安排已更新，请查看最新课表");
  }catch(RuntimeException e){home();toast("请检查课表及开学日期设置");}
 }
  @Override public void onBackPressed(){if(aiAttachmentMenu!=null&&aiAttachmentMenu.open){aiAttachmentMenu.close(true);return;}if(sideNavigation!=null&&sideNavigation.close(true))return;if(pageMotion.isRunning()){if(pageMotion.isReturning())return;pageMotion.reverse(()->{skipPageMotion=true;try{onBackPressed();}finally{skipPageMotion=false;}});return;}if(exitingOverlay!=null&&exitingOverlay.isAttachedToWindow())return;if(navigationOverlay!=null){navigationOverlay.requestClose();return;}if(choiceSheet!=null&&choiceSheet.getVisibility()==View.VISIBLE){choiceSheet.close(true);return;}if(busy){toast("正在同步，请稍候");return;}if(pageBack!=null){cancelBackMotion();backPage();return;}if(tasksVisible||parcelsVisible){home();return;}super.onBackPressed();}
 @Override protected void onDestroy(){if(aiAttachmentMenu!=null)aiAttachmentMenu.close(true);if(aiStrengthDialog!=null)aiStrengthDialog.dismiss();if(aiCall!=null)aiCall.cancel();if(sideNavigation!=null)sideNavigation.close(false);if(taskPage!=null)taskPage.close();if(parcelPage!=null)parcelPage.close();remoteResumed=false;if(remote!=null)remote.unlisten(remoteListener);if(remoteDialog!=null)remoteDialog.dismiss();remoteHandler.removeCallbacksAndMessages(null);if(getWindow().getDecorView().getViewTreeObserver().isAlive())getWindow().getDecorView().getViewTreeObserver().removeOnScrollChangedListener(remoteScroll);if(unsavedDialog!=null)unsavedDialog.dismiss();if(widgetDialog!=null)widgetDialog.dismiss();screen++;if(webTimetableSync!=null)webTimetableSync.cancel();pageMotion.cancel();while(navigationOverlay!=null)navigationOverlay.close(false);if(Build.VERSION.SDK_INT>=33&&backRegistered)MotionBack.unregister(getWindow(),systemBack);if(stage!=null)for(int i=0;i<stage.getChildCount();i++)stage.getChildAt(i).animate().cancel();worker.shutdownNow();homePage=null;if(password!=null)password.getText().clear();super.onDestroy();}
 // Retain one Activity-owned home only; data/term/notes/choices changes replace it.
 private class HomePage {
  final LinearLayout view=root;final ViewPager pages=pager;final ScrollView scroll=timetableScroll;final FrameLayout days=daysBar;
  final TextView label=weekLabel;final String courses=data,changes=adjustments,start=term,note=notes,exams=getPreferences(0).getString("aiExams","[]"),times=classTimes.encode();
  final Set<String> choices=new HashSet<>(displayChoices);
  boolean matches(){return courses.equals(data)&&changes.equals(adjustments)&&start.equals(term)&&note.equals(notes)&&choices.equals(displayChoices)&&exams.equals(getPreferences(0).getString("aiExams","[]"))&&times.equals(classTimes.encode());}
 }
 private class WeekGrid extends FrameLayout {
  int shownWeek,contentWidth;LinearLayout days;
  WeekGrid(){super(MainActivity.this);}
  @Override protected void onMeasure(int widthSpec,int heightSpec){
   int width=MeasureSpec.getSize(widthSpec);
   if(width>0&&width!=contentWidth){contentWidth=width;buildGrid(this,shownWeek);}
   super.onMeasure(widthSpec,heightSpec);
  }
 }

}
