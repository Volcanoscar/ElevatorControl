package com.inovance.elevatorcontrol.activities.StartUp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.inovance.elevatorcontrol.R;
import com.inovance.elevatorcontrol.config.ApplicationConfig;
import com.inovance.elevatorcontrol.models.User;
import com.inovance.elevatorcontrol.utils.ParseSerialsUtils;
import com.inovance.elevatorcontrol.web.WebInterface;

import org.json.JSONArray;
import org.json.JSONException;

import butterknife.InjectView;
import butterknife.Views;

/**
 * Created by IntelliJ IDEA.
 * User: keith.
 * Date: 14-4-10.
 * Time: 11:23.
 */
public class RegisterUserActivity extends Activity {

    private static final String TAG = RegisterUserActivity.class.getSimpleName();

    @InjectView(R.id.user_name)
    EditText userName;

    @InjectView(R.id.company)
    EditText company;

    @InjectView(R.id.cell_phone)
    EditText cellPhone;

    @InjectView(R.id.tel_phone)
    EditText telPhone;

    @InjectView(R.id.email)
    EditText email;

    @InjectView(R.id.submit)
    LinearLayout submitButton;

    @InjectView(R.id.submit_progress)
    ProgressBar submitProgress;

    @InjectView(R.id.submit_text)
    TextView submitTextView;

    @InjectView(R.id.error_text)
    TextView errorTextView;

    private static final int REQUEST_BLUETOOTH_ENABLE = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.activity_open_animation, R.anim.activity_close_animation);
        setContentView(R.layout.activity_register_user_layout);
        setTitle(R.string.register_user_text);
        Views.inject(this);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = telephonyManager.getLine1Number();
        if (phoneNumber != null) {
            phoneNumber.replace("-", "");
            phoneNumber.replace("(", "");
            phoneNumber.replace(")", "");
            cellPhone.setText(phoneNumber);
        }
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitRegisterRequest();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        WebInterface.getInstance().removeListener();
        overridePendingTransition(R.anim.activity_open_animation, R.anim.activity_close_animation);
    }

    @Override
    protected void onStop() {
        super.onStop();
        WebInterface.getInstance().removeListener();
    }

    /**
     * 提交用户注册
     */
    private void submitRegisterRequest() {
        if (validateUserInputInformation()) {
            submitProgress.setVisibility(View.VISIBLE);
            submitTextView.setVisibility(View.GONE);
            User user = new User();
            user.setName(userName.getText().toString());
            user.setCompany(company.getText().toString());
            user.setCellPhone(cellPhone.getText().toString());
            user.setTelephone(telPhone.getText().toString());
            user.setEmail(email.getText().toString());
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            user.setBluetoothAddress(bluetoothAdapter.getAddress());
            WebInterface.getInstance().setOnRequestListener(new WebInterface.OnRequestListener() {
                @Override
                public void onResult(String tag, String responseString) {
                    if (tag.equalsIgnoreCase(ApplicationConfig.RegisterUser)) {
                        try {
                            JSONArray jsonArray = new JSONArray(responseString);
                            submitProgress.setVisibility(View.GONE);
                            submitTextView.setVisibility(View.VISIBLE);
                            submitButton.setEnabled(false);
                            Toast.makeText(RegisterUserActivity.this,
                                    R.string.regist_successful_wait_text,
                                    Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            submitProgress.setVisibility(View.GONE);
                            submitTextView.setVisibility(View.VISIBLE);
                            errorTextView.setText(responseString);
                        }
                    }
                }

                @Override
                public void onFailure(int statusCode, Throwable throwable) {
                    Toast.makeText(RegisterUserActivity.this, R.string.register_failed_text, Toast.LENGTH_SHORT)
                            .show();
                    submitProgress.setVisibility(View.GONE);
                    submitTextView.setVisibility(View.VISIBLE);
                }
            });
            WebInterface.getInstance().registerUser(this, user);
        }
    }

    /**
     * 验证用户注册信息
     *
     * @return 验证结果
     */
    private boolean validateUserInputInformation() {
        boolean userNameCheck = userName.getText().toString().length() > 0 && userName.getText().toString().length() <= 6;
        boolean companyCheck = company.getText().toString().length() > 0;
        boolean cellPhoneCheck = cellPhone.getText().toString().length() > 0;
        boolean emailCheck = ParseSerialsUtils.isValidEmail(email.getText().toString());
        boolean isValidated = true;
        String validateResult = "";
        if (!userNameCheck) {
            isValidated = false;
            validateResult += getResources().getString(R.string.user_name_error) + "\n";
        }
        if (!companyCheck) {
            isValidated = false;
            validateResult += getResources().getString(R.string.company_name_error) + "\n";
        }
        if (!cellPhoneCheck) {
            isValidated = false;
            validateResult += getResources().getString(R.string.cellphone_error) + "\n";
        }
        if (!emailCheck) {
            isValidated = false;
            validateResult += getResources().getString(R.string.email_address_error) + "\n";
        }
        if (!isValidated) {
            Toast.makeText(this, validateResult.trim(), Toast.LENGTH_SHORT)
                    .show();
        }
        return isValidated;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BLUETOOTH_ENABLE) {
            if (resultCode == RESULT_CANCELED) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

}