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
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.astroblaze.GdxActors.ShipPreviewActor;
import com.astroblaze.Interfaces.IPlayerStateChangedListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;

public class FragmentLevelSelect extends Fragment implements IPlayerStateChangedListener {
    private ShipPreviewActor preview;
    private ViewPager2 pagerLevels;
    private ViewPager2 pagerShips;
    private TextView tvLevelSwipeLeft;
    private TextView tvLevelSwipeRight;
    private TextView tvShipSwipeLeft;
    private TextView tvShipSwipeRight;
    private Button btnPlay;
    private float prevShipSliderPosition = 0f;
    private final ViewPager2.OnPageChangeCallback soundCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            AstroblazeGame.getSoundController().playUISwapSound();
        }
    };

    public FragmentLevelSelect() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_levelselect, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnPlay = view.findViewById(R.id.btnPlay);
        // (play) level select -> pause (instantly skips to game fragment if startGame param is true)
        btnPlay.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean("startGame", true);
            bundle.putInt("level", FragmentLevelSelect.this.pagerLevels.getCurrentItem());
            bundle.putInt("ship", FragmentLevelSelect.this.pagerShips.getCurrentItem());

            AstroblazeGame.getSoundController().playUIConfirm();

            NavHostFragment.findNavController(FragmentLevelSelect.this)
                    .navigate(R.id.action_fragmentLevelSelect_to_fragmentPause, bundle);
        });

        // back button
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            AstroblazeGame.getSoundController().playUINegative();
            NavHostFragment.findNavController(FragmentLevelSelect.this)
                    .popBackStack();
        });

        tvLevelSwipeLeft = view.findViewById(R.id.tvLevelLeft);
        tvLevelSwipeRight = view.findViewById(R.id.tvLevelRight);

        tvShipSwipeLeft = view.findViewById(R.id.tvShipLeft);
        tvShipSwipeRight = view.findViewById(R.id.tvShipRight);

        pagerLevels = view.findViewById(R.id.pagerLevels);
        pagerShips = view.findViewById(R.id.pagerShips);

        pagerLevels.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                FragmentLevelSelect.this.refreshLevelSwipeButtons(position);
            }
        });

        pagerShips.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (preview != null) {
                    preview.setSlide(position, positionOffset - prevShipSliderPosition);
                    prevShipSliderPosition = positionOffset;
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                FragmentLevelSelect.this.refreshShipSwipeButtons(position);
            }
        });

        pagerLevels.setAdapter(new LevelsPagerAdapter(requireParentFragment()));
        pagerShips.setAdapter(new ShipsPagerAdapter(requireParentFragment()));

        refreshLevelSwipeButtons(pagerLevels.getCurrentItem());
    }

    public void refreshLevelSwipeButtons(int position) {
        tvLevelSwipeLeft.setVisibility(position >= 1 ? View.VISIBLE : View.INVISIBLE);
        tvLevelSwipeRight.setVisibility(position < AstroblazeGame.getPlayerState().getMaxLevel()
                ? View.VISIBLE : View.INVISIBLE);
    }

    public void refreshShipSwipeButtons(int position) {
        tvShipSwipeLeft.setVisibility(position >= 1 ? View.VISIBLE : View.INVISIBLE);
        tvShipSwipeRight.setVisibility(position < ShipPreviewActor.getVariantCount() - 1
                ? View.VISIBLE : View.INVISIBLE);
        PlayerShipVariant variant = PlayerShipVariant.values()[position];
        btnPlay.setEnabled(AstroblazeGame.getPlayerState().isShipVariantUnlocked(variant));
    }

    @Override
    public void onResume() {
        super.onResume();
        requireView().postDelayed(() -> {
            preview = AstroblazeGame.getInstance().gameScreen.getShipPreview();
            Vector3 worldPos = new Vector3();

            final float xFrac = AstroblazeGame.getInstance().getGuiRenderer().isRightToLeft() ?
                    0.75f : 0.25f;
            if (AstroblazeGame.getInstance().getScene().getXZIntersection(
                    Gdx.graphics.getWidth() * xFrac,
                    Gdx.graphics.getHeight() * 0.5f, worldPos)) {
                preview.setSelectedPosition(worldPos);
            }
            preview.setVisible(true);

            PlayerState state = AstroblazeGame.getPlayerState();

            state.addPlayerStateChangeListener(FragmentLevelSelect.this);

            pagerShips.setOffscreenPageLimit(10);
            pagerLevels.setOffscreenPageLimit(5);

            pagerShips.setCurrentItem(state.getLastSelectedShip(), false);
            pagerLevels.setCurrentItem(state.getMaxLevel(), false);
        }, 100);

        // wait 300ms before registering sound effect callbacks
        requireView().postDelayed(() -> {
            pagerLevels.registerOnPageChangeCallback(this.soundCallback);
            pagerShips.registerOnPageChangeCallback(this.soundCallback);
        }, 400);
    }

    @Override
    public void onPause() {
        super.onPause();
        AstroblazeGame.getPlayerState().removePlayerStateChangeListener(this);
        AstroblazeGame.getPlayerState().setLastSelectedShip(pagerShips.getCurrentItem());
        AstroblazeGame.getInstance().gameScreen.getShipPreview().setVisible(false);

        pagerLevels.unregisterOnPageChangeCallback(this.soundCallback);
        pagerShips.unregisterOnPageChangeCallback(this.soundCallback);
    }

    @Override
    public void onStateChanged(PlayerState state) {
        pagerLevels.post(() -> {
            refreshLevelSwipeButtons(pagerLevels.getCurrentItem());
            refreshShipSwipeButtons(pagerShips.getCurrentItem());
        });
    }

    private static class LevelsPagerAdapter extends FragmentStateAdapter {
        public LevelsPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return new FragmentLevel(position);
        }

        @Override
        public int getItemCount() {
            return AstroblazeGame.getPlayerState().getMaxLevel() + 1;
        }
    }

    private static class ShipsPagerAdapter extends FragmentStateAdapter {
        public ShipsPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return new FragmentShip(ShipPreviewActor.getVariant(position));
        }

        @Override
        public int getItemCount() {
            return ShipPreviewActor.VARIANT_COUNT;
        }
    }
}
