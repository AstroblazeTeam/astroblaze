package com.astroblaze;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

public class FragmentOptions extends Fragment {
    private SoundController soundController;
    private MusicController musicController;

    private TextView tvMusicVolume;
    private TextView tvSoundVolume;
    private TextView tvUIVolume;

    public FragmentOptions() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final int progressSnapTo50 = 8;

        musicController = AstroblazeGame.getMusicController();
        soundController = AstroblazeGame.getSoundController();

        tvMusicVolume = view.findViewById(R.id.tvMusic);
        tvSoundVolume = view.findViewById(R.id.tvSound);
        tvUIVolume = view.findViewById(R.id.tvUI);

        view.findViewById(R.id.btnExitToMenu).setOnClickListener(v -> {
            AstroblazeGame.getSoundController().playUINegative();
            NavHostFragment.findNavController(FragmentOptions.this).popBackStack();
        });

        refreshTextValues();

        SeekBar sbMusic = view.findViewById(R.id.seekBarMusic);
        sbMusic.setProgress((int) (musicController.getVolume() * 100f));
        sbMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 50 - progressSnapTo50 && progress < 50 + progressSnapTo50) {
                    sbMusic.setProgress(50);
                    musicController.setVolume(0.5f);
                } else {
                    musicController.setVolume(progress / 100f);
                }
                refreshTextValues();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SeekBar sbSound = view.findViewById(R.id.seekBarSound);
        sbSound.setProgress((int) (soundController.getSfxVolume() * 100f));
        sbSound.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 50 - progressSnapTo50 && progress < 50 + progressSnapTo50) {
                    sbSound.setProgress(50);
                    soundController.setSfxVolume(0.5f);
                } else {
                    soundController.setSfxVolume(progress / 100f);
                }
                refreshTextValues();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SeekBar sbUI = view.findViewById(R.id.seekBarUI);
        sbUI.setProgress((int) (soundController.getUIVolume() * 100f));
        sbUI.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 50 - progressSnapTo50 && progress < 50 + progressSnapTo50) {
                    sbUI.setProgress(50);
                    soundController.setUIVolume(0.5f);
                } else {
                    soundController.setUIVolume(progress / 100f);
                }
                refreshTextValues();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SwitchCompat cbShake = view.findViewById(R.id.shake_switch);
        cbShake.setChecked(AstroblazeGame.getPlayerState().getScreenShake());
        cbShake.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AstroblazeGame.getPlayerState().setScreenShake(isChecked);
            AstroblazeGame.getSoundController().playUISwapSound();
        });

        SwitchCompat cbVibrate = view.findViewById(R.id.vibrate_switch);
        cbVibrate.setChecked(AstroblazeGame.getPlayerState().getVibrate());
        cbVibrate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AstroblazeGame.getPlayerState().setVibrate(isChecked);
            AstroblazeGame.getSoundController().playUISwapSound();
        });
    }

    private void refreshTextValues() {
        tvMusicVolume.setText(getString(R.string.music_volume, (int) (musicController.getVolume() * 100f)));
        tvSoundVolume.setText(getString(R.string.sound_volume, (int) (soundController.getSfxVolume() * 100f)));
        tvUIVolume.setText(getString(R.string.ui_volume, (int) (soundController.getUIVolume() * 100f)));
    }
}