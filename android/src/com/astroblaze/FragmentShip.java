package com.astroblaze;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

public class FragmentShip extends Fragment implements IScoreChangeListener {
    private final PlayerShipVariant variant;
    private TextView tvDescription;
    private TextView tvStats;
    private Button btnUnlock;

    public FragmentShip() {
        variant = PlayerShipVariant.Scout;
        // Required empty public constructor
    }

    public FragmentShip(PlayerShipVariant variant) {
        this.variant = variant;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ship, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvDescription = requireView().findViewById(R.id.tvShipDescription);
        tvStats = requireView().findViewById(R.id.tvShipStats);
        btnUnlock = requireView().findViewById(R.id.btnUnlockShip);

        btnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AstroblazeGame.getInstance().unlockShip(variant);
            }
        });

        resetText();
    }

    @Override
    public void onResume() {
        super.onResume();
        AstroblazeGame.getInstance().addScoreChangeListener(this);
        resetText();
    }

    @Override
    public void onPause() {
        super.onPause();
        AstroblazeGame.getInstance().removeScoreChangeListener(this);
    }

    private void resetText() {
        btnUnlock.post(new Runnable() {
            @Override
            public void run() {
                btnUnlock.setVisibility(AstroblazeGame.getInstance().isShipUnlocked(variant.id)
                        ? View.INVISIBLE : View.VISIBLE);
                btnUnlock.setText(getString(R.string.unlockShip, (int) variant.price));
                btnUnlock.setEnabled(AstroblazeGame.getInstance().canUnlock(variant));

                tvStats.setText(getString(R.string.shipStats,
                        (int) variant.maxHp, variant.gunPorts, variant.missilePorts));

                switch (variant) {
                    default:
                    case Scout:
                        tvDescription.setText(getString(R.string.ship0));
                        break;
                    case Cruiser:
                        tvDescription.setText(getString(R.string.ship1));
                        break;
                    case Destroyer:
                        tvDescription.setText(getString(R.string.ship2));
                        break;
                }
            }
        });
    }

    @Override
    public void scoreChanged(float newMoney, float newScore) {
        resetText();
    }
}
