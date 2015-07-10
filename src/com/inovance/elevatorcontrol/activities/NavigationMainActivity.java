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

    //����FragmentTabHost����
    private FragmentTabHost mTabHost;

    //����һ������
    private LayoutInflater layoutInflater;

    //�������������Fragment����
    private Class fragmentArray[] = {FragmentPage1.class,FragmentPage2.class,FragmentPage3.class};

    //������������Ű�ťͼƬ
    private int mImageViewArray[] = {R.drawable.tab_home_btn,R.drawable.tab_message_btn,R.drawable.tab_selfinfo_btn};

    //Tabѡ�������
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
     * ��ʼ�����
     */
    private void initMainTab(){
        //��ʼ������
        layoutInflater = LayoutInflater.from(this);

        //��ʼ��FragmentTabHost����
        mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.content);
        mTextviewArray = getResources().getStringArray(R.array.navigation_tab_text);

        //����fragmentҳ��
        int count = fragmentArray.length;

        for(int i = 0; i < count; i++){
            //Ϊÿһ��Tab��ť����ͼ�ꡢ���ֺ�����
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
            //��Tab��ť��ӽ�Tabѡ���
            mTabHost.addTab(tabSpec, fragmentArray[i], null);
            //����Tab��ť�ı���
            mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.selector_tab_background);
        }
    }

    /**
     * ��Tab��ť����ͼ�������
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
        //���û���ʱ��קЧ��
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