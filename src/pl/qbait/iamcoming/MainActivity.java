package pl.qbait.iamcoming;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class MainActivity extends SherlockPreferenceActivity {
    private static final String TAG = "MainActivity";
    private static final int PICK_CONTACT = 0;
    Preferences preferences;
    ContactNumberPreferenceDialog contactNumberPreferenceDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        preferences = new Preferences(this);

        Preference notificationsEnabledPreference = findPreference("notifications_enabled");
        notificationsEnabledPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Intent intent = new Intent(MainActivity.this, ProximityService.class);
                if ((Boolean) newValue) {
                    startService(intent);
                } else {
                    stopService(intent);
                }
                Log.d(getClass().getSimpleName(), "enabled change");
                return true;
            }
        });

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

}
