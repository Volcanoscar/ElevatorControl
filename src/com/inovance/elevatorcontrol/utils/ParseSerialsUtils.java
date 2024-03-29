package com.inovance.elevatorcontrol.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.inovance.bluetoothtool.SerialUtility;
import com.inovance.elevatorcontrol.config.ApplicationConfig;
import com.inovance.elevatorcontrol.config.ParameterUpdateTool;
import com.inovance.elevatorcontrol.daos.ErrorHelpDao;
import com.inovance.elevatorcontrol.factory.ParameterFactory;
import com.inovance.elevatorcontrol.models.ErrorHelp;
import com.inovance.elevatorcontrol.models.ParameterSettings;
import com.inovance.elevatorcontrol.models.RealTimeMonitor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ParseSerialsUtils {

    private static final String TAG = ParseSerialsUtils.class.getSimpleName();

    /**
     * 取得数值
     *
     * @param monitor RealTimeMonitor
     * @return Value String
     */
    public static String getValueTextFromRealTimeMonitor(RealTimeMonitor monitor) {
        byte[] data = monitor.getReceived();
        if (data != null && data.length == 8) {
            if (monitor.getDescription() != null && monitor.getDescription().length() > 0) {
                return TextLocalize.getInstance().getViewDetailText();
            }
            int value = getIntFromBytes(data);
            if (monitor.getUnit() == null || monitor.getUnit().length() <= 0) {
                if (monitor.getStateID() == ApplicationConfig.MonitorStateCode[15]) {
                    return String.format("E%02d", value);
                }
                return String.format("%d", value);
            }
            try {
                return "" + value * Integer.parseInt(monitor.getScale());
            } catch (Exception e) {
                double doubleValue = (double) value * Double.parseDouble(monitor.getScale());
                return String.format("%." + (monitor.getScale().length() - 2) + "f", doubleValue);
            }
        }
        return "";
    }

    /**
     * 1:电梯下行
     * 2:电梯上行
     * 3:停机
     *
     * @return Status
     */
    public static int getElevatorStatus(RealTimeMonitor monitor) {
        int rawValue = ParseSerialsUtils.getIntFromBytes(monitor.getReceived());
        String deviceType = ParameterUpdateTool.getInstance().getDeviceName();
        if (deviceType.equalsIgnoreCase(ApplicationConfig.NormalDeviceType[0])) {
            switch (rawValue) {
                case 0:
                    rawValue = 3;
                    break;
                case 1:
                    rawValue = 1;
                    break;
                case 2:
                    rawValue = 2;
                    break;
            }
        }
        return rawValue;
    }

    /**
     * 根据 DESCRIPTION_TYPE 取得相应的值
     *
     * @param settings ParameterSettings
     * @return Value String
     */
    @SuppressLint("DefaultLocale")
    public static String getValueTextFromParameterSetting(ParameterSettings settings) {
        byte[] data = settings.getReceived();
        if (data.length == 8) {
            int value = getIntFromBytes(data);
            if (settings.getDescriptionType() == ApplicationConfig.DESCRIPTION_TYPE[0]) {
                try {
                    return "" + value * Integer.parseInt(settings.getScale());
                } catch (Exception e) {
                    double doubleValue = (double) value * Double.parseDouble(settings.getScale());
                    return String.format("%." + (settings.getScale().length() - 2) + "f", doubleValue);
                }
            }
            if (settings.getDescriptionType() == ApplicationConfig.DESCRIPTION_TYPE[1]) {
                if (Integer.parseInt(settings.getType()) == ApplicationConfig.FloorShowType) {
                    try {
                        JSONArray jsonArray = new JSONArray(settings.getJSONDescription());
                        int size = jsonArray.length();
                        int index = getIntFromBytes(data);
                        int modValue = index / 100;
                        int remValue = index % 100;
                        if (modValue < size && remValue < size) {
                            JSONObject modObject = jsonArray.getJSONObject(modValue);
                            JSONObject remObject = jsonArray.getJSONObject(remValue);
                            return modObject.optString("value") + "  " + remObject.optString("value");
                        }
                    } catch (JSONException e) {
                        return "Parse value failed";
                    }
                } else {
                    return ParameterFactory.getParameter().getDescriptionText(settings);
                }
            }
            if (settings.getDescriptionType() == ApplicationConfig.DESCRIPTION_TYPE[2]) {
                return TextLocalize.getInstance().getViewDetailText();
            }
        }
        return "";
    }

    /**
     * 功能码: 前2位16进制 后两位10进制
     *
     * @return 统一为16进制
     */
    public static String getCalculatedCode(ParameterSettings settings) {
        String r2 = settings.getCode().substring(0, 2);
        String r4 = settings.getCode().substring(2);
        r4 = r4.replace("-", "");
        r4 = SerialUtility.int2HexStr(new int[]{
                Integer.parseInt(r4)
        });
        return r2 + r4;
    }

    /**
     * 取得十进制数
     *
     * @param data byte[]
     * @return short
     */
    @SuppressLint("GetIntFromBytes")
    public static int getIntFromBytes(byte[] data) {
        if (data.length == 7) {
            data = concatenateByteArrays(data, new byte[]{0});
        }
        if (data.length == 8) {
            return data[4] << 8 & 0xFF00 | data[5] & 0xFF;
        } else {
            return -1;
        }
    }

    /**
     * 返回指定Bit区间的int值
     *
     * @param data    byte[]
     * @param section int[]
     * @return int Value
     */
    @SuppressLint("GetIntValueFromBytesInSection")
    public static int getIntValueFromBytesInSection(byte[] data, int[] section) {
        String binaryString = "";
        for (byte subByte : data) {
            binaryString += String.format("%8s", Integer.toBinaryString(subByte & 0xFF)).replace(' ', '0');
        }
        String reverseBinaryString = reverseString(binaryString);
        if (section.length == 1) {
            String bitsString = Character.toString(reverseBinaryString.charAt(section[0]));
            return Integer.parseInt(reverseString(bitsString), 2);
        }
        if (section.length == 2) {
            String bitsString = reverseBinaryString.substring(section[0], section[1] + 1);
            return Integer.parseInt(reverseString(bitsString), 2);
        }
        return -1;
    }

    /**
     * 取得Error Code
     *
     * @param data byte[]
     * @return Error Code String
     */
    @SuppressLint("GetErrorCode")
    public static String getErrorCode(byte[] data) {
        if (data.length == 8) {
            int value = getIntFromBytes(data);
            return String.format("E%02d", value);
        }
        return "E00";
    }

    /**
     * 取得轿厢状态Code Bit 8-11
     *
     * @param monitor RealTimeMonitor
     * @return System Status Code
     */
    @SuppressLint("GetElevatorBoxStatusCode")
    public static int getElevatorBoxStatusCode(RealTimeMonitor monitor) {
        byte[] data = monitor.getReceived();
        if (data.length == 8) {
            return data[4] & 0x0f;
        }
        return -1;
    }

    @SuppressLint("GetSystemStatusCode")
    public static int getSystemStatusCode(RealTimeMonitor monitor) {
        byte[] data = monitor.getReceived();
        if (data.length == 8) {
            return (data[5] >> 4) & 0x0f;
        }
        return -1;
    }

    /**
     * 把received解析成ErrorHelp对象
     *
     * @param errorHelp ErrorHelp
     * @return ErrorHelpLog
     */
    @SuppressLint("GetErrorHelpFromErrorHelp")
    public static ErrorHelp getErrorHelpFromErrorHelp(Context ctx, ErrorHelp errorHelp) {
        byte[] data = errorHelp.getReceived();
        if (data.length == 8) {
            int value = getIntFromBytes(data);
            String display = String.format("E%02d", value);
            return ErrorHelpDao.findByDisplay(ctx, display);
        }
        return null;
    }

    /**
     * 判读字符是否是数字
     *
     * @param string Char String
     * @return Is Integer
     */
    @SuppressLint("IsInteger")
    public static boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Concatenate Byte Arrays
     *
     * @param a byte array a
     * @param b byte array b
     * @return new byte array
     */
    private static byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /**
     * Get boolean value array
     *
     * @param data byte[] data
     * @return boolean array
     */
    public static boolean[] getBooleanValueArray(byte[] data) {
        boolean[] bits = new boolean[data.length * 8];
        int count = data.length * 8;
        for (int i = 0; i < count; i++) {
            if ((data[i / 8] & (1 << (7 - (i % 8)))) > 0)
                bits[count - i - 1] = true;
        }
        return bits;
    }

    public static boolean[] byteToBoolArray(byte x) {
        boolean[] boolArr = new boolean[8];
        boolArr[0] = ((x & 0x01) != 0);
        boolArr[1] = ((x & 0x02) != 0);
        boolArr[2] = ((x & 0x04) != 0);
        boolArr[3] = ((x & 0x08) != 0);

        boolArr[4] = ((x & 0x10) != 0);
        boolArr[5] = ((x & 0x20) != 0);
        boolArr[6] = ((x & 0x40) != 0);
        boolArr[7] = ((x & 0x80) != 0);
        return boolArr;
    }

    /**
     * Reverse String
     *
     * @param inputString String To Reverse
     * @return Reverse String
     */
    private static String reverseString(String inputString) {
        String reverseBinaryString = "";
        int length = inputString.length();
        for (int i = length - 1; i >= 0; i--) {
            reverseBinaryString = reverseBinaryString + inputString.charAt(i);
        }
        return reverseBinaryString;
    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    /**
     * 转换字节大小
     *
     * @param bytes bytes
     * @return String
     */
    public static String humanReadableByteCount(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "i";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    public static boolean checkInFA26ToFA37(ParameterSettings settings) {
        String name = settings.getCode().substring(0, 2);
        String value = settings.getCode().substring(2);
        int number = Integer.parseInt(value.replace("-", ""));
        return name.equalsIgnoreCase("FA")
                && number >= ApplicationConfig.FA26ToFA37[0]
                && number <= ApplicationConfig.FA26ToFA37[1];
    }

    public static String getErrorString(String receive) {
        if (receive.contains("8001")) {
            if (receive.length() >= 12) {
                String temp = receive.substring(4, 12);
                int index = 0;
                for (String errorCode : ApplicationConfig.ERROR_CODE_ARRAY) {
                    if (temp.equalsIgnoreCase(errorCode)) {
                        return ApplicationConfig.ERROR_NAME_ARRAY[index];
                    }
                    index++;
                }
            }
        }
        return null;
    }

    public static int getErrorIndex(String receive) {
        if (receive.contains("8001")) {
            if (receive.length() >= 12) {
                String temp = receive.substring(4, 12);
                int index = 0;
                for (String errorCode : ApplicationConfig.ERROR_CODE_ARRAY) {
                    if (temp.equalsIgnoreCase(errorCode)) {
                        return index;
                    }
                    index++;
                }
            }
        }
        return -1;
    }
}
