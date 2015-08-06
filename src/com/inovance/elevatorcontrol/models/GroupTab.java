package com.inovance.elevatorcontrol.models;

import com.inovance.elevatorcontrol.R;
import com.mobsandgeeks.adapters.InstantText;

import net.tsz.afinal.annotation.sqlite.OneToMany;
import net.tsz.afinal.annotation.sqlite.Table;
import net.tsz.afinal.annotation.sqlite.Transient;
import net.tsz.afinal.db.sqlite.OneToManyLazyLoader;

import java.util.Date;
import java.util.List;

/**
 * ���ú�ר�ҹ�����
 *
 * @author jch
 */
@Table(name = "GROUP_TAB")
public class GroupTab {
    @net.tsz.afinal.annotation.sqlite.Id
    private int Id;

    private String groupText;

    private String groupId;

    private int deviceID;

    private int groupType;

    @Transient
    private boolean Valid;

    @Transient
    private Date lastTime;

    private List<ParameterSettings> settingsList;

    @OneToMany(manyColumn = "FKTabId")
    private OneToManyLazyLoader<GroupTab, GroupTabDetail> parametersettings;

    public GroupTab() {
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getDeviceID() {
        return deviceID;
    }

    @InstantText(viewId = R.id.text_transaction)
    public String getGroupText() {
        return groupText;
    }

    public void setGroupText(String groupText) {
        this.groupText = groupText;
    }

    public void setDeviceID(int deviceID) {
        this.deviceID = deviceID;
    }


    public void setGroupType(int groupType) {
        this.groupType = groupType;
    }

    public int getGroupType()
    {
        return this.groupType;
    }

    public boolean isValid() {
        return Valid;
    }

    public void setValid(boolean valid) {
        Valid = valid;
    }

    public Date getLastTime() {
        return lastTime;
    }

    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
    }

    public OneToManyLazyLoader<GroupTab, GroupTabDetail> getParametersettings() {
        return parametersettings;
    }

    public void setParametersettings(
            OneToManyLazyLoader<GroupTab, GroupTabDetail> parametersettings) {
        this.parametersettings = parametersettings;
    }

    public List<ParameterSettings> getSettingsList() {
        return settingsList;
    }

    public void setSettingsList(List<ParameterSettings> settingsList) {
        this.settingsList = settingsList;
    }

}

