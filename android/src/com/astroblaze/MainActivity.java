package com.astroblaze;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.fragment.app.FragmentActivity;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

public class MainActivity extends FragmentActivity implements AndroidFragmentApplication.Callbacks, AstroblazeGame.ILoadingFinishedListener {
    private AstroblazeGame game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main_activity);
        game = new AstroblazeGame();
        game.addListener(this);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.game_container, new FragmentRender(game))
                    .commitNow();
        }
        this.findViewById(R.id.menu_container).setVisibility(View.INVISIBLE);
    }

    @Override
    public void exit() {
        this.finish();
    }

    @Override
    public void finished() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.findViewById(R.id.menu_container).setVisibility(View.VISIBLE);
            }
        });
    }
}