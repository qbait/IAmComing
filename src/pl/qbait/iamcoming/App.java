package pl.qbait.iamcoming;

import android.app.Application;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes( formKey = "dGxXSlViaVNmamREWHV5WS11ekpWYlE6MQ")
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }
}
