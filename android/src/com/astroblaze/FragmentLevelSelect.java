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
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;

import org.jetbrains.annotations.NotNull;

public class FragmentLevelSelect extends Fragment {
    private ShipPreviewActor preview;
    private ViewPager pagerLevels;
    private ViewPager pagerShips;
    private TextView tvSwipeLeft;
    private TextView tvSwipeRight;
    private float prevPosition = 0f;

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
                bundle.putInt("level", FragmentLevelSelect.this.pagerLevels.getCurrentItem());
                bundle.putInt("ship", FragmentLevelSelect.this.pagerShips.getCurrentItem());

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

        tvSwipeLeft = view.findViewById(R.id.tvLevelLeft);
        tvSwipeRight = view.findViewById(R.id.tvLevelRight);

        pagerLevels = view.findViewById(R.id.pagerLevels);
        pagerShips = view.findViewById(R.id.pagerShips);

        pagerLevels.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                FragmentLevelSelect.this.refreshSwipeButtons(position);
            }
        });
        pagerShips.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                preview.setSlide(position, positionOffset - prevPosition);
                prevPosition = positionOffset;
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                FragmentLevelSelect.this.refreshSwipeButtons(position);
            }
        });

        PagerAdapter pagerLevelsAdapter = new LevelsPagerAdapter(getChildFragmentManager());
        PagerAdapter pagerShipsAdapter = new ShipsPagerAdapter(getChildFragmentManager());

        pagerLevels.setAdapter(pagerLevelsAdapter);
        pagerShips.setAdapter(pagerShipsAdapter);

        refreshSwipeButtons(pagerLevels.getCurrentItem());
    }

    public void refreshSwipeButtons(int position) {
        tvSwipeLeft.setVisibility(position >= 1 ? View.VISIBLE : View.INVISIBLE);
        tvSwipeRight.setVisibility(position < AstroblazeGame.getInstance().getMaxLevel()
                ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        pagerLevels.setCurrentItem(AstroblazeGame.getInstance().getMaxLevel());
    }

    @Override
    public void onStart() {
        super.onStart();
        Gdx.app.log("FragmentLevelSelect", "onStart");
        preview = AstroblazeGame.getInstance().gameScreen.getShipPreview();
        Vector3 worldPos = new Vector3();
        if (AstroblazeGame.getInstance().getScene().getXZIntersection(
                Gdx.graphics.getWidth() * 0.25f,
                Gdx.graphics.getHeight() * 0.5f, worldPos)) {
            preview.setSelectedPosition(worldPos);
        }
        preview.setVisible(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        Gdx.app.log("FragmentLevelSelect", "onStop");
        AstroblazeGame.getInstance().gameScreen.getShipPreview()
                .setVisible(false);
    }

    private static class LevelsPagerAdapter extends FragmentPagerAdapter {
        public LevelsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            return new FragmentLevel(position);
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

    private class ShipsPagerAdapter extends FragmentPagerAdapter {
        public ShipsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            return new FragmentShip(position);
        }

        @Override
        public void setPrimaryItem(@NonNull @NotNull ViewGroup container, int position, @NonNull @NotNull Object object) {
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public int getCount() {
            return ShipPreviewActor.VARIANT_COUNT;
        }
    }
}
