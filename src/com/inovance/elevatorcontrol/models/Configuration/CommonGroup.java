//package com.inovance.elevatorcontrol.models.Configuration;
//
//import android.content.Context;
//
//import com.inovance.elevatorcontrol.R;
//import com.mobsandgeeks.adapters.InstantText;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by Daniel on 2015/7/5.
// */
//public class CommonGroup {
//
//    private int Id;
//
//    private String name;
//
//    public static List<CommonGroup> getCommonGroupLists(Context context) {
//        ArrayList<CommonGroup> arr = new ArrayList<CommonGroup>();
//        CommonGroup pcy = new CommonGroup();
//        pcy.setName(context.getResources().getString(R.string.common_motor_tuning));
//        arr.add(pcy);
//        pcy = new CommonGroup();
//        pcy.setName(context.getResources().getString(R.string.common_well_study));
//        arr.add(pcy);
//        pcy = new CommonGroup();
//        pcy.setName(context.getResources().getString(R.string.common_service_layer_settings));
//        arr.add(pcy);
//        pcy = new CommonGroup();
//        pcy.setName(context.getResources().getString(R.string.common_weighing_self_study));
//        arr.add(pcy);
//        pcy = new CommonGroup();
//        pcy.setName(context.getResources().getString(R.string.common_parking_setting));
//        arr.add(pcy);
//        pcy = new CommonGroup();
//        pcy.setName(context.getResources().getString(R.string.common_floor_display_settings));
//        arr.add(pcy);
//        pcy = new CommonGroup();
//        pcy.setName(context.getResources().getString(R.string.common_system_time_settings));
//        arr.add(pcy);
//        pcy = new CommonGroup();
//        pcy.setName(context.getResources().getString(R.string.common_energy_saving));
//        arr.add(pcy);
//        return arr;
//    }
//
//    public int getId() {
//        return Id;
//    }
//
//    public void setId(int id) {
//        Id = id;
//    }
//
//    @InstantText(viewId = R.id.text_transaction)
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//}
