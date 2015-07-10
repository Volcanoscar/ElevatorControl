package com.inovance.elevatorcontrol.activities.SlideMenu.Wizard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.inovance.elevatorcontrol.R;

import butterknife.InjectView;
import butterknife.Views;

/**
 * Created by Daniel on 2015/7/5.
 */
public class WizardStartActivity extends Activity {

    @InjectView(R.id.btn_start_debug)
    Button btnStartWizard;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.wizard_title);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        setContentView(R.layout.activity_wizard_start);
        Views.inject(this);

        initActivity();
    }

    private void initActivity()
    {
        btnStartWizard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WizardStartActivity.this, WizardMainActivity.class));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}