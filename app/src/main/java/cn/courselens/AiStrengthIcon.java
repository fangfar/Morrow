package cn.courselens;

import android.graphics.*;
import android.graphics.drawable.Drawable;

final class AiStrengthIcon extends Drawable {
 final int level;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private final int color;private int alpha=255;
 AiStrengthIcon(int level,int color){this.level=Math.max(0,Math.min(3,level));this.color=color;paint.setStrokeWidth(1.8f);paint.setStrokeCap(Paint.Cap.ROUND);}
 @Override public void draw(Canvas canvas){canvas.save();Rect bounds=getBounds();canvas.translate(bounds.left,bounds.top);canvas.scale(bounds.width()/24f,bounds.height()/24f);paint.setColor(color);float sweep=220*level/3f;paint.setStyle(Paint.Style.FILL);paint.setAlpha(alpha*24/100);if(level>0)canvas.drawArc(3,3,21,21,160,sweep,true,paint);paint.setStyle(Paint.Style.STROKE);paint.setAlpha(alpha*24/100);canvas.drawArc(3,3,21,21,160,220,false,paint);paint.setAlpha(alpha);if(level>0)canvas.drawArc(3,3,21,21,160,sweep,false,paint);double angle=Math.toRadians(160+sweep);canvas.drawLine(12,12,12+6*(float)Math.cos(angle),12+6*(float)Math.sin(angle),paint);canvas.drawCircle(12,12,2,paint);canvas.restore();}
 @Override public void setAlpha(int alpha){this.alpha=alpha;invalidateSelf();}
 @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}
 @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
