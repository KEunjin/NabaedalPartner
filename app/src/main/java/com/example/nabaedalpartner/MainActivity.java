package com.example.nabaedalpartner;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.overlay.Marker;
import com.ncorti.slidetoact.SlideToActView;
import com.view.circulartimerview.CircularTimerView;

import org.angmarch.views.NiceSpinner;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity{
    private  final int SEARCH_ADDRESS_ACTIVITY =100 ;
    TessBaseAPI tessBaseAPI;

    static Button button;
    ImageView imageView;
    CameraSurfaceView surfaceView;
    TextView textView;

    EditText editTextTextPostalAddress;
    EditText editTextTextPersonName;
    EditText editTextTextPersonName2;
    EditText editTextPhone;
    EditText editTextTextPersonName3;
    static EditText editTextNumber;
    static EditText editTextTextPersonName6;
    RadioGroup payrootG,rootG;
    //그냥무조건추가임 ㅇㅇ 주소랑 전번만체크후에
    Button button3,button2;

    public double shoplat,shoplng;


    public String idx, name, nickname, nickcolor, nickfile, nickfontcolor,
                  shopidx,shopPhone,email,password,isWorking,shopAddr;


    private static final String CLOUD_VISION_API_KEY = "AIzaSyAZCxF5l91jX2Qu_MbFQbggb81_M-0e0SQ";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_LABEL_RESULTS = 10;
    private static final int MAX_DIMENSION = 1200;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    //addrfull,addrnew, addrold, addrdetail,
    //    addrkm,addrdong,
    //    phone,want,payroot,payamount,root,menu,shopidx,rideridx,isNew
    String addrfull="",addrnew="", addrold="", addrdetail="", addrkm="",addrdong="",phone="",want="",payroot="",payamount="",root="",menu="",isNew="";

    boolean isAddrCheck = false;//주소변환했는지
    //static Camera camera = null;
    int isAddrFind = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                //Toast.makeText(getApplicationContext(), "권한이 허용됨", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                //Toast.makeText(getApplicationContext(), "권한이 거부됨", Toast.LENGTH_SHORT).show();
            }
        };

        //권한 체크
        TedPermission.with(getApplicationContext())
                .setPermissionListener(permissionListener)
                .setRationaleMessage("카메라 권한이 필요합니다.")
                .setDeniedMessage("카메라 권한을 거부하셨습니다.")
                .setPermissions(Manifest.permission.CAMERA)
                .check();


        editTextTextPostalAddress = findViewById(R.id.editTextTextPostalAddress);//등록주소
        editTextTextPersonName = findViewById(R.id.editTextTextPersonName);//주소
        editTextTextPersonName2 = findViewById(R.id.editTextTextPersonName2);//상세주소


        editTextPhone = findViewById(R.id.editTextPhone);
        //등록주소 daum.Postcode 주소변환
        //주소
        //        상세주소
        //전화번호
        //요청사항 기타비번까지 이건그냥통째
        //        결제방법
        //결제금액
        //        구분
        //메뉴
        //핸드폰번호

        //$idx = $_POST['idx2'];
        //
        //$addrfull_input = $_POST['addrfull_input'];
        //$addr_input = $_POST['addr_input'];
        //$addrdetail_input = $_POST['addrdetail_input'];
        //
        //$addisNew_input = $_POST['$addisNew_input'];
        //$addrdong = $_POST['addrdong_input'];
        //
        //$addrkm = $_POST['addrkm_input'];
        //$addrnew = $_POST['addrnew_input'];
        //$addrold = $_POST['addrold_input'];
        //
        ////$strTok =explode('' , $addr_input);
        ////$cnt = count($strTok);
        ////for($i = 0 ; $i < $cnt ; $i++){
        ////$strTok[$i]
        ////동 포함되면 이거임
        ////    if(strpos($strTok[$i],'동') !== false){
        //    	// $strposTest 변수에 '동' 가 포함되어 있는 경우.
        //  //     = $strTok[$i];
        //  //    break;
        //  //  }
        ////}
        //
        //$phone_input = $_POST['phone_input'];
        ////요청사항만이렇게여러개임
        //$want ="";
        //if(isset($_POST['want_input'])){
        //      foreach($_POST['want_input'] as $entry){
        //                      $want = $want.$entry."/";
        //      }
        //}
        //
        //if(isset($_POST['wantpass_input'])&&$_POST['wantpass_input']!=""){
        //    $wantpass_input = $_POST['wantpass_input'];
        //    $want = $want."기타(비밀번호):".$wantpass_input."/";
        //}
        //
        ////주소변환 일단 네이버로 (나중에 다음카카오 도로명 주소 api 더 정확하게 하는게 일단 2차 자료로 들어오니까 ,,,)
        ////합치기해서 보내고 통신
        //
        ////$addrkm new old 받아야 동은 여기서 처리가능함
        //
        //
        //$payroot_input = $_POST['payroot_input'];
        //$payamount_input = $_POST['payamount_input'];
        //$root_input = $_POST['root_input'];
        //$menu_input = $_POST['menu_input'];

        Pattern VALID_PHONE_REGEX_ALPHA_NUM = Pattern.compile( "^(01\\d{1}|02|0505|0502|0506|0\\d{1,2})-?(\\d{3,4})-?(\\d{4})");
                //Pattern.compile("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$");

        editTextPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        //addrkm는 요청하는걸로 그냥 html 상에서 하기로 하자 ㅇㅇ
        // Pattern VALID_PHONE_REGEX_ALPHA_NUM = Pattern.compile("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$");
        //if(!matcher3.matches()){
        //    Toast.makeText(MainActivity.this,"휴대전화 번호를 확인해주세요", Toast.LENGTH_SHORT).show();
        //}

        editTextTextPersonName3 = findViewById(R.id.editTextTextPersonName3);
        editTextNumber = findViewById(R.id.editTextNumber);
        editTextTextPersonName6 = findViewById(R.id.editTextTextPersonName6);

        payrootG = findViewById(R.id.payrootG);
        rootG = findViewById(R.id.rootG);
        button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //주문추가

                Matcher matcher3 = VALID_PHONE_REGEX_ALPHA_NUM.matcher(editTextPhone.getText().toString());

                if(editTextTextPostalAddress.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this,"주소를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }else if(!isAddrCheck){
                    Toast.makeText(MainActivity.this,"주소 변환을 해주세요.", Toast.LENGTH_SHORT).show();
                }else if(isAddrCheck&&!addrnew.equals(editTextTextPersonName.getText().toString())){
                    Toast.makeText(MainActivity.this,"주소 변환을 해주세요.", Toast.LENGTH_SHORT).show();
                }else if(editTextPhone.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this,"전화번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }else{

                    //Toast.makeText(MainActivity.this,"클릭.", Toast.LENGTH_SHORT).show();
                    //일단보내자
                    //카카오로그인하면 shopAddr 올거야 여기로도 ㅇㅇ
                    //Call<String> addGumunPartner(
                    //            @Field("shopidx") String shopidx,
                    //            @Field("addrfull") String addrfull,
                    //            @Field("addr") String addr,
                    //            @Field("addrdetail") String addrdetail,
                    //            @Field("addrisnew") String addrisnew,
                    //            @Field("addrdong") String addrdong,
                    //            @Field("addrnew") String addrnew,
                    //            @Field("addrold") String addrold,//km
                    //            @Field("phone") String phone,
                    //            @Field("want") String want,
                    //            @Field("root") String root,
                    //            @Field("payroot") String payroot,
                    //            @Field("payamount") String payamount,
                    //            @Field("menu") String menu
                    //    );
                    //두 사이 거리 해보자,,,,옥에이




                    addrnew= editTextTextPersonName.getText().toString().trim();
                    System.out.println("여기"+addrnew);
                    //shoplat=y lng


                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    List<Address> addresses = null;
                    try {
                        addresses = geocoder.getFromLocationName(addrnew+"", 1);
                        Address address = addresses.get(0);
                        double longitude = address.getLongitude();
                        double latitude = address.getLatitude();




                        double addrlng = longitude;
                        double addrlat =latitude;

                        double distanceKiloMeter =
                                distance(shoplat, shoplng, addrlat, addrlng, "kilometer");

                        //double per2 = 3.1494949828;
                        double per = Double.parseDouble(String.format("%.1f",distanceKiloMeter));
                        String addrkmF = per+" km";



                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl(LoginInterface.LOGIN_URL)
                                .addConverterFactory(ScalarsConverterFactory.create())
                                .build();

                        LoginInterface api = retrofit.create(LoginInterface.class);
                        addrfull = editTextTextPostalAddress.getText().toString();


                        addrdetail =editTextTextPersonName2.getText().toString();
                        //shopid0임
                        Call<String> call = api.addGumunPartner(idx,addrfull,addrnew,addrdetail,isNew,addrdong,addrnew,
                                addrnew,addrkmF,
                                editTextPhone.getText().toString(),editTextTextPersonName3.getText().toString(),
                                root,payroot,
                                editTextNumber.getText().toString(),editTextTextPersonName6.getText().toString());
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
                                    Toast.makeText(MainActivity.this,"주문을 추가하였습니다. 매장웹의 주문탭을 확인해주세요.", Toast.LENGTH_SHORT).show();
                                    editTextTextPostalAddress.setText(null);
                                    editTextTextPersonName.setText(null);
                                    editTextTextPersonName2.setText(null);
                                    editTextPhone.setText(null);
                                    editTextTextPersonName3.setText(null);
                                    editTextNumber.setText(null);
                                    editTextTextPersonName6.setText(null);
                                    rootG.clearCheck();
                                    payrootG.clearCheck();
                                    //EditText editTextTextPostalAddress,editTextTextPersonName,editTextTextPersonName2,
                                    //        editTextPhone,editTextTextPersonName3,editTextNumber,editTextTextPersonName6;

                                    System.out.println(response.body() +"");
                                    String jsonResponse = response.body();
                                    parseData(jsonResponse);
                                }else{
                                    System.out.println(response.body() +"");
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
                            {
                                Log.e(TAG, "에러 = " + t.getMessage());

                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }







































                }





            }
        });
        
        button3 = findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //조건만 주문에 맞으면 다 넘기기 ㅇㅇ fcm으로보내기
                Intent i = new Intent(MainActivity.this, DaumWebViewActivity.class);
                i.putExtra("originalT",editTextTextPostalAddress.getText().toString());
                i.putExtra("shopAddr",shopAddr);
                startActivityForResult(i, SEARCH_ADDRESS_ACTIVITY);
            }
        });




        //라디오 그룹 클릭 리스너 
        payrootG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if(i == R.id.radioButton4){
                    payroot ="바로결제";
                    //Toast.makeText(MainActivity.this, "라디오 그룹 버튼1 눌렸습니다.", Toast.LENGTH_SHORT).show();
                } else if(i == R.id.radioButton3){
                    payroot ="카드";
                    //Toast.makeText(MainActivity.this, "라디오 그룹 버튼2 눌렸습니다.", Toast.LENGTH_SHORT).show();
                } else if(i == R.id.radioButton2){
                    payroot ="현금";
                    //Toast.makeText(MainActivity.this, "라디오 그룹 버튼1 눌렸습니다.", Toast.LENGTH_SHORT).show();
                } else if(i == R.id.radioButton){
                    payroot ="이체";
                    //Toast.makeText(MainActivity.this, "라디오 그룹 버튼2 눌렸습니다.", Toast.LENGTH_SHORT).show();
                }else if(i == R.id.radioButton5){
                    payroot ="기타";
                    //Toast.makeText(MainActivity.this, "라디오 그룹 버튼2 눌렸습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        rootG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if(i == R.id.radioButton9){
                    root = "배달의민족";
                    //Toast.makeText(MainActivity.this, "라디오 그룹 버튼1 눌렸습니다.", Toast.LENGTH_SHORT).show();
                } else if(i == R.id.radioButton8){
                    root = "요기요";
                    //Toast.makeText(MainActivity.this, "라디오 그룹 버튼2 눌렸습니다.", Toast.LENGTH_SHORT).show();
                } else if(i == R.id.radioButton7){
                    root = "전화";
                    //Toast.makeText(MainActivity.this, "라디오 그룹 버튼1 눌렸습니다.", Toast.LENGTH_SHORT).show();
                } else if(i == R.id.radioButton6){
                    root = "기타";
                    //Toast.makeText(MainActivity.this, "라디오 그룹 버튼2 눌렸습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        Intent secondIntent = getIntent();

        idx =secondIntent.getStringExtra("idx");
        name =secondIntent.getStringExtra("name");
        shopidx= secondIntent.getStringExtra("shopidx");
        nickname =secondIntent.getStringExtra("nickname");
        nickfontcolor =secondIntent.getStringExtra("nickfontcolor");
        nickcolor = secondIntent.getStringExtra("nickcolor");
        nickfile =secondIntent.getStringExtra("nickfile");
        shopPhone =secondIntent.getStringExtra("shopPhone");
        email =secondIntent.getStringExtra("email");
        password =secondIntent.getStringExtra("password");
        isWorking =secondIntent.getStringExtra("isWorking");
        shopAddr =secondIntent.getStringExtra("shopAddr");

        //Toast.makeText(MainActivity.this, shopAddr, Toast.LENGTH_SHORT).show();
        //가져온 주소에서 위도 경도 가져오기
        //변환된 위도 경도 daum 가져오기
        //내가 계산해서 한번에 넣고 알림보내기



        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // All your networking logic
                // should be here
                try {
                    String maptestT = shopAddr;
                    URL githubEndpoint = new URL("https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query="+maptestT);
                    HttpsURLConnection myConnection =
                            (HttpsURLConnection) githubEndpoint.openConnection();
                    myConnection.setRequestProperty("X-NCP-APIGW-API-KEY-ID",
                            "ayndlz9iiv");
                    myConnection.setRequestProperty("X-NCP-APIGW-API-KEY",
                            "KuieyXFkaErCVoZKuXYrhAKWic8YD1O09eHyCrhy");
                    if (myConnection.getResponseCode() == 200) {
                        // Success
                        // Further processing here
                        InputStream responseBody = myConnection.getInputStream();
                        InputStreamReader responseBodyReader =
                                new InputStreamReader(responseBody, "UTF-8");
                        JsonReader jsonReader = new JsonReader(responseBodyReader);

                        jsonReader.beginObject(); // Start processing the JSON object
                        while (jsonReader.hasNext()) { // Loop through all keys
                            String key = jsonReader.nextName(); // Fetch the next key
                            if (key.equals("addresses")) { // Check if desired key
                                // Fetch the value as a String
                                jsonReader.beginArray();
                                while (jsonReader.hasNext())
                                {
                                    read(jsonReader);
                                }
                                jsonReader.endArray();

                                break; // Break out of the loop
                            } else {
                                jsonReader.skipValue(); // Skip values of other keys
                            }
                        }
                        jsonReader.close();
                        myConnection.disconnect();
                    } else {
                        // Error handling code goes here
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

        
        
        
        
        

        imageView = findViewById(R.id.imageView);
        surfaceView = findViewById(R.id.surfaceView);
        textView = findViewById(R.id.textView);

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capture();
            }
        });

        tessBaseAPI = new TessBaseAPI();
        String dir = getFilesDir() + "/tesseract";
   //     if(checkLanguageFile(dir+"/tessdata"))
//            tessBaseAPI.init(dir, "kor");
    }

    private void parseData(String response) {

        try
        {
            JSONObject jsonObject = new JSONObject(response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1));

            if (jsonObject.getString("status").equals("true"))
            {

                String lastidx = jsonObject.getString("message");
                System.out.println("lastidx"+lastidx);
                System.out.println("shopidx"+shopidx);


            }else{
                //Toast.makeText(LoginActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    boolean checkLanguageFile(String dir)
    {
        File file = new File(dir);
        if(!file.exists() && file.mkdirs())
            createFiles(dir);
        else if(file.exists()){
            String filePath = dir + "/eng.traineddata";
            File langDataFile = new File(filePath);
            if(!langDataFile.exists())
                createFiles(dir);
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case SEARCH_ADDRESS_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    isAddrCheck = true;

                    String addrfull_input = intent.getExtras().getString("addrfull_input");
                    if (addrfull_input != null&&!addrfull_input.equals("")) {
                        editTextTextPostalAddress.setText(addrfull_input);
                        addrfull = addrfull_input;
                    }

                    String addr_input = intent.getExtras().getString("addr_input");
                    if (addr_input != null&& !addr_input.equals("")) {
                        editTextTextPersonName.setText(addr_input);
                        addrnew = addr_input;
                    }
                    String addisNew_input = intent.getExtras().getString("addisNew_input");
                    isNew = addisNew_input;//yes no


                    String addrdetail_input = intent.getExtras().getString("addrdetail_input");
                    if (addrdetail_input != null&& !addrdetail_input.equals("")) {
                        editTextTextPersonName2.setText(addrdetail_input);
                        addrdetail = addrdetail_input;
                    }
                    String addrdong_input = intent.getExtras().getString("addrdong_input");
                    addrdong =addrdong_input;
                    String addrkm_input = intent.getExtras().getString("addrkm_input");
                    addrkm =addrkm_input;
                }
                break;
        }
    }

    private void createFiles(String dir)
    {
        AssetManager assetMgr = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = assetMgr.open("eng.traineddata");

            String destFile = dir + "/eng.traineddata";

            outputStream = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void capture()
    {


        editTextTextPostalAddress.setText(null);
        editTextTextPersonName.setText(null);
        editTextTextPersonName2.setText(null);
        editTextPhone.setText(null);
        editTextTextPersonName3.setText(null);
        editTextNumber.setText(null);
        editTextTextPersonName6.setText(null);
        rootG.clearCheck();
        payrootG.clearCheck();
        isAddrCheck = false;


        surfaceView.capture(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;

                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                bitmap = GetRotatedBitmap(bitmap, 90);

                imageView.setImageBitmap(bitmap);

                button.setEnabled(false);
                button.setText("주문지 읽는 중...");

                callCloudVision(bitmap);
                //new AsyncTess().execute(bitmap);

                camera.startPreview();
            }
        });
    }

    public synchronized  Bitmap GetRotatedBitmap(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                if (bitmap != b2) {
                    bitmap = b2;
                }
            } catch (OutOfMemoryError ex) {
                ex.printStackTrace();
            }
        }
        return bitmap;
    }

    private class AsyncTess extends AsyncTask<Bitmap, Integer, String> {
        @Override
        protected String doInBackground(Bitmap... mRelativeParams) {
            tessBaseAPI.setImage(mRelativeParams[0]);
            return tessBaseAPI.getUTF8Text();
        }

        protected void onPostExecute(String result) {
            textView.setText(result);
            
            
            
            
            //Toast.makeText(MainActivity.this, ""+result, Toast.LENGTH_LONG).show();
            System.out.println("Result"+result);

            button.setEnabled(true);
            button.setText("주문지 읽기");
        }
    }




    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                MAX_DIMENSION);

                callCloudVision(bitmap);
               // mMainImage.setImageBitmap(bitmap);

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
              //  Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
           // Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private  double distance(double lat1, double lon1, double lat2, double lon2, String unit) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        if (unit == "kilometer") {
            dist = dist * 1.609344;
        } else if(unit == "meter"){
            dist = dist * 1609.344;
        }

        return (dist);
    }


    // This function converts decimal degrees to radians
    private  double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    private  double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    // 킬로미터(Kilo Meter) 단위
   // double distanceKiloMeter =
    //        distance(37.504198, 127.047967, 37.501025, 127.037701, "kilometer");

    public void read(JsonReader reader) throws Exception
    {
        double x=0,y=0;

        reader.beginObject();
        while (reader.hasNext())
        {
            String name = reader.nextName();

            if (name.equals("x"))
            {
                x = reader.nextDouble();
                System.out.println(x);
            }
            else if (name.equals("y"))
            {

                y = reader.nextDouble();
                System.out.println(y);

                shoplng = x;
                shoplat = y;
            }

            else
            {
                reader.skipValue();
            }
        }
        reader.endObject();
    }
    public void readAddr(JsonReader reader) throws Exception
    {
        double x=0,y=0;

        reader.beginObject();
        while (reader.hasNext())
        {
            String name = reader.nextName();

            if (name.equals("x"))
            {
                x = reader.nextDouble();
                System.out.println(x);
            }
            else if (name.equals("y"))
            {

                y = reader.nextDouble();
                System.out.println(y);

                //shoplng = x;
                //shoplat = y;
                double addrlng = x;
                double addrlat =y;

                double distanceKiloMeter =
                        distance(shoplat, shoplng, addrlat, addrlng, "kilometer");

                //double per2 = 3.1494949828;
                double per = Double.parseDouble(String.format("%.1f",distanceKiloMeter));
                String addrkmF = per+" km";



                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(LoginInterface.LOGIN_URL)
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .build();

                LoginInterface api = retrofit.create(LoginInterface.class);
                addrfull = editTextTextPostalAddress.getText().toString();


                addrdetail =editTextTextPersonName2.getText().toString();
                //shopid0임
                Call<String> call = api.addGumunPartner(idx,addrfull,addrnew,addrdetail,isNew,addrdong,addrnew,
                        addrnew,addrkmF,
                        editTextPhone.getText().toString(),editTextTextPersonName3.getText().toString(),
                        root,payroot,
                        editTextNumber.getText().toString(),editTextTextPersonName6.getText().toString());
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
                            Toast.makeText(MainActivity.this,"주문을 추가하였습니다. 매장웹의 주문탭을 확인해주세요.", Toast.LENGTH_SHORT).show();
                            editTextTextPostalAddress.setText(null);
                            editTextTextPersonName.setText(null);
                            editTextTextPersonName2.setText(null);
                            editTextPhone.setText(null);
                            editTextTextPersonName3.setText(null);
                            editTextNumber.setText(null);
                            editTextTextPersonName6.setText(null);
                            rootG.clearCheck();
                            payrootG.clearCheck();
                            //EditText editTextTextPostalAddress,editTextTextPersonName,editTextTextPersonName2,
                            //        editTextPhone,editTextTextPersonName3,editTextNumber,editTextTextPersonName6;

                            System.out.println(response.body() +"");
                            String jsonResponse = response.body();
                            parseData(jsonResponse);
                        }else{
                            System.out.println(response.body() +"");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
                    {
                        Log.e(TAG, "에러 = " + t.getMessage());

                    }
                });


            }

            else
            {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    /**
                     * We override this so we can inject important identifying fields into the HTTP
                     * headers. This enables use of a restricted cloud platform API key.
                     */
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature labelDetection = new Feature();
                labelDetection.setType("TEXT_DETECTION");
                labelDetection.setMaxResults(MAX_LABEL_RESULTS);
                add(labelDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    private static class LableDetectionTask extends AsyncTask<Object, Void, String> {
        private final WeakReference<MainActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        LableDetectionTask(MainActivity activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                Log.d(TAG, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }

        protected void onPostExecute(String result) {
            MainActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                //TextView imageDetail = activity.findViewById(R.id.image_details);
                //imageDetail.setText(result);
                //Toast.makeText(activity, ""+result, Toast.LENGTH_LONG).show();
                activity.isAddrFind = 0;//클릭하면 펄스
                
                System.out.println("*****************");
                System.out.println("인식한거result:"+result);
                System.out.println("*****************");

                String[] array = result.split("\n");

                for(int i=0;i<array.length;i++) {
                    System.out.println(i+"번째: "+array[i]);
                    if(array[i].contains("배달의민족")){
                        activity.runOnUiThread(new Runnable(){
                            @Override
                            public void run() { activity.findViewById(R.id.radioButton9).performClick();//메뉴
                            }
                        });
                        //activity.root= "배달의민족";
                    }else if(array[i].contains("요기요")){
                        activity.runOnUiThread(new Runnable(){
                            @Override
                            public void run() { activity.findViewById(R.id.radioButton8).performClick();//메뉴
                            }
                        });
                        //activity.root= "요기요";
                    }else if(array[i].contains("set")||array[i].contains("세트")||array[i].contains("SET")){
                        int finalI1 = i;
                        activity.runOnUiThread(new Runnable(){
                            @Override
                            public void run() { activity.editTextTextPersonName6.setText(array[finalI1]);//메뉴
                            }
                        });
                    }else if(array[i].contains("합계")){
                        String intStr = array[i].replaceAll("[^0-9]", "");//숫자만추출

                        activity.runOnUiThread(new Runnable(){
                            @Override
                            public void run() { editTextNumber.setText(intStr);
                            }
                        });
                        //System.out.println(intStr);
                    } else if(array[i].contains("결제")){
                        if(array[i].contains("완료")){
                            //activity.payroot ="바로결제";
                            activity.runOnUiThread(new Runnable(){
                                @Override
                                public void run() { activity.findViewById(R.id.radioButton4).performClick();//메뉴
                                }
                            });
                        }else if(array[i].contains("카드")){
                            //activity.payroot ="카드";
                            activity.runOnUiThread(new Runnable(){
                                @Override
                                public void run() { activity.findViewById(R.id.radioButton3).performClick();//메뉴
                                }
                            });
                        }else if(array[i].contains("현금")){
                            //activity.payroot ="현금";
                            activity.runOnUiThread(new Runnable(){
                                @Override
                                public void run() { activity.findViewById(R.id.radioButton2).performClick();//메뉴
                                }
                            });
                        }
                    }else  if(array[i].contains("밀떡")||array[i].contains("계육")||array[i].contains("정육")||array[i].contains("국내산")){
                        break;//루프탈출긴것만하기로
                    }
                    else {
                        Pattern pattern = //Pattern.compile("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$");
                                Pattern.compile( "^(01\\d{1}|02|0505|0503|0506|0\\d{1,2})-?(\\d{3,4})-?(\\d{4})");
                        Matcher matcher = pattern.matcher(array[i]);
                        String special_str = "" ;
                        while (matcher.find()) {
                            special_str = matcher.group();
                        }
                        if(!special_str.equals("")){

                            String finalSpecial_str = special_str;
                            activity.runOnUiThread(new Runnable(){
                                @Override
                                public void run() { activity.editTextPhone.setText(finalSpecial_str);
                                }
                            });
                        }//핸드폰정규표현식과일치하면 (안심번호도 해야 안심번호랑,, 요청사항수정랑,,,)

                        //출처: https://nanstrong.tistory.com/233 [D의 개발공간(일하면서 공부하면서 끄적끄적)]
                        //주소 전부 돌려보기 ㅇㅇ






                        String maptestT = array[i];
                        Geocoder geocoder = new Geocoder(mActivityWeakReference.get(), Locale.getDefault());
                        List<Address> addresses = null;
                        try {
                            addresses = geocoder.getFromLocationName(maptestT+"", 1);
                            if(addresses.size()>0&&i<6&&activity.isAddrFind==1){
                                Address address = addresses.get(0);
                                mActivityWeakReference.get().editTextTextPostalAddress.setText(maptestT);
                                mActivityWeakReference.get().button3.performClick();
                                activity.isAddrFind =0;
                                //도로명주소만 빼오내는거
                            }else if(addresses.size()>0&&i<6) {//&&!activity.isAddrFind

                                Address address = addresses.get(0);
                                double longitude = address.getLongitude();
                                double latitude = address.getLatitude();
                                //mActivityWeakReference.get().editTextTextPostalAddress.setText(maptestT);
                                //mActivityWeakReference.get().button3.performClick();
                                //activity.isAddrFind = true;
                                activity.isAddrFind++;
                            }

                            } catch (IOException e) {
                                e.printStackTrace();
                                 }

                        /**
                        int finalI = i;
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                // All your networking logic
                                // should be here
                                try {
                                    String maptestT = array[finalI];
                                    URL githubEndpoint = new URL("https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query="+maptestT);
                                    HttpsURLConnection myConnection =
                                            (HttpsURLConnection) githubEndpoint.openConnection();
                                    myConnection.setRequestProperty("X-NCP-APIGW-API-KEY-ID",
                                            "ayndlz9iiv");
                                    myConnection.setRequestProperty("X-NCP-APIGW-API-KEY",
                                            "KuieyXFkaErCVoZKuXYrhAKWic8YD1O09eHyCrhy");
                                    if (myConnection.getResponseCode() == 200) {
                                        // Success
                                        // Further procgeoessing here
                                        InputStream responseBody = myConnection.getInputStream();
                                        InputStreamReader responseBodyReader =
                                                new InputStreamReader(responseBody, "UTF-8");
                                        JsonReader jsonReader = new JsonReader(responseBodyReader);

                                        jsonReader.beginObject(); // Start processing the JSON object
                                        while (jsonReader.hasNext()) { // Loop through all keys
                                            String key = jsonReader.nextName(); // Fetch the next key
                                            if (key.equals("addresses")) { // Check if desired key
                                                // Fetch the value as a String
                                                jsonReader.beginArray();
                                                while (jsonReader.hasNext())
                                                {
                                                    //readAddrCamera(jsonReader);
                                                }
                                                jsonReader.endArray();

                                                if(!activity.isAddrCheck&&finalI<6){//!maptestT.contains("주문")//!maptestT.contains("주문")
                                                    //있는거니까
                                                    activity.runOnUiThread(new Runnable(){
                                                        @Override
                                                        public void run() {
                                                            //activity.editTextTextPostalAddress.setText(maptestT);
                                                            //activity.button3.performClick();
                                                        }
                                                    });

                                                }

                                                break; // Break out of the loop
                                            } else {
                                                jsonReader.skipValue(); // Skip values of other keys
                                            }
                                        }
                                        jsonReader.close();
                                        myConnection.disconnect();
                                    } else {
                                        // Error handling code goes here
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            }
                        });
                        **/


                        //요청사항 수저 0이거는 해야할듯,,

                    }




                }//루프
                System.out.println("*****************");

                button.setEnabled(true);
                button.setText("주문지 읽기");


            }
        }
    }

    private void callCloudVision(final Bitmap bitmap) {
        // Switch text to loading
        //mImageDetails.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        try {
            AsyncTask<Object, Void, String> labelDetectionTask = new LableDetectionTask(this, prepareAnnotationRequest(bitmap));
            labelDetectionTask.execute();
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }
    }

    public  Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private static String convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder("");
        //getTextAnnotations getlabel getannotations
        List<EntityAnnotation> labels = response.getResponses().get(0).getTextAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message.append(String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription()));
                message.append("\n");
            }
        } else {
            message.append("nothing");
        }

        return message.toString();
    }
}