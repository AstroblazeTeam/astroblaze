package com.astroblaze;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.badlogic.gdx.Gdx;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public class FragmentShip extends Fragment implements IPlayerStateChangedListener {
    private final PlayerShipVariant variant;
    private TextView tvDescription;
    private TextView tvStats;
    private Button btnAction;
    private MediaPlayer mp;

    public FragmentShip() {
        this.variant = PlayerShipVariant.Scout;
        // Required empty public constructor
    }

    public FragmentShip(PlayerShipVariant variant) {
        this.variant = variant;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.mp = MediaPlayer.create(getContext(), R.raw.cha_ching);
        return inflater.inflate(R.layout.fragment_ship, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvDescription = requireView().findViewById(R.id.tvShipDescription);
        tvStats = requireView().findViewById(R.id.tvShipStats);
        btnAction = requireView().findViewById(R.id.btnAction);
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

    private void resetText(PlayerState state) {
        btnAction.post(() -> {
            tvDescription.setText(getString(R.string.ship0 + variant.id));
            String hpText = getString(R.string.shipStatHp, (int) variant.getMaxHp(state));
            String hpModText = getString(R.string.shipStatBonus, new DecimalFormat("+#").format((variant.getUpgradeModifier(state, ShopItemType.ShieldUpgrade) - 1f) * 100f));
            String damageText = getString(R.string.shipStatDamage, (int) variant.getDamage(state));
            String damageModifier = getString(R.string.shipStatBonus, new DecimalFormat("+#").format((variant.getUpgradeModifier(state, ShopItemType.DamageUpgrade) - 1f) * 100f));
            String speedText = getString(R.string.shipStatSpeed, (int) variant.getSpeed(state));
            String speedModifier = getString(R.string.shipStatBonus, new DecimalFormat("+#").format((variant.getUpgradeModifier(state, ShopItemType.SpeedUpgrade) - 1f) * 100f));

            CharSequence combined = TextUtils.concat(
                    hpText, hpModText, "<br>",
                    damageText, damageModifier, "<br>",
                    speedText, speedModifier, "<br>"
            );
            tvStats.setText(Html.fromHtml(combined.toString()));

            if (!state.isShipVariantUnlocked(variant)) {
                btnAction.setEnabled(AstroblazeGame.getPlayerState().canUnlockShip(variant));
                btnAction.setText(getString(R.string.unlockShip, (int) variant.price));
                btnAction.setOnClickListener(v -> {
                    if (AstroblazeGame.getPlayerState().unlockShipVariant(variant)) {
                        mp.start();
                    }
                });
            } else {
                btnAction.setEnabled(true);
                btnAction.setText(getString(R.string.buyUpgrade));
                btnAction.setOnClickListener(v -> {
                    Fragment parentFragment = getParentFragment();
                    if (parentFragment == null) {
                        Gdx.app.error("FragmentShip", "Parent fragment was null!");
                        return;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putInt("variant", variant.id);

                    NavHostFragment.findNavController(parentFragment)
                            .navigate(R.id.action_fragmentLevelSelect_to_shopFragment, bundle);
                });
            }
        });
    }

    @Override
    public void onStateChanged(PlayerState state) {
        resetText(state);
    }
}
