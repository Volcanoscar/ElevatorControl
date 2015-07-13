package com.inovance.elevatorcontrol.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.inovance.elevatorcontrol.R;
import com.inovance.elevatorcontrol.config.ParameterUpdateTool;
import com.inovance.elevatorcontrol.views.fragments.LeftMenuFragment;
import com.inovance.elevatorcontrol.views.slidemenu.SlidingMenu;
import com.inovance.elevatorcontrol.web.WebInterface;

/**
 * Created by Daniel on 2015/7/13.
 */
public class NavigationTabActivity2 extends FragmentActivity implements Runnable, WebInterface.OnRequestListener, ParameterUpdateTool.OnCheckResultListener  {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_tab);
        initSlidingMenu();
    }

    private SlidingMenu menu;

    private void initSlidingMenu() {
        //Slide menu for main page
        menu = new SlidingMenu(this);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeEnabled(true);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setBehindWidth(300);

        menu.setMenu(R.layout.menu_frame);
        getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame, new LeftMenuFragment()).commit();
    }
    @Override
    public void onComplete() {

    }

    @Override
    public void onFailed(Throwable throwable, String name, int type) {

    }

    @Override
    public void onResult(String tag, String responseString) {

    }

    @Override
    public void onFailure(int statusCode, Throwable throwable) {

    }

    @Override
    public void run() {

    }
}
