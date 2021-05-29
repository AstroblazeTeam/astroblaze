package com.astroblaze;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.fragment.app.FragmentActivity;
import com.astroblaze.ui.main.MainFragment;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

public class MainActivity extends FragmentActivity implements AndroidFragmentApplication.Callbacks {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new MainFragment())
                    .commitNow();
        }
    }

    @Override
    public void exit() {
        this.finish();
    }
}