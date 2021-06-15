package com.astroblaze;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FragmentShop extends Fragment {
    RecyclerView rvShopItems;
    ArrayList<ShopItemsAdapter.ShopItem> shopItems = new ArrayList<>(16);

    public FragmentShop() {
        // Required empty public constructor
        shopItems.add(new ShopItemsAdapter.ShopItem(R.drawable.upgrade_hp, "Shield", 1f, 1.1f, 3000f));
        shopItems.add(new ShopItemsAdapter.ShopItem(R.drawable.upgrade_damage, "Damage", 1f, 1.1f, 3000f));
        shopItems.add(new ShopItemsAdapter.ShopItem(R.drawable.upgrade_speed, "Speed", 1f, 1.1f, 3000f));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shop, container, false);
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
    }
}