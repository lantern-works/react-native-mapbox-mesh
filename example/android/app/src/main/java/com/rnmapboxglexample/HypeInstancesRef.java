package com.rnmapboxglexample;

import com.hypelabs.hype.Instance;

import java.util.HashMap;

public class HypeInstancesRef {

    public HashMap<String, Instance> instances = new HashMap<String, Instance>();


    private static  HypeInstancesRef sharedRef;
    static {
        sharedRef = new HypeInstancesRef();
    }

    public static HypeInstancesRef shared(){
        return sharedRef;
    }
}
