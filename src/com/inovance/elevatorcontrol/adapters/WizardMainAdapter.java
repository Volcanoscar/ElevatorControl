package com.inovance.elevatorcontrol.adapters;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;

import com.inovance.elevatorcontrol.R;
import com.inovance.elevatorcontrol.views.fragments.WizardContentFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 2015/7/6.
 */
public class WizardMainAdapter extends FragmentPagerAdapter {

    private FragmentActivity fragmentActivity;

    private List<WizardContentFragment> mFragments;

    private String[] titleArray;

    public WizardMainAdapter(FragmentActivity activity) {
        super(activity.getSupportFragmentManager());
        mFragments = new ArrayList<WizardContentFragment>();
        fragmentActivity = activity;
    }

    @Override
    public WizardContentFragment getItem(int position) {
        if (position > mFragments.size() - 1) {
            WizardContentFragment wizardFragment = WizardContentFragment
                    .newInstance(position, fragmentActivity);
            mFragments.add(wizardFragment);
            return wizardFragment;
        } else {
            return mFragments.get(position);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return getTabTextArray()[position];
    }

    @Override
    public int getCount() {
        return getTabTextArray().length;
    }

    /**
     * Get Tab Text Array
     *
     * @return String[]
     */
    private String[] getTabTextArray() {
        if (titleArray == null) {
            titleArray = fragmentActivity
                    .getResources()
                    .getStringArray(R.array.wizard_main_tab_text);
        }
        return titleArray;
    }
}
