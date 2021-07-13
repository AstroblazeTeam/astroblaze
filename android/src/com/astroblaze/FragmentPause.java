package com.astroblaze;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FragmentPause extends Fragment {
    private final NavController.OnDestinationChangedListener onDestinationChangedListener;

    public FragmentPause() {
        // Required empty public constructor
        onDestinationChangedListener = (navController, navDestination, bundle) -> {
            if (navDestination.getId() == R.id.fragmentLevelSelect) {
                AstroblazeGame.getInstance().gameScreen.stopGame();
            }
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.getBoolean("startGame")) {
            try {
                // startGame bool in bundle means start button was pressed
                // in level select fragment and we should directly transition
                // to game fragment
                NavHostFragment.findNavController(FragmentPause.this)
                        .navigate(R.id.action_fragmentPause_to_fragmentGame);
                AstroblazeGame.getInstance().gameScreen.startGame(
                        args.getInt("level", 1),
                        args.getInt("ship", 0));
                AstroblazeGame.getInstance().resumeGame();
            } catch (IllegalArgumentException ex) {
                Log.d("FragmentMenu", "onCreate: fragment navigation failed, possibly duplicate call", ex);
            }
        }
        NavHostFragment.findNavController(FragmentPause.this)
                .addOnDestinationChangedListener(onDestinationChangedListener);
    }

    @Override
    public void onDetach() {
        NavHostFragment.findNavController(FragmentPause.this)
                .removeOnDestinationChangedListener(onDestinationChangedListener);
        super.onDetach();
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
        view.findViewById(R.id.backToGame).setOnClickListener(v -> {
            try {
                NavHostFragment.findNavController(FragmentPause.this)
                        .navigate(R.id.action_fragmentPause_to_fragmentGame);
                AstroblazeGame.getInstance().resumeGame();
                AstroblazeGame.getSoundController().playUIPositive();
            } catch (IllegalArgumentException ex) {
                Log.d("FragmentMenu", "backToGame onClick: fragment navigation failed, possibly duplicate event", ex);
            }
        });

        // options button
        view.findViewById(R.id.openOptions).setOnClickListener(v -> {
            try {
                NavHostFragment.findNavController(FragmentPause.this)
                        .navigate(R.id.action_fragmentPause_to_fragmentOptions);
                AstroblazeGame.getSoundController().playUIConfirm();
            } catch (IllegalArgumentException ex) {
                Log.d("FragmentMenu", "openOptions onClick: fragment navigation failed, possibly duplicate event", ex);
            }
        });

        // quit button
        view.findViewById(R.id.backToMenu).setOnClickListener(v -> {
            try {
                NavHostFragment.findNavController(FragmentPause.this)
                        .popBackStack();
                AstroblazeGame.getSoundController().playUINegative();
            } catch (IllegalArgumentException ex) {
                Log.d("FragmentMenu", "backToMenu onClick: fragment navigation failed, possibly duplicate event", ex);
            }
        });
    }
}
