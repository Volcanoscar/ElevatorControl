package com.inovance.elevatorcontrol.views.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.inovance.bluetoothtool.BluetoothTool;
import com.inovance.elevatorcontrol.R;
import com.inovance.elevatorcontrol.activities.CheckAuthorizationActivity;
import com.inovance.elevatorcontrol.adapters.CheckedListViewAdapter;
import com.inovance.elevatorcontrol.adapters.DialogSwitchListViewAdapter;
import com.inovance.elevatorcontrol.config.ApplicationConfig;
import com.inovance.elevatorcontrol.factory.ParameterFactory;
import com.inovance.elevatorcontrol.models.ParameterSettings;
import com.inovance.elevatorcontrol.models.ParameterStatusItem;
import com.inovance.elevatorcontrol.utils.ParseSerialsUtils;
import com.inovance.elevatorcontrol.web.WebInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UtilsDialog {

    // ================================== Parameter Detail Dialog =========================================== //

    public static AlertDialog.Builder parameterDetailDialog(final Activity activity,
                                                            final ParameterSettings settings) {
        // 单选弹出框
        if (settings.getDescriptionType() == ApplicationConfig.DESCRIPTION_TYPE[1]) {
            try {
                JSONArray jsonArray = new JSONArray(settings.getJSONDescription());
                int size = jsonArray.length();
                String[] statusList = new String[size];
                String[] spinnerList = new String[size];
                int type = Integer.parseInt(settings.getType());
                // Fix list view checked index
                int[] indexStatus = ParameterFactory.getParameter().getIndexStatus(settings);
                for (int i = 0; i < size; i++) {
                    JSONObject value = jsonArray.getJSONObject(i);
                    int idValue = Integer.parseInt(value.optString("id"));
                    int alwaysClose = ParameterFactory.getParameter().getAlwaysCloseValue(idValue);
                    if (indexStatus[0] == value.optInt("id")) {
                        int temp = indexStatus[1];
                        indexStatus = new int[]{i, temp};
                    }
                    if (type == ApplicationConfig.InputTerminalType || type == ApplicationConfig.FloorShowType) {
                        if (i == 0) {
                            statusList[i] = value.optString("id") + ":" + value.optString("value");
                        } else {
                            statusList[i] = value.optString("id") + "/" + alwaysClose + ":" + value.optString("value");
                        }
                    } else {
                        statusList[i] = value.optString("id") + ":" + value.optString("value");
                    }
                    spinnerList[i] = value.optString("value");
                }
                // F5 组输入端子
                if (Integer.parseInt(settings.getType()) == ApplicationConfig.InputTerminalType) {
                    View dialogView = activity.getLayoutInflater()
                            .inflate(R.layout.parameter_terminal_status_dialog, null);
                    final ListView listView = (ListView) dialogView.findViewById(R.id.list_view);
                    TextView defaultValue = (TextView) dialogView.findViewById(R.id.default_value);
                    final ToggleButton toggleButton = (ToggleButton) dialogView.findViewById(R.id.toggle_button);
                    defaultValue.setText("出厂值: " + settings.getDefaultValue());
                    // Get real index and toggle button state
                    toggleButton.setChecked(indexStatus[1] == 1);
                    final CheckedListViewAdapter adapter = new CheckedListViewAdapter(activity, statusList, indexStatus[0]);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                            listView.setSelection(position);
                            adapter.setCheckedIndex(position);
                        }
                    });
                    return new AlertDialog.Builder(activity, R.style.GlobalDialogStyle)
                            .setView(dialogView)
                            .setTitle(settings.getCodeText() + " " + settings.getName());
                } else if (Integer.parseInt(settings.getType()) == ApplicationConfig.FloorShowType) {
                    View dialogView = activity.getLayoutInflater()
                            .inflate(R.layout.parameter_type25_dialog, null);
                    Spinner modSpinner = (Spinner) dialogView.findViewById(R.id.mod_value);
                    Spinner remSpinner = (Spinner) dialogView.findViewById(R.id.rem_value);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
                            android.R.layout.simple_spinner_item,
                            spinnerList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    modSpinner.setAdapter(adapter);
                    remSpinner.setAdapter(adapter);
                    int value = ParseSerialsUtils.getIntFromBytes(settings.getReceived());
                    int modValue = value / 100;
                    int remValue = value % 100;
                    if (modValue < statusList.length && remValue < statusList.length) {
                        modSpinner.setSelection(modValue);
                        remSpinner.setSelection(remValue);
                    }
                    return new AlertDialog.Builder(activity, R.style.GlobalDialogStyle)
                            .setView(dialogView)
                            .setTitle(settings.getCodeText() + " " + settings.getName());
                } else {
                    return new AlertDialog.Builder(activity, R.style.GlobalDialogStyle)
                            .setSingleChoiceItems(statusList,
                                    ParameterFactory.getParameter().getSelectedIndex(settings),
                                    null)
                            .setTitle(settings.getCodeText() + " " + settings.getName());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // 开关弹出框
        if (settings.getDescriptionType() == ApplicationConfig.DESCRIPTION_TYPE[2]) {
            View dialogView = activity.getLayoutInflater().inflate(R.layout.parameter_switch_dialog, null);
            ListView listView = (ListView) dialogView.findViewById(R.id.switch_list);
            List<ParameterStatusItem> itemList = new ArrayList<ParameterStatusItem>();
            boolean[] booleanArray = ParseSerialsUtils
                    .getBooleanValueArray(new byte[]{settings.getReceived()[4], settings.getReceived()[5]});
            boolean isSpecial = Integer.parseInt(settings.getType()) == ApplicationConfig.InputSelectType;
            boolean isInFA26ToFA37 = ParseSerialsUtils.checkInFA26ToFA37(settings);
            try {
                JSONArray jsonArray = new JSONArray(settings.getJSONDescription());
                int size = jsonArray.length();
                int length = booleanArray.length;
                for (int i = 0; i < size; i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (i < length) {
                        ParameterStatusItem item = new ParameterStatusItem();
                        item.setId(jsonObject.optString("id"));
                        item.setName(jsonObject.optString("value"));
                        item.setStatus(booleanArray[i]);
                        if (Integer.parseInt(settings.getMode()) == ApplicationConfig.modifyType[2]) {
                            item.setCanEdit(false);
                        } else {
                            item.setCanEdit(!settings.isElevatorRunning());
                        }
                        item.setSpecial(isSpecial);
                        item.setInFA26ToFA37(isInFA26ToFA37);
                        itemList.add(item);
                    }
                }
                DialogSwitchListViewAdapter adapter = new DialogSwitchListViewAdapter(itemList, activity);
                listView.setAdapter(adapter);
                return new AlertDialog.Builder(activity, R.style.GlobalDialogStyle)
                        .setView(dialogView)
                        .setTitle(settings.getCodeText() + " " + settings.getName());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new AlertDialog.Builder(activity, R.style.GlobalDialogStyle)
                .setTitle(settings.getCodeText() + " " + settings.getName())
                .setNeutralButton(R.string.dialog_btn_cancel, null);
    }

    // =========================================== Exit dialog =============================================== //

    public static AlertDialog.Builder exitDialog(final Activity activity) {
        String[] strings = activity.getResources().getStringArray(
                R.array.exit_multi_choice);
        boolean[] bStartStatus = new boolean[] { true };
        final boolean[] defaultSelectedStatus = { true };
        return new AlertDialog.Builder(activity, R.style.GlobalDialogStyle)
                //.setMessage(activity.getResources().getString(R.string.are_you_sure_exit))
                .setTitle(activity.getResources().getString(R.string.are_you_sure_exit))
                .setMultiChoiceItems(strings, bStartStatus,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which, boolean isChecked) {
                                // TODO Auto-generated method stub
                                defaultSelectedStatus[which] = isChecked;
                            }
                        })
                .setNegativeButton(activity.getResources().getString(R.string.dialog_btn_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.cancel();
                            }
                        })
                .setPositiveButton(activity.getResources().getString(R.string.dialog_btn_ok)
                        , new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        WebInterface.getInstance().removeListener();
                        BluetoothTool.getInstance().setHandler(null);
                        BluetoothTool.getInstance().kill();
                        Intent intent = new Intent(activity, CheckAuthorizationActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("Exit", true);
                        activity.startActivity(intent);
                        if (defaultSelectedStatus[0]) {
                            // 关闭蓝牙
                            BluetoothAdapter.getDefaultAdapter().disable();
                        }
                        activity.finish();
                    }
                });
    }

    // ======================================== Handler bluetooth exception dialog ================================ //

    public static interface OnRetryListener {
        void onClick();
    }

    public static void showBluetoothExceptionDialog(final Activity activity, final OnRetryListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(R.string.bluetooth_connect_exception_title)
                .setMessage(R.string.bluetooth_connect_exception_message)
                .setNegativeButton(R.string.exit_whole_application, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        WebInterface.getInstance().removeListener();
                        BluetoothTool.getInstance().setHandler(null);
                        BluetoothTool.getInstance().kill();
                        Intent intent = new Intent(activity, CheckAuthorizationActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("Exit", true);
                        activity.startActivity(intent);
                        activity.finish();
                    }
                }).setPositiveButton(R.string.retry_connect_device, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (listener != null) {
                            listener.onClick();
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
