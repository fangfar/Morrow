package cn.courselens;

import android.content.Context;
import android.view.View;
import android.widget.*;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.android.controller.ActivityController;
import static org.junit.Assert.*;
import static org.robolectric.util.ReflectionHelpers.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,shadows=AiSetupGuideTest.Keys.class)
public class AiSetupGuideTest {
 // Exercise real provider selection and forms without depending on device Keystore.
 @Implements(value=DeepSeekKey.class,isInAndroidSdk=false) public static class Keys {
  static final Map<String,String> documents=new HashMap<>();private String store;
  @Implementation protected void __constructor__(Context context){store="deepseek_key";}
  @Implementation protected void __constructor__(Context context,String name){store=name;}
  @Implementation protected boolean configured(){return documents.containsKey(store);}
  @Implementation protected String load(){return documents.get(store);}
  @Implementation protected void saveDocument(String value){documents.put(store,value);}
  @Implementation protected void clear(){documents.remove(store);}
 }
 @Before public void reset(){Keys.documents.clear();}
 private static View page(MainActivity a){return getField(a,"root");}
 @Test public void setupEntrySavesAProviderAndReturnsToTheSameDraft()throws Exception{
  try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=controller.get();callInstanceMethod(a,"aiAssistant");View chat=page(a);assertNotNull(chat.findViewWithTag("ai-setup-guide"));assertNull(chat.findViewWithTag("ai-welcome"));EditText draft=chat.findViewWithTag("ai-message");draft.setText("待发送的合成问题");draft.setSelection(2,5);
   chat.findViewWithTag("ai-setup-open").performClick();assertEquals("API 配置",page(a).getTag());page(a).findViewWithTag("ai-provider-add").performClick();page(a).findViewWithTag("preset-deepseek").performClick();((EditText)page(a).findViewWithTag("ai-key")).setText("synthetic-key");page(a).findViewWithTag("ai-key-save").performClick();assertNotNull(new AiProviders(a).active());assertEquals("API 配置",page(a).getTag());callInstanceMethod(a,"backPage");
   assertSame(chat,page(a));assertNull(chat.findViewWithTag("ai-setup-guide"));assertNotNull(chat.findViewWithTag("ai-welcome"));assertSame(draft,chat.findViewWithTag("ai-message"));assertEquals("待发送的合成问题",draft.getText().toString());assertEquals(2,draft.getSelectionStart());assertEquals(5,draft.getSelectionEnd());
  }
 }
 @Test public void sendingWithoutAProviderOpensSettingsAndCancelKeepsTheDraft(){
  try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=controller.get();callInstanceMethod(a,"aiAssistant");View chat=page(a);EditText draft=chat.findViewWithTag("ai-message");draft.setText("不要丢失这条问题");chat.findViewWithTag("ai-send").performClick();assertEquals("API 配置",page(a).getTag());callInstanceMethod(a,"backPage");assertSame(chat,page(a));assertEquals("不要丢失这条问题",draft.getText().toString());assertNotNull(chat.findViewWithTag("ai-setup-open"));assertFalse((boolean)getField(a,"aiSending"));
  }
 }
 @Test public void deletingTheLastProviderShowsTheGuideAlongsideExistingMessages()throws Exception{
  try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=controller.get();AiProviders providers=new AiProviders(a);providers.save(AiProviders.defaultConfig("synthetic-key"));AiChats chats=callInstanceMethod(a,"aiStore");chats.messages(new JSONArray().put(new JSONObject().put("role","user").put("content","历史合成问题")));callInstanceMethod(a,"aiAssistant");View chat=page(a);assertNull(chat.findViewWithTag("ai-setup-guide"));assertNotNull(chat.findViewWithTag("ai-user-message"));providers.delete("deepseek");callInstanceMethod(a,"showAiTab");assertSame(chat,page(a));assertNotNull(chat.findViewWithTag("ai-setup-open"));assertNotNull(chat.findViewWithTag("ai-user-message"));
  }
 }
 @Test public void temporaryChatAlsoHasASetupEntry(){
  try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=controller.get();callInstanceMethod(a,"aiTemporaryChat");assertNotNull(page(a).findViewWithTag("ai-setup-open"));assertNotNull(getField(a,"aiTemporaryStore"));
  }
 }
 @Test public void unreadableConfigurationIsKeptAndReportedAsAnError(){
  Keys.documents.put("ai_providers","invalid synthetic encrypted document");
  try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup()){
   MainActivity a=controller.get();callInstanceMethod(a,"aiAssistant");LinearLayout guide=page(a).findViewWithTag("ai-setup-guide");assertEquals("检查 API 配置",((TextView)guide.getChildAt(0)).getText().toString());assertNotNull(guide.findViewWithTag("ai-setup-open"));assertEquals("invalid synthetic encrypted document",Keys.documents.get("ai_providers"));
  }
 }
}
