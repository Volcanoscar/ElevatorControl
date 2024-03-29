package com.inovance.elevatorcontrol.activities.MainTab;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.inovance.bluetoothtool.BluetoothTalk;
import com.inovance.bluetoothtool.BluetoothTool;
import com.inovance.bluetoothtool.SerialUtility;
import com.inovance.elevatorcontrol.R;
import com.inovance.elevatorcontrol.activities.NavigationTabActivity;
import com.inovance.elevatorcontrol.adapters.ShortcutListViewAdapter;
import com.inovance.elevatorcontrol.cache.ValueCache;
import com.inovance.elevatorcontrol.config.ApplicationConfig;
import com.inovance.elevatorcontrol.daos.RealTimeMonitorDao;
import com.inovance.elevatorcontrol.daos.ShortcutDao;
import com.inovance.elevatorcontrol.handlers.UnlockHandler;
import com.inovance.elevatorcontrol.models.RealTimeMonitor;
import com.inovance.elevatorcontrol.models.Shortcut;
import com.inovance.elevatorcontrol.utils.ParseSerialsUtils;
import com.inovance.elevatorcontrol.views.BlinkTextView;
import com.inovance.elevatorcontrol.views.DoorAnimationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import butterknife.InjectView;
import butterknife.Views;

public class HomeActivity extends Activity implements Runnable {

    private static final String TAG = HomeActivity.class.getSimpleName();

    /**
     * 快捷菜单 List View
     */
    @InjectView(R.id.list_view)
    ListView mListView;

    /**
     * 开关门动画视图
     */
    @InjectView(R.id.door_animation_view)
    DoorAnimationView doorAnimationView;

    /**
     * 当前电梯运行速度
     */
    @InjectView(R.id.running_speed)
    TextView runningSpeedTextView;

    /**
     * 系统状态
     */
    @InjectView(R.id.system_status)
    TextView systemStatusTextView;

    /**
     * 锁梯状态
     */
    @InjectView(R.id.lock_status)
    TextView lockStatusTextView;

    /**
     * 故障
     */
    @InjectView(R.id.error_status)
    BlinkTextView errorStatusTextView;

    /**
     * 获取当前要显示的状态信息的通信内容
     */
    private BluetoothTalk[] communications;

    /**
     * 获取到状态信息后的 Handler
     */
    private SyncStatusHandler mSyncStatusHandler;

    /**
     * 电梯所有状态数组
     */
    private String[] elevatorBoxStatus;

    /**
     * 系统所有状态数组
     */
    private String[] systemStatus;

    /**
     * 快捷菜单 List View Adapter
     */
    private ShortcutListViewAdapter adapter;

    /**
     * 用于 Loop Task 的 Handler
     */
    private static Handler handler = new Handler();

    /**
     * 同步 Task
     */
    private Runnable syncTask;

    /**
     * 是否正在运行同步 Task
     */
    private boolean isRunning = false;

    /**
     * 是否正在读取
     */
    private boolean isReading = false;

    /**
     * 快捷菜单 List
     */
    private List<Shortcut> shortcutList;

    private ExecutorService pool = Executors.newSingleThreadExecutor();

    /**
     * 同步间隔
     */
    private static final int SYNC_TIME = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Views.inject(this);
        mSyncStatusHandler = new SyncStatusHandler(HomeActivity.this);
        syncTask = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    if (BluetoothTool.getInstance().isPrepared()) {
                        if (!isReading) {
                            pool.execute(HomeActivity.this);
                        }
                        handler.postDelayed(this, SYNC_TIME);
                    }
                }
            }
        };
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < shortcutList.size()) {
                    String[] locationArray = shortcutList.get(position).getCommand().split(":");
                    if (locationArray.length == 3) {
                        int navigationTabIndex = Integer.parseInt(locationArray[0]);
                        int pagerIndex = Integer.parseInt(locationArray[1]);
                        NavigationTabActivity tabActivity = (NavigationTabActivity) HomeActivity.this.getParent();
                        if (tabActivity != null) {
                            tabActivity.switchTab(navigationTabIndex, pagerIndex);
                        }
                    }
                }
            }
        });
        errorStatusTextView.setFadingDuration(0);
        errorStatusTextView.setDelayAfterFadein(300);
        errorStatusTextView.setDelayAfterFadeout(300);
        errorStatusTextView.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setListViewDataSource();
        if (BluetoothTool.getInstance().isPrepared()) {
            reSyncData();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
    }

    public void reSyncData() {
        communications = null;
        isRunning = true;
        isReading = false;
        handler.postDelayed(syncTask, SYNC_TIME);
    }

    /**
     * 同步电梯状态信息
     */
    private void syncElevatorStatus() {
        if (communications == null) {
            final List<RealTimeMonitor> monitorLists = RealTimeMonitorDao.findAllByStateIDs(this, ApplicationConfig.HomeStateCode);
            int size = monitorLists.size();
            communications = new BluetoothTalk[size];
            for (int index = 0; index < size; index++) {
                final int i = index;
                communications[i] = new BluetoothTalk() {
                    @Override
                    public void beforeSend() {
                        this.setSendBuffer(SerialUtility.crc16("0103"
                                + monitorLists.get(i).getCode()
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
                            byte[] received = SerialUtility.trimEnd(getReceivedBuffer());
                            try {
                                RealTimeMonitor monitor = (RealTimeMonitor) monitorLists.get(i).clone();
                                monitor.setReceived(received);
                                return monitor;
                            } catch (CloneNotSupportedException e) {
                                e.printStackTrace();
                            }
                        }
                        return null;
                    }
                };
            }
        }
        if (BluetoothTool.getInstance().isPrepared()) {
            isReading = true;
            mSyncStatusHandler.sendCount = communications.length;
            BluetoothTool.getInstance()
                    .setHandler(mSyncStatusHandler)
                    .setCommunications(communications)
                    .startTask();
        }
    }

    /**
     * 更新快捷菜单数据
     */
    public void setListViewDataSource() {
        shortcutList = ShortcutDao.findAll(this);
        if (adapter == null) {
            adapter = new ShortcutListViewAdapter(HomeActivity.this, shortcutList);
            mListView.setAdapter(adapter);
        } else {
            adapter.setShortcutList(shortcutList);
        }
    }

    /**
     * 解析读取到的电梯状态参数
     *
     * @param monitorList RealTimeMonitor List
     */
    private void onGetStatus(List<RealTimeMonitor> monitorList) {
        for (RealTimeMonitor monitor : monitorList) {
            // 电梯运行速度
            if (monitor.getStateID() == ApplicationConfig.HomeStateCode[0]) {
                HomeActivity.this.runningSpeedTextView
                        .setText(ParseSerialsUtils.getValueTextFromRealTimeMonitor(monitor)
                                + monitor.getUnit());
            }
            // 系统状态
            if (monitor.getStateID() == ApplicationConfig.HomeStateCode[1]) {
                int elevatorBoxStatusCode = ParseSerialsUtils.getElevatorBoxStatusCode(monitor);
                // 如果为运行状态则改为关门维持状态,门机状态而非轿厢状态
                elevatorBoxStatusCode = elevatorBoxStatusCode == 5 ? 4 : elevatorBoxStatusCode;
                int systemStatusCode = ParseSerialsUtils.getSystemStatusCode(monitor);
                if (HomeActivity.this.elevatorBoxStatus == null || HomeActivity.this.systemStatus == null) {
                    try {
                        JSONArray jsonArray = new JSONArray(monitor.getJSONDescription());
                        Pattern pattern = Pattern.compile("^\\d*\\-\\d*:.*", Pattern.CASE_INSENSITIVE);
                        int size = jsonArray.length();
                        for (int i = 0; i < size; i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            for (Iterator iterator = jsonObject.keys(); iterator.hasNext(); ) {
                                String name = (String) iterator.next();
                                if (pattern.matcher(name).matches()) {
                                    if (name.replaceAll("\\d*\\-\\d*:", "")
                                            .equalsIgnoreCase(ApplicationConfig.ELEVATOR_BOX_STATUS_NAME)) {
                                        JSONArray subArray = jsonObject.optJSONArray(name);
                                        int subArraySize = subArray.length();
                                        HomeActivity.this.elevatorBoxStatus = new String[subArraySize];
                                        for (int m = 0; m < subArraySize; m++) {
                                            HomeActivity.this.elevatorBoxStatus[m] = subArray
                                                    .getJSONObject(m)
                                                    .optString("value");
                                        }
                                    }
                                    if (name.replaceAll("\\d*\\-\\d*:", "")
                                            .equalsIgnoreCase(ApplicationConfig.SYSTEM_STATUS_NAME)) {
                                        JSONArray subArray = jsonObject.optJSONArray(name);
                                        int subArraySize = subArray.length();
                                        HomeActivity.this.systemStatus = new String[subArraySize];
                                        for (int n = 0; n < subArraySize; n++) {
                                            HomeActivity.this.systemStatus[n] = subArray
                                                    .getJSONObject(n)
                                                    .optString("value");
                                        }
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (elevatorBoxStatusCode < HomeActivity.this.elevatorBoxStatus.length) {
                    HomeActivity.this.lockStatusTextView
                            .setText(HomeActivity.this.elevatorBoxStatus[elevatorBoxStatusCode]);
                    if (elevatorBoxStatusCode == 1 || elevatorBoxStatusCode == 2) {
                        HomeActivity.this.doorAnimationView.openDoor();
                    } else {
                        HomeActivity.this.doorAnimationView.closeDoor();
                    }
                }
                if (systemStatusCode < HomeActivity.this.systemStatus.length) {
                    HomeActivity.this.systemStatusTextView
                            .setText(HomeActivity.this.systemStatus[systemStatusCode]);
                }
            }
            // 故障信息
            if (monitor.getStateID() == ApplicationConfig.HomeStateCode[2]) {
                // Write value cache
                ValueCache.getInstance().setErrorData(monitor.getReceived());

                String errorCode = ParseSerialsUtils.getErrorCode(monitor.getReceived());
                NavigationTabActivity tabActivity = (NavigationTabActivity) HomeActivity.this.getParent();
                if (errorCode.equalsIgnoreCase("E00")) {
                    HomeActivity.this.errorStatusTextView.setTextColor(0xff989898);
                    HomeActivity.this.errorStatusTextView.setText(R.string.home_no_error_text);
                    errorStatusTextView.setEnabled(false);
                    if (tabActivity != null && tabActivity.troubleAnalyzeIcon != null) {
                        tabActivity.troubleAnalyzeIcon.setImageResource(R.drawable.tab_trouble_analyze);
                    }
                } else {
                    HomeActivity.this.errorStatusTextView.setTextColor(0xffff594b);
                    HomeActivity.this.errorStatusTextView.setText(errorCode);
                    errorStatusTextView.setEnabled(true);
                    if (tabActivity != null && tabActivity.troubleAnalyzeIcon != null) {
                        tabActivity.troubleAnalyzeIcon.setImageResource(R.drawable.tab_trouble_analyze_error);
                    }
                }
            }
            // 当前楼层
            if (monitor.getStateID() == ApplicationConfig.HomeStateCode[3]) {
                doorAnimationView.setCurrentFloor(ParseSerialsUtils.getIntFromBytes(monitor.getReceived()));
            }
            // 状态字功能(电梯开关门)
            if (monitor.getStateID() == ApplicationConfig.HomeStateCode[4]) {
                int controllerStatus = ParseSerialsUtils.getElevatorStatus(monitor);
                doorAnimationView.setCurrentDirection(controllerStatus);
            }
        }
    }

    @Override
    public void run() {
        HomeActivity.this.syncElevatorStatus();
    }

    // ====================================== HomeActivity Bluetooth Handler ===================================== //

    /**
     * 首页电梯实时状态
     */
    private class SyncStatusHandler extends UnlockHandler {

        public int sendCount;

        public int receiveCount;

        private List<RealTimeMonitor> receivedMonitorList;

        public SyncStatusHandler(Activity activity) {
            super(activity);
            TAG = SyncStatusHandler.class.getSimpleName();
        }

        @Override
        public void onMultiTalkBegin(Message msg) {
            super.onMultiTalkBegin(msg);
            receiveCount = 0;
            receivedMonitorList = new ArrayList<RealTimeMonitor>();
        }

        @Override
        public void onMultiTalkEnd(Message msg) {
            super.onMultiTalkEnd(msg);
            if (sendCount == receiveCount) {
                HomeActivity.this.onGetStatus(receivedMonitorList);
            }
            isReading = false;
        }

        @Override
        public void onTalkReceive(Message msg) {
            super.onTalkReceive(msg);
            if (msg.obj != null && (msg.obj instanceof RealTimeMonitor)) {
                RealTimeMonitor monitor = (RealTimeMonitor) msg.obj;
                receivedMonitorList.add(monitor);
                receiveCount++;
            }
        }
    }

}
