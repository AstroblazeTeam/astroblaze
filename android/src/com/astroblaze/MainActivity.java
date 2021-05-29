package com.astroblaze;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.fragment.app.FragmentActivity;
import com.astroblaze.ui.main.FragmentGame;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

public class MainActivity extends FragmentActivity implements AndroidFragmentApplication.Callbacks {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.game_container, new FragmentGame())
                    .commitNow();
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.menu_container, new FragmentMenu())
//                    .commitNow();
        }
    }

    @Override
    public void exit() {
        this.finish();
    }
}