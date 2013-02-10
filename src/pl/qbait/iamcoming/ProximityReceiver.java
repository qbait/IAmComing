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
        Bundle extras = intent.getExtras();
        if (extras != null && extras.getBoolean(LocationManager.KEY_PROXIMITY_ENTERING)) {
            String currentAddress = getCurrentAddress();
            sendSms(currentAddress);
        }
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

    private String getCurrentAddress() {
        LatLng currentLocation = getCurrentLocation();
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
                System.out.println("Current Lat,Long : " + currentLocation);
                return currentLocation;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

}
