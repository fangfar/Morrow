package cn.courselens;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.camera.core.*;
import androidx.camera.view.*;
import androidx.lifecycle.*;
import java.io.File;
import java.util.function.Consumer;

/** The camera owns a short lifecycle; closing the panel releases its preview. */
final class AiCameraPanel extends Dialog implements LifecycleOwner {
 private final LifecycleRegistry lifecycle=new LifecycleRegistry(this);
 private final LifecycleCameraController camera;
 private final Consumer<File> result;
 private boolean capturing;
 AiCameraPanel(Context context,Consumer<File> result){
  super(context,R.style.SecondaryDialog);this.result=result;requestWindowFeature(Window.FEATURE_NO_TITLE);
  camera=new LifecycleCameraController(context);camera.setEnabledUseCases(CameraController.IMAGE_CAPTURE);
  FrameLayout panel=new FrameLayout(context);GradientDrawable background=new GradientDrawable();background.setColor(Color.BLACK);background.setCornerRadius(dp(32));panel.setBackground(background);panel.setClipToOutline(true);
  PreviewView preview=new PreviewView(context);preview.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);preview.setController(camera);preview.setContentDescription("相机取景，双指缩放，轻点对焦");panel.addView(preview,new FrameLayout.LayoutParams(-1,-1));
  LinearLayout controls=new LinearLayout(context);controls.setGravity(Gravity.CENTER_VERTICAL);controls.setPadding(dp(20),dp(12),dp(20),dp(24));
  ImageButton close=icon(context,R.drawable.ic_back,"关闭相机");close.setOnClickListener(v->dismiss());controls.addView(close,new LinearLayout.LayoutParams(dp(56),dp(56)));
  controls.addView(new View(context),new LinearLayout.LayoutParams(0,1,1));
  ImageButton shutter=icon(context,R.drawable.ic_ai_camera,"拍照并添加附件");GradientDrawable white=new GradientDrawable();white.setColor(Color.WHITE);white.setShape(GradientDrawable.OVAL);white.setStroke(dp(6),0xff444444);shutter.setBackground(white);shutter.setColorFilter(Color.BLACK);shutter.setTag("camera-shutter");shutter.setOnClickListener(v->capture(shutter));controls.addView(shutter,new LinearLayout.LayoutParams(dp(80),dp(80)));
  controls.addView(new View(context),new LinearLayout.LayoutParams(0,1,1));
  ImageButton flip=icon(context,R.drawable.ic_sync,"切换前后摄像头");flip.setOnClickListener(v->{if(capturing)return;try{CameraSelector next=camera.getCameraSelector()==CameraSelector.DEFAULT_BACK_CAMERA?CameraSelector.DEFAULT_FRONT_CAMERA:CameraSelector.DEFAULT_BACK_CAMERA;if(camera.hasCamera(next))camera.setCameraSelector(next);else message("没有可切换的摄像头");}catch(RuntimeException e){message("暂时无法切换摄像头");}});controls.addView(flip,new LinearLayout.LayoutParams(dp(56),dp(56)));
  panel.addView(controls,new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));
  setContentView(panel);setOnDismissListener(d->{camera.unbind();lifecycle.setCurrentState(Lifecycle.State.DESTROYED);});
 }
 @NonNull @Override public Lifecycle getLifecycle(){return lifecycle;}
 @Override public void show(){super.show();Window window=getWindow();if(window!=null){window.setBackgroundDrawableResource(android.R.color.transparent);window.setGravity(Gravity.BOTTOM);window.setLayout(-1,Math.round(getContext().getResources().getDisplayMetrics().heightPixels*.62f));}lifecycle.setCurrentState(Lifecycle.State.RESUMED);try{camera.bindToLifecycle(this);camera.getInitializationFuture().addListener(()->{try{camera.getInitializationFuture().get();}catch(Exception e){if(isShowing()){message("相机无法启动，请检查相机权限或改用照片添加。");dismiss();}}},androidx.core.content.ContextCompat.getMainExecutor(getContext()));}catch(RuntimeException e){message("无法打开相机，请使用照片添加。");dismiss();}}
 private void capture(View shutter){
  if(capturing)return;File file;try{File dir=new File(getContext().getCacheDir(),"ai-capture");if(!dir.isDirectory()&&!dir.mkdirs())throw new java.io.IOException();file=File.createTempFile("capture-",".jpg",dir);}catch(java.io.IOException e){message("照片无法保存，请重试。");return;}
  capturing=true;shutter.setEnabled(false);
  try{camera.takePicture(new ImageCapture.OutputFileOptions.Builder(file).build(),androidx.core.content.ContextCompat.getMainExecutor(getContext()),new ImageCapture.OnImageSavedCallback(){public void onImageSaved(@NonNull ImageCapture.OutputFileResults saved){if(!isShowing()){file.delete();return;}dismiss();result.accept(file);}public void onError(@NonNull ImageCaptureException error){file.delete();capturing=false;shutter.setEnabled(true);if(isShowing())message("拍照失败，请重试。");}});}catch(RuntimeException e){file.delete();capturing=false;shutter.setEnabled(true);message("相机尚未就绪，请稍后重试。");}
 }
 private ImageButton icon(Context context,int drawable,String label){ImageButton button=new ImageButton(context);button.setImageResource(drawable);button.setContentDescription(label);button.setColorFilter(Color.WHITE);GradientDrawable circle=new GradientDrawable();circle.setShape(GradientDrawable.OVAL);circle.setColor(0xcc202020);button.setBackground(circle);button.setPadding(dp(16),dp(16),dp(16),dp(16));return button;}
 private int dp(float value){return Math.round(value*getContext().getResources().getDisplayMetrics().density);}
 private void message(String value){Toast.makeText(getContext(),value,Toast.LENGTH_LONG).show();}
}
