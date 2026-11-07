package cn.courselens;

import android.content.Context;
import java.net.URI;
import java.util.*;
import org.json.*;

/** Provider metadata and keys share one atomic, Keystore-encrypted document. */
final class AiProviders {
 static final class Config {
  final String id,name,base,model,key;final boolean deepseek,reasoning;
  Config(String id,String name,String base,String model,String key,boolean deepseek,boolean reasoning){this.id=id;this.name=name;this.base=base;this.model=model;this.key=key;this.deepseek=deepseek;this.reasoning=reasoning;}
  JSONObject json()throws Exception{return new JSONObject().put("id",id).put("name",name).put("base",base).put("model",model).put("key",key).put("deepseek",deepseek).put("reasoning",reasoning);}
  static Config read(JSONObject o)throws Exception{return new Config(o.getString("id"),o.getString("name"),o.getString("base"),o.getString("model"),o.getString("key"),o.getBoolean("deepseek"),o.optBoolean("reasoning",true));}
 }
 private final DeepSeekKey store,legacy;private JSONObject state;
 AiProviders(Context context)throws Exception{
  store=new DeepSeekKey(context,"ai_providers");legacy=new DeepSeekKey(context);
  if(store.configured())state=new JSONObject(store.load());
  else{JSONArray rows=new JSONArray();if(legacy.configured())rows.put(defaultConfig(legacy.load()).json());state=new JSONObject().put("selected","deepseek").put("providers",rows);}
 }
 static Config defaultConfig(String key){return new Config("deepseek","DeepSeek","https://api.deepseek.com","deepseek-flash",key,true,true);}
 static List<Config> presets(){return Arrays.asList(
  new Config("preset-deepseek","DeepSeek","https://api.deepseek.com","deepseek-flash","",true,true),
  new Config("preset-qwen","通义千问 · 阿里云百炼","https://dashscope.aliyuncs.com/compatible-mode/v1","qwen-plus","",false,false),
  new Config("preset-glm","智谱 GLM","https://open.bigmodel.cn/api/paas/v4","glm-4.7-flash","",false,false),
  new Config("preset-kimi","Kimi · 月之暗面","https://api.moonshot.cn/v1","kimi-k2.5","",false,false),
  new Config("preset-doubao","豆包 · 火山方舟","https://ark.cn-beijing.volces.com/api/v3","","",false,false),
  new Config("preset-siliconflow","硅基流动","https://api.siliconflow.cn/v1","deepseek-ai/DeepSeek-V4-Flash","",false,false));}
 static Config custom(){return new Config("custom","","","","",false,false);}
 List<Config> all()throws Exception{List<Config> rows=new ArrayList<>();JSONArray values=state.getJSONArray("providers");for(int i=0;i<values.length();i++)rows.add(Config.read(values.getJSONObject(i)));return rows;}
 Config active()throws Exception{for(Config config:all())if(config.id.equals(state.optString("selected")))return config;return null;}
 void select(String id)throws Exception{Config config=find(id);if(config==null)throw new IllegalArgumentException("配置已不存在。");JSONObject next=new JSONObject(state.toString()).put("selected",id);persist(next);}
 Config find(String id)throws Exception{for(Config config:all())if(config.id.equals(id))return config;return null;}
 static String base(String value)throws Exception{
  String cleaned=value.trim().replaceAll("/+$","");URI uri=new URI(cleaned);
  if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null||uri.getRawUserInfo()!=null||uri.getRawQuery()!=null||uri.getRawFragment()!=null||cleaned.length()>512)throw new IllegalArgumentException("请输入 HTTPS API 基础地址，不含查询参数或账号信息。");
  if(cleaned.endsWith("/chat/completions"))cleaned=cleaned.substring(0,cleaned.length()-17);
  return cleaned;
 }
 void save(Config config)throws Exception{
  if(config.name.trim().isEmpty()||config.name.length()>40||config.model.trim().isEmpty()||config.model.length()>160||config.key.trim().isEmpty()||config.key.length()>512||config.key.matches("(?s).*[\\p{Cntrl}].*")||config.model.matches("(?s).*[\\p{Cntrl}].*"))throw new IllegalArgumentException("请填写名称、模型和有效密钥。");
  Config checked=new Config(config.id,config.name.trim(),base(config.base),config.model.trim(),config.key.trim(),config.deepseek,config.reasoning);JSONObject next=new JSONObject(state.toString());JSONArray rows=next.getJSONArray("providers");int index=-1;for(int i=0;i<rows.length();i++)if(rows.getJSONObject(i).getString("id").equals(config.id))index=i;if(index<0){if(rows.length()>=20)throw new IllegalArgumentException("最多保存 20 个配置。");rows.put(checked.json());}else rows.put(index,checked.json());if(active()==null)next.put("selected",checked.id);persist(next);
 }
 void delete(String id)throws Exception{JSONObject next=new JSONObject(state.toString());JSONArray rows=next.getJSONArray("providers");for(int i=0;i<rows.length();i++)if(rows.getJSONObject(i).getString("id").equals(id)){rows.remove(i);break;}if(id.equals(next.optString("selected")))next.put("selected",rows.length()==0?"":rows.getJSONObject(0).getString("id"));persist(next);if(id.equals("deepseek")&&legacy.configured())legacy.clear();}
 private void persist(JSONObject next)throws Exception{store.saveDocument(next.toString());state=next;}
}
