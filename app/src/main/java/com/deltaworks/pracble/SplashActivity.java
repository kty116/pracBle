package com.deltaworks.pracble;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.deltaworks.pracble.databinding.ActivitySplashBinding;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

import javax.annotation.Nullable;
import javax.sql.DataSource;

public class SplashActivity extends AppCompatActivity {

    private Handler mHandler;
    private boolean isPermissionCheck;
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);

//        GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(binding.progressImage);
//
//        Glide.with(this)
//                .load("https://media.giphy.com/media/l4Ho7AfNzHCtwGR0s/giphy.gif")
//                .into(gifImage);

        permissionCheck();

    }

    /**
     * 퍼미션 체크
     */
    public void permissionCheck() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //마시멜로우 이상인지 체크

            int[] permissionChecks = new int[6];

            permissionChecks[0] = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissionChecks[1] = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            permissionChecks[2] = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            permissionChecks[3] = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            permissionChecks[4] = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
            permissionChecks[5] = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

            if (permissionChecks[0] == PackageManager.PERMISSION_DENIED || permissionChecks[1] == PackageManager.PERMISSION_DENIED || permissionChecks[2] == PackageManager.PERMISSION_DENIED ||
                    permissionChecks[3] == PackageManager.PERMISSION_DENIED || permissionChecks[4] == PackageManager.PERMISSION_DENIED || permissionChecks[5] == PackageManager.PERMISSION_DENIED) {  //하나라도 허락안된거 있으면
                setPermissionCheck();
            } else {
                //퍼미션값 다 있으면
                Log.d("ddddddd", "run: 퍼미션값 다있음");
                splashThread();
            }
        } else {
//            마시멜로우 미만
//            퍼미션 체크 x
            Log.d("ddddddd", "run: 퍼미션 체크 안함");
            splashThread();
        }
    }

    public void setPermissionCheck() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {  // 퍼미션 체크 모두 승인 받으면
                Log.d("ddddddd", "run: 퍼미션 모두 승인");
                splashThread();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(SplashActivity.this, "If you deny permission, this service will not be available.", Toast.LENGTH_LONG).show();
                Log.d("ddddddd", "run: 퍼미션 모두 거절");
                finish();
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
//                .setRationaleMessage("구글 로그인을 하기 위해서는 주소록 접근 권한이 필요해요")
                .setDeniedMessage("If you deny permission, this service will not be available.\nSet permissions.\n[setting] > [permission]")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE)
                .check();
    }

    public void splashThread() {

        mHandler = new Handler();
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                Log.d("ddddddd", "run: 스레드");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mHandler.post(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void run() {

                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                });
            }
        });
        thread.start();
    }


}
