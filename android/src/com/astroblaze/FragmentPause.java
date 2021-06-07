package com.astroblaze;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FragmentPause extends Fragment {
    public FragmentPause() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.getBoolean("startGame")) {
            NavHostFragment.findNavController(FragmentPause.this)
                    .navigate(R.id.action_fragmentPause_to_fragmentGame);
            AstroblazeGame.getInstance().gameScreen.startGame(args.getInt("level", 1));
            AstroblazeGame.getInstance().resumeGame();
        }
        NavHostFragment.findNavController(FragmentPause.this).addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NotNull NavController navController, @NotNull NavDestination navDestination, @Nullable Bundle bundle) {
                if (navDestination.getId() == R.id.fragmentLevelSelect) {
                    AstroblazeGame.getInstance().gameScreen.stopGame();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pause, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @androidx.annotation.Nullable @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // resume button
        view.findViewById(R.id.backToGame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(FragmentPause.this)
                        .navigate(R.id.action_fragmentPause_to_fragmentGame);
                AstroblazeGame.getInstance().resumeGame();
            }
        });

        // options button
        view.findViewById(R.id.openOptions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(FragmentPause.this)
                        .navigate(R.id.action_fragmentPause_to_fragmentOptions);
            }
        });

        // quit button
        view.findViewById(R.id.backToMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(FragmentPause.this)
                        .popBackStack();
            }
        });
    }
}