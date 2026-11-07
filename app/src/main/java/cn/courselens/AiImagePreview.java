package cn.courselens;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.widget.ImageView;

final class AiImagePreview extends ImageView {
 private final Matrix transform=new Matrix();
 private final ScaleGestureDetector pinch;
 private final GestureDetector gestures;
 private float zoom=1;
 AiImagePreview(Context context,Bitmap image){
  super(context);setImageBitmap(image);setScaleType(ScaleType.MATRIX);setContentDescription("图片预览，双指缩放，拖动查看，双击还原");
  pinch=new ScaleGestureDetector(context,new ScaleGestureDetector.SimpleOnScaleGestureListener(){@Override public boolean onScale(ScaleGestureDetector detector){float next=Math.max(1,Math.min(5,zoom*detector.getScaleFactor()));transform.postScale(next/zoom,next/zoom,detector.getFocusX(),detector.getFocusY());zoom=next;constrain();return true;}});
  gestures=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){@Override public boolean onDown(MotionEvent e){return true;}@Override public boolean onScroll(MotionEvent first,MotionEvent last,float dx,float dy){if(!pinch.isInProgress()&&zoom>1){transform.postTranslate(-dx,-dy);constrain();}return true;}@Override public boolean onDoubleTap(MotionEvent e){fit();return true;}@Override public boolean onSingleTapUp(MotionEvent e){performClick();return true;}});
 }
 @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){super.onSizeChanged(w,h,oldw,oldh);fit();}
 private void fit(){if(getDrawable()==null||getWidth()==0||getHeight()==0)return;transform.setRectToRect(new RectF(0,0,getDrawable().getIntrinsicWidth(),getDrawable().getIntrinsicHeight()),new RectF(0,0,getWidth(),getHeight()),Matrix.ScaleToFit.CENTER);zoom=1;setImageMatrix(transform);}
 private void constrain(){RectF bounds=new RectF(0,0,getDrawable().getIntrinsicWidth(),getDrawable().getIntrinsicHeight());transform.mapRect(bounds);float dx=bounds.width()<=getWidth()?getWidth()/2f-bounds.centerX():bounds.left>0?-bounds.left:bounds.right<getWidth()?getWidth()-bounds.right:0;float dy=bounds.height()<=getHeight()?getHeight()/2f-bounds.centerY():bounds.top>0?-bounds.top:bounds.bottom<getHeight()?getHeight()-bounds.bottom:0;transform.postTranslate(dx,dy);setImageMatrix(transform);}
 @Override public boolean onTouchEvent(MotionEvent event){pinch.onTouchEvent(event);gestures.onTouchEvent(event);return true;}
 @Override public boolean performClick(){return super.performClick();}
}
