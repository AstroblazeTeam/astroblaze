package com.astroblaze;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.astroblaze.Interfaces.ILoadingFinishedListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

public class MainActivity extends FragmentActivity implements AndroidFragmentApplication.Callbacks, ILoadingFinishedListener {
    private AstroblazeGame game;
    private NavController navController;

    public NavController getNavController() {
        return navController;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        game = new AstroblazeGame();
        game.addOnLoadingFinishedListener(this);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.hud_container, new FragmentHUD())
                    .replace(R.id.game_container, new FragmentRender())
                    .commitNow();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Intent mStartActivity = new Intent(this, MainActivity.class);
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0,
                mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 300, mPendingIntent);
        exit();
    }

    @Override
    public void exit() {
        finishAffinity();
        finishAndRemoveTask();
        System.exit(0);
    }

    @Override
    public void finishedLoadingAssets() {
        this.runOnUiThread(() -> {
            navController = Navigation.findNavController(MainActivity.this, R.id.menu_container);
            navController.addOnDestinationChangedListener((navController, navDestination, bundle) -> {
                Gdx.app.log("MainActivity", "NavController onDestinationChanged -> " + navDestination.getDisplayName());
                String navDisplayName = navDestination.getDisplayName();
                switch (navDisplayName.substring(navDisplayName.indexOf('/') + 1)) {
                    case "fragmentLoading":
                    case "fragmentMenu":
                    case "fragmentLevelSelect":
                        AstroblazeGame.getMusicController().setTrack(MusicController.MusicTrackType.UI);
                        break;
                    case "fragmentPause":
                        AstroblazeGame.getMusicController().setTrackToRandomGameTrack();
                        break;
                    case "fragmentGameOver":
                    case "fragmentLevelComplete":
                        AstroblazeGame.getMusicController().setTrack(MusicController.MusicTrackType.Ending);
                        break;
                    default:
                        Gdx.app.log("MainActivity", "Destination has no assigned music, skipping.");
                        break;
                }
            });

            try {
                navController.navigate(R.id.action_fragmentLoading_to_fragmentMenu);
            } catch (IllegalArgumentException ex) {
                Log.d("MainActivity", "finishedLoadingAssets: fragment navigation failed, possibly duplicate event", ex);
            }
        });
    }
}