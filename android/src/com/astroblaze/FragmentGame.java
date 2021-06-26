package com.astroblaze;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import com.astroblaze.Interfaces.IUIChangeListener;
import com.astroblaze.Rendering.PlayerShip;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FragmentGame extends Fragment implements IUIChangeListener {
    private TextView tvSpecial1;
    private TextView tvSpecial2;

    public FragmentGame() {
        // Required empty public constructor
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

        tvSpecial1 = view.findViewById(R.id.tvExtra1Text);
        tvSpecial2 = view.findViewById(R.id.tvExtra2Text);

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

    @Override
    public void onStart() {
        super.onStart();
        AstroblazeGame.getInstance().addUIChangeListener(this);
    }

    @Override
    public void onHpChanged(PlayerShip playerShip, float oldHp, float newHp) {
    }

    @Override
    public void onHpEnabled(PlayerShip playerShip, boolean enabled) {
    }

    @Override
    public void onSpecialTextChanged(PlayerShip playerShip, String text1, String text2) {
        tvSpecial1.post(new Runnable() {
            @Override
            public void run() {
                tvSpecial1.setText(text1);
                tvSpecial2.setText(text2);
                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(tvSpecial1,
                        1, 36, 1, TypedValue.COMPLEX_UNIT_DIP);

            }
        });
    }
}