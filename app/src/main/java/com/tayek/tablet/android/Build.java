package com.tayek.tablet.android;
import android.app.Activity;
import android.graphics.*;
import android.text.*;
import android.util.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.tayek.tablet.*;
import com.tayek.tablet.gui.common.*;
import com.tayek.tablet.model.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
// http://stackoverflow.com/questions/18153644/android-asynctask-and-threading
// http://stephendnicholas.com/archives/42
class Build implements View.OnClickListener, Observer { // this gui adapter plus some!
    Build(final MainActivity activity) {
        this.activity=activity;
        linearLayout=new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        activity.tablet.start();
        Message message=new Message(activity.group.groupId,activity.tabletId,Message.Type.startup,0);
        activity.tablet.broadcast(message);
        gui=new AndroidGui(activity.tablet,activity.toaster);
        final GuiAdapterABC adapterFor1=new GuiAdapterABC(activity.tablet.group.model) {
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
                                activity.buttons[id-1].setPressed(state);
                            }
                        });
                    }
                },0);
            }
        };
        gui.adapter=adapterFor1;
        logger.info("set gui adapter to: "+gui.adapter);
        activity.tablet.group.model.addObserver(this);
        linearLayout=build();
    }
    int color(int id,boolean state) {
        bg[0]=fg[0]=(float)((id-1)*360./activity.tablet.group.model.buttons);
        return state?Color.HSVToColor(fg):Color.HSVToColor(bg);
    }
    LinearLayout build() {
        DisplayMetrics m=activity.getResources().getDisplayMetrics();
        System.out.println(m);
        double w=m.widthPixels*.99;
        double h=m.widthPixels*.99/activity.tablet.group.model.buttons;
        System.out.println(w+" by +"+h);
        ViewGroup.LayoutParams lp=new ViewGroup.LayoutParams((int)w,(int)h);
        System.out.println(lp);
        LinearLayout layout=new LinearLayout(activity);
        // http://stackoverflow.com/a/11469528/51292
        // http://android-coding.blogspot.in/2011/05/resize-button-programmatically-using.html
        layout.setOrientation(LinearLayout.VERTICAL);  //Can also be done in xml by android:orientation="vertical"
        final TextView top=new TextView(activity);
        top.setText("top");
        layout.addView(top);
        for(int i=1;i<=activity.tablet.group.model.buttons;i++) {
            Button button=new Button(activity);
            activity.buttons[i-1]=button;
            button.setLayoutParams(new ViewGroup.LayoutParams(lp));
            button.setText("Button "+i);
            button.setId(i);
            button.setBackgroundColor(color(i,false));
            layout.addView(button);
            button.setOnClickListener(this);
            gui.idToButton.put(i,button);
        }
        activity.bottom=new TextView(activity);
        activity.bottom.setText("bottom");
        ScrollView scroller=new ScrollView(activity);
        scroller.addView(activity.bottom);
        layout.addView(scroller);
        return layout;
    }
    @Override
    public void onClick(final View v) {
        if(v instanceof Button) {
            System.out.println("click on "+v);
            Button button=(Button)v;
            int id=button.getId();
            boolean state=button.isPressed();
            activity.tablet.group.model.setState(id,state);
            System.out.println("button id "+id);
            int c=color(id,state);
            ((Button)v).setBackgroundColor(c);
            God.toaster.toast(activity.buttonsToString());
        } else
            logger.warning("not a button!");
    }
    @Override
    public void update(Observable o,Object hint) {
        if(o==activity.tablet.group.model) {
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
        bg[1]=.7f;
        bg[2]=.6f;
    }
    final Logger logger=Logger.getLogger(getClass().getName());
}
