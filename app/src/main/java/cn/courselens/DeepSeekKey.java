package cn.courselens;

import android.content.*;
import android.security.keystore.*;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;

final class DeepSeekKey {
 private static final String ALIAS="courselens.deepseek.key.v1";
 private final SharedPreferences prefs;
 private final byte[] context;
 DeepSeekKey(Context app){prefs=app.getSharedPreferences("deepseek_key",Context.MODE_PRIVATE);context=(app.getPackageName()+":deepseek:v1").getBytes(StandardCharsets.UTF_8);}
 boolean configured(){return prefs.contains("key")&&prefs.contains("iv");}
 private SecretKey key(boolean create)throws Exception{
  KeyStore store=KeyStore.getInstance("AndroidKeyStore");store.load(null);
  if(store.containsAlias(ALIAS))return (SecretKey)store.getKey(ALIAS,null);
  if(!create)throw new GeneralSecurityException("密钥无法解密，请重新设置。");
  KeyGenerator generator=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");generator.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setRandomizedEncryptionRequired(true).build());return generator.generateKey();
 }
 void save(String value)throws Exception{
  value=value.trim();if(value.isEmpty()||value.length()>512||value.contains("\n")||value.contains("\r"))throw new IllegalArgumentException("请输入有效的 API 密钥。");
  Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key(true));cipher.updateAAD(context);byte[] plain=value.getBytes(StandardCharsets.UTF_8);
  try{String encrypted=Base64.encodeToString(cipher.doFinal(plain),Base64.NO_WRAP),iv=Base64.encodeToString(cipher.getIV(),Base64.NO_WRAP);String previous=prefs.getString("key",null),previousIv=prefs.getString("iv",null);if(!prefs.edit().putString("key",encrypted).putString("iv",iv).commit()){SharedPreferences.Editor rollback=prefs.edit();if(previous==null)rollback.remove("key");else rollback.putString("key",previous);if(previousIv==null)rollback.remove("iv");else rollback.putString("iv",previousIv);rollback.commit();throw new java.io.IOException("密钥保存失败，请重试。");}}finally{java.util.Arrays.fill(plain,(byte)0);}
 }
 String load()throws Exception{
  if(!configured())throw new GeneralSecurityException("请先设置 DeepSeek API 密钥。");
  Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,key(false),new GCMParameterSpec(128,Base64.decode(prefs.getString("iv",""),Base64.NO_WRAP)));cipher.updateAAD(context);
  byte[] plain=cipher.doFinal(Base64.decode(prefs.getString("key",""),Base64.NO_WRAP));try{return new String(plain,StandardCharsets.UTF_8);}finally{java.util.Arrays.fill(plain,(byte)0);}
 }
 void clear()throws java.io.IOException{if(!prefs.edit().clear().commit())throw new java.io.IOException("密钥删除失败，请重试。");}
}
