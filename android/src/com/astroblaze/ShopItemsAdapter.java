package com.astroblaze;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.MessageFormat;
import java.util.ArrayList;

public class ShopItemsAdapter extends RecyclerView.Adapter<ShopItemsAdapter.ViewHolder> {
    private final PlayerShipVariant variant;
    private final Context context;
    private final ArrayList<UpgradeEntry> items;

    public ShopItemsAdapter(PlayerShipVariant variant, Context context, ArrayList<UpgradeEntry> items) {
        this.variant = variant;
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shop_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UpgradeEntry item = items.get(position);
        String qty;
        if (item.currentTier < item.maxTier) qty = item.currentTier + " / " + item.maxTier;
        else if (item.currentTier == item.maxTier) qty = "MAX";
        else qty = "MAX + " + (item.currentTier - item.maxTier);

        switch (item.type) {
            case ShieldUpgrade:
                holder.getTextViewName().setText(context.getString(R.string.shieldUpgrade, qty));
                break;
            case SpeedUpgrade:
                holder.getTextViewName().setText(context.getString(R.string.speedUpgrade, qty));
                break;
            case LaserCapacity:
                holder.getTextViewName().setText(context.getString(R.string.laserCapacityUpgrade, qty));
                break;
            case MaxMissiles:
                holder.getTextViewName().setText(context.getString(R.string.maxMissilesUpgrade, qty));
                break;
            case TurretSpeed:
                holder.getTextViewName().setText(context.getString(R.string.turretSpeedUpgrade, qty));
                break;
            case DamageUpgrade:
            default:
                holder.getTextViewName().setText(context.getString(R.string.damageUpgrade, qty));
                break;
        }
        holder.getTextViewCurrent().setText(MessageFormat.format("{0,number,#.##}%", 100f * item.getCurrentMultiplier()));
        holder.getTextViewNext().setText(MessageFormat.format("+{0,number,#.##}%", 100 * item.getNextMultiplier()));
        holder.getTextViewPrice().setText(MessageFormat.format("${0,number,#.##}", item.getUpgradePrice()));
        holder.getBtnBuy().setOnClickListener(v -> {
            if (!AstroblazeGame.getPlayerState().buyUpgrade(variant, item)) {
                return;
            }
            ShopItemsAdapter.this.notifyDataSetChanged();

            // delay the next update so both upgrade buttons update and animation also plays properly
            holder.getTextViewCurrent().postDelayed(()
                    -> ShopItemsAdapter.this.notifyItemChanged(position), 10);
        });
        holder.getBtnBuy().setEnabled(AstroblazeGame.getPlayerState()
                .canBuyUpgrade(variant, item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvCurrent;
        private final TextView tvUpgrade;
        private final TextView tvPrice;
        private final Button btnBuy;

        public ViewHolder(View view) {
            super(view);

            tvName = view.findViewById(R.id.tvName);
            tvCurrent = view.findViewById(R.id.tvCurrentUpgrade);
            tvUpgrade = view.findViewById(R.id.tvNextUpgrade);
            tvPrice = view.findViewById(R.id.tvPrice);
            btnBuy = view.findViewById(R.id.btnBuyUpgrade);
        }

        public TextView getTextViewCurrent() {
            return tvCurrent;
        }

        public TextView getTextViewNext() {
            return tvUpgrade;
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
