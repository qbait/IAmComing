package pl.qbait.iamcoming;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import org.holoeverywhere.preference.*;
import java.util.ArrayList;

public class MainActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "MainActivity";
    private static final int PICK_CONTACT = 0;
    Preferences preferences;
    ContactNumberPreferenceDialog contactNumberPreferenceDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = new Preferences(this);
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        if (preferences.getNotificationsEnabled() && validateNotificationsEnabled()) {
            startProximityService();
        } else {
            stopProximityService();
        }

        Preference contactNumberPreference = findPreference("contact_number");
        contactNumberPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                contactNumberPreferenceDialog.setPhoneNumber(preferences.getContactNumber());
                return true;
            }
        });
        contactNumberPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preferences.setContactNumber((String) newValue);
                return true;
            }
        });

        contactNumberPreferenceDialog = (ContactNumberPreferenceDialog) contactNumberPreference;
        contactNumberPreferenceDialog.setPickContactButtonListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(i, PICK_CONTACT);
            }
        });

        final Preference.OnPreferenceClickListener moveCursorToEndClickListener =
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        EditTextPreference editPref = (EditTextPreference) preference;
                        String text = editPref.getText();
                        if (text != null) {
                            editPref.getEditText().setSelection(text.length());
                        }
                        return true;
                    }
                };

        Preference notificationTextPreference = findPreference("notification_text");
        notificationTextPreference.setOnPreferenceClickListener(moveCursorToEndClickListener);

        Preference radiusPreference = findPreference("radius");
        radiusPreference.setOnPreferenceClickListener(moveCursorToEndClickListener);

        Preference locationPreference = findPreference("location");
        locationPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startMapOrInstallGooglePlayServices();
                return false;
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(Preferences.PREFERENCE_NOTIFICATIONS_ENABLED)) {
            SwitchPreference switchPreference = (SwitchPreference) findPreference("notifications_enabled");
            if ( preferences.getNotificationsEnabled() && validateNotificationsEnabled()) {
                startProximityService();
                //switchPreference.setChecked(true);
            } else {
                stopProximityService();
                switchPreference.setChecked(false);
            }
        }
        if (key.equals(Preferences.PREFERENCE_RADIUS) || key.equals(Preferences.PREFERENCE_LATITUDE) || key.equals(Preferences.PREFERENCE_LONGITUDE)) {
            if( preferences.getNotificationsEnabled() && validateNotificationsEnabled() ) {
                restartProximityServiceIfRunning();
            } else {
                stopProximityService();
                SwitchPreference switchPreference = (SwitchPreference) findPreference("notifications_enabled");
                switchPreference.setChecked(false);
            }
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    String phoneNumber = getPhoneNumberFromContact(data.getData());
                    contactNumberPreferenceDialog.setPhoneNumber(phoneNumber);
                    if (phoneNumber != null) {
                        contactNumberPreferenceDialog.setPhoneNumber(phoneNumber);
                    } else {
                        contactNumberPreferenceDialog.setPhoneNumber("");
                        Toast.makeText(this, "Contact hasn't phone number", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    private String getPhoneNumberFromContact(Uri contactData) {
        String phoneNumber = null;
        Cursor c = managedQuery(contactData, null, null, null, null);
        if (c.moveToFirst()) {
            String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
            String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

            if (hasPhone.equalsIgnoreCase("1")) {
                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                phones.moveToFirst();
                phoneNumber = phones.getString(phones.getColumnIndex("data1"));
            }
        }
        return phoneNumber;
    }

    private boolean validateNotificationsEnabled() {
        ArrayList<String> missingPreferences = getMissingPreferences();
        if (!missingPreferences.isEmpty()) {
            String validationMessage = String.format("%s: %s", getString(R.string.complete_alert), TextUtils.join(", ", missingPreferences) );
            Toast.makeText(MainActivity.this, validationMessage, Toast.LENGTH_LONG).show();
            return false;
        } else if (!locationAccessEnabled()) {
            buildAlertMessageNoLocationAccess();
            return false;
        }

        return true;
    }

    private void startProximityService() {
        Intent intent = new Intent(MainActivity.this, ProximityService.class);
        startService(intent);
    }

    private void stopProximityService() {
        Intent intent = new Intent(MainActivity.this, ProximityService.class);
        stopService(intent);
    }

    private void restartProximityServiceIfRunning() {
        if (isProximityServiceRunning()) {
            Intent intent = new Intent(MainActivity.this, ProximityService.class);
            stopService(intent);
            startService(intent);
        }
    }

    private boolean isProximityServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ProximityService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> getMissingPreferences() {
        ArrayList<String> missingPreferences = new ArrayList<String>();
        if(!preferences.isContactNumberSaved()) {
            missingPreferences.add(getString(R.string.title_contact_number));
        }
        if(!preferences.isNotificationTextSaved()) {
            missingPreferences.add(getString(R.string.title_notification_text));
        }
        if(!preferences.isDistanceSaved()) {
            missingPreferences.add(getString(R.string.title_radius));
        }
        if(!preferences.isLocationSaved()) {
            missingPreferences.add(getString(R.string.title_location));
        }
        return missingPreferences;
    }

    private boolean locationAccessEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void buildAlertMessageNoLocationAccess() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void startMapOrInstallGooglePlayServices() {
        // See if google play services are installed.
        boolean services = false;
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo("com.google.android.gms", 0);
            services = true;
        } catch (PackageManager.NameNotFoundException e) {
            services = false;
        }

        if (services) {
            startActivity(new Intent(MainActivity.this, MapActivity.class));
            return;
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

            // set dialog message
            alertDialogBuilder
                    .setTitle("Google Play Services")
                    .setMessage("The map requires Google Play Services to be installed.")
                    .setCancelable(true)
                    .setPositiveButton("Install", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            // Try the new HTTP method (I assume that is the official way now given that google uses it).
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.gms"));
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                intent.setPackage("com.android.vending");
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                // Ok that didn't work, try the market method.
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms"));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                    intent.setPackage("com.android.vending");
                                    startActivity(intent);
                                } catch (ActivityNotFoundException f) {
                                    // Ok, weird. Maybe they don't have any market app. Just show the website.

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.gms"));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                    startActivity(intent);
                                }
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .create()
                    .show();
        }
    }
}