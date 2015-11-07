package com.tayek.tablet.android;
import android.app.*;
import android.content.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.provider.Settings.*;
import android.view.*;

import com.tayek.tablet.*;
import com.tayek.tablet.gui.common.*;
import com.tayek.utilities.*;

import java.io.*;
import java.lang.*;
import java.lang.System;
import java.util.*;
import java.util.logging.*;
//https://plus.google.com/103583939320326217147/posts/BQ5iYJEaaEH driver for usb
//http://davidrs.com/wp/fix-android-device-not-showing-up-on-windows-8/
public class MainActivity extends Activity {
    boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo=connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo!=null&&activeNetworkInfo.isConnected();
    }
    void play() {
        mediaPlayer=MediaPlayer.create(this,R.raw.rabbbit);
        // mediaPlayer.start();
    }
    void init() {
        Properties properties=java.lang.System.getProperties();
        logger.info(properties.size()+" properties.");
        for(Map.Entry<Object,Object> entry : properties.entrySet())
            logger.info(entry.getKey()+"="+entry.getValue());
        android_id=Secure.getString(getContentResolver(),Secure.ANDROID_ID);
        logger.info("android id: "+android_id);
        System.out.println("isNetworkAvailable: "+isNetworkAvailable());
        Character last=android_id.charAt(android_id.length()-1);
       tabletId=null;
        switch(last) { // fix these for the fires
            case 'd': // laurie's tab s
                tabletId=1;
                break;
            case '4': // conrad's nexus 7
                tabletId=2;
                break;
            default:
                tabletId=3;
                break;
        }
        if(android_id.equals("7643fc99c2f8eb5c"))
            tabletId=1; // conrads
        if(android_id.equals("fa37f2329a84e09d"))
            tabletId=2; // rays 2'nd
        else tabletId=3;

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        God.log.init();
        LoggingHandler.setLevel(Level.ALL);
        God.home.init();
        home=new Home();
        System.out.println(home.host+"/"+home.service);
        getSocket=new Home.GetSocket(home.host,home.service);
        new Thread(getSocket).start();
        init();
        System.out.println("tablet id is: "+tabletId);
        group=home.group().newGroup(); // clone the group
        try {
            tablet=group.new Tablet(tabletId);
            build=new Build(this);
        } catch(Exception e) {
            e.printStackTrace();
        }
        setContentView(build.linearLayout);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        logger.info("on create options menu");
        super.onCreateOptionsMenu(menu);
        for(TabletMenuItem menuItem : TabletMenuItem.values())
            menu.add(Menu.NONE,menuItem.ordinal(),Menu.NONE,menuItem.name());
        //getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        logger.info("item: "+item);
        int id=item.getItemId();
        TabletMenuItem.doItem(id,tablet);
        if(id==R.id.action_settings)
            return true;
        return super.onOptionsItemSelected(item);
    }
    Home.GetSocket getSocket;
    String android_id;
    Home home;
    Build build;
    Group group; // thc clone for the tablet
    Integer tabletId;
    Group.Tablet tablet;
    MediaPlayer mediaPlayer;
    final Logger logger=Logger.getLogger(getClass().getName());
}
