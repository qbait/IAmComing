package pl.qbait.iamcoming;

import org.holoeverywhere.app.Application;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes( formKey = "dGxXSlViaVNmamREWHV5WS11ekpWYlE6MQ")
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }

    static {
        config().debugMode.setValue(true);
        config().preferenceImpl.setValue(org.holoeverywhere.app.Application.Config.PreferenceImpl.XML);
    }
}
