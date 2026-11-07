package cn.courselens;

import android.view.*;
import android.webkit.WebView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=33)
public class WebTimetableLayoutTest {
 @Test public void compactControlsLeaveRoomForFullWidthZoomablePage(){
  try(var controller=Robolectric.buildActivity(WebTimetableActivity.class).setup()){
   WebTimetableActivity activity=controller.get();ViewGroup content=activity.findViewById(android.R.id.content);View body=content.getChildAt(0);WebView web=body.findViewWithTag("web-timetable-content");
   assertTrue(web.getSettings().getUseWideViewPort());assertTrue(web.getSettings().getLoadWithOverviewMode());assertTrue(web.getSettings().getBuiltInZoomControls());assertFalse(web.getSettings().getDisplayZoomControls());
   // Robolectric's WebView provider does not measure; verify the space allocated by its parent.
   ViewGroup parent=(ViewGroup)web.getParent();ViewGroup.LayoutParams bounds=web.getLayoutParams();parent.removeView(web);View viewport=new View(activity);parent.addView(viewport,bounds);
   float density=activity.getResources().getDisplayMetrics().density;
   for(int height:new int[]{640,400}){
    int width=Math.round(360*density),pixels=Math.round(height*density);body.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(pixels,View.MeasureSpec.EXACTLY));body.layout(0,0,width,pixels);
    assertEquals(width,viewport.getWidth());assertTrue("Browser height at "+height+"dp: "+viewport.getHeight()/density,viewport.getHeight()>=(height-180)*density);
   }
   parent.removeView(viewport);parent.addView(web,bounds);
  }
 }
}
