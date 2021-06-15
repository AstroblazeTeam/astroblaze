package com.astroblaze;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.badlogic.gdx.Gdx;

import java.util.ArrayList;

public class FragmentHUD extends Fragment implements IGUIRenderer, IScoreChangeListener {
    private final ArrayList<TextView> tvRenders = new ArrayList<>(16);
    private TextView tvMoney;
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
        this.tvMoney = view.findViewById(R.id.tvMoney);
        this.tvRenders.add(view.findViewById(R.id.tvRenderText1));
        this.tvRenders.add(view.findViewById(R.id.tvRenderText2));
        AstroblazeGame.getInstance().setGuiRenderer(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        AstroblazeGame.getInstance().removeOnScoreChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        AstroblazeGame.getInstance().addOnScoreChangeListener(this);
    }

    @Override
    public void renderText(int id, String text, float fontSize, float x, float y) {
        Gdx.app.log("FragmentRender", "renderText(" + id + ", '" + text + "', " +
                fontSize + ", " + x + ", " + y);

        if (view == null) {
            Gdx.app.error("FragmentGame", "renderText without initialized view");
        }

        final TextView tvRender = tvRenders.get(id);

        view.post(new Runnable() {
            @Override
            public void run() {
                tvRender.setText(text);
                tvRender.setTextSize(fontSize);
                tvRender.measure(0, 0);
                tvRender.setTranslationX(x - tvRender.getMeasuredWidth() * 0.5f);
                tvRender.setTranslationY(y - tvRender.getMeasuredHeight() * 0.5f);
            }
        });
    }

    @Override
    public void renderText(int id, int textId, float fontSize, float x, float y) {
        Gdx.app.log("FragmentRender", "renderText(" + id + ", '" + textId + "', " +
                fontSize + ", " + x + ", " + y);

        renderText(id, getString(textId), fontSize, x, y);
    }

    @Override
    public void backToLevelSelect() {
        this.requireView().post(new Runnable() {
            @Override
            public void run() {
                NavController nc = ((MainActivity) requireActivity()).getNavController();
                nc.popBackStack();
                nc.popBackStack();
            }
        });
    }

    @Override
    public void scoreChanged(float newMoney, float newScore) {
        tvMoney.post(new Runnable() {
            @Override
            public void run() {
                tvMoney.setText(getString(R.string.moneyPrint, (int) newMoney));
            }
        });
    }
}