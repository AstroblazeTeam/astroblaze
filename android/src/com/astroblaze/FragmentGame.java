package com.astroblaze;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.astroblaze.Interfaces.IPlayerStateChangedListener;
import com.astroblaze.Interfaces.IUIChangeListener;
import com.astroblaze.Rendering.PlayerShip;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FragmentGame extends Fragment implements IUIChangeListener, IPlayerStateChangedListener {
    private TextView tvMoney;
    private TextView tvSpecial1;
    private TextView tvSpecial2;

    private int lastAnimatedMoneyValue;
    private ValueAnimator moneyAnimator;

    public FragmentGame() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavHostFragment.findNavController(FragmentGame.this).addOnDestinationChangedListener((navController, navDestination, bundle) -> {
            if (navDestination.getId() == R.id.fragmentPause) {
                AstroblazeGame.getInstance().pauseGame();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @androidx.annotation.Nullable @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvSpecial1 = view.findViewById(R.id.tvExtra1Text);
        tvSpecial2 = view.findViewById(R.id.tvExtra2Text);
        tvMoney = view.findViewById(R.id.tvMoney);

        view.findViewById(R.id.btnExtra1).setOnTouchListener((v, event) -> {
            final PlayerShip ship = AstroblazeGame.getInstance().getScene().getPlayer();
            if (ship == null)
                return false;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                ship.setAutoFireMissiles(false);
                return v.performClick();
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ship.setAutoFireMissiles(true);
                return v.performClick();
            }
            return false;
        });

        view.findViewById(R.id.btnExtra2).setOnTouchListener((v, event) -> {
            final PlayerShip ship = AstroblazeGame.getInstance().getScene().getPlayer();
            if (ship == null)
                return false;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                ship.setAutoFireLaser(false);
                return v.performClick();
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ship.setAutoFireLaser(true);
                return v.performClick();
            }
            return false;
        });

        moneyAnimator = ValueAnimator.ofInt(0, (int) AstroblazeGame.getPlayerState().getPlayerMoney());
        moneyAnimator.setDuration(1500); // animate over 1.5 secs
        moneyAnimator.addUpdateListener(animator -> {
            if (FragmentGame.this.getContext() == null) {
                return; // fragment is detached, abort
            }
            lastAnimatedMoneyValue = (int) animator.getAnimatedValue();
            tvMoney.setText(getString(R.string.moneyPrint, lastAnimatedMoneyValue));
        });
        moneyAnimator.start();

        onSpecialTextChanged(null, "", "");
    }

    @Override
    public void onResume() {
        super.onResume();
        AstroblazeGame.getInstance().addUIChangeListener(this);
        AstroblazeGame.getPlayerState().addPlayerStateChangeListener(this);
        this.requireView().setLayoutDirection(AstroblazeGame.getInstance().getFlipHorizontal()
                ? View.LAYOUT_DIRECTION_RTL
                : View.LAYOUT_DIRECTION_LTR);
    }

    @Override
    public void onPause() {
        super.onPause();
        // pause was requested
        if (AstroblazeGame.getInstance().getScene().getLives() != 0) {
            NavHostFragment.findNavController(FragmentGame.this)
                    .popBackStack(R.id.fragmentPause, false);
            AstroblazeGame.getInstance().pauseGame();
        }
        // else we're actually just starting the game and this fragment
        // is transient in the navgraph
        AstroblazeGame.getInstance().removeUIChangeListener(this);
        AstroblazeGame.getPlayerState().removePlayerStateChangeListener(this);
    }

    @Override
    public void onStateChanged(PlayerState state) {
        tvMoney.post(() -> {
            if (getContext() == null) {
                return; // ignore state updates while not attached
            }
            moneyAnimator.setIntValues((int) moneyAnimator.getAnimatedValue(), (int) state.getPlayerMoney());
            if (!moneyAnimator.isRunning()) {
                moneyAnimator.start();
            }
        });
    }

    @Override
    public void onHpChanged(PlayerShip playerShip, float oldHp, float newHp) {
    }

    @Override
    public void onHpEnabled(PlayerShip playerShip, boolean enabled) {
    }

    @Override
    public void onSpecialTextChanged(PlayerShip playerShip, String text1, String text2) {
        tvSpecial1.post(() -> {
            tvSpecial1.setText(text1);
            tvSpecial2.setText(text2);
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(tvSpecial1,
                    1, 36, 1, TypedValue.COMPLEX_UNIT_DIP);
        });
    }
}