package com.astroblaze;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

import org.jetbrains.annotations.NotNull;

public class FragmentLevelSelect extends Fragment {
    private ShipPreviewActor preview;

    public FragmentLevelSelect() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_levelselect, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // (play) level select -> pause (instantly skips to game fragment if startGame param is true)
        view.findViewById(R.id.btnPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("startGame", true);
                bundle.putInt("level", 0);

                NavHostFragment.findNavController(FragmentLevelSelect.this)
                        .navigate(R.id.action_fragmentLevelSelect_to_fragmentPause, bundle);
            }
        });

        // menu -> shop
        view.findViewById(R.id.btnOpenShop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(FragmentLevelSelect.this)
                        .navigate(R.id.action_fragmentLevelSelect_to_shopFragment);
            }
        });

        // back button
        view.findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(FragmentLevelSelect.this)
                        .popBackStack();
            }
        });

        view.findViewById(R.id.btnPrevShip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentLevelSelect.this.preview.prevShip();
            }
        });

        view.findViewById(R.id.btnNextShip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentLevelSelect.this.preview.nextShip();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Gdx.app.log("FragmentLevelSelect", "onStart");

        ModelInstance instance = new ModelInstance(Assets.asset(Assets.spaceShip2));
        preview = AstroblazeGame.getInstance().gameScreen.getShipPreview();
        preview.setModelInstance(instance);
        preview.scale.set(0.5f, 0.5f, 0.5f);
        Vector3 worldPos = new Vector3();
        if (AstroblazeGame.getInstance().getScene().getXZIntersection(
                Gdx.graphics.getWidth() * 0.25f,
                Gdx.graphics.getHeight() * 0.5f, worldPos)) {
            preview.position.set(worldPos);
        }
        preview.applyTRS();
    }

    @Override
    public void onStop() {
        super.onStop();
        Gdx.app.log("FragmentLevelSelect", "onStop");
        AstroblazeGame.getInstance().gameScreen.getShipPreview()
                .setModelInstance(null);
    }
}
