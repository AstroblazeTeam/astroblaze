package com.astroblaze.Interfaces;

import com.astroblaze.Rendering.*;

public interface IUIChangeListener {
    void onHpChanged(PlayerShip playerShip, float oldHp, float newHp);

    void onHpEnabled(PlayerShip playerShip, boolean enabled);

    void onSpecialTextChanged(PlayerShip playerShip, String text1, String text2);
}
