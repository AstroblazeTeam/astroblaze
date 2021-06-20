package com.astroblaze.Interfaces;

import com.badlogic.gdx.math.Vector3;

public interface ITargetable {
    boolean isTargetable();
    Vector3 getPosition();
    Vector3 estimatePosition(float time);
    float distanceSquaredTo(Vector3 pos);
}
