package com.astroblaze;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
        tvRank.setClickable(true);
        tvRank.setOnClickListener(v -> showChangeNameDialog());
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

    private void showChangeNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.changePilotName));
        View viewInflated = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_pilot_name_change, (ViewGroup) getView(), false);
        final EditText input = (EditText) viewInflated.findViewById(R.id.input_pilot_name);
        input.setText(AstroblazeGame.getPlayerState().getName());
        builder.setView(viewInflated);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            AstroblazeGame.getPlayerState().setName(input.getText().toString());
            HiscoresController.submitRank(new HiscoresController.RunnableResponseHandler<Integer>() {
                @Override
                public void run() {
                    if (this.response <= 0) return; // silently fail
                    tvRank.post(() ->
                            tvRank.setText(getString(R.string.rankPrint,
                                    (int) AstroblazeGame.getPlayerState().getPlayerScore(), String.valueOf(this.response))));
                }
            }, false);
            dialog.dismiss();
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
        AlertDialog alert = builder.show();
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
}