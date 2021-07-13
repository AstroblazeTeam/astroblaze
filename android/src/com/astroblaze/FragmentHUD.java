package com.astroblaze;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;

import com.astroblaze.Interfaces.IGUIRenderer;
import com.astroblaze.Interfaces.TranslatedStringId;
import com.astroblaze.Rendering.EnemyType;
import com.badlogic.gdx.Gdx;

import java.util.*;

/**
 * This class handles fragment_hud.xml - one of the layers above LibGDX rendering layer
 * This also implements IGUIRenderer to bridge Android localization and UI into LibGDX
 */
public class FragmentHUD extends Fragment implements IGUIRenderer {
    private final ArrayList<TextView> tvRenders = new ArrayList<>(16);
    private View view;

    public FragmentHUD() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hud, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.view = view;
        this.tvRenders.add(view.findViewById(R.id.tvRenderText1));
        this.tvRenders.add(view.findViewById(R.id.tvRenderText2));
    }

    @Override
    public void onPause() {
        super.onPause();
        AstroblazeGame.getInstance().setGuiRenderer(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        AstroblazeGame.getInstance().setGuiRenderer(this);
    }

    // next functions are a bridge from game loop to android views

    @Override
    public void renderText(int id, String text, float fontSize, float x, float y) {
        Gdx.app.log("FragmentRender", "renderText(" + id + ", '" + text + "', " +
                fontSize + ", " + x + ", " + y);

        if (view == null) {
            Gdx.app.error("FragmentGame", "renderText without initialized view");
            return;
        }

        final TextView tvRender = tvRenders.get(id);

        view.post(() -> {
            tvRender.setText(text);
            tvRender.setTextSize(fontSize);
            tvRender.measure(0, 0);
            tvRender.setTranslationX(x - tvRender.getMeasuredWidth() * 0.5f);
            tvRender.setTranslationY(y - tvRender.getMeasuredHeight() * 0.5f);
        });
    }

    @Override
    public String getTranslatedString(TranslatedStringId id) {
        switch (id) {
            case LevelStartReady:
                return getString(R.string.LevelStartReady);
            case LevelStartSet:
                return getString(R.string.LevelStartSet);
            case LevelStartGo:
                return getString(R.string.LevelStartGo);
            case LevelComplete:
                return getString(R.string.LevelComplete);
            case BossIncoming:
                return getString(R.string.BossIncoming);
            case MiniBossIncoming:
                return getString(R.string.MiniBossIncoming);
            case TutorialTouchScreenToMove:
                return getString(R.string.TutorialTouchScreenToMove);
            case TutorialPrimaryWeapons:
                return getString(R.string.TutorialPrimaryWeapons);
            case TutorialDodgeBulletsAndEnemies:
                return getString(R.string.TutorialDodgeBulletsAndEnemies);
            case TutorialUseButtonToFireMissiles:
                return getString(R.string.TutorialUseButtonToFireMissiles);
            case TutorialUseButtonToFireLasers:
                return getString(R.string.TutorialUseButtonToFireLasers);
            case TutorialBonusCash:
                return getString(R.string.TutorialBonusCash);
            case TutorialBonusLife:
                return getString(R.string.TutorialBonusLife);
            case TutorialBonusLaser:
                return getString(R.string.TutorialBonusLaser);
            case TutorialBonusShield:
                return getString(R.string.TutorialBonusShield);
            case TutorialBonusMissiles:
                return getString(R.string.TutorialBonusMissiles);
            case TutorialComplete:
                return getString(R.string.TutorialComplete);
            case Invalid:
            default:
                return id.toString();

        }
    }

    @Override
    public String getTranslatedEnemyName(EnemyType type) {
        switch (type) {
            case Boss:
                return getString(R.string.enemyBoss);
            case Simple:
                return getString(R.string.enemySimple);
            case Rammer:
                return getString(R.string.enemyRammer);
            case SineWave:
                return getString(R.string.enemyWavy);
            case MiniBoss1:
                return getString(R.string.enemyMiniboss1);
            case MoneyDrop:
                return getString(R.string.enemyMoneyDrop);
            case TrainingDummy:
                return getString(R.string.enemyTrainingDummy);
            default:
                return type.name();
        }
    }

    @Override
    public boolean isDebuggable() {
        return (view.getContext().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    @Override
    public void navigateToGameOver() {
        tvRenders.get(0).post(() -> {
            NavController nc = ((MainActivity) requireActivity()).getNavController();

            nc.popBackStack();
            nc.popBackStack();
            nc.navigate(R.id.action_fragmentLevelSelect_to_fragmentGameOver);
        });
    }

    @Override
    public void navigateToLevelComplete() {
        tvRenders.get(0).post(() -> {
            NavController nc = ((MainActivity) requireActivity()).getNavController();

            nc.popBackStack();
            nc.popBackStack();
            nc.navigate(R.id.action_fragmentLevelSelect_to_fragmentLevelComplete);
        });
    }

    @Override
    public boolean isRightToLeft() {
        return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) != View.LAYOUT_DIRECTION_LTR;
    }
}