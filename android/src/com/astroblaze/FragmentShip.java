package com.astroblaze;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

public class FragmentShip extends Fragment {
    private PlayerShipVariant variant;

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
        resetText();
    }

    private void resetText() {
        TextView tvDescription = requireView().findViewById(R.id.tvShipDescription);
        TextView tvStats = requireView().findViewById(R.id.tvShipStats);

        tvStats.setText(getString(R.string.shipStats,
                (int)variant.maxHp, variant.gunPorts, variant.missilePorts));

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
}
