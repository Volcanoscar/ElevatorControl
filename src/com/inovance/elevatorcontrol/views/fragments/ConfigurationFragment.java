package com.inovance.elevatorcontrol.views.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.inovance.elevatorcontrol.R;
import com.inovance.elevatorcontrol.activities.MainTab.ConfigurationActivity;
import com.inovance.elevatorcontrol.activities.Common.ParameterDetailActivity;
import com.inovance.elevatorcontrol.activities.SlideMenu.Firmware.ParameterDownloadActivity;
import com.inovance.elevatorcontrol.activities.SlideMenu.Firmware.ParameterUploadActivity;
import com.inovance.elevatorcontrol.daos.GroupTabDao;
import com.inovance.elevatorcontrol.daos.ParameterGroupSettingsDao;
import com.inovance.elevatorcontrol.models.Configuration.SpecialistGroup;
import com.inovance.elevatorcontrol.models.GroupTab;
import com.inovance.elevatorcontrol.models.ParameterGroupSettings;
import com.inovance.elevatorcontrol.models.RealTimeMonitor;
import com.mobsandgeeks.adapters.InstantAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 电梯调试
 *
 * @author jch
 */
public class ConfigurationFragment extends Fragment {

    private static final String TAG = ConfigurationFragment.class.getSimpleName();

    private int tabIndex;

    private int layoutId;

    private Context context;

    public InstantAdapter<ParameterGroupSettings> groupAdapter;

    private ListView groupListView;

    private List<ParameterGroupSettings> groupSettingsList = new ArrayList<ParameterGroupSettings>();

    private List<GroupTab> commGroupTabList = new ArrayList<GroupTab>();

    private List<GroupTab> speciaGroupTabList = new ArrayList<GroupTab>();

    private ListView commonListView;

    private ListView speciaListView;


    /**
     * 记录下当前选中的tabIndex
     *
     * @param tabIndex tab index
     * @param ctx      context
     * @return fragment
     */
    public static ConfigurationFragment newInstance(int tabIndex, Context context, List<RealTimeMonitor> monitorList) {
        ConfigurationFragment configurationFragment = new ConfigurationFragment();
        configurationFragment.tabIndex = tabIndex;
        configurationFragment.context = context;
        int layout = R.layout.fragment_not_found;
        switch (tabIndex) {
            case 0:
                layout = R.layout.configuration_sub_common;
                break;
            case 1:
                layout = R.layout.configuration_sub_specialist;
                break;
            case 2:
                layout = R.layout.configuration_tab_setting;
                break;
        }
        configurationFragment.layoutId = layout;
        return configurationFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        View view = getLayoutInflater(savedInstanceState).inflate(layoutId, container, false);
        switch (tabIndex) {
            case 0:
                commonListView = (ListView)view.findViewById(R.id.common_list);
                initCommonListView();
                break;
            case 1:
                speciaListView = (ListView)view.findViewById(R.id.spec_list);
                initSpecialistListView();
                break;
            case 2:
                groupListView = (ListView) view.findViewById(R.id.settings_list);
                initGroupListView();
                break;
        }
        return view;
    }

    private void initCommonListView() {

        commGroupTabList.clear();
        commGroupTabList.addAll(GroupTabDao.findAllCommonTab(context));
        InstantAdapter<GroupTab> instantAdapter = new InstantAdapter<GroupTab>(
                getActivity().getApplicationContext(),
                R.layout.list_configuration_setting_item, GroupTab.class,
                commGroupTabList);

        commonListView.setAdapter(instantAdapter);
        commonListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ParameterDetailActivity.class);
                intent.putExtra("SelectedId", commGroupTabList.get(position).getId());
                getActivity().startActivity(intent);
            }
        });

    }

    private void initSpecialistListView()
    {
        final List<SpecialistGroup> commGroup = SpecialistGroup
                .getSpecialistLists(ConfigurationFragment.this.getActivity());
        InstantAdapter<SpecialistGroup> instantAdapter = new InstantAdapter<SpecialistGroup>(
                getActivity().getApplicationContext(),
                R.layout.list_configuration_setting_item, SpecialistGroup.class,
                commGroup);
        speciaListView.setAdapter(instantAdapter);
        speciaListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //if (BluetoothTool.getInstance().isPrepared())
                {
                    switch (position) {
                        case 0: {
                            Intent intent = new Intent(ConfigurationFragment.this.getActivity(),
                                    ParameterUploadActivity.class);
                            ConfigurationFragment.this.getActivity().startActivity(intent);
                        }
                        break;
                        case 1: {
                            Intent intent = new Intent(ConfigurationFragment.this.getActivity(),
                                    ParameterDownloadActivity.class);
                            ConfigurationFragment.this.getActivity().startActivity(intent);
                        }
                        break;
                        case 2: {
                            final ConfigurationActivity activity = (ConfigurationActivity)
                                    ConfigurationFragment.this.getActivity();
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity,
                                    R.style.GlobalDialogStyle)
                                    .setTitle(R.string.confirm_restore_title)
                                    .setMessage(R.string.confirm_restore_message)
                                    .setNegativeButton(R.string.dialog_btn_cancel, null)
                                    .setPositiveButton(R.string.dialog_btn_ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (activity != null) {
                                                activity.restoreFactory();
                                            }
                                        }
                                    });
                            builder.create().show();
                        }
                        break;
                    }
                }
            }
        });
    }

    private void initGroupListView() {
        groupSettingsList.clear();
        groupSettingsList.addAll(ParameterGroupSettingsDao.findAll(context));
        groupAdapter = new InstantAdapter<ParameterGroupSettings>(
                getActivity().getApplicationContext(),
                R.layout.list_configuration_setting_item,
                ParameterGroupSettings.class, groupSettingsList);
        groupListView.setAdapter(groupAdapter);
        groupListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ParameterDetailActivity.class);
                intent.putExtra("SelectedId", groupSettingsList.get(position).getId());
                getActivity().startActivity(intent);
            }
        });
    }

//    public void reloadDataSource(List<RealTimeMonitor> items) {
//        monitorList.clear();
//        monitorList.addAll(items);
//        monitorAdapter.notifyDataSetChanged();
//    }

    public void reloadDataSource() {
        groupSettingsList.clear();
        groupSettingsList.addAll(ParameterGroupSettingsDao.findAll(context));
        groupAdapter.notifyDataSetChanged();
    }
}
