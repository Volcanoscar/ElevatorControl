package com.inovance.elevatorcontrol.daos;

import android.content.Context;

import com.inovance.elevatorcontrol.config.ApplicationConfig;
import com.inovance.elevatorcontrol.config.ParameterUpdateTool;
import com.inovance.elevatorcontrol.models.GroupTab;

import net.tsz.afinal.FinalDb;

import java.util.List;

public class GroupTabDao {

    private static final boolean DEBUG = false;

    public static List<GroupTab> findAllCommonTab(Context context) {
        String condition = "deviceID = '" + ParameterUpdateTool.getInstance().getDeviceSQLID() + "' "
                            + " and groupTab=0 ";
        FinalDb db = FinalDb.create(context, ApplicationConfig.DATABASE_NAME, DEBUG);

        List<GroupTab> tabs =db.findAllByWhere(GroupTab.class, condition, "GroupID");
        return tabs;
    }

    public static List<GroupTab> findAllSpecialTab(Context context) {
        String condition = "deviceID = '" + ParameterUpdateTool.getInstance().getDeviceSQLID() + "' "
                            + " and groupTab=1";
        FinalDb db = FinalDb.create(context, ApplicationConfig.DATABASE_NAME, DEBUG);
        return db.findAllByWhere(GroupTab.class, condition, "GroupID");
    }

    public static GroupTab findById(Context context, int id) {
        FinalDb db = FinalDb.create(context, ApplicationConfig.DATABASE_NAME, DEBUG);
        return db.findById(id, GroupTab.class);
    }

    public static void deleteAllByDeviceID(Context context, int deviceID) {
        FinalDb db = FinalDb.create(context, ApplicationConfig.DATABASE_NAME, DEBUG);
        db.deleteByWhere(GroupTab.class, " deviceID = '" + deviceID + "'");
    }

}
