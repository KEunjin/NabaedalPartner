package com.example.nabaedalpartner;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.nio.charset.StandardCharsets;

public class DaumWebViewActivity extends AppCompatActivity {

    private WebView daum_webView;

    private TextView daum_result;

    private Handler handler;
    String originalT,shopAddr;
    Dialog dialog =null;



    class MyJavaScriptInterface
    {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processDATA(String data) {
            Bundle extra = new Bundle();
            Intent intent = new Intent();
            extra.putString("data", data);
            intent.putExtras(extra);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_daum_web_view);






        daum_result = (TextView) findViewById(R.id.daum_result);
        originalT = getIntent().getStringExtra("originalT");
        shopAddr = getIntent().getStringExtra("shopAddr");

        // WebView 초기화

        init_webView();


        // 핸들러를 통한 JavaScript 이벤트 반응

        handler = new Handler();

    }


    public void init_webView() {

        // WebView 설정

        daum_webView = (WebView) findViewById(R.id.daum_webview);



        // JavaScript 허용

        daum_webView.getSettings().setJavaScriptEnabled(true);
        daum_webView.getSettings().setSupportMultipleWindows(true);

        // JavaScript의 window.open 허용

        daum_webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);



        //browser = (WebView) findViewById(R.id.webView);
        //browser.getSettings().setJavaScriptEnabled(true);
       // daum_webView.addJavascriptInterface(new DaumWebViewActivity.MyJavaScriptInterface(), "Android");
        //System.out.println(originalT);
        //browser.setWebViewClient(new WebViewClient() {


        // JavaScript이벤트에 대응할 함수를 정의 한 클래스를 붙여줌

        daum_webView.addJavascriptInterface(new AndroidBridge(), "TestApp");


        // web client 를 chrome 으로 설정

        //daum_webView.setWebChromeClient(new WebChromeClient());


        // webview url load. php 파일 주소

        // 구글에서 제공하는 크롬클라이언트를 생성한다.
        //HelloWebChromeClient testChromeClient = new HelloWebChromeClient();

//생성한 크롬 클라이언트를 웹뷰에 셋한다
        daum_webView .setWebChromeClient(new webChromeClient());

        String contentText2 = java.net.URLEncoder.encode(new String(originalT.getBytes(StandardCharsets.UTF_8)));
        daum_webView.loadUrl("https://nabaedal.ga/daum_address.php?originalT="+contentText2+"&shopAddr="+shopAddr);




    }
    private class webChromeClient extends WebChromeClient{
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            WebView newWebView = new WebView(DaumWebViewActivity.this);
            WebSettings webSettings = newWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);

            dialog = new Dialog(DaumWebViewActivity.this);
            dialog.setContentView(newWebView);

            ViewGroup.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setAttributes((WindowManager.LayoutParams)params);
            dialog.setOnDismissListener(

                    new DialogInterface.OnDismissListener() {

                        @Override

                        public void onDismiss(DialogInterface dialogInterface) {

                            // 다이얼로그가 사라질때 할 행위
                            finish();
                        }
                    });

            dialog.show();
            newWebView.setWebChromeClient(new WebChromeClient(){
                @Override
                public void onCloseWindow(WebView window) {
                  dialog.dismiss();
                  //finish();
                }
            });

            ((WebView.WebViewTransport)resultMsg.obj).setWebView(newWebView);
            resultMsg.sendToTarget();
            return true;
        }
    }


    private class AndroidBridge {

        @JavascriptInterface

        public void setAddress(final String arg1,final  String arg2,final  String arg3,
                               final  String arg4,final  String arg5,final  String arg6) {

            handler.post(new Runnable() {

                @Override

                public void run() {

                    //daum_result.setText(String.format("(%s) %s %s", arg1, arg2, arg3));

                    // WebView를 초기화 하지않으면 재사용할 수 없음

                    //init_webView();
                    dialog.dismiss();

                    Bundle extra = new Bundle();
                    Intent intent = new Intent();
                    // window.TestApp.setAddress(addr_input,addisNew_input,addrfull_input,addrdetail_input,addrdong_input,addrkm_input);
                    //
                    extra.putString("addr_input", arg1);
                    extra.putString("addisNew_input", arg2);
                    extra.putString("addrfull_input", arg3);

                    extra.putString("addrdetail_input", arg4);
                    extra.putString("addrdong_input", arg5);
                    extra.putString("addrkm_input", arg6);

                    intent.putExtras(extra);
                    setResult(RESULT_OK, intent);
                    finish();

                }

            });

        }

    }

}
