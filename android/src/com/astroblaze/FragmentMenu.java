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
                (int) AstroblazeGame.getPlayerState().getPlayerScore()));

        HiscoresController.submitRank(new HiscoresController.RunnableResponseHandler<Integer>() {
            @Override
            public void run() {
                if (this.response <= 0) return; // silently fail
                tvRank.post(() ->
                        tvRank.setText(getString(R.string.rankPrint,
                                (int) AstroblazeGame.getPlayerState().getPlayerScore(), String.valueOf(this.response))));
            }
        }, true);

        // menu -> level select
        view.findViewById(R.id.btnStart).setOnClickListener(v
                -> NavHostFragment.findNavController(FragmentMenu.this)
                .navigate(R.id.action_fragmentMenu_to_fragmentLevelSelect));

        // menu -> hiscores
        view.findViewById(R.id.btnHiscores).setOnClickListener(v
                -> NavHostFragment.findNavController(FragmentMenu.this)
                .navigate(R.id.action_fragmentMenu_to_fragmentHiscores));

        // menu -> options
        view.findViewById(R.id.btnOptions).setOnClickListener(v
                -> NavHostFragment.findNavController(FragmentMenu.this)
                .navigate(R.id.action_fragmentMenu_to_fragmentOptions));

        // exit button
        view.findViewById(R.id.btnExitToMenu).setOnClickListener(v
                -> requireActivity().finish());
    }
}