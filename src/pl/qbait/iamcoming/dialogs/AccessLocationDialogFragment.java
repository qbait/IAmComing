
package pl.qbait.iamcoming.dialogs;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.app.AlertDialog.Builder;

public class AccessLocationDialogFragment extends CommitSafeDialogFragment {
    public AccessLocationDialogFragment() {
        setDialogType(DialogType.AlertDialog);
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getSupportActivity());
        prepareBuilder(builder);
        return builder.create();
    }

    protected void prepareBuilder(Builder builder) {
        builder.setTitle("turn on access location");
        builder.setMessage("App need access location to work properly. Turn on access location in settings");
        builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
    }
}
