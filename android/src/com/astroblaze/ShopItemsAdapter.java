package com.astroblaze;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.ArrayList;

public class ShopItemsAdapter extends RecyclerView.Adapter<ShopItemsAdapter.ViewHolder> {
    public static class ShopItem {
        public int iconResource;
        public String name;
        public float current;
        public float next;
        public float price;

        public ShopItem(int iconResource, String name, float current, float next, float price) {
            this.iconResource = iconResource;
            this.name = name;
            this.current = current;
            this.next = next;
            this.price = price;
        }
    }

    private final ArrayList<ShopItem> items;
    private final MediaPlayer mp;

    public ShopItemsAdapter(Context context, ArrayList<ShopItem> items) {
        this.items = items;
        this.mp = MediaPlayer.create(context, R.raw.cha_ching);
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shop_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        ShopItem item = items.get(position);
        holder.getImageViewUpgrade().setImageResource(item.iconResource);
        holder.getTextViewName().setText(item.name);
        holder.getTextViewCurrent().setText(MessageFormat.format("{0,number,#.##%}", item.current));
        holder.getTextViewNext().setText(MessageFormat.format("+{0,number,#.##%}", item.next - item.current));
        holder.getTextViewPrice().setText(MessageFormat.format("{0,number,#.##}", item.price));
        holder.getBtnBuy().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.start();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivIcon;
        private final TextView tvName;
        private final TextView tvCurrent;
        private final TextView tvUpgrade;
        private final TextView tvPrice;
        private final Button btnBuy;

        public ViewHolder(View view) {
            super(view);

            ivIcon = (ImageView) view.findViewById(R.id.ivUpgradeIcon);
            tvName = (TextView) view.findViewById(R.id.tvName);
            tvCurrent = (TextView) view.findViewById(R.id.tvCurrentUpgrade);
            tvUpgrade = (TextView) view.findViewById(R.id.tvNextUpgrade);
            tvPrice = (TextView) view.findViewById(R.id.tvPrice);
            btnBuy = (Button) view.findViewById(R.id.btnBuyUpgrade);
        }

        public TextView getTextViewCurrent() {
            return tvCurrent;
        }

        public TextView getTextViewNext() {
            return tvUpgrade;
        }

        public ImageView getImageViewUpgrade() {
            return ivIcon;
        }

        public Button getBtnBuy() {
            return btnBuy;
        }

        public TextView getTextViewName() {
            return tvName;
        }

        public TextView getTextViewPrice() {
            return tvPrice;
        }
    }
}
