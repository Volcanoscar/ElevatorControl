package com.inovance.elevatorcontrol.models;

import android.annotation.SuppressLint;
import android.content.Context;

import com.inovance.elevatorcontrol.R;
import com.inovance.elevatorcontrol.daos.ErrorHelpLogDao;
import com.mobsandgeeks.adapters.InstantText;

import java.util.Date;

public class ErrorHelpLog {

    private int Id;

    private Date errorTime;

    private int errorHelpId;

    private byte[] received;

    public Context ctx;

    @InstantText(viewId = R.id.text_failure_history)
    public String getUIName() {
        return ErrorHelpLogDao.findErrorHelp(ctx, errorHelpId).getDisplay();
    }

    @InstantText(viewId = R.id.time_failure_history)
    @SuppressLint("SimpleDateFormat")
    public String getUITime() {
        java.text.DateFormat format1 = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return format1.format(errorTime);
    }

    public Date getErrorTime() {
        return errorTime;
    }

    public void setErrorTime(Date errorTime) {
        this.errorTime = errorTime;
    }

    public int getErrorHelpId() {
        return errorHelpId;
    }

    public void setErrorHelpId(int errorHelpId) {
        this.errorHelpId = errorHelpId;
    }

    public Context getCtx() {
        return ctx;
    }

    public void setCtx(Context ctx) {
        this.ctx = ctx;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }


    public byte[] getReceived() {
        return received;
    }


    public void setReceived(byte[] received) {
        this.received = received;
    }
}
