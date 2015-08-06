package com.inovance.elevatorcontrol.models;

import net.tsz.afinal.annotation.sqlite.Id;
import net.tsz.afinal.annotation.sqlite.ManyToOne;
import net.tsz.afinal.annotation.sqlite.Table;


/**
 * Created by Daniel on 2015/8/5.
 */
@Table(name = "GROUP_TAB_DETAIL")
public class GroupItemDetail {
    @Id
    private int Id;

    private String code;// 功能码地址

    @ManyToOne(column = "FKTabId")
    private GroupItem groupItem;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getCode() {
        if (code.contains("FP")) {
            return code.replace("FP", "D5");
        } else if (code.contains("FR")) {
            return code.replace("FR", "D2");
        } else
            return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    private int deviceID;

    public int getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(int deviceID) {
        this.deviceID = deviceID;
    }

    public void setGroupItem(GroupItem groupItem) {
        this.groupItem = groupItem;
    }


}
