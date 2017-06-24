package com.github.nosepass.motoparking.di;

import android.app.Application;

/**
 * Holds a reference to the dagger object graph
 */

public class Dagger {
    private static AppComponent appComponent;
    private static EnvComponent component;

    public static void init(Application app) {
        appComponent = DaggerAppComponent.builder()
                .application(app)
                .build();
        component = appComponent.envComponentBuilder()
            .build();
    }
}
