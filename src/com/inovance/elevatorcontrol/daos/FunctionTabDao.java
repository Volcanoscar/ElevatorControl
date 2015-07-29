package com.inovance.elevatorcontrol.daos;

import android.content.Context;

import com.inovance.elevatorcontrol.config.ApplicationConfig;
import com.inovance.elevatorcontrol.config.ParameterUpdateTool;
import com.inovance.elevatorcontrol.models.FunctionTab;

import net.tsz.afinal.FinalDb;

import java.util.List;

public class FunctionTabDao {

    private static final boolean DEBUG = false;

    public static List<FunctionTab> findAllCommonTab(Context context) {
        String condition = "deviceID = '" + ParameterUpdateTool.getInstance().getDeviceSQLID() + "' "
                            + " and groupTab=0 ";
        FinalDb db = FinalDb.create(context, ApplicationConfig.DATABASE_NAME, DEBUG);

        List<FunctionTab> tabs =db.findAllByWhere(FunctionTab.class, condition, "GroupID");
        return tabs;
    }

    public static List<FunctionTab> findAllSpecialTab(Context context) {
        String condition = "deviceID = '" + ParameterUpdateTool.getInstance().getDeviceSQLID() + "' "
                            + " and groupTab=1";
        FinalDb db = FinalDb.create(context, ApplicationConfig.DATABASE_NAME, DEBUG);
        return db.findAllByWhere(FunctionTab.class, condition, "GroupID");
    }

    public static FunctionTab findById(Context context, int id) {
        FinalDb db = FinalDb.create(context, ApplicationConfig.DATABASE_NAME, DEBUG);
        return db.findById(id, FunctionTab.class);
    }

    public static void deleteAllByDeviceID(Context context, int deviceID) {
        FinalDb db = FinalDb.create(context, ApplicationConfig.DATABASE_NAME, DEBUG);
        db.deleteByWhere(FunctionTab.class, " deviceID = '" + deviceID + "'");
    }

}
