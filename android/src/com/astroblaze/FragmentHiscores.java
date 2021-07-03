package com.astroblaze;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FragmentHiscores extends Fragment {
    RecyclerView rvBoard;
    ProgressBar pgLoading;

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
            AstroblazeGame.getSoundController().playUICancelSound();
            NavHostFragment.findNavController(FragmentHiscores.this).popBackStack();
        });


        pgLoading = view.findViewById(R.id.pgLoading);
        rvBoard = view.findViewById(R.id.rvHiscoresBoard);
        rvBoard.setLayoutManager(new LinearLayoutManager(rvBoard.getContext()));
        rvBoard.setAdapter(new HiscoresItemsAdapter(getContext(), new ArrayList<>()));

        HiscoresController.fetchBoard(new HiscoresController.RunnableResponseHandler<ArrayList<HiscoresEntry>>() {
            @Override
            public void run() {
                rvBoard.postDelayed(() -> {
                    pgLoading.setVisibility(View.INVISIBLE);
                    rvBoard.setAdapter(new HiscoresItemsAdapter(getContext(), response));
                    rvBoard.setVisibility(View.VISIBLE);
                }, 500);
            }
        });
    }
}