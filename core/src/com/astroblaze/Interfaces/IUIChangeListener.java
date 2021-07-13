package com.astroblaze.Interfaces;

import com.astroblaze.Rendering.*;

/**
 * This provides observable interface into the updates needed for more dynamic part of
 * player's state like the hitpoints, amount of missiles, and laser charge.
 */
public interface IUIChangeListener {
    void onHpChanged(PlayerShip playerShip, float oldHp, float newHp);

    void onHpEnabled(PlayerShip playerShip, boolean enabled);

    void onSpecialTextChanged(PlayerShip playerShip, String text1, String text2);
}
