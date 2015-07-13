package com.inovance.elevatorcontrol.activities.MainTab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inovance.elevatorcontrol.R;

/**
 * Created by Daniel on 2015/7/13.
 */
public class ConfigTabFragment2 extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //getActivity().setContentView(R.layout.activity_configuration);
        return inflater.inflate(R.layout.fragment_config_tab, container, false);
    }
}