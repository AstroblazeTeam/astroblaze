package com.astroblaze;

import com.badlogic.gdx.math.Vector3;

public interface CollisionProvider {
    boolean CheckCollision(Vector3 pos, float radius);
}
