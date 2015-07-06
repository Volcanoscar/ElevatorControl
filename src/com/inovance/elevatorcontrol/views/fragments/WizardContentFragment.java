package com.inovance.elevatorcontrol.views.fragments;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inovance.elevatorcontrol.R;

/**
 * Created by Daniel on 2015/7/6.
 */
public class WizardContentFragment extends Fragment {
    private static final String TAG = WizardContentFragment.class.getSimpleName();

    private int tabIndex;

    private int layoutId;

    private Context context;

    /**
     * 记录下当前选中的tabIndex
     *
     * @param tabIndex tab index
     * @param ctx      context
     * @return fragment
     */
    public static WizardContentFragment newInstance(int tabIndex, Context context) {
        WizardContentFragment wizardContentFragment = new WizardContentFragment();
        wizardContentFragment.tabIndex = tabIndex;
        wizardContentFragment.context = context;
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
        wizardContentFragment.layoutId = layout;
        return wizardContentFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        View view = getLayoutInflater(savedInstanceState).inflate(layoutId, container, false);
//        switch (tabIndex) {
//            case 0:
//                commonListView = (ListView)view.findViewById(R.id.common_list);
//                initCommonListView();
//                break;
//            case 1:
//                speciaListView = (ListView)view.findViewById(R.id.spec_list);
//                initSpecialistListView();
//                break;
//            case 2:
//                groupListView = (ListView) view.findViewById(R.id.settings_list);
//                initGroupListView();
//                break;
//        }
        return view;
    }

}
