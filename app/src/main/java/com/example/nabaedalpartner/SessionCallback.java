package com.example.nabaedalpartner;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.kakao.auth.ISessionCallback;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.Profile;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.util.OptionalBoolean;
import com.kakao.util.exception.KakaoException;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class SessionCallback implements ISessionCallback {
    Context mContext ;
    public SessionCallback(Context context) {
        this.mContext = context;
    }

    // 로그인에 성공한 상태
    @Override
    public void onSessionOpened() {
        requestMe();
    }

    // 로그인에 실패한 상태
    @Override
    public void onSessionOpenFailed(KakaoException exception) {
        Log.e("SessionCallback :: ", "onSessionOpenFailed : " + exception.getMessage());
    }

    // 사용자 정보 요청
    public void requestMe() {
        UserManagement.getInstance()
                .me(new MeV2ResponseCallback() {
                    @Override
                    public void onSessionClosed(ErrorResult errorResult) {
                        Log.e("KAKAO_API", "세션이 닫혀 있음: " + errorResult);
                    }

                    @Override
                    public void onFailure(ErrorResult errorResult) {
                        Log.e("KAKAO_API", "사용자 정보 요청 실패: " + errorResult);
                    }

                    @Override
                    public void onSuccess(MeV2Response result) {
                        Log.i("KAKAO_API", "사용자 아이디: " + result.getId());
                        // 사용자 id로 shopidx가 0이면 매장임 아니면 라이더는 아예 가입불가 초대코드땜에
                        // shopaddr도 가져와야할듯 fcm은 안 건드리고 ㅇㅇ
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl(LoginInterface.LOGIN_URL)
                                .addConverterFactory(ScalarsConverterFactory.create())
                                .build();
                        String id = result.getId()+"";
                        //Toast.makeText(mContext, id, Toast.LENGTH_SHORT).show();
                        LoginInterface api = retrofit.create(LoginInterface.class);
                        Call<String> call = api.getUserLoginPartner(id);
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
                                Log.e("TAG", "에러 = " + t.getMessage());
                            }
                        });



                        UserAccount kakaoAccount = result.getKakaoAccount();
                        if (kakaoAccount != null) {

                            // 이메일
                            String email = kakaoAccount.getEmail();

                            if (email != null) {
                                Log.i("KAKAO_API", "email: " + email);

                            } else if (kakaoAccount.emailNeedsAgreement() == OptionalBoolean.TRUE) {
                                // 동의 요청 후 이메일 획득 가능
                                // 단, 선택 동의로 설정되어 있다면 서비스 이용 시나리오 상에서 반드시 필요한 경우에만 요청해야 합니다.

                            } else {
                                // 이메일 획득 불가
                            }

                            // 프로필
                            Profile profile = kakaoAccount.getProfile();

                            if (profile != null) {
                                Log.d("KAKAO_API", "nickname: " + profile.getNickname());
                                Log.d("KAKAO_API", "profile image: " + profile.getProfileImageUrl());
                                Log.d("KAKAO_API", "thumbnail image: " + profile.getThumbnailImageUrl());

                            } else if (kakaoAccount.profileNeedsAgreement() == OptionalBoolean.TRUE) {
                                // 동의 요청 후 프로필 정보 획득 가능

                            } else {
                                // 프로필 획득 불가
                            }
                        }
                    }
                });
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



                Toast.makeText(mContext, jsonObject.getString("name")+"님 환영합니다", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(mContext, MainActivity.class);
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

                intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);
                mContext.startActivity(intent);

            }else{
                Toast.makeText(mContext, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

    }

}