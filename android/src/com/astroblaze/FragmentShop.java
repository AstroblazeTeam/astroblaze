package com.astroblaze;

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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FragmentShop extends Fragment implements IScoreChangeListener {
    TextView moneyDisplay;
    RecyclerView rvShopItems;
    ArrayList<ShopItem> shopItems = new ArrayList<>(16);

    public FragmentShop() {
        // Required empty public constructor
        shopItems.add(new ShopItem(R.drawable.upgrade_hp, "Shield", 1f, 1.1f, 3000f, ShopItemType.ShieldUpgrade));
        shopItems.add(new ShopItem(R.drawable.upgrade_damage, "Damage", 1f, 1.1f, 30000f, ShopItemType.DamageUpgrade));
        shopItems.add(new ShopItem(R.drawable.upgrade_speed, "Speed", 1f, 1.1f, 3000000f, ShopItemType.SpeedUpgrade));
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
        AstroblazeGame.getInstance().addOnScoreChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        AstroblazeGame.getInstance().removeOnScoreChangeListener(this);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnExitToMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(FragmentShop.this).popBackStack();
            }
        });
        rvShopItems = view.findViewById(R.id.rvShopItems);
        rvShopItems.setLayoutManager(new LinearLayoutManager(rvShopItems.getContext()));
        rvShopItems.setAdapter(new ShopItemsAdapter(getContext(), shopItems));
        moneyDisplay = view.findViewById(R.id.tvMoneyDisplay);
    }

    @Override
    public void scoreChanged(float newMoney, float newScore) {
        moneyDisplay.setText(getString(R.string.moneyPrint, (int) newMoney));
    }
}