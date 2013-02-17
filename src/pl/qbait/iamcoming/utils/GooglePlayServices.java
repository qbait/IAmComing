package pl.qbait.iamcoming.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import org.holoeverywhere.app.Activity;
import pl.qbait.iamcoming.dialogs.GooglePlayServicesDialogFragment;

public class GooglePlayServices {

    public static boolean isInstalled(Activity activity) {
        boolean services = false;
        try {
            ApplicationInfo info = activity.getPackageManager().getApplicationInfo("com.google.android.gms", 0);
            services = true;
        } catch (PackageManager.NameNotFoundException e) {
            services = false;
        }
        return services;
    }

    public static void buildInstallationDialog(final Activity activity) {
        GooglePlayServicesDialogFragment dialog = new GooglePlayServicesDialogFragment();
        dialog.show(activity);
    }

}
