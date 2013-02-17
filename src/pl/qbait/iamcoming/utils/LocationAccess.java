package pl.qbait.iamcoming.utils;


import android.content.Context;
import android.location.LocationManager;
import org.holoeverywhere.app.Activity;
import pl.qbait.iamcoming.dialogs.AccessLocationDialogFragment;

public class LocationAccess {
    public static boolean enabled(Activity activity) {
        final LocationManager manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static void buildAlertDialog(final Activity activity) {
        AccessLocationDialogFragment dialog = new AccessLocationDialogFragment();
        dialog.show(activity);
    }
}
