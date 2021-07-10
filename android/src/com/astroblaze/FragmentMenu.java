package com.astroblaze;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.astroblaze.Interfaces.IPlayerStateChangedListener;

public class FragmentMenu extends Fragment implements IPlayerStateChangedListener {
    private TextView tvPilotName;
    private TextView tvRank;
    private TextView tvScore;
    private TextView tvMoney;
    private Button btnChangeName;

    private ValueAnimator scoreAnimator;
    private ValueAnimator moneyAnimator;

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

        btnChangeName = view.findViewById(R.id.btnChangePilotName);
        tvPilotName = view.findViewById(R.id.tvPilotName);
        tvRank = view.findViewById(R.id.tvRank);
        tvScore = view.findViewById(R.id.tvScore);
        tvMoney = view.findViewById(R.id.tvMoneyVal);

        moneyAnimator = ValueAnimator.ofInt(0, 0);
        moneyAnimator.setDuration(2500); // animate over 1.5 secs
        moneyAnimator.addUpdateListener(valueAnimator
                -> tvMoney.setText(getString(R.string.moneyPrint, (int) valueAnimator.getAnimatedValue())));
        moneyAnimator.start();

        scoreAnimator = ValueAnimator.ofInt(0, 0);
        scoreAnimator.setDuration(2500); // animate over 1.5 secs
        scoreAnimator.addUpdateListener(valueAnimator
                -> tvScore.setText(String.valueOf(valueAnimator.getAnimatedValue())));
        scoreAnimator.start();

        // btnChangeName -> AlertDialog
        btnChangeName.setOnClickListener(v -> {
            AstroblazeGame.getSoundController().playUIPositive();
            showChangeNameDialog();
        });

        HiscoresController.submitRank(new HiscoresController.RunnableResponseHandler<Integer>() {
            @Override
            public void run() {
                if (this.response <= 0) return; // silently fail
                tvRank.post(() -> tvRank.setText(String.valueOf(this.response)));
            }
        }, true);

        // menu -> level select
        view.findViewById(R.id.btnStart).setOnClickListener(v -> {
            AstroblazeGame.getSoundController().playUIConfirm();
            NavHostFragment.findNavController(FragmentMenu.this)
                    .navigate(R.id.action_fragmentMenu_to_fragmentLevelSelect);
        });

        // menu -> hiscores
        view.findViewById(R.id.btnHiscores).setOnClickListener(v -> {
            AstroblazeGame.getSoundController().playUIPositive();
            NavHostFragment.findNavController(FragmentMenu.this)
                    .navigate(R.id.action_fragmentMenu_to_fragmentHiscores);
        });

        // menu -> options
        view.findViewById(R.id.btnOptions).setOnClickListener(v -> {
            AstroblazeGame.getSoundController().playUIPositive();
            NavHostFragment.findNavController(FragmentMenu.this)
                    .navigate(R.id.action_fragmentMenu_to_fragmentOptions);
        });

        // exit button
        view.findViewById(R.id.btnExitToMenu).setOnClickListener(v -> {
            AstroblazeGame.getSoundController().playUINegative();
            ((MainActivity) requireActivity()).exit();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        AstroblazeGame.getPlayerState().addPlayerStateChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        AstroblazeGame.getPlayerState().removePlayerStateChangeListener(this);
    }

    private void showChangeNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View viewInflated = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_pilot_name_change, (ViewGroup) getView(), false);
        final EditText input = viewInflated.findViewById(R.id.input_pilot_name);
        input.setText(AstroblazeGame.getPlayerState().getName());

        builder.setView(viewInflated);
        builder.setCancelable(false);

        final AlertDialog alert = builder.create();
        alert.getWindow().getAttributes().windowAnimations = R.style.Theme_Astroblaze;
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alert.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                AstroblazeGame.getSoundController().playUINegative();
                dialog.dismiss();
                return true;
            }
            return false;
        });
        viewInflated.findViewById(R.id.btnSetPilotName).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AstroblazeGame.getPlayerState().setName(input.getText().toString());
                HiscoresController.submitRank(new HiscoresController.RunnableResponseHandler<Integer>() {
                    @Override
                    public void run() {
                        if (this.response <= 0) return; // silently fail
                        tvRank.post(() -> tvRank.setText(String.valueOf(this.response)));
                    }
                }, false);
                AstroblazeGame.getSoundController().playUIConfirm();
                alert.dismiss();
            }
        });
        viewInflated.findViewById(R.id.btnCancelPilotName).setOnClickListener(v -> {
            AstroblazeGame.getSoundController().playUINegative();
            alert.dismiss();
        });
        alert.show();
    }

    @Override
    public void onStateChanged(PlayerState state) {
        tvPilotName.post(() -> {
            tvPilotName.setText(state.getName());
            moneyAnimator.setIntValues((int) moneyAnimator.getAnimatedValue(), (int) state.getPlayerMoney());
            scoreAnimator.setIntValues((int) scoreAnimator.getAnimatedValue(), (int) state.getPlayerScore());
            if (!moneyAnimator.isRunning()) {
                moneyAnimator.start();
            }
            if (!scoreAnimator.isRunning()) {
                scoreAnimator.start();
            }
        });
    }
}