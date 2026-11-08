package cn.courselens;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Arrays;
import javax.crypto.*;
import javax.crypto.spec.*;

/** Versioned, authenticated local archive. The header is authenticated too. */
final class BackupCrypto {
 static final int LIMIT=32*1024*1024;
 private static final int MAGIC=0x4d525742,VERSION=1,ROUNDS=600000,HEADER=40;
 static byte[] read(InputStream in)throws IOException{
  if(in==null)throw new IOException("无法读取备份文件");ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;
  while((n=in.read(buffer))!=-1){if(out.size()+n>LIMIT+HEADER+16)throw new IOException("备份文件超过 32 MB");out.write(buffer,0,n);}return out.toByteArray();
 }
 static byte[] encrypt(byte[] plain,char[] password)throws Exception{
  if(plain.length>LIMIT)throw new IOException("备份数据超过 32 MB");byte[] salt=new byte[16],iv=new byte[12];SecureRandom random=new SecureRandom();random.nextBytes(salt);random.nextBytes(iv);
  byte[] header=ByteBuffer.allocate(HEADER).putInt(MAGIC).putInt(VERSION).putInt(ROUNDS).put(salt).put(iv).array();
  Cipher cipher=cipher(Cipher.ENCRYPT_MODE,password,salt,iv);cipher.updateAAD(header);byte[] encrypted=cipher.doFinal(plain);
  return ByteBuffer.allocate(HEADER+encrypted.length).put(header).put(encrypted).array();
 }
 static byte[] decrypt(byte[] bytes,char[] password)throws Exception{
  if(bytes.length<HEADER+16||bytes.length>LIMIT+HEADER+16)throw new IOException("备份文件损坏或大小无效");ByteBuffer header=ByteBuffer.wrap(bytes);
  if(header.getInt()!=MAGIC)throw new IOException("不是 Morrow 加密备份文件");
  if(header.getInt()!=VERSION)throw new IOException("不支持此备份版本，请使用兼容版本的 Morrow");
  if(header.getInt()!=ROUNDS)throw new IOException("备份加密参数不受支持或已损坏");byte[] salt=new byte[16],iv=new byte[12];header.get(salt);header.get(iv);
  Cipher cipher=cipher(Cipher.DECRYPT_MODE,password,salt,iv);cipher.updateAAD(Arrays.copyOf(bytes,HEADER));
  try{return cipher.doFinal(bytes,HEADER,bytes.length-HEADER);}catch(AEADBadTagException e){throw new IOException("密码错误或备份文件已损坏");}
 }
 private static Cipher cipher(int mode,char[] password,byte[] salt,byte[] iv)throws Exception{
  PBEKeySpec spec=new PBEKeySpec(password,salt,ROUNDS,256);byte[] key=null;
  try{key=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(mode,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));return cipher;}
  finally{spec.clearPassword();if(key!=null)Arrays.fill(key,(byte)0);}
 }
}
