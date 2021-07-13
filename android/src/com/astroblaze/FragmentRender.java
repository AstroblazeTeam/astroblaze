package com.astroblaze;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

/**
 * This fragment is the LibGDX render fragment, it has no layout
 * Don't mess with this fragment as LibGDX wrappers will remove
 * any widgets placed in this fragment
 */
public class FragmentRender extends AndroidFragmentApplication {
    public FragmentRender() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;
        // fragment controlled by libgdx, don't do anything weird here
        return initializeForView(AstroblazeGame.getInstance(), config);
    }
}