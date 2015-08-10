package com.inovance.elevatorcontrol.daos;

import android.content.Context;

import com.inovance.elevatorcontrol.config.ApplicationConfig;
import com.inovance.elevatorcontrol.models.GroupTabDetail;

import net.tsz.afinal.FinalDb;

/**
 * Created by Daniel on 2015/8/6.
 */
public class GroupTabDetailDao {

    private static final boolean DEBUG = false;

    public static void deleteAllByDeviceID(Context context, int deviceID) {
        FinalDb db = FinalDb.create(context, ApplicationConfig.DATABASE_NAME, DEBUG);
        db.deleteByWhere(GroupTabDetail.class, " deviceID = '" + deviceID + "'");
    }
}
