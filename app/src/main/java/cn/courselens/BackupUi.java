package cn.courselens;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.text.InputType;
import android.view.WindowManager;
import android.widget.*;
import java.io.*;
import java.util.Arrays;
import java.util.concurrent.*;
import org.json.JSONObject;

/** SAF grants access only to the file explicitly chosen by the user. */
final class BackupUi {
 private final MainActivity activity;private final SecondaryUi ui;private final ExecutorService worker=Executors.newSingleThreadExecutor();private boolean closed,busy;private AlertDialog dialog;
 BackupUi(MainActivity activity){this.activity=activity;ui=new SecondaryUi(activity);}
 private void message(String text){if(!closed)Toast.makeText(activity,text,Toast.LENGTH_LONG).show();}
 void exportFile(){choose(new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/octet-stream").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE,"Morrow-"+java.time.LocalDate.now()+".morrow"),930);}
 void importFile(){choose(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE),931);}
 private void choose(Intent intent,int request){if(busy)return;try{activity.startActivityForResult(intent,request);}catch(ActivityNotFoundException|SecurityException e){message("无法打开系统文件选择器");}}
 void result(int request,int result,Intent data){if(result!=Activity.RESULT_OK||data==null||data.getData()==null||busy)return;password(data.getData(),request==930);}
 private EditText passwordField(LinearLayout body,String hint){EditText field=new EditText(activity);field.setHint(hint);field.setSingleLine(true);field.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);field.setSaveEnabled(false);field.setImportantForAutofill(android.view.View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);body.addView(field,new LinearLayout.LayoutParams(-1,ui.dp(56)));return field;}
 private void password(Uri uri,boolean exporting){
  LinearLayout body=new LinearLayout(activity);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(ui.dp(24),ui.dp(8),ui.dp(24),0);
  TextView note=ui.text(exporting?"请设置至少 8 位密码。密码无法找回；备份不包含账号密码、API 配置、AI 聊天和网页登录状态。":"输入备份密码。验证成功后仍需确认覆盖；学习通缓存需登录原账号后查看。",14,ui.MUTED);body.addView(note);
  EditText password=passwordField(body,"备份密码"),repeat=exporting?passwordField(body,"再次输入密码"):null;
  dialog=new AlertDialog.Builder(activity,R.style.SecondaryDialog).setTitle(exporting?"导出加密备份":"验证加密备份").setView(body).setNegativeButton("取消",null).setPositiveButton(exporting?"导出":"验证",null).create();dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);dialog.show();ui.styleDialog(dialog,false);
  dialog.setOnDismissListener(d->{password.getText().clear();if(repeat!=null)repeat.getText().clear();});
  dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
   if(busy)return;if(exporting&&password.length()<8){password.setError("请设置至少 8 位密码");return;}if(repeat!=null&&!password.getText().toString().equals(repeat.getText().toString())){repeat.setError("两次密码不一致");return;}
   char[] secret=new char[password.length()];password.getText().getChars(0,password.length(),secret,0);dialog.dismiss();busy=true;message(exporting?"正在加密备份…":"正在验证备份…");
   worker.execute(()->{try{if(exporting){byte[] encrypted=LocalBackup.export(activity,secret);try(OutputStream out=activity.getContentResolver().openOutputStream(uri,"wt")){if(out==null)throw new IOException("无法写入备份文件");out.write(encrypted);out.flush();}post(()->message("加密备份已导出"));}else{JSONObject data;try(InputStream in=activity.getContentResolver().openInputStream(uri)){data=LocalBackup.open(activity,in,secret);}post(()->confirm(data));}}
    catch(Exception e){String text=e instanceof IOException?e.getMessage():"备份处理失败，文件内容或版本不兼容；现有数据未更改。";post(()->message(text));}finally{Arrays.fill(secret,'\0');post(()->busy=false);}});
  });
 }
 private void post(Runnable action){activity.runOnUiThread(()->{if(!closed)action.run();});}
 private void confirm(JSONObject data){
  dialog=new AlertDialog.Builder(activity,R.style.SecondaryDialog).setTitle("覆盖本机数据？").setMessage("将用备份替换全部课表、课程调整、考试、任务、快递及主要设置。账号密码、API 配置、AI 聊天和网页会话保留原状，不从备份导入。\n\n确认后应用关闭，请重新打开完成恢复。恢复前会保存加密回退副本，失败保留原数据。").setNegativeButton("取消",null).setPositiveButton("确认恢复并关闭",(d,w)->{if(busy)return;busy=true;worker.execute(()->{try{LocalBackup.stage(activity,data);post(()->{activity.finishAffinity();android.os.Process.killProcess(android.os.Process.myPid());});}catch(Exception e){post(()->{busy=false;message("无法准备恢复，现有数据未更改。请检查存储空间后重试。");});}});}).create();dialog.show();ui.styleDialog(dialog,true);
 }
 void close(){closed=true;if(dialog!=null)dialog.dismiss();worker.shutdown();}
}
