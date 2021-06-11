package com.astroblaze;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public class FragmentLevel extends Fragment {
    private final int level;

    public FragmentLevel(int level) {
        this.level = level;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_level, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        resetText();
    }

    private void resetText() {
        TextView tv = requireView().findViewById(R.id.tvShipDescription);
        switch (level) {
            case 0:
                tv.setText(getString(R.string.level0));
                break;
            case 1:
                tv.setText(getString(R.string.level1));
                break;
            default:
                tv.setText(getString(R.string.levelGeneric, level, 3000));
                break;
        }
    }
}