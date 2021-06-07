package com.astroblaze;

import com.badlogic.gdx.math.Vector3;

public interface ICollisionProvider {
    boolean checkCollision(Vector3 pos, float radius);
    void damageFromCollision(float damage);
}
