package com.astroblaze;

import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.astroblaze.GdxActors.ShipPreviewActor;
import com.astroblaze.Interfaces.IPlayerStateChangedListener;
import com.badlogic.gdx.Gdx;

import java.text.DecimalFormat;

/**
 * This class handles fragment_ship.xml, hosted in FragmentLevelSelect ViewPager2
 * displays ship description, modified stats etc.
 */
public class FragmentShip extends Fragment implements IPlayerStateChangedListener {
    private final PlayerShipVariant variant;
    private Button btnAction;
    private TextView tvShipSwipeLeft;
    private TextView tvShipSwipeLabel;
    private TextView tvShipSwipeRight;
    private TextView tvDescription;
    private TextView tvStats;

    public FragmentShip() {
        this(PlayerShipVariant.Shuttle);
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
        return inflater.inflate(R.layout.fragment_ship, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvDescription = view.findViewById(R.id.tvShipDescription);
        tvStats = view.findViewById(R.id.tvShipStats);
        btnAction = view.findViewById(R.id.btnAction);
        tvShipSwipeLeft = view.findViewById(R.id.tvShipLeft);
        tvShipSwipeLabel = view.findViewById(R.id.tvShipSwipe);
        tvShipSwipeRight = view.findViewById(R.id.tvShipRight);
        resetText(AstroblazeGame.getPlayerState());
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

    // lint suppressed so we can concat variant name to description
    @android.annotation.SuppressLint("SetTextI18n")
    private void resetText(PlayerState state) {
        if (getContext() == null) {
            return; // too early in the lifecycle
        }

        boolean canSwipeLeft = variant.id > 0;
        boolean canSwipeRight = variant.id < ShipPreviewActor.getVariantCount() - 1;

        tvShipSwipeLeft.setVisibility(canSwipeLeft ? View.VISIBLE : View.INVISIBLE);
        tvShipSwipeLabel.setVisibility(canSwipeLeft || canSwipeRight ? View.VISIBLE : View.INVISIBLE);
        tvShipSwipeRight.setVisibility(canSwipeRight ? View.VISIBLE : View.INVISIBLE);

        tvDescription.setText(getString(R.string.ship0 + variant.id) + "\n" + getString(R.string.shipDesc0 + variant.id));
        String hpText = getString(R.string.shipStatHp, variant.getMaxHp(state));
        String hpModText = getString(R.string.shipStatBonus, new DecimalFormat("+#").format((variant.getUpgradeModifier(state, UpgradeEntryType.ShieldUpgrade) - 1f) * 100f));
        String gunText = getString(R.string.shipStatGuns, variant.gunPorts, variant.getGunDamage(state));
        String gunModifier = getString(R.string.shipStatBonus, new DecimalFormat("+#").format((variant.getUpgradeModifier(state, UpgradeEntryType.DamageUpgrade) - 1f) * 100f));
        String turretText = getString(R.string.shipStatTurrets, variant.turretPorts, variant.getTurretDamage(state));
        String turretModifier = getString(R.string.shipStatBonus, new DecimalFormat("+#").format((variant.getUpgradeModifier(state, UpgradeEntryType.DamageUpgrade) - 1f) * 100f));
        String missileText = getString(R.string.shipStatMissiles, variant.missilePorts, variant.getMissileDamage(state));
        String missileModifier = getString(R.string.shipStatBonus, new DecimalFormat("+#").format((variant.getUpgradeModifier(state, UpgradeEntryType.DamageUpgrade) - 1f) * 100f));
        String laserText = getString(R.string.shipStatLasers, variant.getLaserDamage(state));
        String laserModifier = getString(R.string.shipStatBonus, new DecimalFormat("+#").format((variant.getUpgradeModifier(state, UpgradeEntryType.DamageUpgrade) - 1f) * 100f));
        String speedText = getString(R.string.shipStatSpeed, variant.getSpeed(state));
        String speedModifier = getString(R.string.shipStatBonus, new DecimalFormat("+#").format((variant.getUpgradeModifier(state, UpgradeEntryType.SpeedUpgrade) - 1f) * 100f));

        CharSequence combined = TextUtils.concat(
                hpText, hpModText, "<br>",
                gunText, gunModifier, "<br>",
                turretText, turretModifier, "<br>",
                missileText, missileModifier, "<br>",
                laserText, laserModifier, "<br>",
                speedText, speedModifier, "<br>"
        );

        tvStats.setText(Html.fromHtml(combined.toString(), Html.FROM_HTML_MODE_LEGACY));

        if (!state.isShipOwned(variant)) {
            btnAction.setEnabled(AstroblazeGame.getPlayerState().canBuyShip(variant));
            btnAction.setText(getString(R.string.unlockShip, (int) variant.price));
            btnAction.setOnClickListener(v -> AstroblazeGame.getPlayerState().buyShip(variant));
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

                AstroblazeGame.getSoundController().playUIConfirm();

                try {
                    NavHostFragment.findNavController(parentFragment)
                            .navigate(R.id.action_fragmentLevelSelect_to_shopFragment, bundle);
                } catch (IllegalArgumentException ex) {
                    Log.d("FragmentShip", "btnAction click: fragment navigation failed, possibly duplicate event", ex);
                }
            });
        }
    }

    @Override
    public void onStateChanged(PlayerState state) {
        btnAction.post(() -> resetText(state));
    }
}
