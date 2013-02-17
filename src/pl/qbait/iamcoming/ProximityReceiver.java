package pl.qbait.iamcoming;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.*;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import org.holoeverywhere.widget.Toast;
import pl.qbait.iamcoming.mail.GMailSender;

import java.util.List;
import java.util.Locale;

public class ProximityReceiver extends BroadcastReceiver {
    private static final String TAG = "ProximityReceiver";
    private Context context;
    Preferences preferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        preferences = new Preferences(context);

        LatLng currentLocation = getCurrentLocation();
        if(getDistanceFromHome(currentLocation) <= 500) {
            Log.d(TAG, "User is probably in home");
            return;
        }

        Bundle extras = intent.getExtras();
        if (extras != null && extras.getBoolean(LocationManager.KEY_PROXIMITY_ENTERING)) {
            Log.d(TAG, "onReceive - entering");
            Toast.makeText(context, "I am coming - mail sent", Toast.LENGTH_LONG).show();
            sendEmailWhithoutUserInteraction(currentLocation);
            //sendSms(currentAddress);
            //sendEmail();
        } else {
            Log.d(TAG, "onReceive - out");
        }

        Log.d(TAG, createLogMessage(currentLocation));
    }

    private String createLogMessage(LatLng currentLocation) {
        String address = getAddress(currentLocation);
        float distance = getDistanceFromHome(currentLocation);
        return String.format("location: [%s, %s], distance: %s m, address: %s", currentLocation.latitude+"", currentLocation.longitude+"", distance+"", address+"");
    }

    private void sendSms(String address) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            String message = preferences.getNotificationText();
            String recipient = preferences.getContactNumber();
            smsManager.sendTextMessage(recipient, null, message, null, null);
            Log.d(TAG, String.format("sms sending - address: %s, recipient: %s, message: %s", address, recipient, message));
        } catch (Exception e) {
            Toast.makeText(context, "SMS faild, please try again later!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void sendEmail(LatLng currentLocation) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"kuba.szwiec@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "I am coming");
        i.putExtra(Intent.EXTRA_TEXT   , createLogMessage(currentLocation));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(i);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
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
