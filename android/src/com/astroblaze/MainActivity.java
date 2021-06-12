package com.astroblaze;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public void exit() {
        this.finish();
    }

    @Override
    public void finishedLoadingAssets() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Navigation.findNavController(MainActivity.this, R.id.menu_container)
                        .addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
                            @Override
                            public void onDestinationChanged(@NotNull NavController navController, @NotNull NavDestination navDestination, @Nullable Bundle bundle) {
                                Gdx.app.log("MainActivity", "NavController onDestinationChanged -> " + navDestination.getDisplayName());
                                String navDisplayName = navDestination.getDisplayName();
                                switch (navDisplayName.substring(navDisplayName.indexOf('/') + 1)) {
                                    case "fragmentLoading":
                                    case "fragmentMenu":
                                    case "fragmentLevelSelect":
                                        game.getMusicController().setTrack(MusicController.MusicTrackType.UI);
                                        break;
                                    case "fragmentPause":
                                        game.getMusicController().setTrack(MusicController.MusicTrackType.Game);
                                        break;
                                    default:
                                        Gdx.app.log(MainActivity.class.getSimpleName(), "Destination has no assigned music, skipping.");
                                        break;
                                }
                            }
                        });
                navController = Navigation.findNavController(MainActivity.this, R.id.menu_container);
                navController.navigate(R.id.action_fragmentLoading_to_fragmentMenu);
            }
        });
    }
}