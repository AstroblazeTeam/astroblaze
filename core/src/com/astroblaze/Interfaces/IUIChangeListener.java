package com.astroblaze.Interfaces;

import com.astroblaze.Rendering.*;

public interface IUIChangeListener {
    void onHpChanged(Ship ship, float oldHp, float newHp);

    void onHpEnabled(Ship ship, boolean enabled);

    void onSpecialTextChanged(Ship ship, String text1, String text2);
}
