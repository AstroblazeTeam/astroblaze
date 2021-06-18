package com.astroblaze;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpParametersUtils;

import java.util.HashMap;
import java.util.Map;

public class FragmentMenu extends Fragment {
    TextView tvRank;

    public FragmentMenu() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvRank = view.findViewById(R.id.tvRank);
        tvRank.setText(getString(R.string.rankPrintLoading,
                (int)AstroblazeGame.getPlayerState().getPlayerScore()));

        HiscoresController.fetch(new HiscoresController.RunnableResponseHandler() {
            @Override
            public void run() {
                tvRank.post(() ->
                        tvRank.setText(getString(R.string.rankPrint,
                                (int)AstroblazeGame.getPlayerState().getPlayerScore(), this.response)));
            }
        });

        // menu -> level select
        view.findViewById(R.id.btnStart).setOnClickListener(v -> NavHostFragment.findNavController(FragmentMenu.this)
                .navigate(R.id.action_fragmentMenu_to_fragmentLevelSelect));

        // menu -> hiscores
        view.findViewById(R.id.btnHiscores).setOnClickListener(v -> NavHostFragment.findNavController(FragmentMenu.this)
                .navigate(R.id.action_fragmentMenu_to_fragmentHiscores));

        // menu -> options
        view.findViewById(R.id.btnOptions).setOnClickListener(v -> NavHostFragment.findNavController(FragmentMenu.this)
                .navigate(R.id.action_fragmentMenu_to_fragmentOptions));

        // exit button
        view.findViewById(R.id.btnExitToMenu).setOnClickListener(v -> {
            requireActivity().finish();
        });
    }
}