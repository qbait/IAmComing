package pl.qbait.iamcoming.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

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
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

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
                            activity.startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            // Ok that didn't work, try the market method.
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms"));
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                intent.setPackage("com.android.vending");
                                activity.startActivity(intent);
                            } catch (ActivityNotFoundException f) {
                                // Ok, weird. Maybe they don't have any market app. Just show the website.

                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.gms"));
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                activity.startActivity(intent);
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
