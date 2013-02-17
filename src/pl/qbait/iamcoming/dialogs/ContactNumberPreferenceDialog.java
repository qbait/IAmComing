package pl.qbait.iamcoming.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import org.holoeverywhere.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import pl.qbait.iamcoming.R;

public class ContactNumberPreferenceDialog extends DialogPreference {
    private EditText numberEditText;
    private ImageButton pickContactButton;
    private View.OnClickListener pickContactButtonCallback;

    public ContactNumberPreferenceDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.contact_number_preference_dialog);
    }

    @Override
    protected View onCreateDialogView() {
        View view = super.onCreateDialogView();
        numberEditText = (EditText) view.findViewById(R.id.edittext_number);
        pickContactButton = (ImageButton) view.findViewById(R.id.imagebutton_pick_contact);
        //getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return view;
    }

    public void setPhoneNumber(String phoneNumber) {
        numberEditText.setText(phoneNumber);
        moveCursorToEnd(numberEditText);
    }

    void moveCursorToEnd(EditText editText) {
        editText.setSelection(editText.getText().length());
    }

    public String getPhoneNumber() {
        return numberEditText.getText().toString();
    }

    public void setPickContactButtonListener(View.OnClickListener callback) {
        pickContactButtonCallback = callback;
    }

    @Override
    protected void onBindDialogView(View view) {
        pickContactButton.setOnClickListener(pickContactButtonCallback);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE: // User clicked OK!
                String number = numberEditText.getText().toString();
                callChangeListener(number);
                break;
        }
        super.onClick(dialog, which);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        moveCursorToEnd(numberEditText);

    }

}
