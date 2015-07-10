package com.inovance.elevatorcontrol.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.inovance.elevatorcontrol.R;
import com.inovance.elevatorcontrol.views.fragments.LeftMenuFragment;
import com.inovance.elevatorcontrol.views.slidemenu.SlidingMenu;

/**
 * Created by Daniel on 2015/7/9.
 */
public class NavigationMainActivity extends FragmentActivity {

    private SlidingMenu menu;

    //定义FragmentTabHost对象
    private FragmentTabHost mTabHost;

    //定义一个布局
    private LayoutInflater layoutInflater;

    //定义数组来存放Fragment界面
    private Class fragmentArray[] = {FragmentPage1.class,FragmentPage2.class,FragmentPage3.class};

    //定义数组来存放按钮图片
    private int mImageViewArray[] = {R.drawable.tab_home_btn,R.drawable.tab_message_btn,R.drawable.tab_selfinfo_btn};

    //Tab选项卡的文字
    private String mTextviewArray[];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Content view for main page
        setContentView(R.layout.activity_navigation_main);
        initMainTab();

        //Slide menu for main page
        initSlidingMenu();
    }

    /**
     * 初始化组件
     */
    private void initMainTab(){
        //初始化布局
        layoutInflater = LayoutInflater.from(this);

        //初始化FragmentTabHost对象
        mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.content);
        mTextviewArray = getResources().getStringArray(R.array.navigation_tab_text);

        //加载fragment页面
        int count = fragmentArray.length;

        for(int i = 0; i < count; i++){
            //为每一个Tab按钮设置图标、文字和内容
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
            //将Tab按钮添加进Tab选项卡中
            mTabHost.addTab(tabSpec, fragmentArray[i], null);
            //设置Tab按钮的背景
            mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.selector_tab_background);
        }
    }

    /**
     * 给Tab按钮设置图标和文字
     * @param index
     * @return
     */
    private View getTabItemView(int index){
        View view = layoutInflater.inflate(R.layout.tab_item_view, null);

        ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
        imageView.setImageResource(mImageViewArray[index]);

        TextView textView = (TextView) view.findViewById(R.id.textview);
        textView.setText(mTextviewArray[index]);

        return view;
    }

    private void initSlidingMenu() {
        //Slide menu for main page
        menu = new SlidingMenu(this);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeEnabled(true);
        menu.setFadeDegree(0.35f);
        //设置滑动时拖拽效果
        menu.setBehindScrollScale(0);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setBehindWidth(300);

        menu.setMenu(R.layout.menu_frame);
        getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame, new LeftMenuFragment()).commit();
    }

    public void hideMenu()
    {
        menu.showContent();
    }
}