package com.tayek.tablet.android;
import android.test.*;
import android.test.suitebuilder.annotation.*;
import android.widget.*;
import com.tayek.tablet.*;
import com.tayek.utilities.*;
import java.io.IOException;
import java.util.*;
public class MyFirstTestActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    public MyFirstTestActivityTest() {
        super(MainActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mainActivity=getActivity();
        build=mainActivity.build;
    }
    @MediumTest public void test1() throws Exception {
        System.out.println(build.gui.tablet);
    }
    MainActivity mainActivity;
    Build build;
}
