package com.astroblaze;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

import org.jetbrains.annotations.NotNull;

public class FragmentLevelSelect extends Fragment {
    private ShipPreviewActor preview;
    private ViewPager pager;
    private TextView tvSwipeLeft;
    private TextView tvSwipeRight;

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
                bundle.putInt("level", FragmentLevelSelect.this.pager.getCurrentItem());

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

        tvSwipeLeft = view.findViewById(R.id.tvLevelLeft);
        tvSwipeRight = view.findViewById(R.id.tvLevelRight);

        PagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
        pager = view.findViewById(R.id.pagerLevels);
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                FragmentLevelSelect.this.refreshSwipeButtons(position);
            }
        });
        pager.setAdapter(pagerAdapter);
        int openLevel = AstroblazeGame.getInstance().getMaxLevel() - 1;
        if (openLevel == 1) { // don't automatically skip tutorial level at first
            pager.setCurrentItem(AstroblazeGame.getInstance().getMaxLevel());
        }
        refreshSwipeButtons(pager.getCurrentItem());
    }

    public void refreshSwipeButtons(int position) {
        tvSwipeLeft.setVisibility(position >= 1 ? View.VISIBLE : View.INVISIBLE);
        tvSwipeRight.setVisibility(position < AstroblazeGame.getInstance().getMaxLevel()
                ? View.VISIBLE : View.INVISIBLE);
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

    private static class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            return new LevelFragment(position);
        }

        @Override
        public void setPrimaryItem(@NonNull @NotNull ViewGroup container, int position, @NonNull @NotNull Object object) {
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public int getCount() {
            return AstroblazeGame.getInstance().getMaxLevel() + 1;
        }
    }
}
