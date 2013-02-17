package pl.qbait.iamcoming.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import pl.qbait.iamcoming.utils.Preferences;
import pl.qbait.iamcoming.services.ProximityService;

public class OnBootReceiver extends BroadcastReceiver {
    Preferences preferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        preferences = new Preferences(context);
        if(preferences.getNotificationsEnabled() == true) {
            context.startService(new Intent(context, ProximityService.class));
        }
    }
}
