package com.astroblaze;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager.widget.ViewPager;

import com.astroblaze.GdxActors.ShipPreviewActor;
import com.astroblaze.Interfaces.IPlayerStateChangedListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;

import org.jetbrains.annotations.NotNull;

public class FragmentLevelSelect extends Fragment implements IPlayerStateChangedListener {
    private ShipPreviewActor preview;
    private ViewPager pagerLevels;
    private ViewPager pagerShips;
    private TextView tvLevelSwipeLeft;
    private TextView tvLevelSwipeRight;
    private TextView tvShipSwipeLeft;
    private TextView tvShipSwipeRight;
    private Button btnPlay;
    private float prevShipSliderPosition = 0f;

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

        btnPlay = view.findViewById(R.id.btnPlay);
        // (play) level select -> pause (instantly skips to game fragment if startGame param is true)
        btnPlay.setOnClickListener(new View.OnClickListener() {
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

        // back button
        view.findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(FragmentLevelSelect.this)
                        .popBackStack();
            }
        });

        tvLevelSwipeLeft = view.findViewById(R.id.tvLevelLeft);
        tvLevelSwipeRight = view.findViewById(R.id.tvLevelRight);

        tvShipSwipeLeft = view.findViewById(R.id.tvShipLeft);
        tvShipSwipeRight = view.findViewById(R.id.tvShipRight);

        pagerLevels = view.findViewById(R.id.pagerLevels);
        pagerShips = view.findViewById(R.id.pagerShips);

        pagerLevels.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                FragmentLevelSelect.this.refreshLevelSwipeButtons(position);
            }
        });

        pagerShips.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                preview.setSlide(position, positionOffset - prevShipSliderPosition);
                prevShipSliderPosition = positionOffset;
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                FragmentLevelSelect.this.refreshShipSwipeButtons(position);
                AstroblazeGame.getPlayerState().setLastSelectedShip(position);
            }
        });

        pagerLevels.setAdapter(new LevelsPagerAdapter(getChildFragmentManager()));
        pagerShips.setAdapter(new ShipsPagerAdapter(getChildFragmentManager()));

        refreshLevelSwipeButtons(pagerLevels.getCurrentItem());
    }

    public void refreshLevelSwipeButtons(int position) {
        tvLevelSwipeLeft.setVisibility(position >= 1 ? View.VISIBLE : View.INVISIBLE);
        tvLevelSwipeRight.setVisibility(position < AstroblazeGame.getPlayerState().getMaxLevel()
                ? View.VISIBLE : View.INVISIBLE);
    }

    public void refreshShipSwipeButtons(int position) {
        tvShipSwipeLeft.setVisibility(position >= 1 ? View.VISIBLE : View.INVISIBLE);
        tvShipSwipeRight.setVisibility(position < preview.getVariantCount() - 1
                ? View.VISIBLE : View.INVISIBLE);
        PlayerShipVariant variant = PlayerShipVariant.values()[position];
        btnPlay.setEnabled(AstroblazeGame.getPlayerState().isShipVariantUnlocked(variant));
    }

    @Override
    public void onResume() {
        super.onResume();
        preview = AstroblazeGame.getInstance().gameScreen.getShipPreview();
        Vector3 worldPos = new Vector3();
        if (AstroblazeGame.getInstance().getScene().getXZIntersection(
                Gdx.graphics.getWidth() * 0.25f,
                Gdx.graphics.getHeight() * 0.5f, worldPos)) {
            preview.setSelectedPosition(worldPos);
        }
        preview.setVisible(true);

        PlayerState state = AstroblazeGame.getPlayerState();

        state.addPlayerStateChangeListener(this);
        pagerShips.setCurrentItem(state.getLastSelectedShip(), true);
        pagerLevels.setCurrentItem(state.getMaxLevel(), true);
    }

    @Override
    public void onPause() {
        super.onPause();
        AstroblazeGame.getPlayerState().removePlayerStateChangeListener(this);
        AstroblazeGame.getInstance().gameScreen.getShipPreview().setVisible(false);
    }

    @Override
    public void onStateChanged(PlayerState state) {
        pagerLevels.post(() -> {
            refreshLevelSwipeButtons(pagerLevels.getCurrentItem());
            refreshShipSwipeButtons(pagerShips.getCurrentItem());
        });
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
            return AstroblazeGame.getPlayerState().getMaxLevel() + 1;
        }
    }

    // not static - needs reference to preview actor
    private class ShipsPagerAdapter extends FragmentPagerAdapter {
        public ShipsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            return new FragmentShip(preview.getVariant(position));
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
