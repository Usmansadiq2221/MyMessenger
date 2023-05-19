package com.devtwist.mymessenger.Models;

import android.webkit.JavascriptInterface;

import com.devtwist.mymessenger.Activities.AttendCallActivity;

public class InterfaceJava {

    private AttendCallActivity attendCallActivity;

    public InterfaceJava(AttendCallActivity attendCallActivity) {
        this.attendCallActivity = attendCallActivity;
    }

    @JavascriptInterface
    public void onPeerConnected(){
        attendCallActivity.onPeerConnected();
    }

}
