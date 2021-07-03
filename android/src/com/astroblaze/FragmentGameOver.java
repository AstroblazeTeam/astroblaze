package com.astroblaze;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FragmentGameOver extends Fragment {
    public FragmentGameOver() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gameover, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @androidx.annotation.Nullable @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnGameOverExit).setOnClickListener(v ->
                this.requireView().post(() -> {
                    AstroblazeGame.getSoundController().playUICancelSound();
                    NavController nc = ((MainActivity) requireActivity()).getNavController();
                    nc.popBackStack();
                }));
    }
}