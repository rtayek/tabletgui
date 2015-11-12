package com.tayek.tablet.android;
import android.app.*;
import android.content.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.provider.Settings.*;
import android.view.*;
import android.widget.*;

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
        logger.info("android id: '"+android_id+"'");
        System.out.println("isNetworkAvailable: "+isNetworkAvailable());
        tabletId=null;
        if(android_id.equals("7643fc99c2f8eb5c"))
            tabletId=1; // conrad's fire
        else if(android_id.equals("fa37f2329a84e09d"))
            tabletId=2; // ray's 2'nd fire
        else if(android_id.equals("6f9a6936f633542a"))
            tabletId=99; // rays nexus 4
        else
            tabletId=3;
    }
    public String buttonsToString() {
        String s="{";
            for(int i=0;i<tablet.group.model.buttons;i++)
                s+=buttons[i].isPressed()?'T':"F";
            s+='}';
            return s;
        }

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        God.log.init();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LoggingHandler.setLevel(Level.ALL);
        init();
        if(true) {
            God.toaster=new Toaster() {
                @Override
                public void toast(final String string) {
                    strings.add(string);
                    if(strings.size()>n)
                        strings.remove(0);
                    bottom.post(new Runnable() {
                        public void run() {
                            String lines="";
                            for(String string : strings)
                                lines+=string+"\n";
                            bottom.setText(lines);
                        }
                    });
                }
                int n=5;
                final List<String> strings=new LinkedList();
            };
        }
        System.out.println("tablet id is: "+tabletId);
        group=new Group(1,Group.tablets4);
        tablet=new Tablet(group,tabletId);
        buttons=new Button[tablet.group.model.buttons];
        String h=group.idToHost().get(this.tabletId);
        System.out.println("host="+h);
        build=new Build(this);
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
    String android_id;
    Build build;
    Group group; // thc clone for the tablet
    Integer tabletId;
    Tablet tablet;
    Button[] buttons;
    MediaPlayer mediaPlayer;
    TextView bottom;
    Toaster toaster=new Toaster() {
        @Override
        public void toast(String string) {
            Toast.makeText(MainActivity.this,string,Toast.LENGTH_SHORT).show();
        }
    };
    final Logger logger=Logger.getLogger(getClass().getName());
}
