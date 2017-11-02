package cn.androidstudy.course07;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private EditText etUrl;
    private ImageView ivPic;
    String postURL = "http://10.66.29.238:8080/AndroidTest/UrlPost.jsp";
    //6.声明handler,并重写handleMessage方法，处理消息
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==1){
                ivPic.setImageBitmap((Bitmap) msg.obj);
            }else if(msg.what==2){
                Toast.makeText(MainActivity.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
            
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUrl = (EditText)findViewById(R.id.editText);
        ivPic = (ImageView)findViewById(R.id.imageView);
    }
    //使用Thread+Handler
    public void downPic(View view){
        final String strUrl = etUrl.getText().toString();
        new Thread(){
            @Override
            public void run() {
                try {
                    // 1.声明访问的路径
                    URL url = new URL(strUrl);
                    // 2.通过路径得到一个连接 http的连接
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    // 3.判断服务器给我们返回的状态信息。
                    // 200 成功 302 从定向 404资源没找到 5xx 服务器内部错误
                    int code = con.getResponseCode();
                    if (code == 200) {
                        InputStream inputStream = con.getInputStream();
                        //4.将流转换为图片
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        //5.更新ui ，不能写在子线程
                        Message msg = new Message();
                        msg.obj = bitmap;
                        msg.what = 1;
                        handler.sendMessage(msg);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }
    //2.2使用异步任务
    public void downPic2(View view){
        new DownImage().execute(etUrl.getText().toString());
    }
    //2.1 定义一个类，继承AsyncTask
    class DownImage extends AsyncTask<String,Void,Bitmap>{

        @Override
        protected Bitmap doInBackground(String... params) {

            return getImage(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ivPic.setImageBitmap(bitmap);
        }
    }
    private Bitmap getImage(String strUrl){
        try {
            // 1.声明访问的路径
            URL url = new URL(strUrl);
            // 2.通过路径得到一个连接 http的连接
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            // 3.判断服务器给我们返回的状态信息。
            // 200 成功 302 从定向 404资源没找到 5xx 服务器内部错误
            int code = con.getResponseCode();
            if (code == 200) {
                InputStream inputStream = con.getInputStream();
                //4.将流转换为图片
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void postHttp(View view){
        GetURLResourcesByPost();
    }
    public void GetURLResourcesByPost(){
        new Thread(new Runnable(){
            public void run(){
                try{
                    String Param = "username=" + "张三";	//发送的数据
                    byte[] PostData = Param.getBytes();
                    URL myUrl = new URL(postURL);	//创建一个HttpURLConnection对象，打开链接
                    HttpURLConnection myConn = (HttpURLConnection)myUrl.openConnection();
                    myConn.setConnectTimeout(3000);			//设置连接超时
                    myConn.setDoInput(true);   				//设置输入允许
                    myConn.setDoOutput(true);   			//设置输出允许
                    myConn.setRequestMethod("POST");   		//设置POST方式请求
                    myConn.setUseCaches(false);   			//Post方法不能使用Cache
                    myConn.setInstanceFollowRedirects(true);  //允许HHTP重定向
                    myConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");				//配置请求设置
                    myConn.connect();
                    //发送数据
                    DataOutputStream out = new DataOutputStream(
                            myConn.getOutputStream());
                    out.write(PostData);
                    out.flush();
                    out.close();
                    if(myConn.getResponseCode()==200){		//判断连接状态
                        InputStreamReader in = new InputStreamReader(myConn.getInputStream());
                        BufferedReader buffer = new BufferedReader(in);
                        String inputLine = null;
                        StringBuffer pageBuffer = new StringBuffer();
                        while((inputLine = buffer.readLine())!= null){
                            pageBuffer.append(inputLine +"\n");
                        }
                        //设置字符编码格式
                        String txt =  new String(pageBuffer.toString().getBytes("UTF-8"));
                        Message msg = new Message();
                        msg.obj = txt;
                        msg.what = 2;
                        handler.sendMessage(msg);                        
                        in.close();
                        buffer.close();
                        myConn.disconnect();
                    }
                    else{
                        handler.sendEmptyMessage(3);
                    }
                }
                catch(Exception e){
                    handler.sendEmptyMessage(3);
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
