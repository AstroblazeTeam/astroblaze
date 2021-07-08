package com.astroblaze;

import android.animation.ValueAnimator;
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
    RecyclerView rvBoard;

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

        animateText(view.findViewById(R.id.tvKills), tracker.getTotalKills());
        animateText(view.findViewById(R.id.tvDamageDone), (int) tracker.getDamageDone());
        animateText(view.findViewById(R.id.tvDamageTaken), (int) tracker.getDamageTaken());
        animateText(view.findViewById(R.id.tvScoreDiff), (int) tracker.getScore());
        animateText(view.findViewById(R.id.tvMoneyDiff), (int) tracker.getMoney());

        view.findViewById(R.id.btnGameOverExit2).setOnClickListener(v ->
                this.requireView().post(() -> {
                    AstroblazeGame.getSoundController().playUIConfirm();
                    NavController nc = ((MainActivity) requireActivity()).getNavController();
                    nc.popBackStack();
                }));

        rvBoard = view.findViewById(R.id.rvKills);
        rvBoard.setLayoutManager(new GridLayoutManager(rvBoard.getContext(), 4));
        rvBoard.setAdapter(new KillsItemsAdapter(getContext(), tracker));
    }

    private void animateText(TextView tv, int value) {
        ValueAnimator animator = ValueAnimator.ofInt(0, value);
        animator.setDuration(3000);
        animator.addUpdateListener(valueAnimator
                -> tv.setText(String.valueOf(valueAnimator.getAnimatedValue())));
        animator.start();
    }
}
