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
import com.astroblaze.Interfaces.IPlayerStateChangedListener;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class FragmentLevel extends Fragment implements IPlayerStateChangedListener {
    public boolean canSwipeLeft;
    public boolean canSwipeRight;

    private final int level;
    private TextView tvLevelDescription;
    private TextView tvLevelSwipeLeft;
    private TextView tvLevelSwipeLabel;
    private TextView tvLevelSwipeRight;

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

        tvLevelSwipeLeft = view.findViewById(R.id.tvLevelLeft);
        tvLevelSwipeLabel = view.findViewById(R.id.tvLevelSwipe);
        tvLevelSwipeRight = view.findViewById(R.id.tvLevelRight);
        tvLevelDescription = view.findViewById(R.id.tvLevelDescription);

        resetText(tvLevelDescription);
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

    private void resetText(TextView tv) {
        canSwipeLeft = level > 0;
        canSwipeRight = level < AstroblazeGame.getPlayerState().getMaxLevel();

        tvLevelSwipeLeft.setVisibility(canSwipeLeft ? View.VISIBLE : View.INVISIBLE);
        tvLevelSwipeRight.setVisibility(canSwipeRight ? View.VISIBLE : View.INVISIBLE);
        tvLevelSwipeLabel.setVisibility(canSwipeLeft || canSwipeRight ? View.VISIBLE : View.INVISIBLE);

        String pirateName = getString(R.string.pirate0 + (level % 44)); // 44 pirate names in pool
        Random rng = new Random(AstroblazeGame.getPlayerState().getSeed());
        int reward = (int) LevelControllerActor.getLevelReward(level);
        int levelPick = 0;
        for (int i = 0; i < level; i++) {
            int nextLevelPick;
            do { // loop makes sure 2 adjacent levels don't have same description.
                nextLevelPick = rng.nextInt(8) + 1; // 8 level descriptions in pool
            } while (levelPick == nextLevelPick);
            levelPick = nextLevelPick;
        }

        switch (levelPick) {
            case 0:
                tv.setText(getString(R.string.level0, reward));
                break;
            default:
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
            case 5:
                tv.setText(getString(R.string.level5, level, reward));
                break;
            case 6:
                tv.setText(getString(R.string.level6, level, pirateName, reward));
                break;
            case 7:
                tv.setText(getString(R.string.level7, level, pirateName, reward));
                break;
            case 8:
                tv.setText(getString(R.string.level8, level, pirateName, reward));
                break;
        }
    }

    @Override
    public void onStateChanged(PlayerState state) {
        tvLevelDescription.post(() -> resetText(tvLevelDescription));
    }
}