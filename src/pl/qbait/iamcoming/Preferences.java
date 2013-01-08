package pl.qbait.iamcoming;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.android.gms.maps.model.LatLng;

public class Preferences {
    SharedPreferences preferences;

    private static final String PREFERENCE_LATITUDE = "latitude";
    private static final String PREFERENCE_LONGITUDE = "longitude";
    private static final String PREFERENCE_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String PREFERENCE_CONTACT_NUMBER = "contact_number";
    private static final String PREFERENCE_NOTIFICATION_TYPE = "notification_type";
    private static final String PREFERENCE_RADIUS = "radius";
    public static final int DEFAULT_PREFERENCE_NUMERIC = 0;
    public static final boolean DEFAULT_PREFERENCE_BOOLEAN = false;
    public static final String DEFAULT_PREFERENCE_STRING = "";


    public Preferences(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isLocationSaved() {
        if (getLatitude() == 0 && getLongitude() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public LatLng getLocation() {
        double latitude = getLatitude();
        double longitude = getLongitude();
        LatLng p = new LatLng(latitude, longitude);
        return p;
    }

    public double getLatitude() {
        double result = Double.longBitsToDouble(getLongPreference(PREFERENCE_LATITUDE));
        return result;
    }

    public double getLongitude() {
        double result = Double.longBitsToDouble(getLongPreference(PREFERENCE_LONGITUDE));
        return result;
    }

    public boolean getNotificationsEnabled() {
        return getBooleanPreference(PREFERENCE_NOTIFICATIONS_ENABLED);
    }

    public String getContactNumber() {
        return getStringPreference(PREFERENCE_CONTACT_NUMBER);
    }

    public String getNotificationType() {
        return getStringPreference(PREFERENCE_NOTIFICATION_TYPE);
    }

    public int getRadius() {
        return getIntPreference(PREFERENCE_RADIUS);
    }

    public void setLocation(double latitude, double longitude) {
        setLatitude(latitude);
        setLongitude(longitude);
    }

    public void setLocation(LatLng p) {
        setLatitude(p.latitude);
        setLongitude(p.longitude);
    }

    public void setLatitude(double value) {
        long result = Double.doubleToLongBits(value);
        setLongPreference(PREFERENCE_LATITUDE, result);
    }

    public void setLongitude(double value) {
        long result = Double.doubleToLongBits(value);
        setLongPreference(PREFERENCE_LONGITUDE, result);
    }

    public void setNotificationsEnabled(boolean value) {
        setBooleanPreference(PREFERENCE_NOTIFICATIONS_ENABLED, value);
    }

    public void setContactNumber(String value) {
        setStringPreference(PREFERENCE_CONTACT_NUMBER, value);
    }

    public void setNotificationType(String value) {
        setStringPreference(PREFERENCE_NOTIFICATION_TYPE, value);
    }

    public void setRadius(int value) {
        setIntPreference(PREFERENCE_RADIUS, value);
    }

    private long getLongPreference(String key) {
        return preferences.getLong(key, DEFAULT_PREFERENCE_NUMERIC);
    }

    private int getIntPreference(String key) {
        return preferences.getInt(key, DEFAULT_PREFERENCE_NUMERIC);
    }

    private String getStringPreference(String key) {
        return preferences.getString(key, DEFAULT_PREFERENCE_STRING);
    }

    private boolean getBooleanPreference(String key) {
        return preferences.getBoolean(key, DEFAULT_PREFERENCE_BOOLEAN);
    }

    private void setLongPreference(String key, long value) {
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putLong(key, value);
        preferencesEditor.commit();
    }

    private void setIntPreference(String key, int value) {
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putInt(key, value);
        preferencesEditor.commit();
    }

    private void setStringPreference(String key, String value) {
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putString(key, value);
        preferencesEditor.commit();
    }

    private void setBooleanPreference(String key, boolean value) {
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putBoolean(key, value);
        preferencesEditor.commit();
    }
}
