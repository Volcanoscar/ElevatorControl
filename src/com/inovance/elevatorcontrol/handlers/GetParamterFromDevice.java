package com.inovance.elevatorcontrol.handlers;

import android.app.Activity;
import android.os.Message;

import com.inovance.bluetoothtool.BluetoothTalk;
import com.inovance.bluetoothtool.BluetoothTool;
import com.inovance.bluetoothtool.SerialUtility;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

/**
 * 从设备中获取参数组与参数列表
 * Created by inovance on 2015/8/6.
 */
public class GetParamterFromDevice {
    private GetParameterGroupHandler parameterGroupgHandler;
    private GetParameterListHandler parameterListHandler;

    private List<String> groupList = null;
    HashMap<String, List<String>> parList = new HashMap<String, List<String>>();//二维数组

    private BluetoothTalk[] getParGroupCommunication;
    private BluetoothTalk[] getParamterCommunications;

    public GetParamterFromDevice(Activity activity) {
        parameterGroupgHandler = new GetParameterGroupHandler(activity);
        parameterListHandler = new GetParameterListHandler(activity);
    }

    /**
     * 获取要显示的参数组
     */
    public void startGetParamterGroup() {
        if (getParGroupCommunication == null) {
            getParGroupCommunication = new BluetoothTalk[]{
                    new BluetoothTalk() {
                        @Override
                        public void beforeSend() {
                            this.setSendBuffer(SerialUtility.crc16("010390000001"));
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
                                int nRetCount = (data[2] << 8) | data[3];//返回字节个数,每组3个字节
                                for (int i = 0; i < nRetCount / 3; i++) {
                                    //组个数
                                    int nGroupCount = data[4 + 3 * i] & 0x0F;
                                    //组名
                                    String str = String.format("%X", (data[4 + 3 * i] >> 8) & 0x0F);
                                    int nGroupBit = (data[5 + 3 * i] << 8) | data[6 + 3 * i];
                                    for (int j = 0; j < nGroupCount; j++) {
                                        if (1 == ((nGroupBit >> j) & 0x01)) {
                                            list.add(str + String.format("%X", j));
                                        }
                                    }
                                }
                                groupList = list;
                                return list;
                            }
                            return null;
                        }
                    }
            };
        }
        if (BluetoothTool.getInstance().isPrepared()) {
            BluetoothTool.getInstance()
                    .setHandler(parameterGroupgHandler)
                    .setCommunications(getParGroupCommunication)
                    .startTask();
        }

    }

    /**
     * 获取要显示的参数组
     */
    public void startGetParamterList() {
        if (groupList == null)
            return;
        if (getParamterCommunications == null) {
            int count = groupList.size();
            getParamterCommunications = new BluetoothTalk[count];
            for (int i = 0; i < count; i++) {
                final String strGroup = groupList.get(i);
                getParamterCommunications[i] = new BluetoothTalk() {
                    @Override
                    public void beforeSend() {
                        this.setSendBuffer(SerialUtility.crc16("0103"
                                + strGroup + "FF0001"));
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
        if (BluetoothTool.getInstance().isPrepared()) {
            BluetoothTool.getInstance()
                    .setHandler(parameterListHandler)
                    .setCommunications(getParamterCommunications)
                    .startTask();
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

            }
        }

    }
}
