package com.example.nabaedalpartner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.kakao.auth.AuthType;
import com.kakao.auth.Session;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

public class LoginActivity extends AppCompatActivity
{
    private final String TAG = "LoginActivity";


    private EditText etUname, etPass;
    private Button btnlogin;
    private TextView tvreg,id_btn,password_btn,kakao_btn;
    //private PreferenceHelper preferenceHelper;
    private String fcm ="none";
    //private java.net.Socket mSocket;

    private SessionCallback sessionCallback ;
    Session session;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sessionCallback  = new SessionCallback(this);
        getHashKey();

        //preferenceHelper = new PreferenceHelper(this);

        etUname = (EditText) findViewById(R.id.etusername);
        etPass = (EditText) findViewById(R.id.etpassword);

        btnlogin = (Button) findViewById(R.id.btn);
        tvreg = (TextView) findViewById(R.id.tvreg);
        id_btn = findViewById(R.id.id_btn);
        password_btn = findViewById(R.id.password_btn);


        // mSocket = Socket_Nouse.getInstance();
        //mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener()
        //});
        //mSocket.connect();

        session = Session.getCurrentSession();
        session.addCallback(sessionCallback);


        kakao_btn= findViewById(R.id.kakao_btn);
        //소켓통신임의 테스트용
        kakao_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                session.open(AuthType.KAKAO_TALK, LoginActivity.this);





            }
        });





        tvreg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(intent,0);
                //LoginActivity.this.finish();
            }
        });
        id_btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(LoginActivity.this, IdFindActivity.class);
                //intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);
                //startActivity(intent);
            }
        });
        password_btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(LoginActivity.this, PasswordFindActivity.class);
                //intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);
                //startActivity(intent);
            }
        });


        btnlogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                loginUser();
            }
        });


        // [START log_reg_token]
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, msg);
                        fcm = token;
                        //Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
        // [END log_reg_token]








    }




    private void parseRegDataInvite(String response) throws JSONException
    {
        JSONObject jsonObject = new JSONObject(response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1));

        if (jsonObject.optString("status").equals("true"))
        {
            Log.e("dd","정상보냄");
            //saveInfo(response);
            //shopidxBycode = jsonObject.getString("shopidx");
            //Toast.makeText(MainActivity.this, "[ "+jsonObject.getString("shopname")+" ] 매장 인증되었습니다", Toast.LENGTH_SHORT).show();
            //가입누를때 삭제하는거 코드는 인증하고 안쓸수도있으니까
            //숍 idx 0 이면 점주인거임 ㅇㅇ active 중에

            //manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        }
        else
        {
            Log.e("dd","정상보냄X");
            //Toast.makeText(MainActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
            //manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    private void loginUser()
    {

        final String username = etUname.getText().toString().trim();
        final String password = etPass.getText().toString().trim();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(LoginInterface.LOGIN_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        LoginInterface api = retrofit.create(LoginInterface.class);
        Call<String> call = api.getUserLogin(username, password,fcm);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    //json = json.replace("\\\"","'");
                    //JSONObject jo = new JSONObject(json.substring(1,json.length()-1));
                    Log.e("onSuccess", response.body());

                    String jsonResponse = response.body();
                    parseLoginData(jsonResponse);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });





    }
    private void getHashKey(){
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
    }


    private void parseLoginData(String response)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1));

            if (jsonObject.getString("status").equals("true"))
            {
                //saveInfo(response);
                //로그인성공하면 반갑습니다 누구누구님
                //idx + name 해서 넘기게끔



                Toast.makeText(LoginActivity.this, jsonObject.getString("name")+"님 환영합니다", Toast.LENGTH_SHORT).show();


                //mSocket.emit("join-room", gson.toJson(new RoomData(username, roomNumber)));

                //mSocket.emit("join-room", jsonObject.getString("idx"));

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                //idx랑 name 추가해서 인텐트 넘겨주기
                intent.putExtra("idx", jsonObject.getString("idx"));
                intent.putExtra("name", jsonObject.getString("name"));
                intent.putExtra("nickname", jsonObject.getString("nickname"));
                intent.putExtra("nickcolor", jsonObject.getString("nickcolor"));
                intent.putExtra("nickfile", jsonObject.getString("nickfile"));
                intent.putExtra("nickfontcolor", jsonObject.getString("nickfontcolor"));
                intent.putExtra("shopidx", jsonObject.getString("shopidx"));
                intent.putExtra("shopPhone", jsonObject.getString("shopPhone"));
                //isWorking 초기화용
                intent.putExtra("isWorking", jsonObject.getString("isWorking"));
                intent.putExtra("shopAddr", jsonObject.getString("shopAddr"));
                //final String username = etUname.getText().toString().trim();
                //final String password = etPass.getText().toString().trim();
                intent.putExtra("email", etUname.getText().toString().trim());
                intent.putExtra("password", etPass.getText().toString().trim());

                intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);

            }else{
                Toast.makeText(LoginActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 카카오톡|스토리 간편로그인 실행 결과를 받아서 SDK로 전달
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        } else if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            //회원가입성공
            String email = data.getStringExtra("email");
            String password = data.getStringExtra("password");
            etUname.setText(email);
            etPass.setText(password);
            // do something with B's return values
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mSocket.disconnect();
        // 세션 콜백 삭제
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

}