package com.deltaworks.pracble.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.crashlytics.android.Crashlytics;
import com.deltaworks.pracble.MainActivity;
import com.deltaworks.pracble.R;
import com.deltaworks.pracble.commonLib.CommonCollection;
import com.deltaworks.pracble.commonLib.FileLib;
import com.deltaworks.pracble.commonLib.TinyDB;
import com.deltaworks.pracble.db.DTGContract;
import com.deltaworks.pracble.db.Facade;
import com.deltaworks.pracble.event.AlarmReceiverEvent;
import com.deltaworks.pracble.event.DTGCarDataEvent;
import com.deltaworks.pracble.event.Event;
import com.deltaworks.pracble.event.StartConnectEvent;
import com.deltaworks.pracble.event.ToastEvent;
import com.deltaworks.pracble.model.DTGBasicData;
import com.deltaworks.pracble.model.DTGInfo;
import com.deltaworks.pracble.model.FileInfo;
import com.deltaworks.pracble.model.FileRequestBody;
import com.deltaworks.pracble.model.ResponseInfo;
import com.deltaworks.pracble.retrofit.RetrofitLib;
import com.facebook.stetho.Stetho;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric.sdk.android.Fabric;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainService extends Service {

    public String UUID_SERVICE = "0003cdd0-0000-1000-8000-00805f9b0131";

    public String UUID_CHAR_READ = "0003cdd1-0000-1000-8000-00805f9b0131";
    public String UUID_CHAR_WRITE = "0003cdd2-0000-1000-8000-00805f9b0131";

//    public String UUID_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";
//    public String UUID_DESCRIPTOR_WRITE = "00002902-0000-1000-8000-00805f9b34fb";
//    public String UUID_DESCRIPTOR_READ = "00002902-0000-1000-8000-00805f9b34fb";

    private static int TIME_OUT_SCAN = 5000;

    public static final String DEVICE_NAME = "TS2_BLE";

    public BleDevice mBleDevice; //디바이스 주소
    public boolean isBleDevice = false;
    public boolean isDiscoverBle = false;


    public final String TAG = MainService.class.getSimpleName();

    public final static String ACTION_START_CONNECT = "action_start_connect";
    public final String ACTION_CLOSE_SERVICE = "action_close_service";
    public final String ACTION_CLICK_NOTIBAR = "action_click_notibar";

    private NotificationCompat.Builder mBLEStateNoti;
    public static final String KEY_SETTING_DATA = "setting_data";
    private TinyDB mTinySharedPreference;

    /**
     * 고유번호
     */
    private boolean isDTGSerialNumber;
    private String mDTGSerialNumber;

    public static final String DTG_BASIC_DATA = "dtg_basic_data";  //dtg 시리얼 번호
    // TODO: 2018-04-17 시리얼부분 가져와서 한번만 저장하게 셋팅
    private String mTokenNumber = "true";

    /**
     * 데이터 보내기 관련 알람 변수
     */
    public long UPLOAD_TIME = 1000 * 60;  //1분  >> 앱 처음 로드시 인터넷 연결 안돼있을때 초기값
    private long SEND_LOCATION_DATA_TIME = 1000;  // gps 데이터 보내는 주기 변수 30초
    public static final String ACTION_ALARM_DTG_DATA = "action_alarm_dtg_data";
    public static final String ACTION_ALARM_DTG_LOCATION = "action_alarm_dtg_location";
    private boolean isStartedDataAlarm = false;  // 데이터 보내는 주기 알람 시작 변수
    private boolean isStartedLocationAlarm = false;  // 데이터 보내는 주기 알람 시작 변수

    private boolean isSentDTGData = true;  //인터넷 상태 변경 리시버에서 데이터 못보냈을때 타는 루트 조건을 false로 했기때문에 기본값 true
    private boolean isSentLocationData = true;
    private int mIdOfLastData;

    private int limitTheNumberOfFiles = 4;

    /**
     * 핸드폰 부팅시 리시버
     */
//    public static final String ACTION_ALARM_BOOT_HANDPHONE = "android.intent.action.BOOT_COMPLETED";
    /**
     * 서비스 죽을때 리시버
     */
    public static final String ACTION_ALARM_WAKE_UP_SERVICE = "action_alarm_wake_up_service";


    /**
     * 블루투스 연결시 데이터 성공적으로 오는지 확인하는 알람 변수
     */
//    public static final String ACTION_ALARM_CHECK_FOR_CHANGED_DATA = "action_alarm_check_for_changed_data";
    private long CHECK_FOR_CHANGED_DATA_TIME = 3000;  // notify된 데이터가 들어오는지 확인 주기

    private boolean isCheckForChangedData = false;
    private boolean isStartedCheckForChangedDataAlarm = false;  // 데이터 보내는 주기 알람 시작 변수

    /**
     * 총 거리 변수 (합에 따라 gps 데이터 보내는 주기 달라짐)
     */
    private double mTotalDistance = 0;


    /**
     * 블루투스 켜기 변수
     */
    private boolean isEnableBle;

    /**
     * dtg gps 데이터 값 - 거리 측정해서 위치 데이터 보내는 주기 설정을 위함
     */
    private Location mCurrentLocation;
    private Location mPreviousLocation;


    /**
     * 파싱 변수
     */

    private String mCarLatText;
    private String mCarLonText;

    public static boolean Cycle_rcvStart = false;
    public static boolean Info_rcv = false;

    public static boolean Log_rcvStart = false;
    public static boolean Keyevent_rcvStart = false;

    public static int CycleData_Index = 0;
    public int Rcv_Cnt = 0;

    public static byte[] CycleData = new byte[164];    // Ts2에서 오는 정주기
    private Facade mFacade;

    private File mFolderPath = new File(Environment.getExternalStorageDirectory(), "DTG");
    private RetrofitLib mRetrofit;
    private CommonCollection mCommon;
    private FileLib mFileLib;

    private BleDevice mCurrentBleDevice = null;  //현재 연결된 블루투스값

    /**
     * socket io
     */
//    private Socket mSocket;
//
//
//    {
//        try {
//            mSocket = IO.socket("http://서버ip:포트번호");
//        } catch (URISyntaxException e) {
//        }
//    }
    public MainService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        registerReceiver(mInternetReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));  //인터넷 연결 상태 브로드캐스트 리시버 등록

        Stetho.initializeWithDefaults(this);
        EventBus.getDefault().register(this);
        Fabric.with(this, new Crashlytics());

        /////////////////////// 서버에서 변경되는 설정값 가져오는 곳 //////////////////////

//        mSocket.on("받을 이벤트명", onNewMessage);  //socket io 리스너 설정
//        mSocket.connect();  //socket io 연결
        ////////////////////////////////////////////////////////////////////////////////

        mTinySharedPreference = new TinyDB(getApplicationContext());
        DTGBasicData mDTGBasicData = mTinySharedPreference.getObject(DTG_BASIC_DATA, DTGBasicData.class);

        if (mDTGBasicData != null) {  // 디티지 기본값이 있을때

            mDTGSerialNumber = mDTGBasicData.getDtgSerialNumber();
            BluetoothDevice sharedBleDevice = mDTGBasicData.getBleDevice();

            if (mDTGSerialNumber != null) {  //시리얼번호가 있을때

                isDTGSerialNumber = true;
                Crashlytics.setUserIdentifier(mDTGSerialNumber);  //어떤 사용자에게 특정 비정상 종료가 발생했는지
            } else {
                isDTGSerialNumber = false;
            }

            if (sharedBleDevice != null) {  // BLE 정보가 있을때
                BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(sharedBleDevice.getAddress());
                BleDevice bleDevice1 = new BleDevice(bluetoothDevice);
                mBleDevice = bleDevice1;
                isBleDevice = true;

            } else {
                isBleDevice = false;
            }

        } else {
//            Log.d(TAG, "mDTGBasicData: 값 없음");
        }

        //////////////////블루투스 설정//////////////////
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setOperateTimeout(5000);
        setScanRule(isBleDevice);

        /////////////////////////////////////////////////


        mRetrofit = new RetrofitLib();
        mCommon = new CommonCollection(this);
        mFileLib = new FileLib(this, mFolderPath);

        mFacade = new Facade(this);

        if (mFacade.hasDTGTable()) {
            //테이블 있음
            Cursor dtgData = mFacade.queryDTGAllData();
            if (dtgData != null && dtgData.getCount() > 0) {  //값 있을때
                if (dbDataToFile()) {  //압축 파일 만들고 기존 파일 삭제 성공일때
//                    setUploadFileToServer();  //파일 업로드
                    int networkState = mCommon.checkNetwork();
                    if (networkState == 1 || networkState == 2) {  //2은 mobile 연결됐을 때
                        uploadFileToServer(mFolderPath.getAbsolutePath());
                    }
                    mFacade.dropTable();
                    mFacade.createTable();
                }
            } else {
                Log.d(TAG, "onCreate: dtgData의 값없음");
            }
        } else {
            //테이블 없음
            mFacade.createTable();
        }

//        int networkState = mCommon.checkNetwork();  //현재 네트워크 상태 체크
//
//        if (networkState == 1 || networkState == 2) {  //인터넷 연결 됨
//            //서버에서 로드 시간 가져오기
//            //가져온 시간 저장하기
//            getUploadTimeToServer();
//        } else {  //인터넷 연결 안됨
//            //원래 있던 시간 값 가져와서 upload_time 값 셋팅
//            SettingInfo settingInfo = mTinySharedPreference.getObject(KEY_SETTING_DATA, SettingInfo.class);
//            if (settingInfo != null) {
//                Log.d(TAG, "onCreate: " + settingInfo.getAlarmTime());
//                UPLOAD_TIME = settingInfo.getAlarmTime();
//            } else { //앱 처음 켰을때
//                mTinySharedPreference.putObject(KEY_SETTING_DATA, new SettingInfo(UPLOAD_TIME));
//            }
//        }
    }

//    private Emitter.Listener onNewMessage = new Emitter.Listener() {
//        @Override
//        public void call(final Object... args) {
//            JSONObject data = (JSONObject) args[0];
//            String uploadTime;
//            try {
//                uploadTime = data.getString("alarm_time");
//            } catch (JSONException e) {
//                return;
//            }
//
//            if (uploadTime != null) {
//                long lUploadTime = Long.parseLong(uploadTime);
//                mTinySharedPreference.putObject(KEY_SETTING_DATA, new SettingInfo(lUploadTime));
//                UPLOAD_TIME = lUploadTime;
//            }
//        }
//    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {

                    case ACTION_START_CONNECT: //서비스 시작

                        Log.d(TAG, "onCreate: getAdapter");
                        createNoti(true);

                        if (isBleDevice) {


                            tryConnect();
                        } else {
                            tryStartScan();
                        }
                        break;
                    case ACTION_CLICK_NOTIBAR: //노티 클릭

                        if (!MainActivity.sVisibleActivity) {
                            //메인 액티비티가 보이지 않을때만 화면 새로 띄우기
                            Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent1);
//                            getApplicationContext().startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                }
            }
        }
        return START_STICKY;
    }

    /**
     * 블루투스 안 켜져있으면 켜고 스캔 또는 블루투스 연결하기
     *
     * @param isStartScan
     */
    public void checkEnableBluetooth(final boolean isStartScan) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isEnableBle) {  //블루투스가 켜지지 않았을때
                    try {
                        Log.d(TAG, "run: dd");
                        Thread.sleep(1000);
                        if (BleManager.getInstance().isBlueEnable()) {  //bluetooth가 켜졌을때
                            isEnableBle = true;
                            if (isStartScan) {
                                startScan();
                            } else {
                                EventBus.getDefault().post(new StartConnectEvent());
                            }
                        }
                    } catch (InterruptedException e) {

                    }
                }
            }
        });
        thread.start();
    }


    public void sendNotiAction(final String action) {

        Intent intent = new Intent(this, MainService.class);
        intent.setAction(action);
        startService(intent);
    }

    public void createNoti(boolean firstNoti) {

        if (firstNoti) {
            mBLEStateNoti = new NotificationCompat.Builder(this, "0")
                    .setContentTitle("DTG")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText("오늘도 좋은 하루 되세요~~!")  //연결 상태에 따라 text 다르게 설정
                    .setContentIntent(clickNotiPendingIntent());  //노티 클릭설정

//                    .addAction(R.drawable.ic_launcher_background, "CLOSE", closeNotiPendingIntent());

            startForeground(1, mBLEStateNoti.build());


        }
//        } else {
//            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
////            mBLEStateNoti.setContentText(text);
//            mNotificationManager.notify(1, mBLEStateNoti.build());
//        }

    }

//    private PendingIntent closeNotiPendingIntent() {
//        Intent closeNotiIntent = new Intent(this, this.getClass());
//        closeNotiIntent.setAction(ACTION_CLOSE_SERVICE);
//        PendingIntent pending = PendingIntent.getService(this, 0, closeNotiIntent, PendingIntent.FLAG_IMMUTABLE);
//        return pending;
//    }

    private PendingIntent clickNotiPendingIntent() {

        Intent clickNotiIntent = new Intent(this, this.getClass());
        clickNotiIntent.setAction(ACTION_CLICK_NOTIBAR);
        PendingIntent pending = PendingIntent.getService(this, 1, clickNotiIntent, PendingIntent.FLAG_IMMUTABLE);
        return pending;
    }

    /**
     * 스캔 룰 셋팅
     */
    private void setScanRule(boolean isBleDevice) {
        BleScanRuleConfig scanRuleConfig = null;

        if (isBleDevice) {

            scanRuleConfig = new BleScanRuleConfig.Builder()
//                .setServiceUuids(UUID_SERVICE)      // 只扫描指定的服务的设备，可选
                    .setDeviceName(true, DEVICE_NAME)   // 只扫描指定广播名的设备，可选
                    .setDeviceMac(mBleDevice.getMac())  // 只扫描指定mac的设备，可选  //BLE주소값 있으면 설정
                    .setAutoConnect(true)      // 连接时的autoConnect参数，可选，默认false
                    .setScanTimeOut(TIME_OUT_SCAN)              // 扫描超时时间，可选，默认10秒
                    .build();
        } else {
            Log.d(TAG, "dtgBleMac: setScanRule");
            scanRuleConfig = new BleScanRuleConfig.Builder()
                    .setDeviceName(true, DEVICE_NAME)   // 只扫描指定广播名的设备，可选
                    .setAutoConnect(true)      // 连接时的autoConnect参数，可选，默认false
                    .setScanTimeOut(TIME_OUT_SCAN)              // 扫描超时时间，可选，默认10秒
                    .build();
        }

        if (scanRuleConfig != null) {
            BleManager.getInstance().initScanRule(scanRuleConfig);
        }
    }

    private void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                Log.d(TAG, "onScanStarted: ");
//                createNoti(true, "블루투스 기기와 연결 시도중");
//                EventBus.getDefault().post(new BleStateEvent("블루투스 기기와 연결 시도중"));
            }

            @Override
            public void onLeScan(final BleDevice bleDevice) {
                Log.d(TAG, "onLeScan: ");

                if (bleDevice.getName() != null) {
                    if (bleDevice.getName().equals(DEVICE_NAME)) {
                        if (!isDiscoverBle) {  //BLE가 발견됐으면 TRUE로 바꿔서 스캔 종료 메소드 콜백 됐을때 또 스캔 되지 않게 하기
                            isDiscoverBle = true;
                            BleManager.getInstance().cancelScan();

                            if (!isBleDevice) { //bledevice 정보 없으면 저장
                                Log.d(TAG, "mDTGBasicData: isBleDevice값들어감");
                                mTinySharedPreference.putObject(DTG_BASIC_DATA, new DTGBasicData(bleDevice.getDevice(), mDTGSerialNumber));
                                mBleDevice = bleDevice;

                                Log.d(TAG, "onLeScan: " + mBleDevice.getName());

                            }

                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    connect(bleDevice);
                                    Log.d(TAG, "run: " + bleDevice.getMac());
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {

                if (!isDiscoverBle) {
                    try {
                        Thread.sleep(2000);
                        EventBus.getDefault().post(new ToastEvent("근처에 기기가 검색이 되지 않습니다."));

                    } catch (InterruptedException e) {
                    }
                    startScan();

                }
            }
        });
    }

    /**
     * 스캔 시작
     */
    private void tryStartScan() {
        if (!BleManager.getInstance().isBlueEnable()) {  //블루투스 안 켜짐
            isEnableBle = false;
//            createNoti(true, "블루투스 켜는 중");
            BleManager.getInstance().enableBluetooth();
            checkEnableBluetooth(true);
        } else {
            startScan();
        }
    }

    /**
     * 연결 시작
     */
    private void tryConnect() {

        if (!BleManager.getInstance().isBlueEnable()) {  //블루투스 안 켜짐
            isEnableBle = false;
//                            createNoti(true, "블루투스 켜는 중");
            BleManager.getInstance().enableBluetooth();
            checkEnableBluetooth(false);
        } else {
            EventBus.getDefault().post(new StartConnectEvent());
        }
    }

    private void connect(final BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                Log.d(TAG, "onStartConnect: ");
            }

            @Override
            public void onConnectFail(BleException exception) {
                Log.d(TAG, "onConnectFail: " + exception);
//                createNoti(false, "블루투스 기기와 연결 실패");
//                EventBus.getDefault().post(new BleStateEvent("블루투스 기기와 연결 실패" + exception));
//                if (mCurrentBleDevice != null) {
//                    mCurrentBleDevice = null;
//                }
//                BleManager.getInstance().cancelScan();
//                try {
//                    Thread.sleep(1000);
//                    isDiscoverBle = false;
//                    startScan();
//                } catch (InterruptedException e) {
//                }
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.d(TAG, "onConnectSuccess: ");
//                createNoti(false, "블루투스 기기와 연결 성공");
//                EventBus.getDefault().post(new BleStateEvent("블루투스 기기와 연결 성공"));
                mCurrentBleDevice = bleDevice;
                BleManager.getInstance().cancelScan();

                startNotify(bleDevice);

            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                Log.d(TAG, "onDisConnected: ");

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                tryConnect();
            }
        });
    }


    private void startNotify(final BleDevice bleDevice) {
        BleManager.getInstance().notify(bleDevice, UUID_SERVICE, UUID_CHAR_READ, new BleNotifyCallback() {
            @Override
            public void onNotifySuccess() {
                BleManager.getInstance().cancelScan();
//                createNoti(false, "블루투스 기기 데이터 통신 중");
//                EventBus.getDefault().post(new BleStateEvent("블루투스 기기 데이터 통신 중"));
                Log.d(TAG, "onNotifySuccess: ");

//                if(!isStartedCheckForChangedDataAlarm) {
//                    setCheckForChangeDataAlarm(true);
//                    isStartedCheckForChangedDataAlarm = true;
//                }
            }

            @Override
            public void onNotifyFailure(BleException exception) {
                Log.d(TAG, "onNotifyFailure: " + exception);
//                createNoti(false, "블루투스 기기 데이터 통신 실패");
//                BleManager.getInstance().disconnect(mCurrentBleDevice);

//                EventBus.getDefault().post(new BleStateEvent("블루투스 기기 데이터 통신 실패" + exception));
            }

            @Override
            public void onCharacteristicChanged(byte[] data) {
//                isCheckForChangedData = true;
                DTGInfo dtgInfo = dataParsing(data);
                if (dtgInfo != null) {

                    EventBus.getDefault().post(new DTGCarDataEvent(dtgInfo));  //보이는 화면으로 이벤트 보내기

//                    Log.d(TAG, "onCharacteristicChanged: " + dtgInfo.toString());
                    if (!dtgInfo.getCarSpeed().equals("000")) {  //속도 000이 아닐때만 저장
                        insertData(dtgInfo); //데이터 데이터베이스에 붙이기
                    }


                    /////////////////////////////알람매니저 시작////////////////////////////
                    if (!isStartedDataAlarm) {
                        Log.d(TAG, "알람 시작 Data++");
                        isStartedDataAlarm = true; //알람 시작했으니 알람 끝날때까지는 등록 못함
                        setDataAlarm(true);  //차 데이터 알람 시작
                    }
//                    if (!isStartedLocationAlarm) {
//                        Log.d(TAG, "알람 시작 Location++");
//                        isStartedLocationAlarm = true;
//                        setLocationAlarm(true);  //차 위치 알람 시작
//                    }

                    ////////////////////////////알람매니저 끝////////////////////////////////
                }
            }
        });
    }

    private void startCharWrite(byte[] data) {

        try {
            BleManager.getInstance().write(
                    mCurrentBleDevice,
                    UUID_SERVICE,
                    UUID_CHAR_WRITE,
                    data,
                    new BleWriteCallback() {
                        @Override
                        public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                            Log.d(TAG, "onWriteSuccess: ");
                        }

                        @Override
                        public void onWriteFailure(BleException exception) {

                            Log.d(TAG, "onWriteFailure: " + exception);
                        }
                    });
        } catch (NullPointerException e) {
//            Log.d(TAG, "startCharWrite: 값이 없음");
        }

    }

    /**
     * 연결 상태 리시버
     */
    private final BroadcastReceiver mInternetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {  //인터넷 연결 됐을때 오는 콜백 , 인터넷 연결 됐을때 쌓인 데이터 보내기

                //파일 못보냈으면 인터넷 연결 안된거니 인터넷 연결 됐을때 알람 다시 설정할수 있게
                if (!isSentDTGData) {  //DTG Data를 못 보냈을때
                    Log.d(TAG, "onReceive: 데이터 못보냈을때 보내기 시도 dtg");

                    setUploadFileToServer();  //파일 업로드

                }
                if (!isSentLocationData) {  //DTG Location data를 못 보냈을때
                    Log.d(TAG, "onReceive: 데이터 못보냈을때 보내기 시도 location");

                    setSendLocationToServer();
                }
            }
        }
    };


    /**
     * DB 데이터를 파일로 변환하기
     * 압축파일만드는거 까지
     * 압축파일 제대로 만들어졌으면 true
     */

    public boolean dbDataToFile() {
//        Log.d(TAG, "dbDataToFile: 시작");
        ArrayList<DTGInfo> mDTGInfoList = new ArrayList<>();
        Cursor dtgAllDataCursor = mFacade.queryDTGAllData();  //데이터 전체 값
        if (dtgAllDataCursor.moveToFirst()) {
            for (int i = 0; i < dtgAllDataCursor.getCount(); i++) {

                int id = dtgAllDataCursor.getInt(dtgAllDataCursor.getColumnIndexOrThrow(DTGContract.DTGDataEntry._ID));
                String date = dtgAllDataCursor.getString(dtgAllDataCursor.getColumnIndexOrThrow(DTGContract.DTGDataEntry.COLUMN_NAME_DATE));
                String totalDist = dtgAllDataCursor.getString(dtgAllDataCursor.getColumnIndexOrThrow(DTGContract.DTGDataEntry.COLUMN_NAME_CAR_TOTAL_DIST));
                String dailyDist = dtgAllDataCursor.getString(dtgAllDataCursor.getColumnIndexOrThrow(DTGContract.DTGDataEntry.COLUMN_NAME_CAR_DAILY_DIST));
                String carSpeed = dtgAllDataCursor.getString(dtgAllDataCursor.getColumnIndexOrThrow(DTGContract.DTGDataEntry.COLUMN_NAME_CAR_SPEED));
                String engineRpm = dtgAllDataCursor.getString(dtgAllDataCursor.getColumnIndexOrThrow(DTGContract.DTGDataEntry.COLUMN_NAME_ENGINE_RPM));
                String carBreak = dtgAllDataCursor.getString(dtgAllDataCursor.getColumnIndexOrThrow(DTGContract.DTGDataEntry.COLUMN_NAME_CAR_BREAK));
                String carLat = dtgAllDataCursor.getString(dtgAllDataCursor.getColumnIndexOrThrow(DTGContract.DTGDataEntry.COLUMN_NAME_CAR_LAT));
                String carLon = dtgAllDataCursor.getString(dtgAllDataCursor.getColumnIndexOrThrow(DTGContract.DTGDataEntry.COLUMN_NAME_CAR_LON));
                String carAzimuth = dtgAllDataCursor.getString(dtgAllDataCursor.getColumnIndexOrThrow(DTGContract.DTGDataEntry.COLUMN_NAME_CAR_AZIMUTH));
                String carSleep = dtgAllDataCursor.getString(dtgAllDataCursor.getColumnIndexOrThrow(DTGContract.DTGDataEntry.COLUMN_NAME_CAR_SLEEP));
                String dtgDeviceState = dtgAllDataCursor.getString(dtgAllDataCursor.getColumnIndexOrThrow(DTGContract.DTGDataEntry.COLUMN_NAME_DTG_DEVICE_STATE));
                String carBoot = dtgAllDataCursor.getString(dtgAllDataCursor.getColumnIndexOrThrow(DTGContract.DTGDataEntry.COLUMN_NAME_CAR_BOOT));


                mDTGInfoList.add(new DTGInfo(date, totalDist, dailyDist, carSpeed, engineRpm, carBreak, carLat, carLon, carAzimuth, carSleep, dtgDeviceState, carBoot));
                if (!dtgAllDataCursor.moveToNext()) {
                    mIdOfLastData = id;
//                    Log.d(TAG, "dbDataToFile: " + mIdOfLastData);
                    break;
                }
            }
        }
        Log.d(TAG, "파일 만든 마지막 id: " + mIdOfLastData);
//        Log.d(TAG, "dbDataToFile: mDTGInfoList.size() " + mDTGInfoList.size());
        dtgAllDataCursor.close();
        Gson gson = new Gson();
        String jsonPlace = gson.toJson(mDTGInfoList);

//        Log.d(TAG, "dbDataToFile: " + jsonPlace);
        String dateFormat = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
        String textFileName = mDTGSerialNumber + "_" + dateFormat;

        mFileLib.createFile(textFileName, jsonPlace);  //파일 만들기
        if (mFileLib.makeZipFile(textFileName)) {  //압축 파일이 제대로 만들어졌으면 true 리턴
            mFileLib.deleteFile(textFileName + ".txt");  //text파일 삭제하기
            mFacade.deleteData(String.valueOf(mIdOfLastData));
            return true;
        } else {
            return false;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");

        isDiscoverBle = true;

        //계속 스캔중이면 스캔 끝내야하니까
        if (isStartedDataAlarm) {  //알람 등록 됐으면 true
            setDataAlarm(false);  //알람 해제
        }
        if (isStartedLocationAlarm) {
            setLocationAlarm(false);  //알람 해제
        }


        if (mCurrentBleDevice != null) {
            mCurrentBleDevice = null;
            BleManager.getInstance().stopNotify(mCurrentBleDevice, UUID_SERVICE, UUID_CHAR_WRITE);
        }

        if (BleManager.getInstance().isConnected(mCurrentBleDevice)) {
            BleManager.getInstance().disconnect(mCurrentBleDevice);
        }
        BleManager.getInstance().destroy();
        unregisterReceiver(mInternetReceiver);
//        mSocket.disconnect();  //socket io 연결 해제
//        mSocket.off("받을 이벤트명", onNewMessage);  //socket io 리스너 없애기
        EventBus.getDefault().unregister(this);
        stopForeground(true);  //노티피케이션 지우기

        //연결 해제한뒤 다시 서비스 시작

//        Intent intent1 = new Intent(getApplicationContext(), MainService.class);
//        intent1.setAction(MainService.ACTION_START_CONNECT);
//        getApplicationContext().startService(intent1);

        setWakeUpAlarm(true);


    }


    public void insertData(DTGInfo dtgInfo) {
        ContentValues values = new ContentValues();

        values.put(DTGContract.DTGDataEntry.COLUMN_NAME_DATE, dtgInfo.getDate());
        values.put(DTGContract.DTGDataEntry.COLUMN_NAME_CAR_TOTAL_DIST, dtgInfo.getCarTotalDist());
        values.put(DTGContract.DTGDataEntry.COLUMN_NAME_CAR_DAILY_DIST, dtgInfo.getCarDailyDist());
        values.put(DTGContract.DTGDataEntry.COLUMN_NAME_CAR_SPEED, dtgInfo.getCarSpeed());
        values.put(DTGContract.DTGDataEntry.COLUMN_NAME_ENGINE_RPM, dtgInfo.getEngineRpm());
        values.put(DTGContract.DTGDataEntry.COLUMN_NAME_CAR_BREAK, dtgInfo.getCarBreak());
        values.put(DTGContract.DTGDataEntry.COLUMN_NAME_CAR_LON, dtgInfo.getCarLon());
        values.put(DTGContract.DTGDataEntry.COLUMN_NAME_CAR_LAT, dtgInfo.getCarLat());
        values.put(DTGContract.DTGDataEntry.COLUMN_NAME_CAR_AZIMUTH, dtgInfo.getCarAzimuth());
        values.put(DTGContract.DTGDataEntry.COLUMN_NAME_CAR_SLEEP, dtgInfo.getCarSleep());
        values.put(DTGContract.DTGDataEntry.COLUMN_NAME_DTG_DEVICE_STATE, dtgInfo.getDtgDeviceState());
        values.put(DTGContract.DTGDataEntry.COLUMN_NAME_CAR_BOOT, dtgInfo.getCarBoot());
        // TODO: 2018-04-19 새로 추가된 항목들 insertdata에 추가

        mFacade.insertDTGData(values);  //파사드에서 데이터 인서트 처리
    }

    public void setDataAlarm(boolean start) {
        Intent alarmIntent = new Intent(ACTION_ALARM_DTG_DATA);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        long delay = mSettingInfo.getAlarmTime();  //설정안 변수로 가져오기
        long delay = UPLOAD_TIME;  //설정안 변수로 가져오기

        if (start) {  //true면 알람 시작
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
            }
        } else {  //false면 알람 취소
            alarmManager.cancel(pendingIntent);
        }
    }

    public void setLocationAlarm(boolean start) {
        Intent alarmIntent = new Intent(ACTION_ALARM_DTG_LOCATION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 2, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long delay = SEND_LOCATION_DATA_TIME;  //설정안 변수로 가져오기

        if (start) {  //true면 알람 시작
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
            }
        } else {  //false면 알람 취소
            alarmManager.cancel(pendingIntent);
        }
    }

    public void setWakeUpAlarm(boolean start) {
        Intent alarmIntent = new Intent(ACTION_ALARM_WAKE_UP_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 2, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long delay = 5000;  //설정안 변수로 가져오기

        if (start) {  //true면 알람 시작
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
            }
        } else {  //false면 알람 취소
            alarmManager.cancel(pendingIntent);
        }
    }

//    public void setCheckForChangeDataAlarm(boolean start) {
//        Intent alarmIntent = new Intent(ACTION_ALARM_CHECK_FOR_CHANGED_DATA);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        long delay = CHECK_FOR_CHANGED_DATA_TIME; //5초
//
//        if (start) {  //true면 알람 시작
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
//            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
//            } else {
//                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
//            }
//        } else {  //false면 알람 취소
//            alarmManager.cancel(pendingIntent);
//        }
//    }


    @Subscribe
    public void onEvent(Event event) {
        if (event instanceof AlarmReceiverEvent) {//알람 이벤트
            AlarmReceiverEvent alarmReceiverEvent = (AlarmReceiverEvent) event;
            switch (alarmReceiverEvent.getActionType()) {
                case ACTION_ALARM_DTG_LOCATION:
                    Log.d(TAG, "onEvent: LOCATION 알람 옴");
                    //이건 주기마다 파일을 만들 필요가 없기때문에 데이터가 보내지지 않으면 알람 이벤트가 와도 알람을 재등록 하지 않게 한다
//                    isStartedLocationAlarm = false;  //알람 다시 등록 될 수 있게 false로 바꿔줌
                    setSendLocationToServer();

                    break;
                case ACTION_ALARM_DTG_DATA:
                    Log.d(TAG, "onEvent: DATA 알람 옴");

                    setUploadFileToServer();  //파일 업로드
                    break;

//                case ACTION_ALARM_CHECK_FOR_CHANGED_DATA:
//                    Log.d(TAG, "onEvent: 데이터 잘 들어오나 확인 알람");
//                    if (!isCheckForChangedData) {// false면 데이터 안들어옴
//                        isStartedCheckForChangedDataAlarm = false;
//                        if (BleManager.getInstance().isConnected(mCurrentBleDevice)) {
//                            BleManager.getInstance().disconnect(mCurrentBleDevice);
//                        }
//                    }
            }
        } else if (event instanceof StartConnectEvent) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    connect(mBleDevice);
                }
            });
        }
    }

    /**
     * wifi 연결 확인하고 압축 파일 만들고 업로드 작업
     * wifi 연결 안됐을 때는 isSentDTGData = false
     */
    public void setUploadFileToServer() {
        int networkState = mCommon.checkNetwork();
        if (networkState == 1 || networkState == 2) {  //2은 mobile 연결됐을 때
//        if (mCommon.checkNetwork() == 1) {  //1은 wifi 연결됐을 때
            //압축파일 만들어 보내기
            Cursor dtgData = mFacade.queryDTGAllData();
            if (dtgData != null && dtgData.getCount() > 0) { //데이터에 값 있을때
                if (dbDataToFile()) {
//                    Log.d(TAG, "setUploadFileToServer: 파일보내기");
                    uploadFileToServer(mFolderPath.getAbsolutePath());
                }
            }
            isStartedDataAlarm = false; //인터넷 연결 됬으면 알람 등록 파일이 안간게 인터넷 연결때문이 아닐수도 있으므로

            Log.d(TAG, "setUploadFileToServer: ok");
        } else {
            //압축파일 못 보냄
            Log.d(TAG, "setUploadFileToServer: no");
            isSentDTGData = false;
        }
    }

    /**
     * mobile 연결 확인하고 파일 업로드 작업
     * mobile 연결 안됐을 때는 isSentLocationData = false
     */

    public void setSendLocationToServer() {
        int networkState = mCommon.checkNetwork();
        if (networkState == 1 || networkState == 2) {  //2은 mobile 연결됐을 때
            //GPS 데이터 보내기
            isStartedLocationAlarm = false;  //통신 성공시 알람 재등록 가능
            sendLocationToServer();
            Log.d(TAG, "setSendLocationToServer: ok");
        } else {
            //GPS 데이터 보내기
            Log.d(TAG, "setSendLocationToServer: no");
            isSentLocationData = false;
        }
    }

    /**
     * dtg data file 올리기
     * 업로드 성공시 isSentDTGData = true
     * 업로드 실패시 isSentDTGData = false
     */
    public void uploadFileToServer(String folderPath) {

        final ArrayList<FileRequestBody> mFilePartArrayList = new ArrayList<>(); //서버로 보낼 파일 리스트
        final File files = new File(folderPath);

        files.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (mFilePartArrayList.size() < limitTheNumberOfFiles) { //fileCount 보다 적으면 계속 루트 타고
                    if (name.contains(mDTGSerialNumber)) {  //이름에 mDTGSerialNumber 들어가는 파일

                        File file = new File(dir + "/" + name);
//                        Log.d(TAG, "accept: " + file);
                        RequestBody requestFile = RequestBody.create(MediaType.parse("/*"), file);

                        mFilePartArrayList.add(new FileRequestBody(requestFile, file.getName()));
                    }
                }
                return true;
            }
        });
//        for (int i = 0; i < partArrayList.size(); i++) {
//            Log.d(TAG, "uploadFileToServer: " + partArrayList.get(i).getFileName());
//        }
//        Log.d(TAG, "uploadFileToServer: " + partArrayList.size());
        if (mFilePartArrayList.size() != 0) {  //파일이 있으면
//            Log.d(TAG, "uploadFileToServer: 파일 있음 0아님");
//            Log.d(TAG, "setRetrofit: " + mFilePartArrayList.size());

            RequestBody token = RequestBody.create(MediaType.parse("text/plain"), mTokenNumber);
            RequestBody dtgNum = RequestBody.create(MediaType.parse("text/plain"), mDTGSerialNumber);

            Map<String, RequestBody> requestBodyMap = new HashMap<>();
            requestBodyMap.put("token", token);
            requestBodyMap.put("dtgNum", dtgNum);


            for (int i = 0; i < mFilePartArrayList.size(); i++) {
                String fileName = "dtg_file[]\"; filename=\"" + mFilePartArrayList.get(i).getFileName();
                requestBodyMap.put(fileName, mFilePartArrayList.get(i).getRequestBody());
            }


            Call<FileInfo> fileInfoCall = mRetrofit.getRetrofit().uploadDTGFile(requestBodyMap);

            fileInfoCall.enqueue(new Callback<FileInfo>() {

                @Override
                public void onResponse(Call<FileInfo> call, Response<FileInfo> response) {


                    if (response.isSuccessful()) {
//                        Log.d(TAG, "파일 올리기 성공");
//                        Log.d(TAG, "json 값: " + response.body());
                        System.out.println(response.body());

                        FileInfo fileInfo = response.body();

                        ArrayList<String> files = fileInfo.fileName;
                        for (int i = 0; i < files.size(); i++) {
//                            Log.d(TAG, "onResponse: " + files.get(i));
                            if (mFileLib.deleteFile(files.get(i))) {
//                                Log.d(TAG, "onResponse: " + files.get(i) + "삭제됨");
                            }
                        }


                        if (mFilePartArrayList.size() >= limitTheNumberOfFiles) { //5랑 같거나 많으면 계속 루트 타고
                            //파일 있음
                            uploadFileToServer(mFolderPath.getAbsolutePath());
                        } else {
                            //현재 파일은 더이상 없으니 끝낸다.
                            isSentDTGData = true;  //업로드 성공시 true 바뀐다
                        }

                    }
                }

                @Override
                public void onFailure(Call<FileInfo> call, Throwable t) {
//                    Log.d(TAG, "파일 올리기 실패: " + t.toString());
//                    Log.d(TAG, call.toString());
                    isSentDTGData = true;
                    Crashlytics.log("파일 올리기 실패" + t.toString());
                }

            });
        } else {
//            Log.d(TAG, "uploadFileToServer: 파일 없음");
            isSentDTGData = true;  //업로드 성공시 true 바뀐다
        }
    }

    /**
     * dtg location data 올리기
     * 업로드 성공시 isSentLocationData = true
     * 업로드 실패시 isSentLocationData = false
     */

    public void sendLocationToServer() {


        Call<ResponseInfo> fileInfoCall = mRetrofit.getRetrofit().sendDTGLocation(mTokenNumber, mDTGSerialNumber, mCarLatText, mCarLonText);

        fileInfoCall.enqueue(new Callback<ResponseInfo>() {
            @Override
            public void onResponse(Call<ResponseInfo> call, Response<ResponseInfo> response) {
                if (response.isSuccessful()) {

//                    Log.d(TAG, "sendLocationToServer: gps 데이터 전송 성공");
//                    Log.d(TAG, "json 값: " + response.body().toString());

                    isSentLocationData = true;  //업로드 성공시 true 바뀐다
                }
            }

            @Override
            public void onFailure(Call<ResponseInfo> call, Throwable t) {
                Log.d(TAG, "실패" + t.toString());
                Crashlytics.log("파일 올리기 실패" + t.toString());
                isSentLocationData = true;

            }

        });

    }

    /**
     * 서버에서 설정한 파일 업로드 시간 가져오기
     */
    public void getUploadTimeToServer() {

//        Call<SettingInfo> uploadTimeCall = mRetrofit.getRetrofit().getUploadTime();
//
//        uploadTimeCall.enqueue(new Callback<SettingInfo>() {
//            @Override
//            public void onResponse(Call<SettingInfo> call, Response<SettingInfo> response) {
//                if (response.isSuccessful()) {
//
//                    Log.d(TAG, "성공");
//                    Log.d(TAG, "json 값: " + response.body().toString());
//
//                    // TODO: 2018-04-04 성공시 쉐어드에 넣고 UPLOAD_TIME 변수에 값 넣기
////                    mTinySharedPreference.putObject(KEY_SETTING_DATA, new SettingInfo(lUploadTime));
////                    UPLOAD_TIME = response.body().toString();
//
//                }
//            }
//
//            @Override
//            public void onFailure(Call<SettingInfo> call, Throwable t) {
//                Log.d(TAG, "실패" + t.toString());
//                SettingInfo settingInfo = mTinySharedPreference.getObject(KEY_SETTING_DATA, SettingInfo.class);
//                if (settingInfo != null) {
//                    Log.d(TAG, "onFailure  : " + settingInfo.getAlarmTime());
//                    UPLOAD_TIME = settingInfo.getAlarmTime();
//                } else { //앱 처음 켰을때
//                    mTinySharedPreference.putObject(KEY_SETTING_DATA, new SettingInfo(UPLOAD_TIME));
//                }
//            }
//
//        });
    }

    /**
     * 블루투스 데이터 파싱 메소드
     *
     * @param array
     * @return
     */
    public DTGInfo dataParsing(byte[] array) {

        DTGInfo mDTGInfo = null;

        if (((array[0] & 0x00FF) == 0x02) && ((array[1] & 0x00FF) == 0x37) && ((array[2] & 0x00FF) == 0x30)) {
            //둘 중 하나가 2 & 둘 중 하나가 55 & 둘 중하나가 48 << 조건이 다 맞으면 rcvStart = true;
            Cycle_rcvStart = true;
            CycleData_Index = 0;
        } else if ((array[0] & 0x00FF) == 0x02 && (array[1] & 0x00FF) == 0x35 && (array[2] & 0x00FF) == 0x37) {
            //둘 중 하나가 2 & 둘 중 하나가 53 & 둘 중하나가 55 << 조건이 다 맞으면 rcvStart = true;
            Info_rcv = true;
            CycleData_Index = 0;
        }

        if (Cycle_rcvStart == true) {
            for (int i = 0; i < array.length; i++) {
                CycleData[CycleData_Index++] = array[i];

            }

            //배열값을 cycleData에 받은 어레이데이터 넣기
            if (CycleData_Index > 154) { //155인 데이터만 들어감
                Rcv_Cnt++;
                Cycle_rcvStart = false;
                CycleData_Index = 0;
                byte CheckBCC = calcBCC(CycleData, 152);

//                Log.d(TAG, "CheckBCC: "+CheckBCC);
                byte[] arrayBCC = new byte[2];
                //배열 두개 만들어서 checkbbc를 넣고

                arrayBCC[0] = (byte) ((CheckBCC & 0xF0) >> 4);
                arrayBCC[1] = (byte) (CheckBCC & 0x0F);

                String s = arrayBCC.toString();

//                Log.d(TAG, "arrayBCC: "+arrayBCC[0]); //0은 11
//                Log.d(TAG, "arrayBCC: "+arrayBCC[1]); //1은 13, 9
                if (arrayBCC[0] < 10) {
                    arrayBCC[0] += 0x30; //48  //10보다 작은 숫자면 48더하

                } else {
                    arrayBCC[0] += 0x37; //55
                }

//                Log.d(TAG, "arrayBCC: "+arrayBCC[0]);

                if (arrayBCC[1] < 10) {
                    arrayBCC[1] += 0x30;
                } else {
                    arrayBCC[1] += 0x37;
                }

//                Log.d(TAG, "arrayBCC: "+arrayBCC[1]);

                if (arrayBCC[0] == CycleData[152] && arrayBCC[1] == CycleData[153]) {  //0값이 cycleData[152]값이랑 같다 && 1값이 cycleData[153]값이랑 같다 둘다 만족하면 []

//                    Log.d(TAG, "dataParsing: "+CycleData[152] +" , "+ CycleData[153]);
                    StringBuffer sb = new StringBuffer();
                    byte[] BackupFile = new byte[155];  //전체 크기
                    String str = "";

                    for (int i = 0; i < 155; i++) {
                        BackupFile[i] = CycleData[i];
                    }


                    for (byte byteChar : BackupFile) {
                        sb.append(String.format("%02x", byteChar));
//                        Log.d(TAG, "dataParsing: "+byteChar);


                    }
                    //모든 데이터
//                    mDatevalue.post(m_DateTextWriteRun);
                    /////////////////////////////////////////
                    //차량 속도
                    int[] speed = new int[3];
                    speed[0] = CycleData[54];
                    speed[1] = CycleData[55];
                    speed[2] = CycleData[56];

                    str = "";
                    for (int i : speed) {
                        str += Character.toString((char) i);

                    }

                    String carSpeedText = str;
                    //차량 속도

//                    mCommon.MeasureDistance(carSpeedText);
//                    double currentDistance = mCommon.MeasureDistance(carSpeedText);
////                    Log.d(TAG, "dataParsing: " + currentDistance);

//                    mTotalDistance += currentDistance;
//
//                    if (mTotalDistance > 15) {
//                        setSendLocationToServer();
////                        Log.d(TAG, "dataParsing: " + mTotalDistance + "서버로 전송");
//                        mTotalDistance = 0;
//                    }

                    /////////////////////////////////////////

                    // 정보 발생일시
                    int[] date = new int[14]; // 14자리
                    int dateIndex = 29; // CycleData의 배열의 자리
                    for (int i = 0; i < date.length; i++) {
                        date[i] = CycleData[dateIndex];
                        dateIndex++;
                    }

                    str = "";
                    for (int i : date) {
                        str += Character.toString((char) i);

                    }
                    //날짜
                    String carDateText = str;
                    /////////////////////////////////////////

                    int[] totalDist = new int[7];
                    totalDist[0] = CycleData[43];
                    totalDist[1] = CycleData[44];
                    totalDist[2] = CycleData[45];
                    totalDist[3] = CycleData[46];
                    totalDist[4] = CycleData[47];
                    totalDist[5] = CycleData[48];
                    totalDist[6] = CycleData[49];

                    str = "";
                    for (int i : totalDist) {
                        str += Character.toString((char) i);

                    }

                    //누적 주행 거리
                    String carTotalDistText = str;

                    /////////////////////////////////////////
                    int[] dailyDist = new int[4];
                    dailyDist[0] = CycleData[50];
                    dailyDist[1] = CycleData[51];
                    dailyDist[2] = CycleData[52];
                    dailyDist[3] = CycleData[53];

                    str = "";
                    for (int i : dailyDist) {
                        str += Character.toString((char) i);

                    }
                    //일일 주행 거리
                    String carDailyDistText = str;

                    /////////////////////////////////////////

                    int[] rpm = new int[4];
                    rpm[0] = CycleData[57];
                    rpm[1] = CycleData[58];
                    rpm[2] = CycleData[59];
                    rpm[3] = CycleData[60];
                    str = "";
                    for (int i : rpm) {
                        str += Character.toString((char) i);

                    }

                    // rpm = 1분당 엔진 회전수
                    String carEngineRpmText = str;

                    /////////////////////////////////////////
                    if (CycleData[61] == 0x30) {
                        str = "Break Off";
                    } else {
                        str = "Break On";
                    }

                    // 브레이크
                    String carBreakText = str;
                    /////////////////////////////////////////
                    int[] lon = new int[9];
                    lon[0] = CycleData[62];
                    lon[1] = CycleData[63];
                    lon[2] = CycleData[64];
                    lon[3] = CycleData[65];
                    lon[4] = CycleData[66];
                    lon[5] = CycleData[67];
                    lon[6] = CycleData[68];
                    lon[7] = CycleData[69];
                    lon[8] = CycleData[70];
                    str = "";
                    for (int i : lon) {
                        str += Character.toString((char) i);

                    }
                    String carLon;
                    if (!str.equals("000000000")) {
                        carLon = parserLonAndLat(str);
                    } else {
                        carLon = str;
                    }
                    // 차량 위치 - 경도
                    mCarLonText = carLon;

                    /////////////////////////////////////////
                    int[] lat = new int[9];
                    lat[0] = CycleData[71];
                    lat[1] = CycleData[72];
                    lat[2] = CycleData[73];
                    lat[3] = CycleData[74];
                    lat[4] = CycleData[75];
                    lat[5] = CycleData[76];
                    lat[6] = CycleData[77];
                    lat[7] = CycleData[78];
                    lat[8] = CycleData[79];
                    str = "";
                    for (int i : lat) {
                        str += Character.toString((char) i);

                    }
                    String carLat;
                    if (!str.equals("000000000")) {
                        carLat = parserLonAndLat(str);
                    } else {
                        carLat = str;
                    }
                    // 차량 위치 - 위도
                    mCarLatText = carLat;

                    if (!carSpeedText.equals("000")) {
                        sendDataAfterDistanceComparison();
                    }

                    /////////////////////////////////////////
                    int[] gps = new int[3];
                    gps[0] = CycleData[80];
                    gps[1] = CycleData[81];
                    gps[2] = CycleData[82];

                    str = "";
                    for (int i : gps) {
                        str += Character.toString((char) i);

                    }

                    //차량 위치 gps 방위각
                    String carAzimuthText = str;
                    /////////////////////////////////////////
                    String carSleepText = "0";

                    if (!carSpeedText.equals("000")) {  //속도 0이 아닐때
                        int[] sleep = new int[2];
                        sleep[0] = CycleData[149];
                        sleep[1] = CycleData[150];

                        str = "";
                        for (int i : sleep) {
                            str += Character.toString((char) i);

                        }

                        if (CycleData[149] != 0x20 && CycleData[150] != 0x20) {  //이벤트 발생
//                        Log.d(TAG, "dataParsing !=: " + str);

                            switch (str) {
                                case "16":  //전방 미주시

                                    break;
                                case "17":  //졸음 1
                                    carSleepText = "1";
                                    Log.d(TAG, "dataParsing: 졸음");
                                    break;
                                case "18":  //졸음 2
                                    carSleepText = "1";
                                    Log.d(TAG, "dataParsing: 졸음");
                                    break;
                                case "19":  //이탈

                                    break;
                            }
                        } else {
                            carSleepText = "0";
//                        Log.d(TAG, "dataParsing ==: " + str);
                        }
                    } else {
                        carSleepText = "0";
                    }

                    //////////////////////////////////////
                    //기기 상태
                    int[] dtgDeviceState = new int[2];

                    dtgDeviceState[0] = CycleData[93];
                    dtgDeviceState[1] = CycleData[94];


                    str = "";
                    for (int i : dtgDeviceState) {
                        str += Character.toString((char) i);

                    }

                    String dtgDeviceStateText = str;

                    ////////////////////////////////////////////////
                    //시동 상태
                    int[] carBootState = new int[1];

                    carBootState[0] = CycleData[95];

                    str = "";
                    for (int i : carBootState) {
                        str += Character.toString((char) i);

                    }

                    String carBootStateText = str;

                    ////////////////////////////////////////////////

                    if (!isDTGSerialNumber) { //저장된 시리얼번호 없을때만

                        int[] dtgSerialNumber = new int[14];
                        int dtgSerialNumberIndex = 96; // CycleData의 배열의 자리
                        for (int i = 0; i < dtgSerialNumber.length; i++) {
                            dtgSerialNumber[i] = CycleData[dtgSerialNumberIndex];
                            dtgSerialNumberIndex++;
                        }

                        str = "";
                        for (int i : dtgSerialNumber) {
                            str += Character.toString((char) i);

                        }
                        Log.d(TAG, "mDTGBasicData: isDTGSerialNumber 값 들어감");
                        mTinySharedPreference.putObject(DTG_BASIC_DATA, new DTGBasicData(mBleDevice.getDevice(), str));  //쉐어드에 시리얼번호 넣고


                        mDTGSerialNumber = str;  //변수에 시리얼번호 입력
                        isDTGSerialNumber = true;  //시리얼번호를 쉐어드에 넣었으니 다시 안넣어도 되니 true로 바꿔서 이 if문 다시 안나타게 하기

                        Crashlytics.setUserIdentifier(mDTGSerialNumber);  //어떤 사용자에게 특정 비정상 종료가 발생했는지

                    }

                    mDTGInfo = new DTGInfo(carDateText, carTotalDistText, carDailyDistText, carSpeedText, carEngineRpmText, carBreakText, mCarLatText, mCarLonText, carAzimuthText, carSleepText, dtgDeviceStateText, carBootStateText);
//                    }
//
                    ////////////////////////////////////////////////

                    String PhoneNumber = getPhoneNumber();

                    if (PhoneNumber.equals("")) {

                        PhoneNumber = "--------------";

                    }
                    char[] P_char = PhoneNumber.toCharArray();
                    byte[] Data = new byte[20];

                    Data[0] = 0x02;
                    CheckBCC = 0;

                    Data[1] = 0x38;       // Message code     2
                    CheckBCC += Data[1];
                    Data[2] = 0x30;
                    CheckBCC += Data[2];

                    Data[3] = 0x30;      // 핸드폰 번호 앞자리 0 으로 채움  12
                    CheckBCC += Data[3];

                    for (int i = 0; i < 11; i++) {
                        Data[i + 4] = (byte) P_char[i];
                        CheckBCC += Data[i + 4];
                    }

                    Data[15] = 0x37;
                    CheckBCC += Data[15];
                    Data[16] = 0x30;
                    CheckBCC += Data[16];


                    CheckBCC = (byte) ~CheckBCC;
                    CheckBCC += (byte) 1;

                    arrayBCC = new byte[2];

                    arrayBCC[0] = (byte) ((CheckBCC & 0xF0) >> 4);
                    arrayBCC[1] = (byte) (CheckBCC & 0x0F);

                    s = arrayBCC.toString();

                    if (arrayBCC[0] < 10) {
                        arrayBCC[0] += 0x30;
                    } else {
                        arrayBCC[0] += 0x37;
                    }

                    if (arrayBCC[1] < 10) {
                        arrayBCC[1] += 0x30;
                    } else {
                        arrayBCC[1] += 0x37;

                    }
                    Data[17] = arrayBCC[0];
                    Data[18] = arrayBCC[1];


                    Data[19] = (byte) 0x03;
                    startCharWrite(Data); //기기로 write


                    return mDTGInfo;
                }
            }

        } else {
            CycleData_Index = 0;
            Cycle_rcvStart = false;
            Keyevent_rcvStart = false;
            Log_rcvStart = false;
            return null;
        }

        return null;
    }

    byte calcBCC(byte[] pBuf, int len) {
        byte chksum;
        chksum = 0;
        for (int i = 1; i < len; i++) {
            chksum = (byte) (chksum + pBuf[i]);
        }
        chksum = (byte) (~chksum);
        chksum += 1;
        return chksum;
    }

    public String getPhoneNumber() {
        TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = "";
        try {
            if (telephony.getLine1Number() != null) {
                phoneNumber = telephony.getLine1Number();
            } else {
                if (telephony.getSimSerialNumber() != null) {
                    phoneNumber = telephony.getSimSerialNumber();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return phoneNumber;
    }

    public String parserLonAndLat(String str) {
        String s1 = str.substring(0, 3);  //앞에 3자리
        int lastZeroIndex = s1.lastIndexOf("0");  //앞의 3자리 중에 마지막 0이 있는 index
        String zeroDeleteFirstText = s1.substring(lastZeroIndex + 1);  //앞의 3자리 중에 마지막 0이 있는 자리 삭제된 text값

        String lastText = str.substring(3);
        String finalText = zeroDeleteFirstText + "." + lastText;

//        Log.d(TAG, "dataParsing: " + finalText);
        return finalText;
    }

    /**
     * 거리 비교 후 데이터 보내기
     */
    public void sendDataAfterDistanceComparison() {

        String typeCheckLat = mCarLatText.replace(".", "");
        String typeCheckLon = mCarLonText.replace(".", "");


//        Log.d(TAG, "sendDataAfterDistanceComparison: " + typeCheckLat + typeCheckLon);
        if (isStringDouble(typeCheckLat, typeCheckLon)) {

            if (mPreviousLocation != null) {
                //지금데이터 위치데이터에 넣고
                mCurrentLocation = new Location("cur");
                mCurrentLocation.setLatitude(Double.parseDouble(mCarLatText));
                mCurrentLocation.setLongitude(Double.parseDouble(mCarLonText));

                double currentDistance = mPreviousLocation.distanceTo(mCurrentLocation);  //전, 현 데이터 비교
//                Log.d(TAG, "currentDistance: " + mPreviousLocation.getLatitude());
//                Log.d(TAG, "currentDistance: " + mCurrentLocation.getLatitude());

                if (currentDistance >= 15) {  //15m 이상이면 서버로 현 위치 데이터 보냄
                    setSendLocationToServer();
                    mPreviousLocation = mCurrentLocation;  // 전 위치에 현재 위치 넣기
                }
            } else {
                mPreviousLocation = new Location("pre");
                mPreviousLocation.setLatitude(Double.parseDouble(mCarLatText));
                mPreviousLocation.setLongitude(Double.parseDouble(mCarLonText));
            }
        }

    }

    public boolean isStringDouble(String s1, String s2) {
        try {
            Double.parseDouble(s1);
            Double.parseDouble(s2);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}

