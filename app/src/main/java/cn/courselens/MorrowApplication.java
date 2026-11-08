package cn.courselens;

import android.app.Application;

public class MorrowApplication extends Application implements androidx.work.Configuration.Provider {
 static String restoreMessage;
 @Override public androidx.work.Configuration getWorkManagerConfiguration(){return new androidx.work.Configuration.Builder().build();}
 @Override public void onCreate(){
  super.onCreate();
  java.util.concurrent.FutureTask<String> recovery=new java.util.concurrent.FutureTask<>(()->LocalBackup.recover(this));new Thread(recovery,"backup-recovery").start();
  try{restoreMessage=recovery.get();}catch(Exception e){throw new IllegalStateException("本地恢复尚未完成，原始加密恢复日志已保留。请释放存储空间后重新打开。",e);}
  if(restoreMessage!=null){ParcelNotifications.cancelAll(this);ParcelWorker.schedule(this);ParcelRepository.IO.execute(()->{CourseWidget.updateAll(this);ParcelWidget.updateAll(this);});}
 }
}
