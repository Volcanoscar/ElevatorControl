package com.inovance.elevatorcontrol.models.Configuration;

import android.content.Context;

import com.inovance.elevatorcontrol.R;
import com.mobsandgeeks.adapters.InstantText;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 2015/7/5.
 */
public class SpecialistGroup {

    private int Id;

    private String name;

    public static List<SpecialistGroup> getSpecialistLists(Context context) {
        ArrayList<SpecialistGroup> arr = new ArrayList<SpecialistGroup>();
        SpecialistGroup pcy = new SpecialistGroup();
        pcy.setName(context.getResources().getString(R.string.specialist_comfort_optimization));
        arr.add(pcy);
        pcy = new SpecialistGroup();
        pcy.setName(context.getResources().getString(R.string.specialist_skid_test));
        arr.add(pcy);
        pcy = new SpecialistGroup();
        pcy.setName(context.getResources().getString(R.string.specialist_flat_adjustment));
        arr.add(pcy);
        pcy = new SpecialistGroup();
        pcy.setName(context.getResources().getString(R.string.specialist_parallel_control));
        arr.add(pcy);
        pcy = new SpecialistGroup();
        pcy.setName(context.getResources().getString(R.string.specialist_test));
        arr.add(pcy);
        pcy = new SpecialistGroup();
        pcy.setName(context.getResources().getString(R.string.specialist_through_doors));
        arr.add(pcy);
        pcy = new SpecialistGroup();
        pcy.setName(context.getResources().getString(R.string.specialist_outage_rescue_function));
        arr.add(pcy);
        pcy = new SpecialistGroup();
        pcy.setName(context.getResources().getString(R.string.specialist_driver_function));
        arr.add(pcy);

        pcy = new SpecialistGroup();
        pcy.setName(context.getResources().getString(R.string.specialist_fire_function));
        arr.add(pcy);

        pcy = new SpecialistGroup();
        pcy.setName(context.getResources().getString(R.string.specialist_locking_function));
        arr.add(pcy);

        return arr;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    @InstantText(viewId = R.id.text_transaction)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
