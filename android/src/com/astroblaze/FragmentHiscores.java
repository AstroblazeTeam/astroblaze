package com.astroblaze;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * This handles the highscores screen, animating the loading, fetching the data and displaying
 * it in the RecyclerView
 */
public class FragmentHiscores extends Fragment {
    private RecyclerView rvBoard;
    private ProgressBar pgLoading;

    public FragmentHiscores() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hiscores, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnExitToMenu).setOnClickListener(v -> {
            AstroblazeGame.getSoundController().playUINegative();
            NavHostFragment.findNavController(FragmentHiscores.this).popBackStack();
        });

        pgLoading = view.findViewById(R.id.pgLoading);

        rvBoard = view.findViewById(R.id.rvHiscoresBoard);
        rvBoard.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBoard.setAdapter(new HiscoresItemsAdapter(getContext(), new ArrayList<>()));

        HiscoresController.fetchBoard(new HiscoresController.RunnableResponseHandler<ArrayList<HiscoresEntry>>() {
            @Override
            public void run() {
                rvBoard.postDelayed(() -> {
                    AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
                    pgLoading.setInterpolator(new AccelerateInterpolator(8f));
                    pgLoading.setAnimation(fadeOut);
                    fadeOut.setDuration(800);
                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            pgLoading.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    fadeOut.start();

                    for (int i = 0; i < response.size(); i++) {
                        if (response.get(i).id.equals(AstroblazeGame.getPlayerState().getId())) {
                            // reorder self to be at top of the list
                            HiscoresEntry self = response.remove(i);
                            response.add(0, self);
                        }
                    }

                    rvBoard.setAdapter(new HiscoresItemsAdapter(getContext(), response));
                    rvBoard.setVisibility(View.VISIBLE);
                    rvBoard.scheduleLayoutAnimation();
                }, 500);
            }
        });
    }
}