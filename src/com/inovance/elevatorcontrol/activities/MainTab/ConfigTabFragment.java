package com.inovance.elevatorcontrol.activities.MainTab;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.inovance.bluetoothtool.BluetoothTalk;
import com.inovance.bluetoothtool.BluetoothTool;
import com.inovance.bluetoothtool.SerialUtility;
import com.inovance.elevatorcontrol.R;
import com.inovance.elevatorcontrol.adapters.ConfigurationAdapter;
import com.inovance.elevatorcontrol.adapters.ParameterStatusAdapter;
import com.inovance.elevatorcontrol.config.ApplicationConfig;
import com.inovance.elevatorcontrol.daos.ParameterSettingsDao;
import com.inovance.elevatorcontrol.daos.RealTimeMonitorDao;
import com.inovance.elevatorcontrol.factory.ParameterFactory;
import com.inovance.elevatorcontrol.handlers.ConfigurationHandler;
import com.inovance.elevatorcontrol.handlers.UnlockHandler;
import com.inovance.elevatorcontrol.models.ObjectListHolder;
import com.inovance.elevatorcontrol.models.ParameterSettings;
import com.inovance.elevatorcontrol.models.ParameterStatusItem;
import com.inovance.elevatorcontrol.models.RealTimeMonitor;
import com.inovance.elevatorcontrol.utils.LogUtils;
import com.inovance.elevatorcontrol.utils.ParseSerialsUtils;
import com.inovance.elevatorcontrol.views.fragments.ConfigurationFragment;
import com.viewpagerindicator.TabPageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * ��ǩ�� ���ݵ���
 *
 * @author jch
 */
public class ConfigTabFragment extends Fragment implements Runnable {

    private static final String TAG = ConfigTabFragment.class.getSimpleName();

    /**
     * ��ǰ Viewpager Index
     */
    public int pageIndex;

    /**
     * ��ȡʵʱ״̬ Handler
     */
    private ConfigurationHandler configurationHandler;

    /**
     * ��ȡʵʱ״̬ͨ������
     */
    private BluetoothTalk[] getRealTimeStateCommunications;

    /**
     * ��ȡ��ѹ�������ֵ Handler
     */
    private GetHVInputTerminalValueHandler getHVInputTerminalValueHandler;

    /**
     * ��ȡ��ѹ�������״̬ Handler
     */
    private GetHVInputTerminalStateHandler getHVInputTerminalStateHandler;

    /**
     * ��ȡ��ѹ�������ֵͨ������
     */
    private BluetoothTalk[] getHVInputTerminalValueCommunications;

    /**
     * ��ȡ��ѹ�������״̬ͨ������
     */
    private BluetoothTalk[] getHVInputTerminalStateCommunications;

    /**
     * ��ȡ�������ֵ Handler
     */
    private GetInputTerminalValueHandler getInputTerminalValueHandler;

    /**
     * ��ȡ�������״̬ Handler
     */
    private GetInputTerminalStateHandler getInputTerminalStateHandler;

    /**
     * ��ȡ�������ֵͨ������
     */
    private BluetoothTalk[] getInputTerminalValueCommunications;

    /**
     * ��ȡ�������״̬ͨ������
     */
    private BluetoothTalk[] getInputTerminalStateCommunications;

    /**
     * ��ȡ����״̬ Handler
     */
    private ElevatorStatusHandler getElevatorStatusHandler;

    /**
     * ��ȡ��������״̬ͨ������
     */
    private BluetoothTalk[] getElevatorStateCommunications;

    /**
     * �ָ������������� Handler
     */
    private RestoreFactoryHandler restoreFactoryHandler;

    /**
     * �ָ�����״̬ͨ������
     */
    private BluetoothTalk[] restoreElevatorCommunications;

    /**
     * ��ȡ�������ֵ Handler
     */
    private GetOutputTerminalValueHandler getOutputTerminalValueHandler;

    /**
     * ��ȡ�������״̬ Handler
     */
    private GetOutputTerminalStateHandler getOutputTerminalStateHandler;

    /**
     * ��ȡ�������ֵͨ������
     */
    private BluetoothTalk[] getOutputTerminalValueCommunications;

    /**
     * ��ȡ�������״̬ͨ������
     */
    private BluetoothTalk[] getOutputTerminalStateCommunications;

    /**
     * ��ȡϵͳ״̬ Handler
     */
    private GetSystemStateHandler getSystemStateHandler;

    /**
     * ��ȡϵͳ״̬ͨ������
     */
    private BluetoothTalk[] getSystemStateCommunications;

    /**
     * ��ȡ�ζ�������״̬
     */
    private GetCeilingInputStateHandler getCeilingInputStateHandler;

    /**
     * ��ȡ�ζ�������״̬ͨ������
     */
    private BluetoothTalk[] getCeilingInputStateCommunications;

    /**
     * ��ȡ�ζ������״̬
     */
    private GetCeilingOutputStateHandler getCeilingOutputStateHandler;

    /**
     * ��ȡ�ζ������״̬ͨ������
     */
    private BluetoothTalk[] getCeilingOutputStateCommunications;

    /**
     * ��ȡ�ȴ���Ϣ
     */
    private TextView waitTextView;

    /**
     * ����״̬��Ϣ�б�
     */
    private ListView terminalListView;

    /**
     * ͬ��ʵʱ״̬ Task
     */
    private Runnable syncTask;

    /**
     * ��ǰ Loop �Ƿ�����
     */
    private boolean isRunning;

    /**
     * ͬ�� Handler ���ڲ���ѭ����ȡ
     */
    private Handler syncHandler = new Handler();

    /**
     * ͬ��ʱ����
     */
    private static final int SYNC_TIME = 3000;

    /**
     * �Ƿ�����ͬ��ʵʱ״̬
     */
    public boolean isSyncing = false;

    private static final int NO_TASK = -1;

    /**
     * ��ȡʵʱ״̬
     */
    private static final int GET_MONITOR_STATE = 1;

    /**
     * ��ȡ��ѹ�������ֵ
     */
    private static final int GET_HV_INPUT_TERMINAL_VALUE = 2;

    /**
     * ��ȡ��ѹ�������״̬
     */
    private static final int GET_HV_INPUT_TERMINAL_STATE = 3;

    /**
     * ��ȡ�������ֵ
     */
    private static final int GET_INPUT_TERMINAL_VALUE = 4;

    /**
     * ��ȡ�������״̬
     */
    private static final int GET_INPUT_TERMINAL_STATE = 5;

    /**
     * ��ȡ�������ֵ
     */
    private static final int GET_OUTPUT_TERMINAL_VALUE = 6;

    /**
     * ��ȡ�������״̬
     */
    private static final int GET_OUTPUT_TERMINAL_STATE = 7;

    /**
     * ��ȡϵͳ״̬
     */
    private static final int GET_SYSTEM_STATE = 8;

    /**
     * ��ȡ�ζ�������״̬
     */
    private static final int GET_CEILING_OUTPUT_STATE = 9;

    /**
     * ��ȡ�ζ������״̬
     */
    private static final int GET_CEILING_INPUT_STATE = 10;

    /**
     * ��ȡ��������״̬
     */
    private static final int GET_ELEVATOR_STATE = 11;

    /**
     * �ָ����ݳ���״̬
     */
    private static final int RESTORE_ELEVATOR_FACTORY = 12;

    /**
     * ��ǰִ�е�����
     */
    private int currentTask;

    private ExecutorService pool = Executors.newSingleThreadExecutor();

    /**
     * View Pager
     */
    public ViewPager pager;

    /**
     * View Pager Indicator
     */
    protected TabPageIndicator indicator;

    /**
     * View Pager Adapter
     */
    public ConfigurationAdapter mConfigurationAdapter;

    /**
     * ����ͨ�ŵ�ʵʱ����б�
     */
    private List<RealTimeMonitor> talkStateList = new ArrayList<RealTimeMonitor>();

    /**
     * ������ʾ��ʵʱ����б�
     */
    public List<RealTimeMonitor> showStateList = new ArrayList<RealTimeMonitor>();

    FragmentActivity parentActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = getActivity();
        parentActivity.setContentView(R.layout.activity_configuration);
//        pager = (ViewPager)parentActivity.findViewById(R.id.pager);
//        indicator = (TabPageIndicator)parentActivity.findViewById(R.id.indicator);
//        // �����ݿ��������
//        reloadDataFromDataBase();
//        ////////////////////mConfigurationAdapter = new ConfigurationAdapter(parentActivity, showStateList);
//        // ͬ����ѹ�������״̬
//        getHVInputTerminalValueHandler = new GetHVInputTerminalValueHandler(parentActivity);
//        getHVInputTerminalStateHandler = new GetHVInputTerminalStateHandler(parentActivity);
//        // ͬ���������״̬
//        getInputTerminalValueHandler = new GetInputTerminalValueHandler(parentActivity);
//        getInputTerminalStateHandler = new GetInputTerminalStateHandler(parentActivity);
//        // ͬ���������״̬
//        getOutputTerminalValueHandler = new GetOutputTerminalValueHandler(parentActivity);
//        getOutputTerminalStateHandler = new GetOutputTerminalStateHandler(parentActivity);
//        // ͬ��ϵͳ״̬
//        getSystemStateHandler = new GetSystemStateHandler(parentActivity);
//        // ͬ���ζ�������״̬
//        getCeilingInputStateHandler = new GetCeilingInputStateHandler(parentActivity);
//        // ͬ���ζ������״̬
//        getCeilingOutputStateHandler = new GetCeilingOutputStateHandler(parentActivity);
//        // ��ȡ����״̬
//        getElevatorStatusHandler = new ElevatorStatusHandler(parentActivity);
//        // �ָ���������
//        restoreFactoryHandler = new RestoreFactoryHandler(parentActivity);
//
//        pager.setAdapter(mConfigurationAdapter);
//        pager.setOffscreenPageLimit(3);
//        indicator.setViewPager(pager);
//        configurationHandler = new ConfigurationHandler(parentActivity);
//        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrollStateChanged(int arg0) {
//
//            }
//
//            @Override
//            public void onPageScrolled(int arg0, float arg1, int arg2) {
//
//            }
//
//            @Override
//            public void onPageSelected(int index) {
//                pageIndex = index;
//            }
//        });
//        // ͬ��ʵʱ״̬
//        syncTask = new Runnable() {
//            @Override
//            public void run() {
//                if (isRunning) {
//                    if (BluetoothTool.getInstance().isPrepared()) {
//                        if (!isSyncing) {
//                            pool.execute(ConfigTabFragment.this);
//                        }
//                        syncHandler.postDelayed(syncTask, SYNC_TIME);
//                    }
//                }
//            }
//        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private class SortComparator implements Comparator<RealTimeMonitor> {

        @Override
        public int compare(RealTimeMonitor object1, RealTimeMonitor object2) {
            if (object1.getSort() < object2.getSort()) {
                return -1;
            } else if (object1.getSort() > object2.getSort()) {
                return 1;
            } else {
                return 0;
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (BluetoothTool.getInstance().isPrepared()) {
            isRunning = true;
            isSyncing = false;
            currentTask = GET_MONITOR_STATE;
            syncHandler.postDelayed(syncTask, SYNC_TIME);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isRunning = false;
    }

    /**
     * Change Pager Index
     *
     * @param index Index
     */
    public void changePagerIndex(int index) {
        if (index != pager.getCurrentItem()) {
            pager.setCurrentItem(index);
        }
    }

    public void reSyncData() {
        getRealTimeStateCommunications = null;
        getHVInputTerminalValueCommunications = null;
        getHVInputTerminalStateCommunications = null;
        getInputTerminalValueCommunications = null;
        getInputTerminalStateCommunications = null;
        getOutputTerminalValueCommunications = null;
        getOutputTerminalStateCommunications = null;
        getSystemStateCommunications = null;
        getCeilingInputStateCommunications = null;
        getCeilingOutputStateCommunications = null;
        getElevatorStateCommunications = null;
        restoreElevatorCommunications = null;
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ConfigurationFragment monitorFragment = mConfigurationAdapter.getItem(0);
                if (monitorFragment != null) {
                    reloadDataFromDataBase();
                    //monitorFragment.reloadDataSource(showStateList);
                }
                ConfigurationFragment groupFragment = mConfigurationAdapter.getItem(1);
                if (groupFragment != null) {
                    groupFragment.reloadDataSource();
                }
            }
        });
        isSyncing = false;
        currentTask = GET_MONITOR_STATE;
    }

    /**
     * ���´����ݿ��������
     */
    private void reloadDataFromDataBase() {
        talkStateList = RealTimeMonitorDao.findAllByStateIDs(parentActivity, ApplicationConfig.MonitorStateCode);
        // ������� ID
        int inputStateID = ApplicationConfig.MonitorStateCode[5];
        // ������� ID
        int outputStateID = ApplicationConfig.MonitorStateCode[6];
        List<RealTimeMonitor> tempInputMonitor = new ArrayList<RealTimeMonitor>();
        List<RealTimeMonitor> tempOutputMonitor = new ArrayList<RealTimeMonitor>();
        showStateList = new ArrayList<RealTimeMonitor>();
        for (RealTimeMonitor item : talkStateList) {
            if (item.getStateID() == inputStateID) {
                tempInputMonitor.add(item);
            } else if (item.getStateID() == outputStateID) {
                tempOutputMonitor.add(item);
            } else {
                showStateList.add(item);
            }
        }
        // ���� Sort ����
        Collections.sort(tempInputMonitor, new SortComparator());
        Collections.sort(tempOutputMonitor, new SortComparator());
        // ȡ�����롢�������λ������
        if (tempInputMonitor.size() > 0) {
            showStateList.add(tempInputMonitor.get(0));
        }
        if (tempOutputMonitor.size() > 0) {
            showStateList.add(tempOutputMonitor.get(0));
        }
    }

    /**
     * �ָ���������
     */
    public void restoreFactory() {
        isSyncing = false;
        currentTask = GET_ELEVATOR_STATE;
    }

    /**
     * ��ȡ����״̬
     */
    private void getElevatorState() {
        if (getElevatorStateCommunications == null) {
            final RealTimeMonitor monitor = RealTimeMonitorDao.findByStateID(parentActivity, ApplicationConfig.RunningStatusType);
            if (monitor != null) {
                getElevatorStateCommunications = new BluetoothTalk[]{
                        new BluetoothTalk() {
                            @Override
                            public void beforeSend() {
                                this.setSendBuffer(SerialUtility.crc16("0103"
                                        + monitor.getCode()
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
                                    byte[] data = SerialUtility.trimEnd(getReceivedBuffer());
                                    if (data.length == 8) {
                                        monitor.setReceived(data);
                                        return monitor;
                                    }
                                }
                                return null;
                            }
                        }
                };
            }
        }
        if (getElevatorStateCommunications != null) {
            if (BluetoothTool.getInstance().isPrepared()) {
                BluetoothTool.getInstance()
                        .setCommunications(getElevatorStateCommunications)
                        .setHandler(getElevatorStatusHandler)
                        .startTask();
            }
        }
    }

    /**
     * �Ѿ���ȡ����������״̬
     *
     * @param monitor RealTimeMonitor
     */
    private void onGetElevatorState(RealTimeMonitor monitor) {
        int state = ParseSerialsUtils.getElevatorStatus(monitor);
        // ����ͣ��״̬
        if (state == 3) {
            isSyncing = false;
            currentTask = RESTORE_ELEVATOR_FACTORY;
        } else {
            isSyncing = false;
            currentTask = NO_TASK;
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(parentActivity.getApplicationContext(), R.string.cannot_restore_elevator_factory, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * д��ָ���������
     */
    private void startRestoreElevatorFactory() {
        if (restoreElevatorCommunications == null) {
            final RealTimeMonitor monitor = RealTimeMonitorDao.findByStateID(parentActivity, ApplicationConfig.RestoreFactoryStateCode);
            if (monitor != null) {
                restoreElevatorCommunications = new BluetoothTalk[]{
                        new BluetoothTalk() {
                            @Override
                            public void beforeSend() {

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
                                    // д��ָ�����������־
                                    LogUtils.getInstance().write(ApplicationConfig.LogRestoreFactory,
                                            SerialUtility.byte2HexStr(getSendBuffer()),
                                            SerialUtility.byte2HexStr(received));
                                    monitor.setReceived(received);
                                    return monitor;
                                }
                                return null;
                            }
                        }
                };
            }
        }
        if (restoreElevatorCommunications != null) {
            if (BluetoothTool.getInstance().isPrepared()) {
                BluetoothTool.getInstance()
                        .setCommunications(restoreElevatorCommunications)
                        .setHandler(restoreFactoryHandler)
                        .startTask();
            }
        }
    }

    /**
     * ��ȡʵʱ״̬
     */
    public void getRealTimeMonitorState() {
        if (getRealTimeStateCommunications == null) {
            getRealTimeStateCommunications = new BluetoothTalk[talkStateList.size()];
            int commandSize = talkStateList.size();
            for (int index = 0; index < commandSize; index++) {
                final String code = talkStateList.get(index).getCode();
                final RealTimeMonitor monitor = talkStateList.get(index);
                getRealTimeStateCommunications[index] = new BluetoothTalk() {
                    @Override
                    public void beforeSend() {
                        this.setSendBuffer(SerialUtility.crc16("0103"
                                + code
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
                            monitor.setReceived(received);
                            return monitor;
                        }
                        return null;
                    }
                };
            }
        }
        if (BluetoothTool.getInstance().isPrepared()) {
            isSyncing = true;
            configurationHandler.sendCount = getRealTimeStateCommunications.length;
            BluetoothTool.getInstance()
                    .setHandler(configurationHandler)
                    .setCommunications(getRealTimeStateCommunications)
                    .startTask();
        }
    }

    /**
     * �鿴��ѹ�������״̬
     *
     * @param index RealTimeMonitor index
     */
    public void viewHVInputTerminalStatus(int index) {
        showTerminalStatusDialog(showStateList.get(index));
        isSyncing = false;
        getHVInputTerminalValueHandler.index = index;
        currentTask = GET_HV_INPUT_TERMINAL_VALUE;
    }

    /**
     * ��ȡ��ѹ�������ֵ
     */
    public void getHVInputTerminalValue() {
        if (getHVInputTerminalValueCommunications == null) {
            final RealTimeMonitor monitor = RealTimeMonitorDao.findByStateID(parentActivity, ApplicationConfig.MonitorStateCode[14]);
            if (monitor != null) {
                getHVInputTerminalValueCommunications = new BluetoothTalk[1];
                getHVInputTerminalValueCommunications[0] = new BluetoothTalk() {
                    @Override
                    public void beforeSend() {
                        this.setSendBuffer(SerialUtility.crc16("0103"
                                + monitor.getCode()
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
                            monitor.setReceived(received);
                            return monitor;
                        }
                        return null;
                    }
                };
            }
        }
        if (BluetoothTool.getInstance().isPrepared()) {
            isSyncing = true;
            getHVInputTerminalValueHandler.sendCount = getHVInputTerminalValueCommunications.length;
            BluetoothTool.getInstance()
                    .setHandler(getHVInputTerminalValueHandler)
                    .setCommunications(getHVInputTerminalValueCommunications)
                    .startTask();
        }
    }

    /**
     * ��ʼ��ȡ��ѹ�������״̬ͨ��
     */
    private void getHVInputTerminalState() {
        if (getHVInputTerminalStateCommunications == null) {
            final List<ParameterSettings> terminalList = ParameterSettingsDao.findByType(parentActivity, ApplicationConfig.HVInputTerminalType);
            final int size = terminalList.size();
            final int count = size <= 10 ? 1 : ((size - size % 10) / 10 + (size % 10 == 0 ? 0 : 1));
            getHVInputTerminalStateCommunications = new BluetoothTalk[count];
            for (int i = 0; i < count; i++) {
                final int position = i;
                final ParameterSettings firstItem = terminalList.get(position * 10);
                final int length = size <= 10 ? size : (size % 10 == 0 ? 10 : ((position == count - 1) ? size % 10 : 10));
                getHVInputTerminalStateCommunications[i] = new BluetoothTalk() {
                    @Override
                    public void beforeSend() {
                        this.setSendBuffer(SerialUtility.crc16("0103"
                                + ParseSerialsUtils.getCalculatedCode(firstItem)
                                + String.format("%04x", length)));
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
                            byte[] data = SerialUtility.trimEnd(getReceivedBuffer());
                            short bytesLength = ByteBuffer.wrap(new byte[]{data[2], data[3]}).getShort();
                            if (length * 2 == bytesLength) {
                                List<ParameterSettings> tempList = new ArrayList<ParameterSettings>();
                                for (int j = 0; j < length; j++) {
                                    if (position * 10 + j < terminalList.size()) {
                                        ParameterSettings item = terminalList.get(position * 10 + j);
                                        byte[] tempData = SerialUtility.crc16("01030002"
                                                + SerialUtility.byte2HexStr(new byte[]{data[4 + j * 2], data[5 + j * 2]}));
                                        item.setReceived(tempData);
                                        tempList.add(item);
                                    }
                                }
                                ObjectListHolder holder = new ObjectListHolder();
                                holder.setParameterSettingsList(tempList);
                                return holder;
                            }
                        }
                        return null;
                    }
                };
            }
        }
        if (BluetoothTool.getInstance().isPrepared()) {
            isSyncing = true;
            getHVInputTerminalStateHandler.sendCount = getHVInputTerminalStateCommunications.length;
            BluetoothTool.getInstance()
                    .setHandler(getHVInputTerminalStateHandler)
                    .setCommunications(getHVInputTerminalStateCommunications)
                    .startTask();
        }
    }

    /**
     * �鿴�������״̬
     *
     * @param index List view item index
     */
    public void viewInputTerminalStatus(int index) {
        showTerminalStatusDialog(showStateList.get(index));
        isSyncing = false;
        getInputTerminalValueHandler.index = index;
        currentTask = GET_INPUT_TERMINAL_VALUE;
    }

    /**
     * ��ȡ�������ֵ
     */
    public void getInputTerminalValue() {
        if (getInputTerminalValueCommunications == null) {
            List<RealTimeMonitor> monitorList = RealTimeMonitorDao.findAllByStateID(parentActivity,
                    ApplicationConfig.MonitorStateCode[5]);
            Collections.sort(monitorList, new SortComparator());
            int size = monitorList.size();
            getInputTerminalValueCommunications = new BluetoothTalk[size];
            for (int index = 0; index < size; index++) {
                final String code = monitorList.get(index).getCode();
                final RealTimeMonitor monitor = monitorList.get(index);
                getInputTerminalValueCommunications[index] = new BluetoothTalk() {
                    @Override
                    public void beforeSend() {
                        this.setSendBuffer(SerialUtility.crc16("0103"
                                + code
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
                            monitor.setReceived(received);
                            return monitor;
                        }
                        return null;
                    }
                };
            }
        }
        if (BluetoothTool.getInstance().isPrepared()) {
            isSyncing = true;
            getInputTerminalValueHandler.sendCount = getInputTerminalValueCommunications.length;
            BluetoothTool.getInstance()
                    .setHandler(getInputTerminalValueHandler)
                    .setCommunications(getInputTerminalValueCommunications)
                    .startTask();
        }
    }

    /**
     * ��ʼ��ȡ�������״̬ͨ��
     */
    private void getInputTerminalState() {
        if (getInputTerminalStateCommunications == null) {
            final List<ParameterSettings> terminalList = ParameterSettingsDao.findByType(parentActivity,
                    ApplicationConfig.InputTerminalType);
            final int size = terminalList.size();
            final int count = size <= 10 ? 1 : ((size - size % 10) / 10 + (size % 10 == 0 ? 0 : 1));
            getInputTerminalStateCommunications = new BluetoothTalk[count];
            for (int i = 0; i < count; i++) {
                final int position = i;
                final ParameterSettings firstItem = terminalList.get(position * 10);
                final int length = size <= 10 ? size : (size % 10 == 0 ? 10 : ((position == count - 1) ? size % 10 : 10));
                getInputTerminalStateCommunications[i] = new BluetoothTalk() {
                    @Override
                    public void beforeSend() {
                        this.setSendBuffer(SerialUtility.crc16("0103"
                                + ParseSerialsUtils.getCalculatedCode(firstItem)
                                + String.format("%04x", length)));
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
                            byte[] data = SerialUtility.trimEnd(getReceivedBuffer());
                            short bytesLength = ByteBuffer.wrap(new byte[]{data[2], data[3]}).getShort();
                            if (length * 2 == bytesLength) {
                                List<ParameterSettings> tempList = new ArrayList<ParameterSettings>();
                                for (int j = 0; j < length; j++) {
                                    if (position * 10 + j < terminalList.size()) {
                                        ParameterSettings item = terminalList.get(position * 10 + j);
                                        byte[] tempData = SerialUtility.crc16("01030002"
                                                + SerialUtility.byte2HexStr(new byte[]{data[4 + j * 2], data[5 + j * 2]}));
                                        item.setReceived(tempData);
                                        tempList.add(item);
                                    }
                                }
                                ObjectListHolder holder = new ObjectListHolder();
                                holder.setParameterSettingsList(tempList);
                                return holder;
                            }
                        }
                        return null;
                    }
                };
            }
        }
        if (BluetoothTool.getInstance().isPrepared()) {
            isSyncing = true;
            getInputTerminalStateHandler.sendCount = getInputTerminalStateCommunications.length;
            BluetoothTool.getInstance()
                    .setHandler(getInputTerminalStateHandler)
                    .setCommunications(getInputTerminalStateCommunications)
                    .startTask();
        }
    }

    /**
     * �鿴�������״̬
     *
     * @param index RealTimeMonitor index
     */
    public void viewOutputTerminalStatus(int index) {
        showTerminalStatusDialog(showStateList.get(index));
        isSyncing = false;
        getOutputTerminalValueHandler.index = index;
        currentTask = GET_OUTPUT_TERMINAL_VALUE;
    }

    /**
     * ��ȡ�������ֵ
     */
    public void getOutputTerminalValue() {
        if (getOutputTerminalValueCommunications == null) {
            List<RealTimeMonitor> monitorList = RealTimeMonitorDao.findAllByStateID(parentActivity,
                    ApplicationConfig.MonitorStateCode[6]);
            Collections.sort(monitorList, new SortComparator());
            int size = monitorList.size();
            getOutputTerminalValueCommunications = new BluetoothTalk[size];
            for (int index = 0; index < size; index++) {
                final String code = monitorList.get(index).getCode();
                final RealTimeMonitor monitor = monitorList.get(index);
                getOutputTerminalValueCommunications[index] = new BluetoothTalk() {
                    @Override
                    public void beforeSend() {
                        this.setSendBuffer(SerialUtility.crc16("0103"
                                + code
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
                            monitor.setReceived(received);
                            return monitor;
                        }
                        return null;
                    }
                };
            }
        }
        if (BluetoothTool.getInstance().isPrepared()) {
            isSyncing = true;
            getOutputTerminalValueHandler.sendCount = getOutputTerminalValueCommunications.length;
            BluetoothTool.getInstance()
                    .setHandler(getOutputTerminalValueHandler)
                    .setCommunications(getOutputTerminalValueCommunications)
                    .startTask();
        }
    }

    /**
     * ��ʼ��ȡ�������״̬ͨ��
     */
    private void getOutputTerminalState() {
        if (getOutputTerminalStateCommunications == null) {
            final List<ParameterSettings> terminalList = ParameterSettingsDao.findByType(parentActivity, ApplicationConfig.OutputTerminalType);
            final int size = terminalList.size();
            final int count = size <= 10 ? 1 : ((size - size % 10) / 10 + (size % 10 == 0 ? 0 : 1));
            getOutputTerminalStateCommunications = new BluetoothTalk[count];
            for (int i = 0; i < count; i++) {
                final int position = i;
                final ParameterSettings firstItem = terminalList.get(position * 10);
                final int length = size <= 10 ? size : (size % 10 == 0 ? 10 : ((position == count - 1) ? size % 10 : 10));
                getOutputTerminalStateCommunications[i] = new BluetoothTalk() {
                    @Override
                    public void beforeSend() {
                        this.setSendBuffer(SerialUtility.crc16("0103"
                                + ParseSerialsUtils.getCalculatedCode(firstItem)
                                + String.format("%04x", length)));
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
                            byte[] data = SerialUtility.trimEnd(getReceivedBuffer());
                            short bytesLength = ByteBuffer.wrap(new byte[]{data[2], data[3]}).getShort();
                            if (length * 2 == bytesLength) {
                                List<ParameterSettings> tempList = new ArrayList<ParameterSettings>();
                                for (int j = 0; j < length; j++) {
                                    if (position * 10 + j < terminalList.size()) {
                                        ParameterSettings item = terminalList.get(position * 10 + j);
                                        byte[] tempData = SerialUtility.crc16("01030002"
                                                + SerialUtility.byte2HexStr(new byte[]{data[4 + j * 2], data[5 + j * 2]}));
                                        item.setReceived(tempData);
                                        tempList.add(item);
                                    }
                                }
                                ObjectListHolder holder = new ObjectListHolder();
                                holder.setParameterSettingsList(tempList);
                                return holder;
                            }
                        }
                        return null;
                    }
                };
            }
        }
        if (BluetoothTool.getInstance().isPrepared()) {
            isSyncing = true;
            getOutputTerminalStateHandler.sendCount = getOutputTerminalStateCommunications.length;
            BluetoothTool.getInstance()
                    .setHandler(getOutputTerminalStateHandler)
                    .setCommunications(getOutputTerminalStateCommunications)
                    .startTask();
        }
    }

    /**
     * ��ȡϵͳ״̬
     */
    private void getSystemState() {
        if (getSystemStateCommunications == null) {
            final RealTimeMonitor monitor = RealTimeMonitorDao.findByStateID(parentActivity, ApplicationConfig.MonitorStateCode[12]);
            if (monitor != null) {
                getSystemStateCommunications = new BluetoothTalk[]{new BluetoothTalk() {
                    @Override
                    public void beforeSend() {
                        this.setSendBuffer(SerialUtility.crc16("0103"
                                + monitor.getCode()
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
                            monitor.setReceived(received);
                            return monitor;
                        }
                        return null;
                    }
                }};
            }
        }
        if (BluetoothTool.getInstance().isPrepared()) {
            isSyncing = true;
            getSystemStateHandler.sendCount = getSystemStateCommunications.length;
            BluetoothTool.getInstance()
                    .setHandler(getSystemStateHandler)
                    .setCommunications(getSystemStateCommunications)
                    .startTask();
        }
    }

    /**
     * �鿴ϵͳ״̬
     *
     * @param index RealTimeMonitor index
     */
    public void viewSystemTerminalStatus(int index) {
        showTerminalStatusDialog(showStateList.get(index));
        isSyncing = false;
        currentTask = GET_SYSTEM_STATE;
    }

    /**
     * ��ȡ�ζ�������״̬
     */
    private void getCeilingInputState() {
        if (getCeilingInputStateCommunications == null) {
            final RealTimeMonitor monitor = RealTimeMonitorDao.findByStateID(parentActivity, ApplicationConfig.MonitorStateCode[10]);
            if (monitor != null) {
                getCeilingInputStateCommunications = new BluetoothTalk[]{new BluetoothTalk() {
                    @Override
                    public void beforeSend() {
                        this.setSendBuffer(SerialUtility.crc16("0103"
                                + monitor.getCode()
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
                            monitor.setReceived(received);
                            return monitor;
                        }
                        return null;
                    }
                }};
            }
        }
        if (BluetoothTool.getInstance().isPrepared()) {
            isSyncing = true;
            getCeilingInputStateHandler.sendCount = getCeilingInputStateCommunications.length;
            BluetoothTool.getInstance()
                    .setHandler(getCeilingInputStateHandler)
                    .setCommunications(getCeilingInputStateCommunications)
                    .startTask();
        }
    }

    /**
     * �鿴�ζ�������״̬
     *
     * @param index RealTimeMonitor index
     */
    public void viewCeilingInputStatus(int index) {
        showTerminalStatusDialog(showStateList.get(index));
        isSyncing = false;
        currentTask = GET_CEILING_INPUT_STATE;
    }

    /**
     * ��ȡ�ζ������״̬
     */
    private void getCeilingOutputState() {
        if (getCeilingOutputStateCommunications == null) {
            final RealTimeMonitor monitor = RealTimeMonitorDao.findByStateID(parentActivity, ApplicationConfig.MonitorStateCode[11]);
            if (monitor != null) {
                getCeilingOutputStateCommunications = new BluetoothTalk[]{new BluetoothTalk() {
                    @Override
                    public void beforeSend() {
                        this.setSendBuffer(SerialUtility.crc16("0103"
                                + monitor.getCode()
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
                            monitor.setReceived(received);
                            return monitor;
                        }
                        return null;
                    }
                }};
            }
        }
        if (BluetoothTool.getInstance().isPrepared()) {
            isSyncing = true;
            getCeilingOutputStateHandler.sendCount = getCeilingOutputStateCommunications.length;
            BluetoothTool.getInstance()
                    .setHandler(getCeilingOutputStateHandler)
                    .setCommunications(getCeilingOutputStateCommunications)
                    .startTask();
        }
    }

    /**
     * �鿴�ζ������״̬
     *
     * @param index RealTimeMonitor index
     */
    public void viewCeilingOutputStatus(int index) {
        showTerminalStatusDialog(showStateList.get(index));
        isSyncing = false;
        currentTask = GET_CEILING_OUTPUT_STATE;
    }

    /**
     * ��ʾ����״̬�Ի���
     */
    private void showTerminalStatusDialog(RealTimeMonitor monitor) {
        View dialogView = parentActivity.getLayoutInflater().inflate(R.layout.terminal_status_dialog, null);
        waitTextView = (TextView) dialogView.findViewById(R.id.wait_text);
        terminalListView = (ListView) dialogView.findViewById(R.id.list_view);
        AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity,
                R.style.GlobalDialogStyle)
                .setView(dialogView)
                .setTitle(monitor.getName())
                .setPositiveButton(R.string.dialog_btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isSyncing = false;
                        currentTask = GET_MONITOR_STATE;
                        getHVInputTerminalStateHandler.statusAdapter = null;
                        getInputTerminalStateHandler.statusAdapter = null;
                        getOutputTerminalStateHandler.statusAdapter = null;
                        getSystemStateHandler.statusAdapter = null;
                        getCeilingInputStateHandler.statusAdapter = null;
                        getCeilingOutputStateHandler.statusAdapter = null;
                    }
                });
        // ����״̬��Ϣ Dialog
        AlertDialog terminalDialog = builder.create();
        terminalDialog.show();
        terminalDialog.setCancelable(false);
        terminalDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void run() {
        switch (currentTask) {
            // ��ȡʵʱ״ֵ̬
            case GET_MONITOR_STATE:
                getRealTimeMonitorState();
                break;
            // ��ȡ��ѹ�������ֵ
            case GET_HV_INPUT_TERMINAL_VALUE:
                getHVInputTerminalValue();
                break;
            // ��ȡ��ѹ�������״̬
            case GET_HV_INPUT_TERMINAL_STATE:
                getHVInputTerminalState();
                break;
            // ��ȡ�������ֵ
            case GET_INPUT_TERMINAL_VALUE:
                getInputTerminalValue();
                break;
            // ��ȡ�������״̬
            case GET_INPUT_TERMINAL_STATE:
                getInputTerminalState();
                break;
            // ��ȡ�������ֵ
            case GET_OUTPUT_TERMINAL_VALUE:
                getOutputTerminalValue();
                break;
            // ��ȡ�������״̬
            case GET_OUTPUT_TERMINAL_STATE:
                getOutputTerminalState();
                break;
            // ��ȡϵͳ״̬
            case GET_SYSTEM_STATE:
                getSystemState();
                break;
            // ��ȡ�ζ�������״̬
            case GET_CEILING_INPUT_STATE:
                getCeilingInputState();
                break;
            // ��ȡ�ζ������״̬
            case GET_CEILING_OUTPUT_STATE:
                getCeilingOutputState();
                break;
            // ��ȡ��������״̬
            case GET_ELEVATOR_STATE:
                getElevatorState();
                break;
            // �ָ���������
            case RESTORE_ELEVATOR_FACTORY:
                startRestoreElevatorFactory();
                break;
        }
    }

    // ================================ ��ѹ�������״̬ Handler  ===================================== //

    private class GetHVInputTerminalStateHandler extends UnlockHandler {

        /**
         * ���͵�ָ����
         */
        public int sendCount;

        /**
         * ���յ���ָ����
         */
        private int receiveCount;

        public RealTimeMonitor monitor;

        public ParameterStatusAdapter statusAdapter;

        private List<ObjectListHolder> holderList;

        public GetHVInputTerminalStateHandler(Activity activity) {
            super(activity);
        }

        @Override
        public void onMultiTalkBegin(Message msg) {
            super.onMultiTalkBegin(msg);
            receiveCount = 0;
            holderList = new ArrayList<ObjectListHolder>();
        }

        @Override
        public void onMultiTalkEnd(Message msg) {
            super.onMultiTalkEnd(msg);
            if (sendCount == receiveCount) {
                if (ConfigTabFragment.this.waitTextView != null
                        && ConfigTabFragment.this.terminalListView != null) {
                    List<ParameterSettings> settingsList = new ArrayList<ParameterSettings>();
                    for (ObjectListHolder holder : holderList) {
                        settingsList.addAll(holder.getParameterSettingsList());
                    }
                    byte[] newByte = new byte[]{monitor.getReceived()[4], monitor.getReceived()[5]};
                    boolean[] bitValues = ParseSerialsUtils.getBooleanValueArray(newByte);
                    int length = bitValues.length;
                    List<ParameterStatusItem> statusList = new ArrayList<ParameterStatusItem>();
                    for (ParameterSettings settings : settingsList) {
                        int indexValue = ParseSerialsUtils.getIntFromBytes(settings.getReceived());
                        if (indexValue < length && indexValue >= 0) {
                            try {
                                JSONArray jsonArray = new JSONArray(settings.getJSONDescription());
                                int size = jsonArray.length();
                                String[] valueStringArray = new String[size];
                                for (int i = 0; i < size; i++) {
                                    JSONObject value = jsonArray.getJSONObject(i);
                                    valueStringArray[i] = indexValue + ":" + value.optString("value");
                                }
                                if (indexValue < valueStringArray.length) {
                                    ParameterStatusItem item = new ParameterStatusItem();
                                    item.setName(settings.getName().replace("����ѡ��", "����   ") + valueStringArray[indexValue]);
                                    item.setStatus(bitValues[indexValue]);
                                    if (indexValue < bitValues.length) {
                                        item.setStatus(bitValues[indexValue]);
                                    }
                                    statusList.add(item);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    // ���� AlertDialog ListView
                    if (statusAdapter == null) {
                        statusAdapter = new ParameterStatusAdapter(parentActivity, statusList);
                        ConfigTabFragment.this.terminalListView.setAdapter(statusAdapter);
                        ConfigTabFragment.this.waitTextView.setVisibility(View.GONE);
                        ConfigTabFragment.this.terminalListView.setVisibility(View.VISIBLE);
                    } else {
                        statusAdapter.setStatusList(statusList);
                    }
                }
                ConfigTabFragment.this.currentTask = GET_HV_INPUT_TERMINAL_VALUE;
            } else {
                ConfigTabFragment.this.currentTask = GET_HV_INPUT_TERMINAL_STATE;
            }
            ConfigTabFragment.this.isSyncing = false;
        }

        @Override
        public void onTalkReceive(Message msg) {
            super.onTalkReceive(msg);
            if (msg.obj != null && msg.obj instanceof ObjectListHolder) {
                holderList.add((ObjectListHolder) msg.obj);
                receiveCount++;
            }
        }
    }

    // =============================== Get Input Terminal Status Handler ======================================== //

    private class GetInputTerminalStateHandler extends UnlockHandler {

        /**
         * ���͵�ָ����
         */
        public int sendCount;

        public ParameterStatusAdapter statusAdapter;

        /**
         * ���յ���ָ����
         */
        private int receiveCount;

        public RealTimeMonitor monitor;

        private List<ObjectListHolder> holderList;

        public GetInputTerminalStateHandler(android.app.Activity activity) {
            super(activity);
            TAG = GetInputTerminalStateHandler.class.getSimpleName();
        }

        @Override
        public void onMultiTalkBegin(Message msg) {
            super.onMultiTalkBegin(msg);
            receiveCount = 0;
            holderList = new ArrayList<ObjectListHolder>();
        }

        @Override
        public void onMultiTalkEnd(Message msg) {
            super.onMultiTalkEnd(msg);
            if (sendCount == receiveCount) {
                if (ConfigTabFragment.this.waitTextView != null
                        && ConfigTabFragment.this.terminalListView != null) {

                    List<ParameterSettings> settingsList = new ArrayList<ParameterSettings>();
                    for (ObjectListHolder holder : holderList) {
                        settingsList.addAll(holder.getParameterSettingsList());
                    }
                    boolean[] bitValues = new boolean[monitor.getCombineBytes().length * 8];
                    int dataIndex = 0;
                    for (byte data : monitor.getCombineBytes()) {
                        boolean[] valueArray = ParseSerialsUtils.byteToBoolArray(data);
                        System.arraycopy(valueArray, 0, bitValues, dataIndex * 8, valueArray.length);
                        dataIndex++;
                    }
                    // ��������״ֵ̬
                    List<ParameterStatusItem> statusList = ParameterFactory
                            .getParameter()
                            .getInputTerminalStateList(bitValues, settingsList);
                    // ���� AlertDialog ListView
                    if (statusAdapter == null) {
                        statusAdapter = new ParameterStatusAdapter(parentActivity, statusList);
                        ConfigTabFragment.this.terminalListView.setAdapter(statusAdapter);
                        ConfigTabFragment.this.waitTextView.setVisibility(View.GONE);
                        ConfigTabFragment.this.terminalListView.setVisibility(View.VISIBLE);
                    } else {
                        statusAdapter.setStatusList(statusList);
                    }
                }
                ConfigTabFragment.this.currentTask = GET_INPUT_TERMINAL_VALUE;
            } else {
                ConfigTabFragment.this.currentTask = GET_INPUT_TERMINAL_STATE;
            }
            ConfigTabFragment.this.isSyncing = false;
        }

        @Override
        public void onTalkReceive(Message msg) {
            super.onTalkReceive(msg);
            if (msg.obj != null && msg.obj instanceof ObjectListHolder) {
                holderList.add((ObjectListHolder) msg.obj);
                receiveCount++;
            }
        }
    }

    // ================================= Get Output Terminal Status Handler ======================================= //

    private class GetOutputTerminalStateHandler extends UnlockHandler {

        public int sendCount;

        private int receiveCount;

        public RealTimeMonitor monitor;

        private List<ObjectListHolder> holderList;

        public ParameterStatusAdapter statusAdapter;

        public GetOutputTerminalStateHandler(android.app.Activity activity) {
            super(activity);
            TAG = GetInputTerminalStateHandler.class.getSimpleName();
        }

        @Override
        public void onMultiTalkBegin(Message msg) {
            super.onMultiTalkBegin(msg);
            receiveCount = 0;
            holderList = new ArrayList<ObjectListHolder>();
        }

        @Override
        public void onMultiTalkEnd(Message msg) {
            super.onMultiTalkEnd(msg);
            if (sendCount == receiveCount) {
                List<ParameterSettings> settingsList = new ArrayList<ParameterSettings>();
                for (ObjectListHolder holder : holderList) {
                    settingsList.addAll(holder.getParameterSettingsList());
                }
                if (ConfigTabFragment.this.waitTextView != null
                        && ConfigTabFragment.this.terminalListView != null) {
                    boolean[] bitValues = new boolean[monitor.getCombineBytes().length * 8];
                    int dataIndex = 0;
                    for (byte data : monitor.getCombineBytes()) {
                        boolean[] valueArray = ParseSerialsUtils.byteToBoolArray(data);
                        System.arraycopy(valueArray, 0, bitValues, dataIndex * 8, valueArray.length);
                        dataIndex++;
                    }
                    List<ParameterStatusItem> statusList = ParameterFactory
                            .getParameter()
                            .getOutputTerminalStateList(bitValues, settingsList);
                    // ���� AlertDialog ListView
                    if (statusAdapter == null) {
                        statusAdapter = new ParameterStatusAdapter(parentActivity, statusList);
                        ConfigTabFragment.this.terminalListView.setAdapter(statusAdapter);
                        ConfigTabFragment.this.waitTextView.setVisibility(View.GONE);
                        ConfigTabFragment.this.terminalListView.setVisibility(View.VISIBLE);
                    } else {
                        statusAdapter.setStatusList(statusList);
                    }
                }
                ConfigTabFragment.this.currentTask = GET_OUTPUT_TERMINAL_VALUE;
            } else {
                ConfigTabFragment.this.currentTask = GET_OUTPUT_TERMINAL_STATE;
            }
            ConfigTabFragment.this.isSyncing = false;
        }

        @Override
        public void onTalkReceive(Message msg) {
            super.onTalkError(msg);
            if (msg.obj != null && msg.obj instanceof ObjectListHolder) {
                holderList.add((ObjectListHolder) msg.obj);
                receiveCount++;
            }
        }
    }

    // ================================= Get HV Input Terminal Value Handler ===================================== //

    private class GetHVInputTerminalValueHandler extends UnlockHandler {

        public int sendCount;

        public int index;

        private int receiveCount;

        private RealTimeMonitor monitor;

        public GetHVInputTerminalValueHandler(Activity activity) {
            super(activity);
        }

        @Override
        public void onMultiTalkBegin(Message msg) {
            super.onMultiTalkBegin(msg);
            receiveCount = 0;
            monitor = null;
        }

        @Override
        public void onMultiTalkEnd(Message msg) {
            super.onMultiTalkEnd(msg);
            if (sendCount == receiveCount && monitor != null) {
                getHVInputTerminalStateHandler.monitor = monitor;
                ConfigTabFragment.this.currentTask = GET_HV_INPUT_TERMINAL_STATE;
            } else {
                ConfigTabFragment.this.currentTask = GET_HV_INPUT_TERMINAL_VALUE;
            }
            ConfigTabFragment.this.isSyncing = false;
        }

        @Override
        public void onTalkReceive(Message msg) {
            super.onTalkReceive(msg);
            if (msg.obj != null && msg.obj instanceof RealTimeMonitor) {
                monitor = (RealTimeMonitor) msg.obj;
                receiveCount++;
            }
        }
    }

    // ================================= Get Input Terminal Value Handler ======================================= //

    private class GetInputTerminalValueHandler extends UnlockHandler {

        public int sendCount;

        public int index;

        private int receiveCount;

        private List<RealTimeMonitor> monitorList = new ArrayList<RealTimeMonitor>();

        public GetInputTerminalValueHandler(Activity activity) {
            super(activity);
        }

        @Override
        public void onMultiTalkBegin(Message msg) {
            super.onMultiTalkBegin(msg);
            receiveCount = 0;
            monitorList = new ArrayList<RealTimeMonitor>();
        }

        @Override
        public void onMultiTalkEnd(Message msg) {
            super.onMultiTalkEnd(msg);
            if (sendCount == receiveCount) {
                if (monitorList.size() > 0) {
                    RealTimeMonitor monitor = monitorList.get(monitorList.size() - 1);
                    monitor.setCombineBytes(ConfigurationHandler.getCombineBytes(monitorList));
                    getInputTerminalStateHandler.monitor = monitor;
                    ConfigTabFragment.this.currentTask = GET_INPUT_TERMINAL_STATE;
                }
            } else {
                ConfigTabFragment.this.currentTask = GET_INPUT_TERMINAL_VALUE;
            }
            ConfigTabFragment.this.isSyncing = false;
        }

        @Override
        public void onTalkReceive(Message msg) {
            super.onTalkReceive(msg);
            if (msg.obj != null && msg.obj instanceof RealTimeMonitor) {
                monitorList.add((RealTimeMonitor) msg.obj);
                receiveCount++;
            }
        }
    }

    // ================================= Get Output Terminal Value Handler ====================================== //

    private class GetOutputTerminalValueHandler extends UnlockHandler {

        public int sendCount;

        public int index;

        private int receiveCount;

        private List<RealTimeMonitor> monitorList = new ArrayList<RealTimeMonitor>();

        public GetOutputTerminalValueHandler(Activity activity) {
            super(activity);
        }

        @Override
        public void onMultiTalkBegin(Message msg) {
            super.onMultiTalkBegin(msg);
            receiveCount = 0;
            monitorList = new ArrayList<RealTimeMonitor>();
        }

        @Override
        public void onMultiTalkEnd(Message msg) {
            super.onMultiTalkEnd(msg);
            if (sendCount == receiveCount) {
                if (monitorList.size() > 0) {
                    RealTimeMonitor monitor = monitorList.get(monitorList.size() - 1);
                    monitor.setCombineBytes(ConfigurationHandler.getCombineBytes(monitorList));
                    getOutputTerminalStateHandler.monitor = monitor;
                    ConfigTabFragment.this.currentTask = GET_OUTPUT_TERMINAL_STATE;
                }
            } else {
                ConfigTabFragment.this.currentTask = GET_OUTPUT_TERMINAL_VALUE;
            }
            ConfigTabFragment.this.isSyncing = false;
        }

        @Override
        public void onTalkReceive(Message msg) {
            super.onTalkReceive(msg);
            if (msg.obj != null && msg.obj instanceof RealTimeMonitor) {
                monitorList.add((RealTimeMonitor) msg.obj);
                receiveCount++;
            }
        }
    }

    // ============================================ Get elevator state handler =================================== //

    private class ElevatorStatusHandler extends UnlockHandler {

        private RealTimeMonitor monitor;

        public ElevatorStatusHandler(Activity activity) {
            super(activity);
        }

        @Override
        public void onMultiTalkBegin(Message msg) {
            super.onMultiTalkBegin(msg);
            monitor = null;
        }

        @Override
        public void onTalkReceive(Message msg) {
            super.onTalkReceive(msg);
            if (msg.obj != null && msg.obj instanceof RealTimeMonitor) {
                monitor = (RealTimeMonitor) msg.obj;
                // ��ȡ����������״̬
                ConfigTabFragment.this.onGetElevatorState(monitor);
            }
        }

        @Override
        public void onMultiTalkEnd(Message msg) {
            super.onMultiTalkEnd(msg);
        }
    }

    // ============================================= Restore factory handler ===================================== //

    private class RestoreFactoryHandler extends UnlockHandler {

        public RestoreFactoryHandler(Activity activity) {
            super(activity);
        }

        @Override
        public void onTalkReceive(Message msg) {
            super.onTalkReceive(msg);
            if (msg.obj != null && msg.obj instanceof RealTimeMonitor) {
                RealTimeMonitor monitor = (RealTimeMonitor) msg.obj;
                String valueString = SerialUtility.byte2HexStr(monitor.getReceived());
                boolean writeSuccessful = true;
                String result = ParseSerialsUtils.getErrorString(valueString);
                if (result != null) {
                    writeSuccessful = false;
                }
                final String tips;
                if (writeSuccessful) {
                    tips = getResources().getString(R.string.restore_factory_successful);
                } else {
                    tips = getResources().getString(R.string.restore_factory_failed);
                }
                parentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(parentActivity, tips, Toast.LENGTH_SHORT).show();
                    }
                });
                isSyncing = false;
                currentTask = NO_TASK;
            }
        }
    }

    // ====================================== Get system state handler ========================================= //

    private class GetSystemStateHandler extends UnlockHandler {

        public int sendCount;

        private int receiveCount;

        private RealTimeMonitor monitor;

        public ParameterStatusAdapter statusAdapter;

        public GetSystemStateHandler(Activity activity) {
            super(activity);
        }

        @Override
        public void onMultiTalkBegin(Message msg) {
            super.onMultiTalkBegin(msg);
            receiveCount = 0;
        }

        @Override
        public void onTalkReceive(Message msg) {
            super.onTalkReceive(msg);
            if (msg.obj != null && msg.obj instanceof RealTimeMonitor) {
                receiveCount = 1;
                monitor = (RealTimeMonitor) msg.obj;
            }
        }

        @Override
        public void onMultiTalkEnd(Message msg) {
            super.onMultiTalkEnd(msg);
            if (sendCount == receiveCount && monitor != null) {
                byte[] data = monitor.getReceived();
                List<ParameterStatusItem> statusList = new ArrayList<ParameterStatusItem>();
                try {
                    JSONArray valuesArray = new JSONArray(monitor.getJSONDescription());
                    int size = valuesArray.length();
                    Pattern pattern = Pattern.compile("^\\d*\\-\\d*:.*", Pattern.CASE_INSENSITIVE);
                    for (int i = 0; i < size; i++) {
                        JSONObject value = valuesArray.getJSONObject(i);
                        ParameterStatusItem status = new ParameterStatusItem();
                        for (Iterator iterator = value.keys(); iterator.hasNext(); ) {
                            String name = (String) iterator.next();
                            if (name.equalsIgnoreCase("value")) {
                                if (!value.optString("value").contains(ApplicationConfig.RETAIN_NAME)) {
                                    status.setName(value.optString("value"));
                                }
                            }
                            if (name.equalsIgnoreCase("id")) {
                                status.setStatus(ParseSerialsUtils
                                        .getIntValueFromBytesInSection(new byte[]{data[4], data[5]},
                                                new int[]{Integer.parseInt(value.optString("id"))}) == 1);
                            }
                            if (pattern.matcher(name).matches()) {
                                String[] intStringArray = name.split(":")[0].split("-");
                                status.setName(name.replaceAll("\\d*\\-\\d*:", ""));
                                JSONArray subArray = value.optJSONArray(name);
                                int intValue = ParseSerialsUtils
                                        .getIntValueFromBytesInSection(new byte[]{data[4], data[5]}, new int[]{
                                                Integer.parseInt(intStringArray[0]),
                                                Integer.parseInt(intStringArray[1])
                                        });
                                int subArraySize = subArray.length();
                                for (int j = 0; j < subArraySize; j++) {
                                    int index = Integer.parseInt(subArray.getJSONObject(j).optString("id"));
                                    if (index == intValue) {
                                        status.setStatusString(subArray.getJSONObject(j).optString("value"));
                                    }
                                }
                            }
                        }
                        if (status.getName() != null) {
                            statusList.add(status);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (statusAdapter == null) {
                    statusAdapter = new ParameterStatusAdapter(parentActivity, statusList);
                    ConfigTabFragment.this.terminalListView.setAdapter(statusAdapter);
                    ConfigTabFragment.this.waitTextView.setVisibility(View.GONE);
                    ConfigTabFragment.this.terminalListView.setVisibility(View.VISIBLE);
                } else {
                    statusAdapter.setStatusList(statusList);
                }
            }
            ConfigTabFragment.this.currentTask = GET_SYSTEM_STATE;
            ConfigTabFragment.this.isSyncing = false;
        }
    }


    // =================================== Get ceiling input state handler ===================================== //

    private class GetCeilingInputStateHandler extends UnlockHandler {

        public int sendCount;

        private int receiveCount;

        private RealTimeMonitor monitor;

        public ParameterStatusAdapter statusAdapter;

        public GetCeilingInputStateHandler(Activity activity) {
            super(activity);
        }

        @Override
        public void onMultiTalkBegin(Message msg) {
            super.onMultiTalkBegin(msg);
            receiveCount = 0;
        }

        @Override
        public void onTalkReceive(Message msg) {
            super.onTalkReceive(msg);
            if (msg.obj != null && msg.obj instanceof RealTimeMonitor) {
                receiveCount = 1;
                monitor = (RealTimeMonitor) msg.obj;
            }
        }


        @Override
        public void onMultiTalkEnd(Message msg) {
            super.onMultiTalkEnd(msg);
            if (sendCount == receiveCount && monitor != null) {
                byte[] data = monitor.getReceived();
                List<ParameterStatusItem> statusList = new ArrayList<ParameterStatusItem>();
                boolean[] booleanArray = ParseSerialsUtils.getBooleanValueArray(new byte[]{data[4], data[5]});
                int bitsSize = booleanArray.length;
                try {
                    JSONArray valuesArray = new JSONArray(monitor.getJSONDescription());
                    int size = valuesArray.length();
                    for (int i = 0; i < size; i++) {
                        JSONObject value = valuesArray.getJSONObject(i);
                        if (i < bitsSize) {
                            if (!value.optString("value").contains(ApplicationConfig.RETAIN_NAME)) {
                                ParameterStatusItem status = new ParameterStatusItem();
                                status.setName(value.optString("value"));
                                status.setStatus(booleanArray[Integer.parseInt(value.optString("id"))]);
                                statusList.add(status);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (statusAdapter == null) {
                    statusAdapter = new ParameterStatusAdapter(parentActivity, statusList);
                    ConfigTabFragment.this.terminalListView.setAdapter(statusAdapter);
                    ConfigTabFragment.this.waitTextView.setVisibility(View.GONE);
                    ConfigTabFragment.this.terminalListView.setVisibility(View.VISIBLE);
                } else {
                    statusAdapter.setStatusList(statusList);
                }
            }
            ConfigTabFragment.this.currentTask = GET_CEILING_INPUT_STATE;
            ConfigTabFragment.this.isSyncing = false;
        }
    }

    // =================================== Get ceiling output state handler ==================================== //

    private class GetCeilingOutputStateHandler extends UnlockHandler {

        public int sendCount;

        private int receiveCount;

        private RealTimeMonitor monitor;

        public ParameterStatusAdapter statusAdapter;

        public GetCeilingOutputStateHandler(Activity activity) {
            super(activity);
        }

        @Override
        public void onMultiTalkBegin(Message msg) {
            super.onMultiTalkBegin(msg);
            receiveCount = 0;
        }

        @Override
        public void onTalkReceive(Message msg) {
            super.onTalkReceive(msg);
            if (msg.obj != null && msg.obj instanceof RealTimeMonitor) {
                receiveCount = 1;
                monitor = (RealTimeMonitor) msg.obj;
            }
        }

        @Override
        public void onMultiTalkEnd(Message msg) {
            super.onMultiTalkEnd(msg);
            if (sendCount == receiveCount && monitor != null) {
                byte[] data = monitor.getReceived();
                List<ParameterStatusItem> statusList = new ArrayList<ParameterStatusItem>();
                boolean[] booleanArray = ParseSerialsUtils.getBooleanValueArray(new byte[]{data[4], data[5]});
                int bitsSize = booleanArray.length;
                try {
                    JSONArray valuesArray = new JSONArray(monitor.getJSONDescription());
                    int size = valuesArray.length();
                    for (int i = 0; i < size; i++) {
                        JSONObject value = valuesArray.getJSONObject(i);
                        if (i < bitsSize) {
                            if (!value.optString("value").contains(ApplicationConfig.RETAIN_NAME)) {
                                ParameterStatusItem status = new ParameterStatusItem();
                                status.setName(value.optString("value"));
                                status.setStatus(booleanArray[Integer.parseInt(value.optString("id"))]);
                                statusList.add(status);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (statusAdapter == null) {
                    statusAdapter = new ParameterStatusAdapter(parentActivity, statusList);
                    ConfigTabFragment.this.terminalListView.setAdapter(statusAdapter);
                    ConfigTabFragment.this.waitTextView.setVisibility(View.GONE);
                    ConfigTabFragment.this.terminalListView.setVisibility(View.VISIBLE);
                } else {
                    statusAdapter.setStatusList(statusList);
                }
            }
            ConfigTabFragment.this.currentTask = GET_CEILING_OUTPUT_STATE;
            ConfigTabFragment.this.isSyncing = false;
        }
    }
}
