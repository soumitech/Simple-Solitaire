/*
 * Copyright (C) 2016  Tobias Bielefeld
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * If you want to contact me, send me an e-mail at tobias.bielefeld@gmail.com
 */

package de.tobiasbielefeld.solitaire;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import com.google.android.gms.ads.MobileAds;

import java.util.Locale;

import de.tobiasbielefeld.solitaire.helper.LocaleChanger;

/**
 * Application class to load custom locales
 */

public class MainApplication extends Application {

    public final static String PREFS_KEY_LAUNCH_COUNT = "LaunchCount";
    public final static String PREFS_LAUNCH_NAME = "MainLaunch";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, initializationStatus -> {

        });

        new Thread(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_LAUNCH_NAME, Context.MODE_PRIVATE);
            int launchCount = sharedPreferences.getInt(PREFS_KEY_LAUNCH_COUNT, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            launchCount = Math.min(launchCount, 6);
            editor.putInt(PREFS_KEY_LAUNCH_COUNT, ++launchCount);
            editor.apply();
        }).start();
    }

    @Override
    protected void attachBaseContext(Context base) {
        LocaleChanger.setDefaultLocale(Locale.getDefault());
        super.attachBaseContext(LocaleChanger.onAttach(base));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleChanger.setLocale(this);
    }
}