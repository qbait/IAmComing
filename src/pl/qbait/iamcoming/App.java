package pl.qbait.iamcoming;

import org.holoeverywhere.app.Application;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes( formKey = "dGxXSlViaVNmamREWHV5WS11ekpWYlE6MQ")
public class App extends Application {
    Preferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);

        preferences = new Preferences(this);
        if (preferences.getFirstRun() == true) {
            preferences.setNotificationText(getString(R.string.default_notification_text));
            preferences.setFirstRun(false);
        }

    }

    static {
        config().debugMode.setValue(true);
        config().preferenceImpl.setValue(org.holoeverywhere.app.Application.Config.PreferenceImpl.XML);
    }
}
