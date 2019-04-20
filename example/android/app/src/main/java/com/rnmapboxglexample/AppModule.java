package com.rnmapboxglexample;

import android.util.Log;

import com.facebook.react.ReactActivity;
import com.facebook.react.bridge.Dynamic;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableNativeMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.common.ArrayUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import static com.facebook.react.bridge.ReadableType.Null;

public class AppModule extends ReactContextBaseJavaModule {

    public MainActivity activity = null;
    public ReactApplicationContext reactContext = null;

    public AppModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Nonnull
    @Override
    public String getName() {
        return "NativeMainViewController";
    }

    // Reading Data from JS to Native

    @ReactMethod
    public void sendText(ReadableMap params) throws JSONException {

        JSONObject jsonObject = new JSONObject();

        if (params != null) {
            try {
                jsonObject = AppModule.toJSONObject(params);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (params != null) {
            ReadableArray array = params.getArray("coordinates");
            Log.i("Message", String.format("Hype is ready", array.toString()));
            try {
                this.activity.sendMessageToInstances(jsonObject);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public static JSONObject toJSONObject(ReadableMap readableMap) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();

        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = readableMap.getType(key);

            switch (type) {
                case Null:
                    jsonObject.put(key, null);
                    break;
                case Boolean:
                    jsonObject.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    jsonObject.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    jsonObject.put(key, readableMap.getString(key));
                    break;
                case Map:
                    jsonObject.put(key, AppModule.toJSONObject(readableMap.getMap(key)));
                    break;
                case Array:
                    jsonObject.put(key, AppModule.toJSONArray(readableMap.getArray(key)));
                    break;
            }
        }

        return jsonObject;
    }

    static JSONArray toJSONArray(ReadableArray readableArray) throws JSONException {
        JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < readableArray.size(); i++) {
            ReadableType type = readableArray.getType(i);

            switch (type) {
                case Null:
                    jsonArray.put(i, null);
                    break;
                case Boolean:
                    jsonArray.put(i, readableArray.getBoolean(i));
                    break;
                case Number:
                    jsonArray.put(i, readableArray.getDouble(i));
                    break;
                case String:
                    jsonArray.put(i, readableArray.getString(i));
                    break;
                case Map:
                    jsonArray.put(i, AppModule.toJSONObject(readableArray.getMap(i)));
                    break;
                case Array:
                    jsonArray.put(i, AppModule.toJSONArray(readableArray.getArray(i)));
                    break;
            }
        }

        return jsonArray;
    }
}

//        ReadableMap map = params;
//        if (map != null) {
//            ReadableArray array = map.getArray("coordinates");
//            Log.i("Message", String.format("Hype is ready", array.toString()));
//            try {
//                this.activity.sendMessageToInstances(map);
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//        }


