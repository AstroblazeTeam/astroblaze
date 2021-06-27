package com.astroblaze;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FragmentLevelComplete extends Fragment {
    public FragmentLevelComplete() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_level_complete, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @androidx.annotation.Nullable @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final LevelStatTracker tracker = AstroblazeGame.getLevelStatTracker();

        int kills = 0;
        for (int k : tracker.getKills().values()) {
            kills += k;
        }
        ((TextView) view.findViewById(R.id.tvKills)).setText(String.valueOf(kills));
        ((TextView) view.findViewById(R.id.tvDamageDone)).setText(String.valueOf((long) tracker.getDamageDone()));
        ((TextView) view.findViewById(R.id.tvDamageTaken)).setText(String.valueOf((long) tracker.getDamageTaken()));
        ((TextView) view.findViewById(R.id.tvScoreDiff)).setText(String.valueOf((long) tracker.getScore()));
        ((TextView) view.findViewById(R.id.tvMoneyDiff)).setText(String.valueOf((long) tracker.getMoney()));

        view.findViewById(R.id.btnGameOverExit2).setOnClickListener(v ->
                this.requireView().post(() -> {
                    NavController nc = ((MainActivity) requireActivity()).getNavController();
                    nc.popBackStack();
                }));

        RecyclerView rvBoard = view.findViewById(R.id.rvKills);
        if (rvBoard != null) {
            rvBoard.setLayoutManager(new GridLayoutManager(rvBoard.getContext(), 4));
            rvBoard.setAdapter(new KillsItemsAdapter(getContext(), tracker.getKills()));
        }
    }
}