package com.astroblaze.Interfaces;

import com.astroblaze.*;

/**
 * This interface is part of observable-like pattern on PlayerState class
 * Class that is interested in receiving PlayerState updates like money/score
 * updates should implement this and register itself in PlayerState
 */
public interface IPlayerStateChangedListener {
    void onStateChanged(PlayerState state);
}
