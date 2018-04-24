package com.deltaworks.pracble.retrofit;


import com.deltaworks.pracble.model.FileInfo;
import com.deltaworks.pracble.model.ResponseInfo;
import com.deltaworks.pracble.model.SettingInfo;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by kyoungae on 2018-03-12.
 */

public interface RetrofitService {

    String url = "http://dtg.delta-on.com/";


    //    @Headers({
//            "Content-Type: multipart/form-data"
//    })
    @Multipart
    @POST("dtg_file.php")
//url 다음 바뀌는 것 쓰기
//    Call<String> uploadDTGFile(@Part("token") RequestBody token, @Part("dtgNum") RequestBody dtgNum, @Part("dtg_file[]\"; filename=\"pp.png") ArrayList<RequestBody> dtg_file);  //call 제너릭타입은 받으려는 데이터 타입 ()안의 타입은 내가 보내는 데이터 타입
    Call<FileInfo> uploadDTGFile(@PartMap Map<String, RequestBody> params);  //call 제너릭타입은 받으려는 데이터 타입 ()안의 타입은 내가 보내는 데이터 타입
//    Call<String> uploadDTGFile(@Part ("token") RequestBody token, @Part("dtgNum") RequestBody dtgNum, @Part ArrayList<MultipartBody.Part> dtg_file);  //call 제너릭타입은 받으려는 데이터 타입 ()안의 타입은 내가 보내는 데이터 타입

    @GET("index.php")
    Call<ResponseInfo> sendDTGLocation(@Query("token") String token, @Query("dtgNum") String dtgNum, @Query("lat") String lat, @Query("lon") String lon);  //경도 위도 값 보내기

    @POST("load_time.php")
    Call<SettingInfo> getUploadTime();  //서버에서 업로드 시간 설정


}
