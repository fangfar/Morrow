package cn.courselens;

import android.view.*;
import android.webkit.WebView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public class WebTimetableLayoutTest {
 @Test public void compactControlsLeaveRoomForFullWidthZoomablePage(){
  try(var controller=Robolectric.buildActivity(WebTimetableActivity.class).setup()){
   WebTimetableActivity activity=controller.get();ViewGroup content=activity.findViewById(android.R.id.content);View body=content.getChildAt(0);WebView web=body.findViewWithTag("web-timetable-content");
   float density=activity.getResources().getDisplayMetrics().density;
   for(int height:new int[]{640,400}){
    int width=Math.round(360*density),pixels=Math.round(height*density);body.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(pixels,View.MeasureSpec.EXACTLY));body.layout(0,0,width,pixels);
    assertEquals(width,web.getWidth());assertTrue("Browser height at "+height+"dp: "+web.getHeight()/density,web.getHeight()>=(height-180)*density);
   }
   assertTrue(web.getSettings().getUseWideViewPort());assertTrue(web.getSettings().getLoadWithOverviewMode());assertTrue(web.getSettings().getBuiltInZoomControls());assertFalse(web.getSettings().getDisplayZoomControls());
  }
 }
}
