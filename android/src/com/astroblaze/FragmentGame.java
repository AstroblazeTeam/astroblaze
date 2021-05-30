package com.astroblaze;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badlogic.gdx.Gdx;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FragmentGame extends Fragment {
    public FragmentGame() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavHostFragment.findNavController(FragmentGame.this).addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NotNull NavController navController, @NotNull NavDestination navDestination, @Nullable Bundle bundle) {
                if (navDestination.getId() == R.id.fragmentPause) {
                    AstroblazeGame.getInstance().gameScreen.pauseGame();
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        AstroblazeGame.getInstance().gameScreen.pauseGame();
        NavHostFragment.findNavController(FragmentGame.this)
                .navigate(R.id.action_fragmentGame_to_fragmentPause);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }
}