package cn.courselens;

import org.json.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class AiProvidersTest {
 @Test public void addressesAreNormalizedAndCredentialsCannotHideInTheUrl()throws Exception{
  assertEquals("https://example.com/v1",AiProviders.base(" https://example.com/v1/chat/completions/ "));
  for(String bad:new String[]{"http://example.com","https://key@example.com/v1","https://example.com?key=secret","https://example.com/#key","https:///broken"})try{AiProviders.base(bad);fail(bad);}catch(Exception expected){}
 }
 @Test public void protocolAndReasoningParametersBelongToTheSelectedProvider()throws Exception{
  AiProviders.Config original=AiProviders.defaultConfig("synthetic-key");JSONObject deepseek=DeepSeekClient.payload(original,3,new JSONArray());assertEquals("deepseek-flash",deepseek.getString("model"));assertEquals("max",deepseek.getString("reasoning_effort"));
  AiProviders.Config custom=new AiProviders.Config("custom","Custom","https://example.com/v1","custom-model","synthetic-key",false,false);JSONObject generic=DeepSeekClient.payload(custom,3,new JSONArray());assertEquals("custom-model",generic.getString("model"));assertFalse(generic.has("thinking"));assertFalse(generic.has("reasoning_effort"));assertFalse(generic.has("max_tokens"));AiProviders.Config noThinking=new AiProviders.Config("d","DeepSeek",original.base,original.model,original.key,true,false);assertEquals("disabled",DeepSeekClient.payload(noThinking,3,new JSONArray()).getJSONObject("thinking").getString("type"));
  AiProviders.Config reasoning=new AiProviders.Config("custom","Custom",custom.base,custom.model,custom.key,false,true);assertEquals("medium",DeepSeekClient.payload(reasoning,2,new JSONArray()).getString("reasoning_effort"));assertEquals(custom.key,AiProviders.Config.read(custom.json()).key);
 }
 @Test public void switchingProvidersDoesNotForwardAnotherProvidersFileIds()throws Exception{
  JSONObject image=new JSONObject().put("kind","image").put("name","synthetic.png").put("file_id","file-api-synthetic").put("provider","deepseek").put("origin","https://api.deepseek.com");JSONArray history=new JSONArray().put(new JSONObject().put("role","user").put("content","查看附件").put("attachments",new JSONArray().put(image)));
  AiProviders.Config other=new AiProviders.Config("other","Other","https://example.com/v1","vision","synthetic",false,false);String wire=DeepSeekClient.wireMessages(history,0,other).toString();assertFalse(wire.contains("file-api-synthetic"));assertTrue(wire.contains("重新添加"));
  image.put("data","aW1hZ2U=").put("mime","image/png");String inline=DeepSeekClient.wireMessages(history,0,other).toString();assertTrue(inline.contains("image_url"));assertTrue(inline.contains("data:image/png;base64,aW1hZ2U="));
 }
}
