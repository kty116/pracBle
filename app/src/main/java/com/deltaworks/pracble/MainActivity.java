package com.deltaworks.pracble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.clj.fastble.BleManager;
import com.crashlytics.android.Crashlytics;
import com.deltaworks.pracble.commonLib.CommonCollection;
import com.deltaworks.pracble.databinding.ActivityMainBinding;
import com.deltaworks.pracble.event.DTGCarDataEvent;
import com.deltaworks.pracble.event.Event;
import com.deltaworks.pracble.event.FinishAppEvent;
import com.deltaworks.pracble.event.ToastEvent;
import com.deltaworks.pracble.service.MainService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MainActivity extends AppCompatActivity {


    /**
     * 파싱 변수
     */

    public static boolean sVisibleActivity;  //화면 보이면 노티 눌렀을때 다시 액티비티 켜지지 않게 설정하는 변수

    public static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;
    private Intent mGattServiceIntent;
    public static BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);


        setSupportActionBar(binding.toolbar);

        checkBleSupportAndInitialize();

        mGattServiceIntent = new Intent(this, MainService.class);
        mGattServiceIntent.setAction(MainService.ACTION_START_CONNECT);
        startService(mGattServiceIntent);

    }

    private void checkBleSupportAndInitialize() {
        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish();
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_close) {

            stopService(mGattServiceIntent);
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sVisibleActivity = true;
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sVisibleActivity = false;
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEvent(Event event) {
        if (event instanceof DTGCarDataEvent) { //알람 이벤트
            DTGCarDataEvent dtgCarDataEvent = (DTGCarDataEvent) event;
            binding.bleData.setText(dtgCarDataEvent.getDtgInfo().toScreenString());

        } else if (event instanceof ToastEvent) {
            ToastEvent toastEvent = (ToastEvent) event;
            Toast.makeText(this, "" + toastEvent.getText(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sVisibleActivity = false;
    }


}
