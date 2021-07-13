package com.astroblaze;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
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

/**
 * This class handles fragment_shop.xml to provide the player with a way to purchase
 * and upgrade ships
 */
public class FragmentShop extends Fragment implements IPlayerStateChangedListener {
    private PlayerShipVariant variant;
    private TextView moneyDisplay;
    private RecyclerView rvShopItems;
    private ValueAnimator moneyAnimator;

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
        moneyAnimator = ValueAnimator.ofInt(0, (int) state.getPlayerMoney());
        moneyAnimator.setDuration(1500); // animate over 1.5 secs
        moneyAnimator.addUpdateListener(valueAnimator -> {
            if (FragmentShop.this.getContext() == null) {
                return; // fragment is detached, abort
            }
            moneyDisplay.setText(getString(R.string.moneyPrint, (int) valueAnimator.getAnimatedValue()));
        });
        moneyAnimator.start();
        TextView title = view.findViewById(R.id.tvTitleShop);
        title.setText(getString(R.string.upgrades_for, getString(R.string.ship0 + variant.id)));

        ArrayList<PlayerShipVariant> variants = state.getUnlockedShips();

        PlayerShipVariant v = variants.get(variants.indexOf(variant));
        int slideTime = requireContext().getResources().getInteger(R.integer.anim_slide);
        int fadeTime = requireContext().getResources().getInteger(R.integer.anim_fade);

        rvShopItems = view.findViewById(R.id.rvShopItems);
        rvShopItems.setLayoutManager(new LinearLayoutManager(rvShopItems.getContext()));
        rvShopItems.setAdapter(new ShopItemsAdapter(variant, getContext(), new ArrayList<>()));
        rvShopItems.setItemAnimator(new RVItemAnimator());
        view.postDelayed(() -> {
            rvShopItems.setAdapter(new ShopItemsAdapter(variant, getContext(), state.getUpgrades(v.id)));
            rvShopItems.scheduleLayoutAnimation();
            rvShopItems.postDelayed(() -> {
                RecyclerView.Adapter<?> adapter = rvShopItems.getAdapter();
                if (adapter instanceof ShopItemsAdapter) {
                    ((ShopItemsAdapter) adapter).delayForAnimation = true;
                } else {
                    Log.e("FragmentShop", "Adapter wasn't a ShopItemsAdapter");
                }
            }, fadeTime); // wait until fragment finishes sliding and fading in
        }, slideTime); // wait until fragment slides in
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
