package com.astroblaze;

public interface IHpChangeListener {
    void onHpChanged(Ship ship, float oldHp, float newHp);

    void onHpEnabled(Ship ship, boolean enabled);
}
