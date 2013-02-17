
package pl.qbait.iamcoming.dialogs;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.AlertDialog.Builder;

public class GooglePlayServicesDialogFragment extends CommitSafeDialogFragment {
    public GooglePlayServicesDialogFragment() {
        setDialogType(DialogType.AlertDialog);
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getSupportActivity());
        prepareBuilder(builder);
        return builder.create();
    }

    protected void prepareBuilder(Builder builder) {
        builder.setTitle("Google Play Services")
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
                });
    }
}
