package pl.qbait.iamcoming;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class ProximityService extends Service {
    private static final String TAG = "ProximityService";
    private Preferences preferences;
    private final String PROX_ALERT = "pl.qbait.iamcoming.intent.action.PROXIMITY_ALERT";
    private ProximityReceiver proximityReceiver = null;
    private LocationManager locationManager = null;
    PendingIntent pendingIntent = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "start service");
        preferences = new Preferences(this);
        super.onCreate();
        createProximityAlert();
        protectServiceAgainstKilling();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "stop service");
        super.onDestroy();
        destroyProximityAlert();
    }

    private void createProximityAlert() {
        double lat = preferences.getLatitude();
        double lng = preferences.getLongitude();
        float radius = preferences.getRadiusFloat();
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        String geo = "geo:" + lat + "," + lng;
        Intent intent = new Intent(PROX_ALERT, Uri.parse(geo));
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        locationManager.addProximityAlert(lat, lng, radius, -1L, pendingIntent);
        proximityReceiver = new ProximityReceiver();
        IntentFilter intentFilter = new IntentFilter(PROX_ALERT);
        intentFilter.addDataScheme("geo");
        registerReceiver(proximityReceiver, intentFilter);
    }

    private void destroyProximityAlert() {
        unregisterReceiver(proximityReceiver);
        locationManager.removeProximityAlert(pendingIntent);
    }

    private void protectServiceAgainstKilling() {
        final int myID = 1234;

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notice = new Notification(R.drawable.ic_stat_notifications_enabled, getText(R.string.notification_ticker), System.currentTimeMillis());

        notice.setLatestEventInfo(this, getText(R.string.notification_title), getText(R.string.notification_content), pendIntent);

        notice.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(myID, notice);
    }


}
