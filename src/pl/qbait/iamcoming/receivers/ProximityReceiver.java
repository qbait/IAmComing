package pl.qbait.iamcoming.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.*;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import org.holoeverywhere.widget.Toast;
import pl.qbait.iamcoming.App;
import pl.qbait.iamcoming.utils.Encoding;
import pl.qbait.iamcoming.utils.Preferences;
import pl.qbait.iamcoming.utils.mail.GMailSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProximityReceiver extends BroadcastReceiver {
    private static final String TAG = "ProximityReceiver";
    private static final int DISTANCE_USER_IS_PROBABLY_IN_DESTINATION = 200;
    private Context context;
    Preferences preferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        preferences = new Preferences(context);

        LatLng currentLocation = getCurrentLocation();
        if(getDistanceFromHome(currentLocation) <= DISTANCE_USER_IS_PROBABLY_IN_DESTINATION) {
            sendEmailWhithoutUserInteraction(currentLocation);
            Log.d(TAG, "User is probably in home");
            return;
        }

        Bundle extras = intent.getExtras();
        if (extras != null && extras.getBoolean(LocationManager.KEY_PROXIMITY_ENTERING)) {
            Log.d(TAG, "onReceive - entering");
            Toast.makeText(context, "I am coming - sms sent", Toast.LENGTH_LONG).show();
            sendEmailWhithoutUserInteraction(currentLocation);
            boolean debugMode = ((App)context.getApplicationContext()).getConfig().debugMode.getValue();
            if(!debugMode) {
                sendSms(currentLocation);
            }
        } else {
            Log.d(TAG, "onReceive - out");
        }

        Log.d(TAG, createLogMessage(currentLocation));
    }

    private String getNotificationMessage(LatLng currentLocation) {
        String address = getAddress(currentLocation);
        float distance = getDistanceFromHome(currentLocation);
        String notificationMessage = preferences.getNotificationText();
        notificationMessage = notificationMessage.replace("[address]", address);
        notificationMessage = notificationMessage.replace("[distance]", String.format("%.2g km", distance/1000));
        return notificationMessage;
    }

    private String createLogMessage(LatLng currentLocation) {
        String notificationMessage = getNotificationMessage(currentLocation);
        return String.format("location: [%s, %s], notificationMessage: %s", currentLocation.latitude+"", currentLocation.longitude+"", notificationMessage);
    }

    private void sendSms(LatLng currentLocation) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            String message = getNotificationMessage(currentLocation);
            String recipient = preferences.getContactNumber();
            message = Encoding.convertNonAscii(message);
            //smsManager.sendTextMessage(recipient, null, message, null, null);
            if (message.length() > 160) {
                ArrayList msgTexts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(recipient, null, msgTexts, null, null);
            } else {
                try {
                    smsManager.sendTextMessage(recipient, null, message, null, null);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, String.format("sms sending, recipient: %s, message: %s", recipient, message));
        } catch (Exception e) {
            Toast.makeText(context, "SMS faild, please try again later!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void sendEmailWhithoutUserInteraction(final LatLng currentLocation) {
        Thread thread = new Thread()
        {
            @Override
            public void run() {
                try {
                    GMailSender sender = new GMailSender("qbks88@gmail.com", "kuba2210");
                    sender.sendMail("I am coming",
                            createLogMessage(currentLocation),
                            "qbks88@gmail.com",
                            "kuba.szwiec@gmail.com");
                } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                }
            }
        };
        thread.start();
    }

    private float getDistanceFromHome(LatLng location) {
        LatLng home = preferences. getLocation();
        Location homeLocation = new Location("homeLocation");
        homeLocation.setLatitude(home.latitude);
        homeLocation.setLongitude(home.longitude);
        Location currentLocation = new Location("currentLocation");
        currentLocation.setLatitude(location.latitude);
        currentLocation.setLongitude(location.longitude);
        float distance = currentLocation.distanceTo(homeLocation);
        return distance;
    }

    private String getAddress(LatLng currentLocation) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String formattedAddress = null;
        try {
            List<Address> list = geocoder.getFromLocation(currentLocation.latitude, currentLocation.longitude, 1);
            Address address = list.get(0);
            formattedAddress = address.getAddressLine(0) + ", " + address.getLocality();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return formattedAddress;
    }

    private LatLng getCurrentLocation() {
        LocationManager locationManager;
        String locationService = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) context.getSystemService(locationService);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(criteria, true);

        if (provider != null) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                return currentLocation;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

}
