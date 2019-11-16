package ru.neva_energy;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;

import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.flutter.app.FlutterActivity;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;
import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.interfaces.BluetoothCallback;
import me.aflak.bluetooth.interfaces.DeviceCallback;

public class MainActivity extends FlutterActivity implements SoundPool.OnLoadCompleteListener {
    private static final String CHANNEL = "flutter.native/helper";
    private Bluetooth bluetooth;
    private DataLocation dataLocation;
    private NMEAManager nmeaManager;
    private LocationManager locationManager;
    private String provider;
    private int quality = 0;
    private Deque<String> messages = new ArrayDeque<>();
    private boolean connected = false;
    private String APP_PREFERENCES = "mysettings";
    private String ON_FIXED_SOUND = "on_fixed";
    private String ON_ANOTHER_SOUND = "on_another";
    private String ON_FIXED_SOUND_PATH = "on_fixed_path";
    private String ON_ANOTHER_SOUND_PATH = "on_another_path";
    private boolean CHOOSE_ON_FIXED = true;
    private String LOGGING_ON = "logging";
    private String ALT_ERR = "alt_err";
    private SoundPool mSoundPool;
    private FileOutputStream fos;
    private BluetoothCallback bluetoothCallback = new BluetoothCallback() {
        @Override public void onBluetoothTurningOn() {}
        @Override public void onBluetoothTurningOff() {}
        @Override public void onBluetoothOff() {}
        @Override public void onUserDeniedActivation() {}
        @Override public void onBluetoothOn() {}
    };
    private DeviceCallback deviceCallback = new DeviceCallback() {
        @Override
        public void onDeviceConnected(BluetoothDevice device) {
            connected = true;
        }
        @Override
        public void onDeviceDisconnected(BluetoothDevice device, String message) {
            connected = false;
            dataLocation = new DataLocation();
            CommonMethods.updateNotification(getApplicationContext(), "NevaEnergy", "Position: Offline", "Offline");
        }
        @Override
        public void onMessage(byte[] message) {
            SharedPreferences sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
            nmeaManager.parse(new String(message), sharedPreferences.getInt(ALT_ERR, 10));
            messages.push(new String(message));
            if (sharedPreferences.contains(LOGGING_ON) && sharedPreferences.getBoolean(LOGGING_ON, false)) {
                if (fos != null) {
                    try {
                        fos.write((new String(message) + "\n").getBytes());
                    } catch (IOException e) {}
                }
            }
            if (messages.size() > 20) {
                messages.pollLast();
            }
            if (nmeaManager.getStatus() != quality) {
                quality = dataLocation.status;
                switch (dataLocation.status) {
                    case 0:
                        CommonMethods.updateNotification(getApplicationContext(), "NevaEnergy", "Position: No", "No position");
                        if (sharedPreferences.contains(ON_ANOTHER_SOUND) && sharedPreferences.getBoolean(ON_ANOTHER_SOUND, false)) {
                            playSound(sharedPreferences.getString(ON_ANOTHER_SOUND_PATH, ""));
                        }
                        break;
                    case 1:
                        CommonMethods.updateNotification(getApplicationContext(), "NevaEnergy", "Position: Autonom", "Autonom");
                        if (sharedPreferences.contains(ON_ANOTHER_SOUND) && sharedPreferences.getBoolean(ON_ANOTHER_SOUND, false)) {
                            playSound(sharedPreferences.getString(ON_ANOTHER_SOUND_PATH, ""));
                        }
                        break;
                    case 2:
                        CommonMethods.updateNotification(getApplicationContext(), "NevaEnergy", "Position: DGPS", "DGPS");
                        if (sharedPreferences.contains(ON_ANOTHER_SOUND) && sharedPreferences.getBoolean(ON_ANOTHER_SOUND, false)) {
                            playSound(sharedPreferences.getString(ON_ANOTHER_SOUND_PATH, ""));
                        }
                        break;
                    case 3:
                        CommonMethods.updateNotification(getApplicationContext(), "NevaEnergy", "Position: GPS PPS", "GPS PPS");
                        if (sharedPreferences.contains(ON_ANOTHER_SOUND) && sharedPreferences.getBoolean(ON_ANOTHER_SOUND, false)) {
                            playSound(sharedPreferences.getString(ON_ANOTHER_SOUND_PATH, ""));
                        }
                        break;
                    case 4:
                        CommonMethods.updateNotification(getApplicationContext(), "NevaEnergy", "Position: FIXED RTK", "FIXED RTK");
                        if (sharedPreferences.contains(ON_FIXED_SOUND) && sharedPreferences.getBoolean(ON_FIXED_SOUND, false)) {
                            playSound(sharedPreferences.getString(ON_FIXED_SOUND_PATH, ""));
                        }
                        break;
                    case 5:
                        CommonMethods.updateNotification(getApplicationContext(), "NevaEnergy", "Position: Float RTK", "Float RTK");
                        if (sharedPreferences.contains(ON_FIXED_SOUND) && sharedPreferences.getBoolean(ON_FIXED_SOUND, false)) {
                            playSound(sharedPreferences.getString(ON_ANOTHER_SOUND_PATH, ""));
                        }
                        break;
                    case 6:
                        CommonMethods.updateNotification(getApplicationContext(), "NevaEnergy", "Position: Extrapolation", "Extrapolation");
                        if (sharedPreferences.contains(ON_FIXED_SOUND) && sharedPreferences.getBoolean(ON_FIXED_SOUND, false)) {
                            playSound(sharedPreferences.getString(ON_ANOTHER_SOUND_PATH, ""));
                        }
                        break;
                    case 7:
                        CommonMethods.updateNotification(getApplicationContext(), "NevaEnergy", "Position: Manual", "Manual");
                        if (sharedPreferences.contains(ON_FIXED_SOUND) && sharedPreferences.getBoolean(ON_FIXED_SOUND, false)) {
                            playSound(sharedPreferences.getString(ON_ANOTHER_SOUND_PATH, ""));
                        }
                        break;
                    case 8:
                        CommonMethods.updateNotification(getApplicationContext(), "NevaEnergy", "Position: Simulation", "Simulation");
                        if (sharedPreferences.contains(ON_FIXED_SOUND) && sharedPreferences.getBoolean(ON_FIXED_SOUND, false)) {
                            playSound(sharedPreferences.getString(ON_ANOTHER_SOUND_PATH, ""));
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        @Override public void onError(int errorCode) {}
        @Override public void onConnectError(BluetoothDevice device, String message) {}
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GeneratedPluginRegistrant.registerWith(this);

        createFlutterChannel();

        dataLocation = new DataLocation();

        try {
            fos = openFileOutput("NMEA.txt", MODE_APPEND);
        } catch (IOException e) {}

        bluetooth = new Bluetooth(this);
        bluetooth.setBluetoothCallback(bluetoothCallback);
        bluetooth.setDeviceCallback(deviceCallback);

    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetooth.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        bluetooth.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonMethods.deleteNotification(getApplicationContext());
    }

    private void createFlutterChannel() {
        new MethodChannel(getFlutterView(), CHANNEL)
                .setMethodCallHandler(new MethodChannel.MethodCallHandler() {
                    @Override
                    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
                        switch (methodCall.method) {
                            case "testMockLocation":
                                testMockLocation(result);
                                break;
                            case "getPairedDevices":
                                getPairedDevices(result);
                                break;
                            case "getData":
                                getData(result);
                                break;
                            case "getSatellites":
                                getSatellites(result);
                                break;
                            case "getGeolocation":
                                getGeolocation(result);
                                break;
                            case "getSoundsState":
                                getSoundsState(result);
                                break;
                            case "setOnFixedSound":
                                setOnFixedSound();
                                break;
                            case "setOnAnotherSound":
                                setOnAnotherSound();
                                break;
                            case "setLoggingOn":
                                setLoggingOn();
                                break;
                            default:
                                Pattern pattern = Pattern.compile("(.+?):(.+)");
                                Matcher matcher = pattern.matcher(methodCall.method);
                                if (matcher.find()) {
                                    if (matcher.group(1).equals("setAltErr")) {
                                        setAltErr(Integer.valueOf(matcher.group(2)));
                                    } else {
                                        connectToDevice(matcher.group(2));
                                    }
                                } else {
                                    result.notImplemented();
                                }
                                break;
                        }
                    }
                });
    }

    void testMockLocation(MethodChannel.Result result) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        provider = locationManager.getBestProvider(criteria, false);
        TestMockLocationResponse response = new TestMockLocationResponse();
        if (provider != null) {
            try {
                locationManager.addTestProvider(provider, false, false, false, false, true, true, true, 0, 5);
                locationManager.setTestProviderEnabled(provider, true);
                response.status = true;
            } catch (SecurityException e) {
                response.status = false;
                response.message = "Фиктивное местоположение не включено.";
            }
        } else {
            provider = LocationManager.GPS_PROVIDER;
            try {
                locationManager.addTestProvider(provider, false, false, false, false, true, true, true, 0, 5);
                locationManager.setTestProviderEnabled(provider, true);
                response.status = true;
            } catch (SecurityException e) {
                response.status = false;
                response.message = "Фиктивное местоположение не включено.";
            }
        }
        nmeaManager = new NMEAManager(locationManager, provider);
        Gson gson = new Gson();
        result.success(gson.toJson(response));
    }

    void getPairedDevices(MethodChannel.Result result) {
        ArrayList<String[]> devices = new ArrayList<>();
        List<BluetoothDevice> pairedDevices = bluetooth.getPairedDevices();
        for (BluetoothDevice device : pairedDevices) {
            devices.add(new String[] {device.getName(), device.getAddress()});
        }
        Gson gson = new Gson();
        result.success(gson.toJson(devices));
    }

    void getData(MethodChannel.Result result) {
        if (connected) {
            dataLocation = nmeaManager.getData();
        }
        dataLocation.nmea = messages;
        Gson gson = new Gson();
        result.success(gson.toJson(dataLocation));
    }

    void getGeolocation(MethodChannel.Result result) {
        Gson gson = new Gson();
        Map<String, Double> geolocation = nmeaManager.getGeolocation();
        result.success(gson.toJson(geolocation));
    }

    void getSatellites(MethodChannel.Result result) {
        Gson gson = new Gson();
        ArrayList<Satellite> satellites = nmeaManager.getSatellites();
        result.success(gson.toJson(satellites));
    }

    void getSoundsState(MethodChannel.Result result) {
        Gson gson = new Gson();
        Map map = new HashMap<>();
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences.contains(ON_FIXED_SOUND)) {
            map.put(ON_FIXED_SOUND, sharedPreferences.getBoolean(ON_FIXED_SOUND, false));
        }
        if (sharedPreferences.contains(ON_ANOTHER_SOUND)) {
            map.put(ON_ANOTHER_SOUND, sharedPreferences.getBoolean(ON_ANOTHER_SOUND, false));
        }
        if (sharedPreferences.contains(LOGGING_ON)) {
            map.put(LOGGING_ON, sharedPreferences.getBoolean(LOGGING_ON, false));
        }
        if (sharedPreferences.contains(ALT_ERR)) {
            map.put(ALT_ERR, sharedPreferences.getInt(ALT_ERR, 10));
        }
        result.success(gson.toJson(map));
    }

    void playSound(String path) {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        mSoundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
        mSoundPool.setOnLoadCompleteListener(this);
        if (path != null && !path.equals("")) {
            mSoundPool.load(path, 1);
        }
    }

    void setOnFixedSound() {
        CHOOSE_ON_FIXED = true;
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
        startActivityForResult(intent, 5);

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.contains(ON_FIXED_SOUND)) {
            if (sharedPreferences.getBoolean(ON_FIXED_SOUND, false)) {
                editor.remove(ON_FIXED_SOUND_PATH);
            }
            editor.putBoolean(ON_FIXED_SOUND, !sharedPreferences.getBoolean(ON_FIXED_SOUND, false));
        } else {
            editor.putBoolean(ON_FIXED_SOUND, true);
        }
        editor.commit();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent)
    {
        if (resultCode == Activity.RESULT_OK && requestCode == 5)
        {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            SharedPreferences sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            if (uri != null) {
                editor.putString(CHOOSE_ON_FIXED ? ON_FIXED_SOUND_PATH : ON_ANOTHER_SOUND_PATH, uri.toString());
            } else {
                editor.remove(CHOOSE_ON_FIXED ? ON_FIXED_SOUND_PATH : ON_ANOTHER_SOUND_PATH);
            }

            editor.commit();
        }
    }

    void setOnAnotherSound() {
        CHOOSE_ON_FIXED = false;
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
        startActivityForResult(intent, 5);

        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.contains(ON_ANOTHER_SOUND)) {
            if (sharedPreferences.getBoolean(ON_ANOTHER_SOUND, false)) {
                editor.remove(ON_ANOTHER_SOUND_PATH);
            }
            editor.putBoolean(ON_ANOTHER_SOUND, !sharedPreferences.getBoolean(ON_ANOTHER_SOUND, false));
        } else {
            editor.putBoolean(ON_ANOTHER_SOUND, true);
        }
        editor.commit();
    }

    void setAltErr(int altErr) {
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(ALT_ERR, altErr);
        editor.commit();
    }

    void setLoggingOn() {
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.contains(LOGGING_ON)) {
            editor.putBoolean(LOGGING_ON, !sharedPreferences.getBoolean(LOGGING_ON, false));
        } else {
            editor.putBoolean(LOGGING_ON, true);
        }
        editor.commit();
    }

    void connectToDevice(String deviceAddress) {
        bluetooth.connectToAddress(deviceAddress);
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int i, int i1) {
        mSoundPool.play(i, 1, 1, 1, 0, 1);
    }
}
