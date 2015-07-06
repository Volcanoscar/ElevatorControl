package com.inovance.elevatorcontrol.activities.SlideMenu.Wizard;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.inovance.elevatorcontrol.R;
import com.inovance.elevatorcontrol.adapters.WizardMainAdapter;
import com.viewpagerindicator.TabPageIndicator;

import butterknife.InjectView;
import butterknife.Views;


/**
 * Created by Daniel on 2015/7/5.
 */
public class WizardMainActivity extends FragmentActivity implements Runnable{

    private static final String TAG = WizardMainActivity.class.getSimpleName();
    /**
     * µ±Ç° Viewpager Index
     */
    public int pageIndex;

    /**
     * View Pager
     */
    @InjectView(R.id.pager)
    public ViewPager pager;

    /**
     * View Pager Indicator
     */
    @InjectView(R.id.indicator)
    protected TabPageIndicator indicator;

    /**
     * View Pager Adapter
     */
    public WizardMainAdapter wizardMainAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wizard_main);
        Views.inject(this);

        wizardMainAdapter = new WizardMainAdapter(this);
        pager.setAdapter(wizardMainAdapter);
        pager.setOffscreenPageLimit(4);

        indicator.setViewPager(pager);
        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) {

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageSelected(int index) {
                pageIndex = index;
            }
        });
    }

    @Override
    public void run() {

    }
}