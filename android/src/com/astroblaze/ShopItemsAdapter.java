package com.astroblaze;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.astroblaze.Interfaces.IPlayerStateChangedListener;

import java.text.MessageFormat;
import java.util.ArrayList;

public class ShopItemsAdapter extends RecyclerView.Adapter<ShopItemsAdapter.ViewHolder> {
    private final PlayerShipVariant variant;
    private final Context context;
    private final ArrayList<UpgradeEntry> items;
    public boolean delayForAnimation;

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
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        AstroblazeGame.getPlayerState().addPlayerStateChangeListener(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        AstroblazeGame.getPlayerState().removePlayerStateChangeListener(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!holder.firstDraw && delayForAnimation) {
            holder.itemView.postDelayed(() -> updateData(holder, position),
                    RVItemAnimator.firstAnimDuration); // update after the first animation finishes
        } else {
            updateData(holder, position);
        }
    }

    private void updateData(ViewHolder holder, int position) {
        final UpgradeEntry item = items.get(position);
        final boolean canBuyIgnoreMoney = AstroblazeGame.getPlayerState().canBuyUpgradeIgnoreMoney(variant, item);
        holder.item = item;
        holder.variant = variant;
        holder.firstDraw = false;
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
        holder.getTextViewNext().setText(canBuyIgnoreMoney
                ? MessageFormat.format("+{0,number,#.##}%", 100f * item.getNextMultiplier())
                : "-");
        holder.getTextViewPrice().setText(canBuyIgnoreMoney
                ? MessageFormat.format("${0,number,#.##}", item.getUpgradePrice())
                : "-");
        holder.getBtnBuy().setOnClickListener(v -> {
            if (holder.currentlyAnimating || !AstroblazeGame.getPlayerState().buyUpgrade(variant, item)) {
                return;
            }

            ShopItemsAdapter.this.notifyItemChanged(position);
        });
        holder.getBtnBuy().setEnabled(AstroblazeGame.getPlayerState().canBuyUpgrade(variant, item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements IPlayerStateChangedListener {
        private final TextView tvName;
        private final TextView tvCurrent;
        private final TextView tvUpgrade;
        private final TextView tvPrice;
        private final Button btnBuy;

        public PlayerShipVariant variant;
        public UpgradeEntry item;
        public boolean firstDraw = true;

        // needs separate flag because setClickable() isn't usable due to weird android shenanigans
        // https://stackoverflow.com/questions/18825747/button-setclickablefalse-is-not-working
        public boolean currentlyAnimating;

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

        @Override
        public void onStateChanged(PlayerState state) {
            getBtnBuy().setEnabled(AstroblazeGame.getPlayerState().canBuyUpgrade(variant, item));
        }
    }
}
