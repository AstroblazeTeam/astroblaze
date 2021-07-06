package com.astroblaze;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astroblaze.Interfaces.IPlayerStateChangedListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FragmentShop extends Fragment implements IPlayerStateChangedListener {
    PlayerShipVariant variant;
    TextView moneyDisplay;
    RecyclerView rvShopItems;
    ValueAnimator moneyAnimator;

    public FragmentShop() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            variant = PlayerShipVariant.values()[args.getInt("variant")];
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop, container, false);
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

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnExitToMenu).setOnClickListener(v -> {
            AstroblazeGame.getSoundController().playUINegative();
            NavHostFragment.findNavController(FragmentShop.this).popBackStack();
        });

        PlayerState state = AstroblazeGame.getPlayerState();

        moneyDisplay = view.findViewById(R.id.tvMoneyDisplay);
        int money = (int) state.getPlayerMoney();
        moneyAnimator = ValueAnimator.ofInt(money, money);
        moneyAnimator.setDuration(1500); // animate over 1.5 secs
        moneyAnimator.addUpdateListener(valueAnimator
                -> moneyDisplay.setText(getString(R.string.moneyPrint, (int)valueAnimator.getAnimatedValue())));
        moneyAnimator.start();
        TextView title = view.findViewById(R.id.tvTitleShop);
        title.setText(getString(R.string.upgrades_for, getString(R.string.ship0 + variant.id)));

        ArrayList<PlayerShipVariant> variants = state.getUnlockedShips();

        PlayerShipVariant v = variants.get(variants.indexOf(variant));
        ArrayList<UpgradeEntry> upgradeEntries = state.getUpgrades(v.id);

        rvShopItems = view.findViewById(R.id.rvShopItems);
        rvShopItems.setLayoutManager(new LinearLayoutManager(rvShopItems.getContext()));
        rvShopItems.setAdapter(new ShopItemsAdapter(variant, getContext(), upgradeEntries));
    }

    @Override
    public void onStateChanged(PlayerState state) {
        moneyDisplay.post(() -> {
            moneyAnimator.setIntValues((int) moneyAnimator.getAnimatedValue(), (int) state.getPlayerMoney());
            if (!moneyAnimator.isRunning())
                moneyAnimator.start();
        });
    }
}