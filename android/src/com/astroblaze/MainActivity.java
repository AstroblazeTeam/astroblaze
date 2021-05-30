package com.astroblaze;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

public class MainActivity extends FragmentActivity implements AndroidFragmentApplication.Callbacks, AstroblazeGame.ILoadingFinishedListener {
    private AstroblazeGame game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main_activity);
        game = new AstroblazeGame();
        game.addOnLoadingFinishedListener(this);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.game_container, new FragmentRender(game))
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
                NavController nc = Navigation.findNavController(MainActivity.this, R.id.menu_container);
                nc.navigate(R.id.action_fragmentLoading_to_fragmentMenu);
            }
        });
    }
}