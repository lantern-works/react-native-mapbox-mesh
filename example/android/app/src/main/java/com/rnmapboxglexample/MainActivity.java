package com.rnmapboxglexample;

import com.facebook.react.ReactActivity;
import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableNativeMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.ViewManager;
import com.hypelabs.hype.Error;
import com.hypelabs.hype.Hype;
import com.hypelabs.hype.Instance;
import com.hypelabs.hype.Message;
import com.hypelabs.hype.MessageInfo;
import com.hypelabs.hype.MessageObserver;
import com.hypelabs.hype.NetworkObserver;
import com.hypelabs.hype.StateObserver;
import com.hypelabs.hype.Version;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.Nonnull;

import static com.facebook.react.bridge.Arguments.*;

public class MainActivity extends ReactActivity implements MessageObserver, NetworkObserver, StateObserver, ReactPackage {

    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "RNMapboxGLExample";
    }

    private static final String TAG = MainActivity.class.getName();
    private static final int REQUEST_ACCESS_COARSE_LOCATION_ID = 0;
    public List<NativeModule> modules = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(android.R.layout.activity_main2);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_ACCESS_COARSE_LOCATION_ID);
                }
                else
                {
                    startHype();
                }
            }
        });
    }

    private void startHype()
    {
        Log.i(TAG, String.format("Hype is starting"));

        // The application context is used to query the user for permissions, such as using
        // the Bluetooth adapter or enabling Wi-Fi. The context must be set before anything
        // else is attempted, otherwise resulting in an exception being thrown.
        Hype.setContext(getApplicationContext());

        // I/O notifications indicate when messages are sent, delivered, or fail to be sent.
        // Notice that a message being sent does not imply that it has been delivered, only
        // that it has been queued for output. This is especially important when using mesh
        // networking, as the destination device might not be connected on a direct link.
        Hype.addMessageObserver(this);

        // Network observer notifications include other devices entering and leaving the
        // network. When a device is found all observers get a onInstanceFound notification,
        // and when they leave onInstanceLost is triggered instead.
        Hype.addNetworkObserver(this);

        // Adding itself as a Hype state observer makes sure that the application gets
        // notifications for lifecycle events being triggered by the Hype SDK. These
        // events include starting and stopping, as well as some error handling.
        Hype.addStateObserver(this);

        // App identifiers are unique for each app. Access the HypeLabs dashboard in
        // https://hypelabs.io/apps/ and, after logging in, press the card for creating
        // a new app. Name it. The new app will display an app identifier number. Copy
        // that number and paste it here. Hype will not start if this identifier is
        // not configured correctly.
        Hype.setAppIdentifier("e6961486");

        Hype.setAnnouncement(new byte[10]);

        // Requesting Hype to start is equivalent to requesting the device to publish
        // itself on the network and start browsing for other devices in proximity. If
        // everything goes well, the onHypeStart() observer method gets called, indicating
        // that the device is actively participating on the network.
        Hype.start();

        Log.i(TAG, String.format("Version = " + Version.getVersionString()));
    }

    @Override
    public void onHypeInstanceFound(Instance instance) {

        Log.i(TAG, String.format("Hype found instance: %s", instance.getStringIdentifier()));

        // Resolving an instance consists of forcing the two devices to perform an handshake,
        // a necessary step for communicating. In this demo all instances are resolved, but
        // implementations should first assert whether the found instance is interesting,
        // whatever that means in the context of the app. Only instances deemed interesting
        // should be resolved, saving network overhead. In case of success, the SDK calls
        // the onHypeInstanceResolved(Instance) called. In case of failure, the method
        // onHypeInstanceFailResolving(Instance, Error) is called instead.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), String.format("Hype found instance: %s", instance.getStringIdentifier()), Toast.LENGTH_SHORT).show();
            }
        });
        Hype.resolve(instance);
    }

    @Override
    public void onHypeInstanceLost(Instance instance, Error error) {

        Log.i(TAG, String.format("Hype lost instance: %s [%s]", instance.getStringIdentifier(), error.getDescription()));

        // An instance being lost means that communicating with it is no longer possible.
        // This usually happens by the link being broken, which may be caused by the connection
        // timing out, or the device going out of range, among others. Another possibility is
        // the user turning the adapters off, in which case not only are all instances lost
        // but the framework also stops with an error.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), String.format("Hype lost instance: %s [%s]", instance.getStringIdentifier(), error.getDescription()), Toast.LENGTH_SHORT).show();
            }
        });

        HypeInstancesRef.shared().instances.remove(instance.getStringIdentifier());
    }

    @Override
    public void onHypeInstanceResolved(Instance instance) {

        Log.i(TAG, String.format("Hype resolved instance: %s", instance.getStringIdentifier()));

        // An instance being resolved means that it's ready for communicating. In this
        // case, the implementation sends an "Hello World" string encoded in UTF-8 format.
        // Although this is showing a simple scenario, application level protocols can be
        // as complex as necessary.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), String.format("Hype resolved instance: %s", instance.getStringIdentifier()), Toast.LENGTH_SHORT).show();
            }
        });


        HypeInstancesRef.shared().instances.put(instance.getStringIdentifier(), instance);

//        String helloWorld = "Hello World";
//
//        Hype.send(helloWorld.getBytes(), instance);
    }

    @Override
    public void onHypeInstanceFailResolving(Instance instance, Error error) {

        Log.i(TAG, String.format("Hype failed to resolve instance: %s [%s]", instance.getStringIdentifier(), error.getDescription()));

        // Failing to resolve an instance may indicate that the instance went out of range.
        // It's also a possibility that the instance was an attacker in disguise, but failed
        // to perform an handshake and Hype is refusing to communicate with it. The error
        // argument indicates a proper cause for the error.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), String.format("Hype failed to resolve instance: %s [%s]", instance.getStringIdentifier(), error.getDescription()), Toast.LENGTH_SHORT).show();
            }
        });

        HypeInstancesRef.shared().instances.remove(instance.getAppStringIdentifier());
    }

    @Override
    public void onHypeMessageReceived(Message message, Instance instance) {

        if (message.getData() == null) {
            return;
        }

        Log.i(TAG, String.format("Hype received message from instance: %s | %s", instance.getStringIdentifier(), new String(message.getData())));

//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(getApplicationContext(), String.format("Hype received message from instance: %s | %s", instance.getStringIdentifier(), new String(message.getData())), Toast.LENGTH_SHORT).show();
//            }
//        });

        // A message has arrived from another device. In this case, the message is expected
        // to be text encoded in UTF-8 format, the same protocol that was used when sending
        // a message.
        onLocationUpdate(message);

    }

    @Override
    public void onHypeMessageFailedSending(MessageInfo messageInfo, Instance instance, Error error) {

        Log.i(TAG, String.format("Hype failed to send message: %d [%s]", messageInfo.getIdentifier(), error.getDescription()));

        // Sending messages can fail for several reasons, such as the adapters (Bluetooth
        // or Wi-Fi) being turned off by the user while the process of sending the data
        // is still ongoing. The error parameter describes the cause for the failure.
    }

    @Override
    public void onHypeMessageSent(MessageInfo messageInfo, Instance instance, float progress, boolean done) {

        Log.i(TAG, String.format("Hype is sending a message: %d | %f (%s)", messageInfo.getIdentifier(), progress, done ? "done" : "ongoing"));

        // A message being sent indicates that it has been written to the output streams.
        // However, the content could still be buffered for output, so it has not necessarily
        // left the device. This is useful to indicate when a message is being processed,
        // but it does not indicate delivery to the destination device. The full contents
        // of the message have been written when the boolean flag "done" is set to true.
        // To indicate delivery check onHypeMessageDelivered(MessageInfo, Instance, float, boolean).
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(getApplicationContext(), String.format("Hype is sending a message: %d | %f (%s)", messageInfo.getIdentifier(), progress, done ? "done" : "ongoing"), Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    @Override
    public void onHypeMessageDelivered(MessageInfo messageInfo, Instance instance, float progress, boolean done) {

        Log.i(TAG, String.format("Hype delivered a message: %d | %f (%s)", messageInfo.getIdentifier(), progress, done ? "done" : "ongoing"));

        // A message being delivered indicates that the destination device has acknowledge
        // reception. If the "done" argument is true, then the message has been fully
        // delivered and the content is available at the destination device. This method
        // is useful for implementing progress bars.
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(getApplicationContext(), String.format("Hype delivered a message: %d | %f (%s)", messageInfo.getIdentifier(), progress, done ? "done" : "ongoing"), Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    @Override
    public void onHypeStart() {

        Log.i(TAG, "Hype started");

        // This method indicates that Hype successfully started. The device is currently
        // discoverable on the network.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Hype Started", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onHypeStop(Error error) {

        String description = "null";

        if (error != null) {

            // The error parameter will usually be null if the framework stopped because
            // it was requested to stop. This might not always happen, as even if requested
            // to stop the framework might do so with an error.
            description = String.format("[%s]", error.getDescription());
        }

        Log.i(TAG, String.format("Hype stopped [%s]", description));

        // The framework has stopped working for some reason. If it was asked to do so (by
        // calling stop) the error parameter is null. If, on the other hand, it was forced
        // by some external means, the error parameter indicates the cause. Common causes
        // include the user turning the Bluetooth and/or Wi-Fi adapters off. When the later
        // happens, you shouldn't attempt to start the Hype services again. Instead, the
        // framework triggers a onHypeReady() delegate method call if recovery from the
        // failure becomes possible.
    }

    @Override
    public void onHypeFailedStarting(Error error) {

        Log.i(TAG, String.format("Hype failed starting [%s]", error.getDescription()));

        // The framework couldn't start for some reason. Common causes include the Bluetooth
        // and Wi-Fi adapters being turned off. The error parameter always indicates the
        // cause for the failure, it's never null.
    }

    @Override
    public void onHypeReady() {

        Log.i(TAG, String.format("Hype is ready"));

        // This Hype delegate event indicates that the framework believes that it's capable
        // of recovering from a previous start failure. This event is only triggered once.
        // It's not guaranteed that starting the services will result in success, but it's
        // known to be highly likely. If the services are not needed at this point it's
        // possible to delay the execution for later, but it's not guaranteed that the
        // recovery conditions will still hold by then.
        Hype.start();
    }

    @Override
    public void onHypeStateChange() {

        Log.i(TAG, String.format("Hype state change"));

        // State change updates are triggered before their corresponding, specific, observer
        // call. For instance, when Hype starts, it transits to the State.Running state,
        // triggering a call to this method, and only then is onHypeStart() called. Every
        // such event has a corresponding observer method, so state change notifications
        // are mostly for convenience. This method is often not used.
    }

    @Override
    public String onHypeRequestAccessToken(int userIdentifier) {

        Log.i(TAG, String.format("Hype requested an access token for user identifier: %d", userIdentifier));

        // This method is called because Hype is requiring a new digital certificate. This
        // may happen because no certificate exists or the current one has already expired.
        // The access token will be sent to the HypeLabs backend, which requires access to
        // the Internet. The backend validates this token against the configurations set on
        // the HypeLabs dashboard for this app. By default, this template uses a token used
        // for testing. It's recommended that a backend is implemented and configured to
        // generate and validate access tokens before the app is deployed. The test token
        // for this app is given below, but it's only valid for 10 deployments.
        return "4e38959a17cbb204";
    }

    private boolean shouldResolveInstance(Instance instance) {

        // This method should decide whether an instance is interesting for communicating.
        // For that purpose, the implementation could use instance.userIdentifier, but it's
        // noticeable that announcements may not be available yet. Announcements are only
        // exchanged during the handshake.
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_COARSE_LOCATION_ID:
                startHype();
                break;
        }
    }

    // call to React-Native Method
    private void onLocationUpdate(Message message) {
        byte[] rawData = message.getData();
        String data = new String(message.getData());
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject != null) {
                WritableMap map = Arguments.createMap();
                JSONArray array = jsonObject.getJSONArray("coordinates");
                WritableArray topArray = Arguments.createArray();
                WritableArray writableArray = Arguments.createArray();
                for (int i = 0; array != null && i < array.length(); i++) {
                    JSONArray jArray = array.getJSONArray(i);
                    for (int j = 0; jArray != null && j < jArray.length(); j++) {
                        String item = jArray.getString(j);
                        writableArray.pushDouble(new Double(item));
                    }
                    topArray.pushArray(writableArray);
                }
                map.putArray("coordinates", topArray);
                this.getReactInstanceManager().getCurrentReactContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("locationUpdate", map);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    @Nonnull
    @Override
    public List<NativeModule> createNativeModules(@Nonnull ReactApplicationContext reactContext) {

        AppModule module = new AppModule(reactContext);
        module.activity = this;
        modules.add(module);

        return modules;
    }

    @Nonnull
    @Override
    public List<ViewManager> createViewManagers(@Nonnull ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    public void sendMessageToInstances(JSONObject jsonObject) throws UnsupportedEncodingException {
        byte[] data = jsonObject.toString().getBytes("UTF-8");
        String text = jsonObject.toString();

        Log.i(TAG, String.format(HypeInstancesRef.shared().instances.toString()));

        if (HypeInstancesRef.shared().instances != null && HypeInstancesRef.shared().instances.size() > 0) {
            for (String key: HypeInstancesRef.shared().instances.keySet()) {
                Instance instance = HypeInstancesRef.shared().instances.get(key);
                if (instance != null) {
                    Hype.send(data, instance);
                }
            }
        }
    }

    public void sendMessageToInstances(ReadableMap params) throws UnsupportedEncodingException {
        byte[] data = params.toString().getBytes("UTF-8");
        String text = data.toString();

        Log.i(TAG, String.format(HypeInstancesRef.shared().instances.toString()));

        if (HypeInstancesRef.shared().instances != null && HypeInstancesRef.shared().instances.size() > 0) {
            for (String key: HypeInstancesRef.shared().instances.keySet()) {
                Instance instance = HypeInstancesRef.shared().instances.get(key);
                if (instance != null) {
                    Hype.send(data, instance);
                }
            }
        }
    }
}