package com.astroblaze;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

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
                    AstroblazeGame.getInstance().pauseGame();
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        NavHostFragment.findNavController(FragmentGame.this)
                .popBackStack(R.id.fragmentPause, false);
        AstroblazeGame.getInstance().pauseGame();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @androidx.annotation.Nullable @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnExtra1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AstroblazeGame.getInstance().handleBtnExtra1Click();
            }
        });

        view.findViewById(R.id.btnExtra2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AstroblazeGame.getInstance().handleBtnExtra2Click();
            }
        });
    }
}