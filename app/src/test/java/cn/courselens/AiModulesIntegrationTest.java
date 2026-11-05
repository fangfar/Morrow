package cn.courselens;
import static org.junit.Assert.*;import static org.robolectric.util.ReflectionHelpers.*;import org.junit.Test;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.Config;import org.robolectric.android.controller.ActivityController;import java.util.concurrent.ExecutorService;import org.json.*;import android.widget.TextView;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,shadows=DataManagementTest.AtomicRename.class)
public class AiModulesIntegrationTest {
 private void drain(TaskPage page)throws Exception{ExecutorService worker=getField(page,"worker");worker.submit(()->{}).get();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();}
 @Test public void cacheRoundtripAcrossUpdates()throws Exception{android.content.Context c=RuntimeEnvironment.getApplication();ChaoxingCache cache=new ChaoxingCache(c,"synthetic");ChaoxingClient.Result data=new ChaoxingClient.Result();data.items.add(new ChaoxingClient.Work("测试","测试","未完成",""));cache.save(data);data.items.clear();data.items.add(new ChaoxingClient.Work("测试","测试","已完成",""));cache.save(data);assertEquals("已完成",cache.load().items.get(0).status);}
 @Test public void tasksUseExistingSaveAndRecovery()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();callInstanceMethod(a,"prepareTasks");TaskPage page=getField(a,"taskPage");drain(page);page.aiApply(new JSONObject("{\"type\":\"add_task\",\"value\":{\"title\":\"合成复习任务\"}}"),null);drain(page);JSONObject expected=page.aiContext().getJSONObject(0);assertEquals("合成复习任务",expected.getString("title"));page.aiApply(new JSONObject("{\"type\":\"complete_task\"}"),expected);drain(page);expected=page.aiContext().getJSONObject(0);assertEquals("已完成",expected.getString("status"));assertEquals("disk completed", "已完成",new ChaoxingCache(a,"").load().items.get(0).status);page.aiApply(new JSONObject("{\"type\":\"delete_task\"}"),expected);drain(page);ChaoxingClient.Result saved=new ChaoxingCache(a,"").load();assertEquals(0,saved.items.size());assertEquals(1,saved.deleted.size());JSONArray chat=getField(a,"aiChat");assertTrue(chat.getJSONObject(chat.length()-1).getString("content").contains("成功保存"));
  }
 }
 @Test public void parcelsUseRepositoryAndDeletionHistory()throws Exception{
  try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=c.get();JSONObject context=new JSONObject().put("parcels",new JSONArray());AiModulePlan add=AiModulePlan.build(new JSONObject("{\"type\":\"add_parcel\",\"value\":{\"code\":\"1-23\",\"station\":\"合成驿站\"}}"),context);execute(a,add);java.util.List<ParcelRecord> records=ParcelRepository.IO.submit(()->ParcelDatabase.get(a).parcels().all()).get();assertEquals(1,records.size());context.put("parcels",new JSONArray().put(AiModulePlan.parcel(records.get(0))));execute(a,AiModulePlan.build(new JSONObject("{\"type\":\"delete_parcel\",\"index\":1}"),context));assertEquals(1,(int)ParcelRepository.IO.submit(()->ParcelDatabase.get(a).parcels().deleted().size()).get());
  }
 }
 private void execute(MainActivity a,AiModulePlan p)throws Exception{callInstanceMethod(a,"executeAiModule",ClassParameter.from(AiModulePlan.class,p),ClassParameter.from(TextView.class,new TextView(a)));ParcelRepository.IO.submit(()->{}).get();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();}
}
