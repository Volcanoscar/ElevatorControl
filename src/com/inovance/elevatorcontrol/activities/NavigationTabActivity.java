package com.inovance.elevatorcontrol.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LocalActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.inovance.bluetoothtool.BluetoothTalk;
import com.inovance.bluetoothtool.BluetoothTool;
import com.inovance.bluetoothtool.SerialUtility;
import com.inovance.elevatorcontrol.R;
import com.inovance.elevatorcontrol.activities.MainTab.ConfigurationActivity;
import com.inovance.elevatorcontrol.activities.MainTab.HomeActivity;
import com.inovance.elevatorcontrol.activities.MainTab.TroubleAnalyzeActivity;
import com.inovance.elevatorcontrol.cache.ValueCache;
import com.inovance.elevatorcontrol.config.ApplicationConfig;
import com.inovance.elevatorcontrol.config.ParameterUpdateTool;
import com.inovance.elevatorcontrol.daos.DeviceDao;
import com.inovance.elevatorcontrol.handlers.MessageHandler;
import com.inovance.elevatorcontrol.handlers.SearchBluetoothHandler;
import com.inovance.elevatorcontrol.handlers.UnlockHandler;
import com.inovance.elevatorcontrol.models.CommunicationCode;
import com.inovance.elevatorcontrol.models.Device;
import com.inovance.elevatorcontrol.models.NormalDevice;
import com.inovance.elevatorcontrol.models.SpecialDevice;
import com.inovance.elevatorcontrol.utils.ParseSerialsUtils;
import com.inovance.elevatorcontrol.views.customspinner.NoDefaultSpinner;
import com.inovance.elevatorcontrol.views.dialogs.UtilsDialog;
import com.inovance.elevatorcontrol.views.fragments.LeftMenuFragment;
import com.inovance.elevatorcontrol.views.slidemenu.SlidingMenu;
import com.inovance.elevatorcontrol.web.WebInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.InjectView;
import butterknife.Views;

/**
 * TabActivity 导航
 */

public class NavigationTabActivity extends FragmentActivity implements Runnable, WebInterface.OnRequestListener, ParameterUpdateTool.OnCheckResultListener {

    private static final String TAG = NavigationTabActivity.class.getSimpleName();

    /**
     * 优先显示的设备名称
     */
    private static final String BluetoothPrefixName = "M-Tools";

    /**
     * 用于搜索蓝牙的 Handler
     */
    private SearchBluetoothHandler searchBluetoothHandler;

    /**
     * 是否已读取到设备型号和名称
     */
    private boolean hasGetDeviceType = false;

    private boolean hasProtocolParameter = false;

    /**
     * 用于读取标准设备型号的 Handler
     */
    private GetNormalDeviceTypeHandler getNormalDeviceTypeHandler;

    /**
     * 用于读取专用设备型号的 Handler
     */
    private GetSpecialDeviceTypeHandler getSpecialDeviceTypeHandler;

    /**
     * 用于获取协议类型 Handler
     */
    private  GetProtocolTypeHandler getDeviceProtocolTypeHandler;

    /**
     * 用于获取设备功能分组的 Handler
     */
    private GetParameterGroupHandler parameterGroupgHandler;
    private GetParameterListHandler parameterListHandler;


    private List<String> groupList = null;
    HashMap<String, List<String>> parList = new HashMap<String, List<String>>();//二维数组

    private BluetoothTalk[] getParGroupCommunication;
    private BluetoothTalk[] getParameterCommunications;

    /**
     * 用于取得设备型号的通信内容
     */
    private BluetoothTalk[] getNormalDeviceTypeTalk;

    /**
     * 用于取得设备协议的通信内容
     */
    private BluetoothTalk[] getProtocolTypeTalk;

    /**
     * 用于读取设备型号的 Task (标准设备或者专用设备)
     */
    private Runnable getDeviceTypeTask;

    /**
     * 读取间隔
     */
    private static final int LOOP_TIME = 1000;


    /**
     * 是否暂停读取
     */
    private boolean isRunning = false;

    /**
     * 正在搜索蓝牙设备提示
     */
    @InjectView(R.id.search_device_tips)
    TextView searchDeviceTipsView;

    /**
     * 搜索到得设备列表显示
     */
    @InjectView(R.id.custom_spinner)
    NoDefaultSpinner deviceListSpinner;

    /**
     * 重新搜索蓝牙设备按钮
     */
    @InjectView(R.id.research_devices_button)
    public View researchDevicesButton;

    /**
     * 重新搜索设备按钮图标ImageView
     */
    @InjectView(R.id.refresh_button_icon)
    ImageView refreshButtonIcon;

    /**
     * 重新搜索设备按钮ProgressBar
     */
    @InjectView(R.id.refresh_button_progress)
    ProgressBar refreshButtonProgress;

    /**
     * 连接状态指示
     */
    @InjectView(R.id.connect_status_view)
    ImageButton connectStatusView;

    /**
     * 下拉菜单
     */
    @InjectView(R.id.call_floor_button)
    ImageButton callFloorButton;

    /**
     * 故障分析 Tab Icon ImageView
     */
    public ImageView troubleAnalyzeIcon;

    /**
     * 已经搜索到的蓝牙设备列表
     */
    private List<BluetoothDevice> tempDeviceList;

    /**
     * 已经搜索到的蓝牙设备列表名称
     */
    private String[] tempDevicesName;

    /**
     * 搜索蓝牙设备状态
     */
    private static final int SEARCH_DEVICE = 1;

    /**
     * 连接蓝牙设备状态
     */
    private static final int CONNECT_DEVICE = 2;

    /**
     * 识别新旧协议信号状态
     */
    private static final int GET_DEVICE_PROTOCOL = 3;

    /**
     * 读取设备分组一级菜单
     *
     */
    private static final int READ_DEVICE_GROUP = 4;

    /**
     * 读取设备分组二级菜单
     *
     */
    private static final int READ_DEVICE_GROUP_DETAIL = 5;

    /**
     * 读取标准设备信号状态
     */
    private static final int GET_NORMAL_DEVICE_TYPE = 6;

    /**
     * 读取非标准设备信号状态
     */
    private static final int GET_SPECIAL_DEVICE_TYPE = 7;


    /**
     * 当前的 Task 状态
     */
    private int currentTask;

    /**
     * 上一次连接的蓝牙设备
     */
    private BluetoothDevice tempDevice;

    /**
     * 通信次数
     */
    private int talkTime = 0;

    /**
     * 最大重试次数
     */
    private static final int MaxRetryTime = 5;

    /**
     * 专有设备通信码
     */
    private List<CommunicationCode> communicationCodeList = new ArrayList<CommunicationCode>();

    /**
     * 当前正在尝试的通信码索引
     */
    private int specialDeviceCodeIndex = -1;

    private Handler delayHandler = new Handler();

    /**
     * 可供选择的标准设备列表
     */
    private List<NormalDevice> normalDeviceList = new ArrayList<NormalDevice>();

    /**
     * 可供选择的专有设备列表
     */
    private List<SpecialDevice> specialDeviceList = new ArrayList<SpecialDevice>();

    private static final int StartRecogniseDevice = 1;

    private static final int FailedRecogniseDevice = 2;

    /**
     * 连接设备失败
     */
    public boolean failedToConnectDevice = false;

    private int selectedOperationType = -1;

    private RecogniseHandler recogniseHandler;

    private ExecutorService pool = Executors.newSingleThreadExecutor();

    /**
     * Tab控件和活动管理器
     */
    LocalActivityManager lam;

    TabHost tabHost;

    /**
     * menu按钮，点击按钮展示左侧布局菜单
     */
    private ImageButton menuButton;

    private SlidingMenu menu;


    private static class RecogniseHandler extends Handler {

        private final WeakReference<NavigationTabActivity> mActivity;

        public RecogniseHandler(NavigationTabActivity activity) {
            mActivity = new WeakReference<NavigationTabActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            NavigationTabActivity activity = mActivity.get();
            if (activity != null) {
                if (activity.isRunning) {
                    switch (msg.what) {
                        case StartRecogniseDevice:
                            Toast.makeText(activity, R.string.recognise_device_wait_text, Toast.LENGTH_SHORT).show();
                            break;
                        case FailedRecogniseDevice:
                            Toast.makeText(activity, R.string.recognise_device_failed_text, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_tab);
        Views.inject(this);

        initTabs(savedInstanceState);
        initSlidingMenu();

        menuButton = (ImageButton) findViewById(R.id.menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (menu.isMenuShowing())
                    menu.showContent();
                else
                    menu.showMenu();
            }
        });
        recogniseHandler = new RecogniseHandler(this);

        deviceListSpinner.setBackgroundResource(R.drawable.custom_spinner_background);
        deviceListSpinner.setSpinnerItemLayout(R.layout.custom_white_spinner_item);
        callFloorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showCheckProtocolDialog();
//                if (BluetoothTool.getInstance().isPrepared()) {
//                    startActivity(new Intent(NavigationTabActivity.this, CallFloorWindow.class));
//
//                }
            }
        });

        connectStatusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BluetoothTool.getInstance().isConnected()) {
                    BluetoothDevice device = BluetoothTool.getInstance().connectedDevice;
                    String message = device.getName() + "\n" + device.getAddress();
                    AlertDialog.Builder builder = new AlertDialog.Builder(NavigationTabActivity.this, R.style.GlobalDialogStyle)
                            .setTitle(R.string.bluetooth_address_text)
                            .setMessage(message)
                            .setPositiveButton(R.string.dialog_btn_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.create().show();
                }
            }
        });

        searchBluetoothHandler = new SearchBluetoothHandler(this);

        getDeviceProtocolTypeHandler = new GetProtocolTypeHandler(this);
        parameterGroupgHandler = new GetParameterGroupHandler(this);
        parameterListHandler = new GetParameterListHandler(this);
        getNormalDeviceTypeHandler = new GetNormalDeviceTypeHandler(this);
        getSpecialDeviceTypeHandler = new GetSpecialDeviceTypeHandler(this);

        createDeviceTask();
        showRefreshButtonProgress(false);
        /**
         * 绑定搜索按钮动作
         */
        researchDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRefreshButtonProgress(true);
                searchDeviceTipsView.setText(R.string.searching_device_text);
                searchDeviceTipsView.setVisibility(View.VISIBLE);
                deviceListSpinner.setVisibility(View.GONE);
                currentTask = SEARCH_DEVICE;
                pool.execute(NavigationTabActivity.this);
            }
        });
        MessageHandler.getInstance(NavigationTabActivity.this);
        overridePendingTransition(R.anim.activity_open_animation, R.anim.activity_close_animation);
    }

    private void createDeviceTask()
    {
        getDeviceTypeTask = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    if (BluetoothTool.getInstance().isLocked()) {
                        return;
                    }

                    if (!hasGetDeviceType) {
                        if (currentTask == GET_NORMAL_DEVICE_TYPE) {
                            if (talkTime >= MaxRetryTime) {
                                // 内部用户版本
                                if (ApplicationConfig.IsInternalVersion) {
                                    communicationCodeList = new ArrayList<CommunicationCode>();
                                    for (SpecialDevice device : specialDeviceList) {
                                        CommunicationCode code = new CommunicationCode();
                                        code.setCode(device.getCode());
                                        Timestamp timestamp = Timestamp.valueOf("2090-10-10 00:00:00.0");
                                        long time = timestamp.getTime() / 1000;
                                        code.setExpirationTime(time);
                                        communicationCodeList.add(code);
                                    }
                                }
                                talkTime = 0;
                                currentTask = GET_SPECIAL_DEVICE_TYPE;
                                specialDeviceCodeIndex = 0;
                            }
                        }
                        if (currentTask == GET_SPECIAL_DEVICE_TYPE) {
                            if (communicationCodeList.size() == 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(NavigationTabActivity.this,
                                                R.string.recognise_device_failed_text,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                                isRunning = false;
                            } else {
                                if (specialDeviceCodeIndex >= communicationCodeList.size() - 1) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(NavigationTabActivity.this,
                                                    R.string.recognise_device_failed_text,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    isRunning = false;
                                }
                                if (talkTime >= MaxRetryTime) {
                                    talkTime = 0;
                                    currentTask = GET_SPECIAL_DEVICE_TYPE;
                                    specialDeviceCodeIndex++;
                                }
                            }
                        }
                    }

                    talkTime++;
                    if (isRunning) {
                        delayHandler.postDelayed(getDeviceTypeTask, LOOP_TIME);
                        pool.execute(NavigationTabActivity.this);
                    }
                }
            }
        };
    }

    /**
     * 取得当前连接的设备协议和参数
     */
    public void startGetProtocolParameterTask() {
        if (!hasProtocolParameter) {

            currentTask = GET_DEVICE_PROTOCOL;
            pool.execute(NavigationTabActivity.this);
        }
    }

    /**
     * 取得当前连接的标准设备型号
     */
    private void startGetNormalDeviceTypeTask() {
        if (!hasGetDeviceType) {
            isRunning = true;
            talkTime = 0;

            currentTask =  GET_NORMAL_DEVICE_TYPE;
            delayHandler.postDelayed(getDeviceTypeTask, LOOP_TIME);
            recogniseHandler.sendEmptyMessage(StartRecogniseDevice);
        }
    }

    private void initSlidingMenu() {
        //Slide menu for main page
        menu = new SlidingMenu(this);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setBehindOffsetRes(R.dimen.sliding_menu_offset);
        menu.setFadeEnabled(true);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setBehindWidth(300);

        menu.setMenu(R.layout.menu_frame);
        getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame, new LeftMenuFragment()).commit();
    }

    /**
     * 更新蓝牙状态标识
     */
    public void updateConnectStatusUI() {
        if (BluetoothTool.getInstance().isConnected()) {
            connectStatusView.setImageResource(R.drawable.ic_bluetooth_connected);
        } else {
            connectStatusView.setImageResource(R.drawable.ic_bluetooth);
        }
    }

    @Override
    protected void onResume() {
        lam.dispatchResume();
        super.onResume();

        WebInterface.getInstance().setOnRequestListener(this);
        WebInterface.getInstance().getNormalDeviceList(this);
        WebInterface.getInstance().getSpecialDeviceList(this);
        WebInterface.getInstance().getSpecialDeviceCodeList(this, BluetoothAdapter.getDefaultAdapter().getAddress());

        ParameterUpdateTool.getInstance().setOnCheckResultListener(this);

        retryConnectDevice();
    }

    /**
     * 尝试重新连接设备
     */
    private void retryConnectDevice() {
        if (BluetoothTool.getInstance().isConnected()
                && !BluetoothTool.getInstance().isPrepared()
                && !BluetoothTool.getInstance().isLocked()) {
            currentTask = CONNECT_DEVICE;
            failedToConnectDevice = false;
            pool.execute(NavigationTabActivity.this);
        }
    }

    /**
     * 显示选择操作类型对话框
     * 调试电梯
     * 烧录 DSP
     */
    public void showSelectOperationDialog() {
        if (selectedOperationType == 0) {
            startGetNormalDeviceTypeTask();
        } else {
            String[] operationArray = getResources().getStringArray(R.array.device_operation_array);
            AlertDialog.Builder builder = new AlertDialog.Builder(NavigationTabActivity.this);
            builder.setTitle(R.string.choice_operation_title);
            builder.setItems(operationArray, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            // 识别设备
                            selectedOperationType = 0;
                            startGetNormalDeviceTypeTask();
                            break;
                        case 1:
                            // 跳转到程序烧录界面
                            selectedOperationType = 1;
                            // TODO Switch tab
                            switchTab(3, 2);
                            break;
                    }
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        }
    }


    /**
     * Show refresh button progress
     *
     * @param shown shown
     */
    public void showRefreshButtonProgress(boolean shown) {
        if (shown) {
            refreshButtonIcon.setVisibility(View.GONE);
            refreshButtonProgress.setVisibility(View.VISIBLE);
        } else {
            refreshButtonIcon.setVisibility(View.VISIBLE);
            refreshButtonProgress.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        lam.dispatchPause(isFinishing());
        super.onPause();
        WebInterface.getInstance().removeListener();
    }

    /**
     * 标签初始化
     */
    private void initTabs(Bundle savedInstanceState) {

        lam = new LocalActivityManager(NavigationTabActivity.this, false);
        lam.dispatchCreate(savedInstanceState);

        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(lam);
        if (tabHost != null) {
            LinearLayout tabIndicator;
            String[] tabsText = getResources().getStringArray(R.array.navigation_tab_text);
            int[] icons = new int[]{
                    R.drawable.tab_configuration,
                    R.drawable.tab_home,
                    R.drawable.tab_trouble_analyze
            };
            Class<?>[] classes = new Class[]{
                    ConfigurationActivity.class,
                    HomeActivity.class,
                    TroubleAnalyzeActivity.class
            };
            int index = 0;
            for (String title : tabsText) {
                tabIndicator = (LinearLayout) LayoutInflater.from(this)
                        .inflate(R.layout.navigation_tab_indicator, tabHost.getTabWidget(), false);
                ((TextView) tabIndicator.findViewById(R.id.title)).setText(title);
                ImageView tabIcon = ((ImageView) tabIndicator.findViewById(R.id.icon));
                tabIcon.setImageResource(icons[index]);
                if (index == 0) {
                    troubleAnalyzeIcon = tabIcon;
                }
                TabHost.TabSpec tabSpec = tabHost.newTabSpec("tab" + title)
                        .setIndicator(tabIndicator)
                        .setContent(new Intent(this, classes[index]));
                tabHost.addTab(tabSpec);
                index++;
            }
        }
        tabHost.setCurrentTab(1);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                BluetoothTool.getInstance().setHandler(null);
            }
        });
    }

    /**
     * Change Current Tab Index
     *
     * @param tabIndex   New Tab Index
     * @param pagerIndex Pager Index
     */
    public void switchTab(final int tabIndex, final int pagerIndex) {
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                Activity currentActivity = lam.getActivity(tabHost.getCurrentTabTag());
                if (tabIndex == 0) {
                    if (currentActivity instanceof ConfigurationActivity) {
                        final ConfigurationActivity configurationActivity = (ConfigurationActivity) currentActivity;
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                configurationActivity.changePagerIndex(pagerIndex);
                            }
                        };
                        Handler handler = new Handler();
                        handler.postDelayed(runnable, 300);
                        tabHost.setOnTabChangedListener(null);
                    }
                } else if (tabIndex == 2) {
                    if (currentActivity instanceof TroubleAnalyzeActivity) {
                        final TroubleAnalyzeActivity troubleAnalyzeActivity = (TroubleAnalyzeActivity) currentActivity;
                        troubleAnalyzeActivity.pageIndex = pagerIndex;
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                troubleAnalyzeActivity.changePagerIndex(pagerIndex);
                            }
                        };
                        Handler handler = new Handler();
                        handler.postDelayed(runnable, 300);
                        tabHost.setOnTabChangedListener(null);
                    }
                }
            }
        });
        tabHost.setCurrentTab(tabIndex);
    }

    /**
     * 绑定导航按键动作
     *
     * @param event KeyEvent
     * @return True or false
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (tabHost.getCurrentTab() != 2) {
                tabHost.setCurrentTab(2);
            } else {
                UtilsDialog.exitDialog(this).show();
            }
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 更新蓝牙设备 Spinner 下拉列表
     *
     * @param items 蓝牙设备列表
     */
    public void updateSpinnerDropdownItem(List<BluetoothDevice> deviceList) {
        Set<BluetoothDevice> bluetoothDeviceSet = new HashSet<BluetoothDevice>();
        bluetoothDeviceSet.addAll(deviceList);
        deviceList = new ArrayList<BluetoothDevice>();
        deviceList.addAll(bluetoothDeviceSet);
        List<String> specialDevicesName = new ArrayList<String>();
        List<String> normalDevicesName = new ArrayList<String>();
        List<BluetoothDevice> specialDevices = new ArrayList<BluetoothDevice>();
        for (BluetoothDevice device : deviceList) {
            String deviceName = device.getName();
            if (deviceName == null) {
                deviceName = "NULL";
            }
            String name = deviceName + "(" + device.getAddress() + ")";
            if (deviceName.contains(BluetoothPrefixName)) {
                specialDevicesName.add(name);
                specialDevices.add(device);
            }

        }
        List<String> concatenateName = new ArrayList<String>();
        concatenateName.addAll(specialDevicesName);
        concatenateName.addAll(normalDevicesName);
        tempDeviceList = new ArrayList<BluetoothDevice>();
        tempDeviceList.addAll(specialDevices);
        tempDevicesName = concatenateName.toArray(new String[concatenateName.size()]);
        if (tempDevicesName.length > 0) {
            setSpinnerDataSource();
            deviceListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    tempDevice = tempDeviceList.get(position);
                    currentTask = CONNECT_DEVICE;
                    // 连接设备
                    failedToConnectDevice = false;
                    pool.execute(NavigationTabActivity.this);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
    }

    /**
     * Set spinner data source
     */
    public void setSpinnerDataSource() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(),
                R.layout.custom_white_spinner_item, tempDevicesName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceListSpinner.setAdapter(adapter);
        searchDeviceTipsView.setVisibility(View.GONE);
        deviceListSpinner.setVisibility(View.VISIBLE);
    }

    public void updateSearchResult() {
        if (tempDevicesName.length == 0) {
            searchDeviceTipsView.setText(R.string.found_none_device_text);
            searchDeviceTipsView.setVisibility(View.VISIBLE);
            deviceListSpinner.setVisibility(View.GONE);
        }
    }

    /**
     * 取得标准设备型号、厂家编号
     */
    private void getNormalDeviceType() {
        if (getNormalDeviceTypeTalk == null) {
            BluetoothTool.getInstance().setCRCValue(BluetoothTool.CRCValueNone);
            getNormalDeviceTypeTalk = new BluetoothTalk[]{
                    new BluetoothTalk() {
                        @Override
                        public void beforeSend() {
                            this.setSendBuffer(SerialUtility.crc16("0103"
                                    + ApplicationConfig.GetDeviceTypeCode
                                    + "0001"));
                        }

                        @Override
                        public void afterSend() {

                        }

                        @Override
                        public void beforeReceive() {

                        }

                        @Override
                        public void afterReceive() {

                        }

                        @Override
                        public Object onParse() {
                            if (SerialUtility.isCRC16Valid(getReceivedBuffer())) {
                                byte[] data = getReceivedBuffer();
                                String result = SerialUtility.byte2HexStr(data);
                                if (result.contains(ApplicationConfig.ErrorCode)) {
                                    return -1;
                                } else {
                                    return ParseSerialsUtils.getIntFromBytes(data);
                                }
                            }
                            return null;
                        }
                    }
            };
        }
        if (BluetoothTool.getInstance().isConnected()) {
            BluetoothTool.getInstance()
                    .setHandler(getNormalDeviceTypeHandler)
                    .setCommunications(getNormalDeviceTypeTalk)
                    .startTask();
        }
    }

    /**
     * 取得专用设备型号
     */
    private void getSpecialDeviceType() {
        if (specialDeviceCodeIndex < communicationCodeList.size()) {
            final CommunicationCode code = communicationCodeList.get(specialDeviceCodeIndex);
            BluetoothTool.getInstance().setCRCValue(code.getCrcValue());
            // 通信码是否过期
            if (!code.isExpire()) {
                BluetoothTalk[] talks = new BluetoothTalk[1];
                talks[0] = new BluetoothTalk() {
                    @Override
                    public void beforeSend() {
                        this.setSendBuffer(SerialUtility.crc16("0103"
                                + ApplicationConfig.GetDeviceTypeCode
                                + "0001"));
                    }

                    @Override
                    public void afterSend() {

                    }

                    @Override
                    public void beforeReceive() {

                    }

                    @Override
                    public void afterReceive() {

                    }

                    @Override
                    public Object onParse() {
                        if (SerialUtility.isCRC16Valid(getReceivedBuffer())) {
                            String result = SerialUtility.byte2HexStr(getReceivedBuffer());
                            if (!result.contains(ApplicationConfig.ErrorCode)) {
                                return result;
                            }
                        }
                        return null;
                    }
                };
                if (BluetoothTool.getInstance().isConnected()) {
                    BluetoothTool.getInstance()
                            .setHandler(getSpecialDeviceTypeHandler)
                            .setCommunications(talks)
                            .startTask();
                }
            } else {
                talkTime = 0;
                specialDeviceCodeIndex++;
            }
        }
    }

    /**
     * 显示标准设备选择框
     *
     * @param type 1000/3000
     */
    private void onGetNormalDeviceType(int type) {
        // 3000/3000+ 返回3000
        // 1000/1000+ 返回1000
        // ErrorCode && Other
        final List<NormalDevice> tempNormalDevice = new ArrayList<NormalDevice>();
        List<String> names = new ArrayList<String>();
        if (type == 3000 || type == 1000) {
            String typeString = String.valueOf(type);
            for (NormalDevice device : normalDeviceList) {
                if (device.getName().contains(typeString)) {
                    tempNormalDevice.add(device);
                    names.add(device.getName());
                }
            }
        } else {
            for (NormalDevice device : normalDeviceList) {
                tempNormalDevice.add(device);
                names.add(device.getName());
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(NavigationTabActivity.this);
        builder.setTitle(R.string.choice_device_type_title);
        builder.setItems(names.toArray(new String[names.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                final NormalDevice device = tempNormalDevice.get(position);
                BluetoothTool.getInstance().setHasSelectDeviceType(true);
                // 检查标准设备功能码、状态码、故障帮助更新状态
                ParameterUpdateTool.getInstance().setNormalDevice(device);
                String deviceName = ParameterUpdateTool.getInstance().getDeviceName();
                // NICE 1000 / NICE 3000 设备提示用户选择同步异步
                if (deviceName.equals(ApplicationConfig.NormalDeviceType[0]) ||
                        deviceName.equals(ApplicationConfig.NormalDeviceType[2])) {
                    showChooseSyncOrAsyncDialog(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            // position == 0 同步状态
                            // position == 1 异步状态
                            ParameterUpdateTool.getInstance().setSync(position == 0);
                            ParameterUpdateTool.getInstance().selectDevice(NavigationTabActivity.this,
                                    device.getID(),
                                    device.getName(),
                                    Device.NormalDevice);
                        }
                    });
                } else {
                    ParameterUpdateTool.getInstance().selectDevice(NavigationTabActivity.this,
                            device.getID(),
                            device.getName(),
                            Device.NormalDevice);
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     * 解析读取的非标设备类型
     *
     * @param code 通信码
     */
    private void onGetSpecialDeviceType(CommunicationCode code) {
        for (final SpecialDevice device : specialDeviceList) {
            if (code.getCode().equalsIgnoreCase(device.getCode())) {
                BluetoothTool.getInstance().setHasSelectDeviceType(true);
                // 检查专有设备功能码、状态码、故障帮助更新状态
                ParameterUpdateTool.getInstance().setSpecialDevice(device);
                // NICE 1000 / NICE 3000 设备提示用户选择同步异步
                String deviceName = ParameterUpdateTool.getInstance().getDeviceName();
                if (deviceName.equals(ApplicationConfig.NormalDeviceType[0]) ||
                        deviceName.equals(ApplicationConfig.NormalDeviceType[2])) {
                    showChooseSyncOrAsyncDialog(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            // position == 0 同步状态
                            // position == 1 异步状态
                            ParameterUpdateTool.getInstance().setSync(position == 0);
                            ParameterUpdateTool.getInstance().selectDevice(NavigationTabActivity.this,
                                    device.getId(),
                                    device.getName(),
                                    Device.SpecialDevice);

                        }
                    });
                } else {
                    ParameterUpdateTool.getInstance().selectDevice(NavigationTabActivity.this,
                            device.getId(),
                            device.getName(),
                            Device.SpecialDevice);
                }
            }
        }

    }


    /**
     * 显示选择同步或者异步对话框
     *
     * @param listener DialogInterface.OnClickListener
     */
    private void showChooseSyncOrAsyncDialog(DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(NavigationTabActivity.this);
        builder.setTitle(R.string.choice_device_sync_or_async_title);
        builder.setItems(R.array.choice_device_sync_or_async_option_array, listener);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void run() {
        Log.v("BluetoothTool", "Run: " + currentTask);
        switch (currentTask) {
            case SEARCH_DEVICE:
                BluetoothTool.getInstance().setEventHandler(searchBluetoothHandler).search();
                break;
            case CONNECT_DEVICE:
                if (tempDevice != null && !failedToConnectDevice) {
                    hasGetDeviceType = false;
                    BluetoothTool.getInstance().setHasSelectDeviceType(false);
                    BluetoothTool.getInstance().connectDevice(tempDevice);
                }
                break;

            case GET_DEVICE_PROTOCOL:
                if (!BluetoothTool.getInstance().isLocked()) {
                    getProtocolType();
                }
                break;
            case READ_DEVICE_GROUP:
                if (!BluetoothTool.getInstance().isLocked()) {
                    getParameterGroup();
                }
                break;
            case READ_DEVICE_GROUP_DETAIL:
                if (!BluetoothTool.getInstance().isLocked()) {
                    getParameterList();
                }
                break;


            case GET_NORMAL_DEVICE_TYPE:
                if (!BluetoothTool.getInstance().isLocked()) {
                    getNormalDeviceType();
                }
                break;
            case GET_SPECIAL_DEVICE_TYPE:
                if (!BluetoothTool.getInstance().isLocked()) {
                    getSpecialDeviceType();
                }
                break;


        }
    }

    private void getProtocolType() {
        if (getProtocolTypeTalk == null) {
            BluetoothTool.getInstance().setCRCValue(BluetoothTool.CRCValueNone);
            getProtocolTypeTalk = new BluetoothTalk[]{
                    new BluetoothTalk() {
                        @Override
                        public void beforeSend() {
                            this.setSendBuffer(SerialUtility.getCRCCheck(ApplicationConfig.CheckDeviceProtocol));
                        }

                        @Override
                        public void afterSend() {

                        }

                        @Override
                        public void beforeReceive() {

                        }

                        @Override
                        public void afterReceive() {

                        }

                        @Override
                        public Object onParse() {
                            if (SerialUtility.isCRC16Valid(getReceivedBuffer())) {
                                byte[] receive = this.getReceivedBuffer();
                                String value = SerialUtility.byte2HexStr(receive);
                                if (value.substring(0, 8).equals("01700002")) {
                                    ApplicationConfig.bNewProtocol = 1;
                                } else {
                                    ApplicationConfig.bNewProtocol = 0;
                                }
                            }
                            return ApplicationConfig.bNewProtocol;
                        }
                    }
            };
        }
        if (BluetoothTool.getInstance().isConnected()) {
            BluetoothTool.getInstance()
                    .setHandler(getDeviceProtocolTypeHandler)
                    .setCommunications(getProtocolTypeTalk)
                    .startTask();
        }
    }
    /**
     * 获取要显示的参数组
     */
    private void getParameterGroup() {
        if (getParGroupCommunication == null) {
            getParGroupCommunication = new BluetoothTalk[]{
                    new BluetoothTalk() {
                        @Override
                        public void beforeSend() {
                            //this.setSendBuffer(SerialUtility.getCRCCheck(ApplicationConfig.CheckDeviceProtocol));
                            this.setSpecialCommand(true);
                            this.setSendBuffer(SerialUtility.getCRCCheck(ApplicationConfig.GetDeviceGroupForAll));
                        }

                        @Override
                        public void afterSend() {

                        }

                        @Override
                        public void beforeReceive() {
                        }

                        @Override
                        public void afterReceive() {

                        }

                        @Override
                        public Object onParse() {
                            byte[] data = getReceivedBuffer();
                            List<String> list = new ArrayList<String>();
                            if (SerialUtility.isCRC16Valid(data)) {
                                int nRetCount = (data[2] << 8) | data[3];//返回字节个数,每组3个字节
                                for (int i = 0; i < nRetCount / 3; i++) {
                                    //组个数
                                    int nGroupCount = data[4 + 3 * i] & 0x0F;
                                    //组名
                                    String str = String.format("%X", (data[4 + 3 * i] >> 4) & 0x0F);
                                    int nGroupBit = (data[5 + 3 * i] << 8) | data[6 + 3 * i];
                                    for (int j = 0; j < nGroupCount; j++) {
                                        if (1 == ((nGroupBit >> j) & 0x01)) {
                                            list.add(str + String.format("%X", j));
                                        }
                                    }
                                }
                                groupList = list;
                            }
                            return list;
                        }
                    }
            };
        }
        if (BluetoothTool.getInstance().isConnected()) {
            BluetoothTool.getInstance()
                    .setHandler(parameterGroupgHandler)
                    .setCommunications(getParGroupCommunication)
                    .startTask();
        }

    }

    /**
     * 获取要显示的参数组
     */
    private void getParameterList() {
        if (groupList == null)
            return;
        if (getParameterCommunications == null) {
            int  count = groupList.size();
            getParameterCommunications = new BluetoothTalk[2];
            for (int i = 0; i < 2; i++) {
                final String strGroup = groupList.get(i);
                getParameterCommunications[i] = new BluetoothTalk() {
                    @Override
                    public void beforeSend() {
//                        this.setSendBuffer(SerialUtility.getCRCCheck("0103"
//                                + strGroup + "FF0001"));
//                        this.setSpecialCommand(true);
                    }

                    @Override
                    public void afterSend() {

                    }

                    @Override
                    public void beforeReceive() {
                    }

                    @Override
                    public void afterReceive() {

                    }

                    @Override
                    public Object onParse() {
                        byte[] data = getReceivedBuffer();
                        if (SerialUtility.isCRC16Valid(data)) {
                            List<String> list = new ArrayList<String>();
                            int nRetCount = (data[2] << 8) | data[3];//返回字节个数
                            for (int i = nRetCount - 1; i >= 0; i--) {
                                int nParData = data[4 + i];
                                for (int j = 0; j < 8; j++) {
                                    if (nParData >> j == 1) {
                                        //参数名
                                        int nParCount = j + (nRetCount - i - 1) * 8;
                                        String str;
                                        if (nParCount >= 100)
                                            str = String.format("%03X", nParCount);
                                        else
                                            str = String.format("%02X", nParCount);
                                        list.add(strGroup + "-" + str);
                                    }
                                }
                            }
                            parList.put(strGroup, list);
                            return list;
                        }
                        return null;
                    }
                };
            }
        }
        if (BluetoothTool.getInstance().isConnected()) {
            BluetoothTool.getInstance()
                    .setHandler(parameterListHandler)
                    .setCommunications(getParameterCommunications)
                    .startTask();
        }

    }

    @Override
    public void onResult(String tag, String responseString) {
        if (responseString != null && responseString.length() > 0) {
            if (tag.equalsIgnoreCase(ApplicationConfig.GetNormalDeviceList)) {
                try {
                    JSONArray jsonArray = new JSONArray(responseString);
                    normalDeviceList.clear();
                    int size = jsonArray.length();
                    for (int i = 0; i < size; i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        NormalDevice device = new NormalDevice(object);
                        normalDeviceList.add(device);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (tag.equalsIgnoreCase(ApplicationConfig.GetSpecialDeviceList)) {
                try {
                    JSONArray jsonArray = new JSONArray(responseString);
                    specialDeviceList.clear();
                    int size = jsonArray.length();
                    for (int i = 0; i < size; i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        SpecialDevice device = new SpecialDevice(object);
                        specialDeviceList.add(device);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (tag.equalsIgnoreCase(ApplicationConfig.GetSpecialDeviceCodeList)) {
                try {
                    JSONArray jsonArray = new JSONArray(responseString);
                    int size = jsonArray.length();
                    communicationCodeList = new ArrayList<CommunicationCode>();
                    for (int i = 0; i < size; i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        CommunicationCode code = new CommunicationCode(object);
                        communicationCodeList.add(code);
                    }
                    ValueCache.getInstance().setCodeList(communicationCodeList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onFailure(int statusCode, Throwable throwable) {
        Toast.makeText(this, R.string.server_error_text, Toast.LENGTH_SHORT).show();
    }

    /**
     * 开启 HomeActivity Sync Task
     */
    private void startHomeActivityStatusSyncTask() {
//        if (hasGetDeviceType) {
//            Activity currentActivity = lam.getActivity(tabHost.getCurrentTabTag());
////            <string name="tab_config">tab电梯调试</string>
////            <string name="tab_home">tab监控参数</string>
////            <string name="tab_analysis">tab故障记录</string>
//            switch (tabHost.getCurrentTab()) {
//                case 0: {
//                    if (currentActivity instanceof ConfigurationActivity) {
//                        ConfigurationActivity configurationActivity = (ConfigurationActivity) currentActivity;
//                        configurationActivity.reSyncData();
//                    }
//                }
//                break;
//                case 1: {
//                    if (currentActivity instanceof HomeActivity) {
//                        HomeActivity homeActivity = (HomeActivity) currentActivity;
//                        homeActivity.reSyncData();
//                    }
//                }
//                break;
//                case 2: {
//                    if (currentActivity instanceof TroubleAnalyzeActivity) {
//                        TroubleAnalyzeActivity troubleAnalyzeActivity = (TroubleAnalyzeActivity) currentActivity;
//                        troubleAnalyzeActivity.reSyncData();
//                    }
//                }
//                break;
//            }
//        }
    }

    /**
     * 检查参数更新成功
     */
    @Override
    public void onComplete() {
        startHomeActivityStatusSyncTask();
    }

    /**
     * 检查参数更新失败
     *
     * @param throwable Throwable
     * @param name      Device name
     * @param type      Device type
     */
    @Override
    public void onFailed(Throwable throwable, String name, int type) {
        Device temp = DeviceDao.findByName(NavigationTabActivity.this, name, type);
        if (temp != null) {
            // 本地存在当前连接设备的参数
            Toast.makeText(NavigationTabActivity.this,
                    R.string.check_parameter_failed_use_local_parameter_message,
                    Toast.LENGTH_SHORT).show();
            // 使用本地保存的参数
            NavigationTabActivity.this.startHomeActivityStatusSyncTask();
        } else {
            // 本地不存在当前连接设备的参数
            Toast.makeText(NavigationTabActivity.this,
                    R.string.check_parameter_failed_message,
                    Toast.LENGTH_SHORT).show();
        }
    }


    // ============================== Get Device ProtocolType Handler ================================================ //

    private class GetProtocolTypeHandler extends UnlockHandler {

        public GetProtocolTypeHandler(Activity activity) {
            super(activity);
            TAG = GetNormalDeviceTypeHandler.class.getSimpleName();
        }

        @Override
        public void onMultiTalkBegin(Message msg) {
            super.onMultiTalkBegin(msg);
        }

        @Override
        public void onMultiTalkEnd(Message msg) {
            super.onMultiTalkEnd(msg);
        }

        @Override
        public void onTalkReceive(Message msg) {
            super.onTalkReceive(msg);
            if (msg.obj != null && msg.obj instanceof Integer) {
                if (ApplicationConfig.bNewProtocol == 1) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(NavigationTabActivity.this,
//                                    R.string.new_protocol_text,
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    });
                    getParameterGroup();
                }
            }
        }

        @Override
        public void onBluetoothConnectException(Message message) {
            super.onBluetoothConnectException(message);
            ApplicationConfig.bNewProtocol = -1;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(NavigationTabActivity.this,
                            "Get protocol failed! Please retry later.",
                            Toast.LENGTH_SHORT).show();
                }
            });

        }
    }



    // ============================== Get Normal DeviceType Handler ================================================ //

    private class GetNormalDeviceTypeHandler extends UnlockHandler {

        public GetNormalDeviceTypeHandler(Activity activity) {
            super(activity);
            TAG = GetNormalDeviceTypeHandler.class.getSimpleName();
        }

        @Override
        public void onMultiTalkBegin(Message msg) {
            super.onMultiTalkBegin(msg);
        }

        @Override
        public void onMultiTalkEnd(Message msg) {
            super.onMultiTalkEnd(msg);
        }

        @Override
        public void onTalkReceive(Message msg) {
            super.onTalkReceive(msg);
            if (msg.obj != null && msg.obj instanceof Integer) {
                isRunning = false;
                hasGetDeviceType = true;
                BluetoothTool.getInstance().setCRCValue(BluetoothTool.CRCValueNone);
                onGetNormalDeviceType((Integer) msg.obj);
            }
        }
    }

    // ============================== Get Special DeviceType Handler =============================================== //

    private class GetSpecialDeviceTypeHandler extends UnlockHandler {

        public GetSpecialDeviceTypeHandler(Activity activity) {
            super(activity);
            TAG = GetSpecialDeviceTypeHandler.class.getSimpleName();
        }

        @Override
        public void onMultiTalkBegin(Message msg) {
            super.onMultiTalkBegin(msg);
        }

        @Override
        public void onMultiTalkEnd(Message msg) {
            super.onMultiTalkEnd(msg);
        }

        @Override
        public void onTalkReceive(Message msg) {
            super.onTalkReceive(msg);
            if (msg.obj != null && msg.obj instanceof String) {
                isRunning = false;
                hasGetDeviceType = true;
                onGetSpecialDeviceType(communicationCodeList.get(specialDeviceCodeIndex));
            }
        }
    }

    // ============================== Get ParameterGroup Handler ================================================ //

    private class GetParameterGroupHandler extends UnlockHandler {

        public GetParameterGroupHandler(Activity activity) {
            super(activity);
            TAG = GetParameterGroupHandler.class.getSimpleName();
        }

        @Override
        public void onMultiTalkBegin(Message msg) {
            super.onMultiTalkBegin(msg);
        }

        @Override
        public void onMultiTalkEnd(Message msg) {
            super.onMultiTalkEnd(msg);
        }

        @Override
        public void onTalkReceive(Message msg) {
            super.onTalkReceive(msg);
            if (msg.obj != null) {
                //收到数据
                //talkTime = 0;
                Log.v("BluetoothTool", "Group Receive: " + msg.toString());
                currentTask = READ_DEVICE_GROUP_DETAIL;
//                Toast.makeText(NavigationTabActivity.this,
//                        R.string.read_group_parameter1_success_text,
//                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ============================== Get ParameterList Handler ================================================ //

    private class GetParameterListHandler extends UnlockHandler {

        public GetParameterListHandler(Activity activity) {
            super(activity);
            TAG = GetParameterListHandler.class.getSimpleName();
        }

        @Override
        public void onMultiTalkBegin(Message msg) {
            super.onMultiTalkBegin(msg);
        }

        @Override
        public void onMultiTalkEnd(Message msg) {
            super.onMultiTalkEnd(msg);
        }

        @Override
        public void onTalkReceive(Message msg) {
            super.onTalkReceive(msg);
            if (msg.obj != null) {
                talkTime = 0;

                Log.v("BluetoothTool", "Parameter Detail Receive: " + msg.toString());
                //currentTask = GET_NORMAL_DEVICE_TYPE;
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(NavigationTabActivity.this,
//                                R.string.read_group_parameter2_success_text,
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
            }
        }

    }

}
