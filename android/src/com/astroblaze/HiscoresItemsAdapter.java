package com.astroblaze;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HiscoresItemsAdapter extends RecyclerView.Adapter<HiscoresItemsAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<HiscoresEntry> items;

    public HiscoresItemsAdapter(Context context, ArrayList<HiscoresEntry> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hiscores_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HiscoresEntry item = items.get(position);
        String name = item.name == null || item.name.equals("")
                ? context.getString(R.string.anonymous) : item.name;
        holder.getTextViewRank().setText(String.valueOf(item.rank));
        holder.getTextViewName().setText(name);
        holder.getTextViewScore().setText(String.valueOf((int) item.score));
        holder.getTextViewLevel().setText(String.valueOf(item.maxLevel));

        // highlight the user's own highscore if exists
        boolean isSelf = item.id.equals(AstroblazeGame.getPlayerState().getId());

        int color = isSelf
                ? holder.itemView.getContext().getColor(R.color.hiscore_self)
                : holder.itemView.getContext().getColor(R.color.hiscore_normal);

        for (TextView textView : holder.allTextViews) {
            textView.setTextColor(color);
            textView.setTypeface(textView.getTypeface(), isSelf ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRank;
        private final TextView tvName;
        private final TextView tvScore;
        private final TextView tvLevel;
        private final ArrayList<TextView> allTextViews = new ArrayList<>(4);

        public ViewHolder(View view) {
            super(view);

            tvRank = view.findViewById(R.id.tvEntryRank);
            tvName = view.findViewById(R.id.tvEntryName);
            tvScore = view.findViewById(R.id.tvEntryScore);
            tvLevel = view.findViewById(R.id.tvEntryLevel);

            allTextViews.add(tvRank);
            allTextViews.add(tvName);
            allTextViews.add(tvScore);
            allTextViews.add(tvLevel);
        }

        public TextView getTextViewRank() {
            return tvRank;
        }

        public TextView getTextViewName() {
            return tvName;
        }

        public TextView getTextViewScore() {
            return tvScore;
        }

        public TextView getTextViewLevel() {
            return tvLevel;
        }
    }
}
