package com.astroblaze;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.astroblaze.GdxActors.LevelControllerActor;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class FragmentLevel extends Fragment {
    private final int level;

    public FragmentLevel() {
        // Required empty public constructor
        level = 0;
    }

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
        String pirateName = getString(R.string.pirate0 + (level % 44)); // 44 pirate names in pool
        Random rng = new Random(AstroblazeGame.getPlayerState().getSeed());
        int reward = (int) LevelControllerActor.getLevelReward(level);
        int levelPick = 0;
        for (int i = 0; i < level; i++) {
            int nextLevelPick;
            do { // loop makes sure 2 adjacent levels don't have same description.
                nextLevelPick = rng.nextInt(4) + 1; // 4 level descriptions in pool
            } while (levelPick == nextLevelPick);
            levelPick = nextLevelPick;
        }

        switch (levelPick) {
            case 0:
                tv.setText(getString(R.string.level0, reward));
                break;
            case 1:
                tv.setText(getString(R.string.level1, level, pirateName, reward));
                break;
            case 2:
                tv.setText(getString(R.string.level2, level, reward));
                break;
            case 3:
                tv.setText(getString(R.string.level3, level, reward));
                break;
            case 4:
                tv.setText(getString(R.string.level4, level, reward));
                break;
            default:
                tv.setText(getString(R.string.levelGeneric, level, reward));
                break;
        }
    }
}