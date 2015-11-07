package com.tayek.tablet.android;
import android.app.Activity;
import android.graphics.*;
import android.text.*;
import android.util.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
//import com.tayek.tablet.gui.*;
import com.tayek.audio.*;
import com.tayek.tablet.gui.common.*;
//import com.tayek.tablet.gui.android.*;
import com.tayek.tablet.*;
import com.tayek.utilities.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
class Build implements View.OnClickListener, Observer {
    Build(final MainActivity activity) throws IOException, InterruptedException {
        this.activity=activity;
        linearLayout=new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        activity.tablet.client().start();
        if(activity.tablet.client().socket()!=null) {
            InetAddress inetAddress=activity.group.checkForInetAddress(activity.tabletId,activity.tablet.client().socket());
            int address=inetAddress!=null?Utility.toInteger(inetAddress):0;
            Message message=new Message(activity.group.groupId,activity.tabletId,Message.Type.start,address);
            activity.tablet.client().send(message);
        }
        gui=new AndroidGui(activity.tablet,new Toaster() {
            @Override
            public void toast(String string) {
                Toast.makeText(activity,string,Toast.LENGTH_LONG).show();
            }
        });
        final GuiAdapterABC adapterFor1=new GuiAdapterABC(activity.tablet.model()) {
            @Override
            public void setText(final int id,final String string) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((Button)gui.idToButton.get(id)).setText(string);
                            }
                        });
                    }
                },0);
            }
            @Override
            public void setState(final int id,final boolean state) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("set "+id+" checked "+state);
                                //((CheckBox)gui.idToButton.get(id)).setChecked(state);
                            }
                        });
                    }
                },0);
            }
        };
        gui.adapter=adapterFor1;
        logger.info("set gui adapter to: "+gui.adapter);
        activity.tablet.model().addObserver(this);
        activity.tablet.model().addObserver(ModelObserver.instance);
        linearLayout=build();
    }
    int color(int id,boolean state) {
        bg[0]=fg[0]=(float)((id-1)*360./activity.tablet.model().buttons);
        return state?Color.HSVToColor(fg):Color.HSVToColor(bg);
    }
    LinearLayout build() {
        DisplayMetrics m=activity.getResources().getDisplayMetrics();
        System.out.println(m);
        double w=m.widthPixels*.99;
        double h=m.widthPixels*.99/activity.tablet.model().buttons;
        System.out.println(w+" by +"+h);
        ViewGroup.LayoutParams lp=new ViewGroup.LayoutParams((int)w,(int)h);
        System.out.println(lp);
        LinearLayout layout=new LinearLayout(activity);
        // http://stackoverflow.com/a/11469528/51292
        // http://android-coding.blogspot.in/2011/05/resize-button-programmatically-using.html
        layout.setOrientation(LinearLayout.VERTICAL);  //Can also be done in xml by android:orientation="vertical"
        for(int i=1;i<=activity.tablet.model().buttons;i++) {
            Button button=new Button(activity);
            button.setLayoutParams(new ViewGroup.LayoutParams(lp));
            button.setText("Button "+i);
            button.setId(i);
            button.setBackgroundColor(color(i,false));
            layout.addView(button);
            button.setOnClickListener(this);
            gui.idToButton.put(i,button);
            //button.setOnTouchListener(otl);
        }
        return layout;
        //a.setContentView(layout);
        //setContentView(R.layout.activity_main);
    }
    @Override
    public void onClick(final View v) {
        if(v instanceof Button) {
            System.out.println("click on "+v);
            if(v instanceof CheckBox) {
                logger.info("checkbox");
                gui.onClick(new Integer(v.getId()),((CheckBox)v).isChecked());
            } else {
                Button button=(Button)v;
                int id=button.getId();
                System.out.println("button id "+id);
                // was clicked, so supposedly state has changed
                // but this is just a button, so it has not!
                // so assume that is was consistent with the model
                // so it now should be the opposite
                // so fake it
                boolean b=!activity.tablet.model().state(id);
                gui.onClick(id,b);
                int c=color(id,b);
                ((Button)v).setBackgroundColor(c);
            }
        } else
            logger.warning("not a button!");
    }
    @Override
    public void update(Observable o,Object hint) {
        if(o==activity.tablet.model()) {
            if(gui.adapter!=null)
                gui.adapter.update(o,hint);
            else
                logger.info("adapter for gui is null! ");
        } else
            throw new RuntimeException("no gui for model: "+o);
    }
    LinearLayout linearLayout;
    final MainActivity activity;
    final AndroidGui gui;
    float[] fg=new float[3];
    {
        fg[1]=1;
        fg[2]=1;
    }
    float[] bg=new float[3];
    {
        bg[1]=.6f;
        bg[2]=.6f;
    }
    final Logger logger=Logger.getLogger(getClass().getName());
}
