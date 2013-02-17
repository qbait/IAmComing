package pl.qbait.iamcoming.dialogs;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import org.holoeverywhere.app.DialogFragment;

public abstract class CommitSafeDialogFragment extends DialogFragment {

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        try {
            return super.show(transaction, tag);
        } catch (IllegalStateException e) {
            // ignore
        }
        return -1;
    }

    @Override
    public int show(FragmentManager manager, String tag) {
        try {
            super.show(manager, tag);
        } catch (IllegalStateException e) {
            // ignore
        }
        return -1;
    }
}
