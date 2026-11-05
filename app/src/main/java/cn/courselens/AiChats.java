package cn.courselens;

import android.content.*;import org.json.*;import java.util.UUID;

final class AiChats {
 private final SharedPreferences prefs;private JSONObject state;
 AiChats(Context context)throws Exception{prefs=context.getSharedPreferences("ai_chats",Context.MODE_PRIVATE);String saved=prefs.getString("state",null);if(saved==null){state=new JSONObject().put("projects",new JSONArray());newProject("默认项目");}else{state=new JSONObject(saved);project();chat();}}
 JSONArray projects()throws Exception{return state.getJSONArray("projects");}
 JSONObject project()throws Exception{JSONArray rows=projects();for(int i=0;i<rows.length();i++)if(rows.getJSONObject(i).getString("id").equals(state.getString("project")))return rows.getJSONObject(i);throw new IllegalStateException("项目记录无法读取");}
 JSONObject chat()throws Exception{JSONArray rows=project().getJSONArray("chats");for(int i=0;i<rows.length();i++)if(rows.getJSONObject(i).getString("id").equals(state.getString("chat")))return rows.getJSONObject(i);throw new IllegalStateException("聊天记录无法读取");}
 void newProject(String name)throws Exception{if(name.trim().isEmpty()||name.length()>40||projects().length()>=50)throw new IllegalArgumentException("项目名称应为1–40字，最多50个项目");JSONObject p=new JSONObject().put("id",UUID.randomUUID().toString()).put("name",name.trim()).put("memory","").put("chats",new JSONArray());projects().put(p);state.put("project",p.getString("id"));newChat();}
 void newChat()throws Exception{JSONArray chats=project().getJSONArray("chats");if(chats.length()>=200)throw new IllegalArgumentException("本项目聊天数量已达上限");JSONObject chat=new JSONObject().put("id",UUID.randomUUID().toString()).put("title","新聊天").put("strength",0).put("messages",new JSONArray());chats.put(chat);state.put("chat",chat.getString("id"));save();}
 void select(String projectId,String chatId)throws Exception{state.put("project",projectId);if(chatId==null){JSONArray chats=project().getJSONArray("chats");state.put("chat",chats.getJSONObject(chats.length()-1).getString("id"));}else state.put("chat",chatId);chat();save();}
 void messages(JSONArray messages)throws Exception{chat().put("messages",new JSONArray(messages.toString()));if(chat().getString("title").equals("新聊天"))for(int i=0;i<messages.length();i++){JSONObject m=messages.getJSONObject(i);if(m.optString("role").equals("user")){String text=m.optString("content");chat().put("title",text.substring(0,Math.min(24,text.length())));break;}}save();}
 void strength(int value)throws Exception{if(value<0||value>3)throw new IllegalArgumentException();chat().put("strength",value);save();}
 void memory(String value)throws Exception{if(value.length()>8000)throw new IllegalArgumentException("项目记忆最多8000字");project().put("memory",value);save();}
 private void save()throws Exception{String encoded=state.toString(),previous=prefs.getString("state",null);if(encoded.length()>4000000)throw new IllegalStateException("聊天记录较多，暂时无法保存更多内容");if(!prefs.edit().putString("state",encoded).commit()){SharedPreferences.Editor rollback=prefs.edit();if(previous==null)rollback.remove("state");else rollback.putString("state",previous);rollback.commit();if(previous!=null)state=new JSONObject(previous);throw new java.io.IOException("聊天保存失败，请重试");}}
}
