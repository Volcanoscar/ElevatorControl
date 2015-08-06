package com.inovance.elevatorcontrol.daos;

import android.content.Context;

import com.inovance.elevatorcontrol.config.ApplicationConfig;
import com.inovance.elevatorcontrol.config.ParameterUpdateTool;
import com.inovance.elevatorcontrol.models.GroupItem;

import net.tsz.afinal.FinalDb;

import java.util.List;

public class GroupTabDao {

    private static final boolean DEBUG = false;

    public static List<GroupItem> findAllCommonTab(Context context) {
        String condition = "deviceID = '" + ParameterUpdateTool.getInstance().getDeviceSQLID() + "' "
                            + " and groupTab=0 ";
        FinalDb db = FinalDb.create(context, ApplicationConfig.DATABASE_NAME, DEBUG);

        List<GroupItem> tabs =db.findAllByWhere(GroupItem.class, condition, "GroupID");
        return tabs;
    }

    public static List<GroupItem> findAllSpecialTab(Context context) {
        String condition = "deviceID = '" + ParameterUpdateTool.getInstance().getDeviceSQLID() + "' "
                            + " and groupTab=1";
        FinalDb db = FinalDb.create(context, ApplicationConfig.DATABASE_NAME, DEBUG);
        return db.findAllByWhere(GroupItem.class, condition, "GroupID");
    }

    public static GroupItem findById(Context context, int id) {
        FinalDb db = FinalDb.create(context, ApplicationConfig.DATABASE_NAME, DEBUG);
        return db.findById(id, GroupItem.class);
    }

    public static void deleteAllByDeviceID(Context context, int deviceID) {
        FinalDb db = FinalDb.create(context, ApplicationConfig.DATABASE_NAME, DEBUG);
        db.deleteByWhere(GroupItem.class, " deviceID = '" + deviceID + "'");
    }

}
