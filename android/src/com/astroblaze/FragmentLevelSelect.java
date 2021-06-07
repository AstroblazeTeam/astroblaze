package com.astroblaze;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

public class FragmentLevelSelect extends Fragment {
    public FragmentLevelSelect() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_levelselect, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // (play) level select -> pause (instantly skips to game fragment if startGame param is true)
        view.findViewById(R.id.btnPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("startGame", true);
                bundle.putInt("level", 0);

                NavHostFragment.findNavController(FragmentLevelSelect.this)
                        .navigate(R.id.action_fragmentLevelSelect_to_fragmentPause, bundle);
            }
        });

        // menu -> shop
        view.findViewById(R.id.btnOpenShop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(FragmentLevelSelect.this)
                        .navigate(R.id.action_fragmentLevelSelect_to_shopFragment);
            }
        });

        // back button
        view.findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(FragmentLevelSelect.this)
                        .popBackStack();
            }
        });
    }
}
