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

package de.tobiasbielefeld.solitaire.classes;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import de.tobiasbielefeld.solitaire.ad.AdConfig;
import de.tobiasbielefeld.solitaire.ad.AdDialogInteractionListener;
import de.tobiasbielefeld.solitaire.handler.HandlerStopBackgroundMusic;
import de.tobiasbielefeld.solitaire.helper.LocaleChanger;

import static de.tobiasbielefeld.solitaire.SharedData.*;

/**
 * Custom AppCompatActivity to implement language changing in attachBaseContext()
 * and some settings in onResume().  It also sets the Preferences, in case the app
 * was paused for a longer time and the references got lost.
 */

public class CustomAppCompatActivity extends AppCompatActivity {

    public static final String INTERSTITIAL_AD_TAG = "InterstitialAd";

    private InterstitialAd mInterstitialAd;

    HandlerStopBackgroundMusic handlerStopBackgroundMusic = new HandlerStopBackgroundMusic();

    /**
     * Sets the screen orientation according to the settings. It is called from onResume()
     */
    public void setOrientation() {
        switch (prefs.getSavedOrientation()) {
            case 1: //follow system settings
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                break;
            case 2: //portrait
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case 3: //landscape
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case 4: //landscape upside down
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reinitializeData(this);

        loadAd();

        // Set your test devices. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
        // to get test ads on this device."
        /*MobileAds.setRequestConfiguration(
                new RequestConfiguration.Builder()
                        .setTestDeviceIds(Collections.singletonList("ABCDEF012345"))
                        .build()
        );*/
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleChanger.onAttach(base));
    }

    /**
     * Apply the preferences from orientation and status bar to the current activity. It will be
     * called in the onResume, so after changing the preferences I don't need a listener to update
     * the changes on the previous activities.
     */
    @Override
    public void onResume() {
        super.onResume();
        setOrientation();
        showOrHideStatusBar();

        backgroundSound.doInBackground(this);
        activityCounter++;
    }

    /**
     * Check here if the application is closed. If the activityCounter reaches zero, no activity
     * is in the foreground so stop the background music. But try stopping some milliseconds delayed,
     * because otherwise the music would stop/restart between the activities
     */
    @Override
    protected void onPause() {
        super.onPause();

        activityCounter--;
        handlerStopBackgroundMusic.sendEmptyMessageDelayed(0, 100);
    }

    /**
     * Hides the status bar according to the settings. It is called from onResume().
     */
    public void showOrHideStatusBar() {
        if (prefs.getSavedHideStatusBar()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public void loadAd() {

        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, AdConfig.INTERSTITIAL_AD_UNIT_ID, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
                Log.i(INTERSTITIAL_AD_TAG, "onAdLoaded");
                afterOnAdLoaded();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Log.i(INTERSTITIAL_AD_TAG, loadAdError.getMessage());
                mInterstitialAd = null;
            }
        });

    }

    public void afterOnAdLoaded() {

    }

    public void showInterstitial() {
        Log.d(INTERSTITIAL_AD_TAG, "mInterstitialAd = " + mInterstitialAd);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (mInterstitialAd != null) {
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d(INTERSTITIAL_AD_TAG, "Ad was dismissed.");
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mInterstitialAd = null;
                        loadAd();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        Log.d(INTERSTITIAL_AD_TAG, "Ad failed to show.");
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mInterstitialAd = null;
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d(INTERSTITIAL_AD_TAG, "Ad showed fullscreen content.");
                        // Called when ad is dismissed.
                    }
                });
                mInterstitialAd.show(this);
            }
        }, 1200);
    }

    protected final AdDialogInteractionListener listener = new AdDialogInteractionListener() {
        @Override
        public void onShowAd() {
            showInterstitial();
        }

        @Override
        public void onCancelAd() {

        }
    };
}
