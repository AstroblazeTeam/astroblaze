package com.astroblaze;

import android.animation.ValueAnimator;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.astroblaze.GdxActors.LevelControllerActor;
import com.astroblaze.Rendering.EnemyType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class KillsItemsAdapter extends RecyclerView.Adapter<KillsItemsAdapter.ViewHolder> {
    public static class EnemyKillCount {
        public final EnemyType type;
        public final int count;

        public EnemyKillCount(EnemyType type, int count) {
            this.type = type;
            this.count = count;
        }
    }

    private final Context context;
    private final LevelStatTracker tracker;
    private final ArrayList<EnemyKillCount> items = new ArrayList<>();

    public KillsItemsAdapter(Context context, LevelStatTracker tracker) {
        this.context = context;
        this.tracker = tracker;
        for (Map.Entry<EnemyType, Integer> x : tracker.getKills().entrySet()) {
            items.add(new EnemyKillCount(x.getKey(), x.getValue()));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.kill_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EnemyKillCount item = items.get(position);
        String translatedName = AstroblazeGame.getInstance().getGuiRenderer().getTranslatedEnemyName(item.type);
        holder.getTextViewName().setText(translatedName);
        if (item.type != EnemyType.Boss) {
            animateKillCountText(holder.getTextViewPrice(), item.count, (int) item.type.value);
        } else {
            float reward = LevelControllerActor.getLevelReward(tracker.getLevel());
            animateKillCountText(holder.getTextViewPrice(), item.count, (int) reward);
        }
    }

    private void animateKillCountText(TextView tv, int count, int value) {
        ValueAnimator animator = ValueAnimator.ofInt(0, count);
        animator.setDuration(3000); // animate over 3 secs
        animator.addUpdateListener(valueAnimator -> {
            Spanned countXvalue = Html.fromHtml(context.getString(R.string.multiplesOf, (int) valueAnimator.getAnimatedValue(), value), Html.FROM_HTML_MODE_LEGACY);
            tv.setText(countXvalue);
        });
        animator.start();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvPrice;

        public ViewHolder(View view) {
            super(view);

            tvName = view.findViewById(R.id.tvName);
            tvPrice = view.findViewById(R.id.tvPrice);
        }

        public TextView getTextViewName() {
            return tvName;
        }

        public TextView getTextViewPrice() {
            return tvPrice;
        }
    }
}
