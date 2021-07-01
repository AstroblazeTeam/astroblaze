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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import org.jetbrains.annotations.NotNull;

public class FragmentOptions extends Fragment {
    private SoundController soundController;
    private MusicController musicController;

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

        musicController = AstroblazeGame.getMusicController();
        soundController = AstroblazeGame.getSoundController();

        view.findViewById(R.id.btnExitToMenu).setOnClickListener(v
                -> NavHostFragment.findNavController(FragmentOptions.this).popBackStack());

        SeekBar sbMusic = view.findViewById(R.id.seekBarMusic);
        sbMusic.setProgress((int) (musicController.getVolume() * 100f));
        sbMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                musicController.setVolume(progress / 100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SeekBar sbSound = view.findViewById(R.id.seekBarSound);
        sbSound.setProgress((int) (soundController.getVolume() * 100f));
        sbSound.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                soundController.setVolume(progress / 100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SwitchCompat cbShake = view.findViewById(R.id.checkBox);
        cbShake.setChecked(AstroblazeGame.getPlayerState().getScreenShake());
        cbShake.setOnCheckedChangeListener((buttonView, isChecked)
                -> AstroblazeGame.getPlayerState().setScreenShake(isChecked));
    }
}