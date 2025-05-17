package muli.labs;

import android.app.Application;

import io.realm.Realm;

public class MuliLabsApplication extends Application {

    public void onCreate()
    {
        super.onCreate();

        Realm.init(this);
    }
}