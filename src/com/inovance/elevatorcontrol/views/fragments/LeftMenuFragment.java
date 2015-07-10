package com.inovance.elevatorcontrol.views.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inovance.elevatorcontrol.R;
import com.inovance.elevatorcontrol.activities.Common.BluetoothAddressActivity;
import com.inovance.elevatorcontrol.activities.Common.CallInsideActivity;
import com.inovance.elevatorcontrol.activities.Common.CallOutsideActivity;
import com.inovance.elevatorcontrol.activities.NavigationMainActivity;
import com.inovance.elevatorcontrol.activities.SlideMenu.Firmware.FirmwareManageActivity;
import com.inovance.elevatorcontrol.activities.SlideMenu.Help.HelpSystemActivity;
import com.inovance.elevatorcontrol.activities.SlideMenu.Wizard.WizardStartActivity;
import com.inovance.elevatorcontrol.models.SlideMenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 2015/7/9.
 */
public class LeftMenuFragment extends Fragment {

    private List<SlideMenuItem> menuItemList = new ArrayList<SlideMenuItem>();
    private Class[] menuClassList = {
                            WizardStartActivity.class, //引导调试
                            BluetoothAddressActivity.class,    //连接设备
                            CallInsideActivity.class, //招梯测试
                            CallOutsideActivity.class,    //参数拷贝 ---
                            FirmwareManageActivity.class, //程序管理
                            HelpSystemActivity.class};    //帮助

    private Integer[] menuIconList = {R.drawable.menu_wizard_32,R.drawable.menu_connect_32,
            R.drawable.menu_call_elevator_32,R.drawable.menu_copy1_32,
            R.drawable.menu_program_32,R.drawable.menu_help_32};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = getLayoutInflater(savedInstanceState).inflate(R.layout.menu_left, container, false);

        initMenus();
        MenuAdapter adapter = new MenuAdapter(getActivity(), R.layout.menu_item, menuItemList);
        ListView menuListView = (ListView) view.findViewById(R.id.left_menu_list);
        menuListView.setAdapter(adapter);
        menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                NavigationMainActivity mainActivity = (NavigationMainActivity)getActivity();
                mainActivity.hideMenu();
                SlideMenuItem item = menuItemList.get(i);
                startActivity(new Intent(getActivity(), item.getFragmentClass()));
            }
        });
        return view;
    }

    private void initMenus()
    {
        String menuNames[] = getResources().getStringArray(R.array.left_menu_text);

        int index = 0;
        for(String name : menuNames)
        {
            SlideMenuItem item = new SlideMenuItem(name, menuIconList[index%menuIconList.length], menuClassList[index]);
            menuItemList.add(item);
            index++;
        }
    }

    public class MenuAdapter extends ArrayAdapter<SlideMenuItem> {

        private int resourceId;
        public MenuAdapter(Context context, int resource, List<SlideMenuItem> objects) {
            super(context, resource, objects);
            resourceId = resource;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(resourceId, null);
            }
            ImageView icon = (ImageView) convertView.findViewById(R.id.menu_image);
            icon.setImageResource(getItem(position).getImageId());
            TextView title = (TextView) convertView.findViewById(R.id.menu_title);
            title.setText(getItem(position).getName());

            return convertView;
        }

    }
}
