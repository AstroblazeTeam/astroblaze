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

import java.text.MessageFormat;
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

        public ViewHolder(View view) {
            super(view);

            tvRank = (TextView) view.findViewById(R.id.tvEntryRank);
            tvName = (TextView) view.findViewById(R.id.tvEntryName);
            tvScore = (TextView) view.findViewById(R.id.tvEntryScore);
            tvLevel = (TextView) view.findViewById(R.id.tvEntryLevel);
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
