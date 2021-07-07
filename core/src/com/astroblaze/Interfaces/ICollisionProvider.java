package com.astroblaze.Interfaces;

import com.badlogic.gdx.math.Vector3;

public interface ICollisionProvider {
    boolean checkCollision(Vector3 pos, float radius);
    void applyDamage(float damage, boolean isPlayer);
}
