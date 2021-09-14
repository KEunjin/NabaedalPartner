package com.example.nabaedalpartner;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface LoginInterface {
    String LOGIN_URL = "https://nabaedal.ga/";
    String LOGIN_URL2 = "https://nabaedal.ga:7777/";


    @FormUrlEncoded
    @POST("retrofit_addchat.php")
    Call<String> addChat(
            @Field("from") String from,
            @Field("to") String to,
            @Field("msg") String msg,
            @Field("type") String type,
            @Field("date") String date
    );

    @FormUrlEncoded
    @POST("retrofit_gumundetail.php")
    Call<String> gumunDetail(
            @Field("idx") String idx
    );




    @FormUrlEncoded
    @POST("retrofit_simplelogin.php")
    Call<String> getUserLogin(
            @Field("username") String username,
            @Field("password") String password,
            @Field("fcm") String fcm
    );

    @FormUrlEncoded
    @POST("retrofit_simplelogin_partner.php")
    Call<String> getUserLoginPartner(
            @Field("id") String id
    );//@Field("fcm") String fcm할거면 한개에 웹 앱 각각 fcm 넣어줘야



    @FormUrlEncoded
    @POST("retrofit_checkaddr_partner_camera.php")
    Call<String> getCheckaddrPartner(
            @Field("data") String data
    );//통쨰로 걍 검색해서 있으면 파싱해서 넣고 없으면 넘어가고 다 돌려보는거임 ㅇㅇ



    @FormUrlEncoded
    @POST("retrofit_addGumun_partner.php")
    Call<String> addGumunPartner(
            @Field("shopidx") String shopidx,
            @Field("addrfull") String addrfull,
            @Field("addr") String addr,
            @Field("addrdetail") String addrdetail,
            @Field("addrisnew") String addrisnew,
            @Field("addrdong") String addrdong,
            @Field("addrnew") String addrnew,
            @Field("addrold") String addrold,//km
            @Field("addrkm") String addrkm,//km
            @Field("phone") String phone,
            @Field("want") String want,
            @Field("root") String root,
            @Field("payroot") String payroot,
            @Field("payamount") String payamount,
            @Field("menu") String menu
    );

    @FormUrlEncoded
    @POST("retrofit_isRiderMoneyGet.php")
    Call<String> getisRiderMoneyGet(
            @Field("idx") String idx,
            @Field("isRiderMoneyGet") String isRiderMoneyGet
    );

    @FormUrlEncoded
    @POST("addgumun_getaddrkm.php")
    Call<String> getaddrkm(
            @Field("shopidx") String shopidx,
            @Field("gumunidx") String gumunidx
    );




    @FormUrlEncoded
    @POST("rideronoff.php")
    Call<String> getRiderOnOff(
            @Field("idx") String idx,
            @Field("title") String title,
            @Field("content") String content,
            @Field("onoff") String onoff
    );

    @FormUrlEncoded
    @POST("timerget.php")
    Call<String> getTimer(
            @Field("idx") String idx
    );


    @FormUrlEncoded
    @POST("shoparrive.php")
    Call<String> getShoparrive(
            @Field("idx") String idx
    );

    @FormUrlEncoded
    @POST("baedalarrive.php")
    Call<String> getBaedalarrive(
            @Field("idx") String idx
    );

}
