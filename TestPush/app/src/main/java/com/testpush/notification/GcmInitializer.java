package com.testpush.notification;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.testpush.Constants;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class GcmInitializer {

    public interface GcmInitialiserCallBack {
        public void onGcmInitialised(String token);
    }

    GcmInitialiserCallBack callBack;

    public static final int tempsAttenteSecondes = 3;

    public static final String TAG = "PLAY";

    public static final String ERROR_SERVICE_NOT_AVAILABLE =
            "SERVICE_NOT_AVAILABLE";

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = Constants.PROPERTY_REG_ID;
    private static final String PROPERTY_APP_VERSION = "1";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = Constants.SENDER_ID;

    Activity activity;
    Context context;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    String regid;

    public GcmInitializer(Activity a, GcmInitialiserCallBack callBack) {
        this.activity = a;
        this.context = a.getApplicationContext();
        this.callBack = callBack;
    }

    //-----------------------------------GoogleCloudMessaging----------------------------------

    public void enregistrerCloudMessaginClient() {
        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(activity);
            regid = getRegistrationId(context);
            Log.e(TAG, "regid :" + regid);
            if (regid.trim().length() == 0) {
                registerInBackground();
            } else {
                callBack.onGcmInitialised(regid);
            }
        } else {
            Log.i("PLAY", "No valid Google Play Services APK found.");
        }
    }

    public void desinscrireCloudMessaging(String token, String jeton) {

        if (checkPlayServices()) {

            gcm = GoogleCloudMessaging.getInstance(activity);

            try {
               // new SuppressionJetonTask((AppFragmentActivity) activity).execute(token, jeton);
                gcm.unregister();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                Log.e("GcmInitializer", "desinscrireCloudMessaging \n" + e.getMessage());
            }

        }
    }

    public void onResume() {
        checkPlayServices();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                activity.finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.length() == 0) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        Log.d("PLAY", "registrationId: " + registrationId);

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                Log.e(TAG, "registerInBackground");
                String msg = "";
                try {
                    if (gcm == null) {
                        Log.e(TAG, "gcm = null");
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    Log.e(TAG, "register gcm");
                    regid = gcm.register(SENDER_ID);
                    Log.e(TAG, "ok");
                    msg = "Device registered, registration ID=" + regid;

                    Log.e(TAG, msg);

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();

                    Log.e(TAG,msg);

                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {

            }
        }.execute(null, null, null);
    }




    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return activity.getSharedPreferences(GcmInitializer.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
        Log.d("PLAY_ID", regid);


        callBack.onGcmInitialised(regid);

        //Donnees.jeton = regid;
        //JetonManager jm = new JetonManager(this.activity);
        //jm.setJeton(regid);
    }

}
