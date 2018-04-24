package com.deltaworks.pracble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.clj.fastble.BleManager;
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

    public static boolean Cycle_rcvStart = false;
    public static boolean Info_rcv = false;

    public static boolean Log_rcvStart = false;
    public static boolean Keyevent_rcvStart = false;

    public static int CycleData_Index = 0;
    public int Rcv_Cnt = 0;

    public static byte[] CycleData = new byte[164];    // Ts2에서 오는 정주기

    public String UUID_SERVICE = "0003cdd0-0000-1000-8000-00805f9b0131";

    public String UUID_CHAR_WRITE = "0003cdd1-0000-1000-8000-00805f9b0131";
    public String UUID_CHAR_READ = "0003cdd2-0000-1000-8000-00805f9b0131";

    public String UUID_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";
    public String UUID_DESCRIPTOR_WRITE = "00002902-0000-1000-8000-00805f9b34fb";
    public String UUID_DESCRIPTOR_READ = "00002902-0000-1000-8000-00805f9b34fb";

    private static int TIME_OUT_SCAN = 10000;

    public static final String DEVICE_NAME = "TS2_BLE";
    public boolean isAutoConnect = true;
    public boolean isDiscoverBle = false;
    public static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;
    private Intent mGattServiceIntent;
    public static BluetoothAdapter mBluetoothAdapter;

    //    public Activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        // Inflate the menu; this adds items to the action bar if it is present.
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
