package com.astroblaze.Interfaces;

import com.badlogic.gdx.math.Vector3;

/**
 * Interface represents a targetable entity in the game world
 */
public interface ITargetable {
    /**
     * @return true if this entity is currently targetable (not dead/incapacitated etc)
     */
    boolean isTargetable();
    Vector3 getPosition();

    /**
     * @param time Time to provide estimate for, in seconds
     * @return Position the entity is estimated to be in after 'time'
     */
    Vector3 estimatePosition(float time);

    /**
     * @param pos Position to measure distance from the entity to
     * @return Distance from the entity to it's position
     */
    float distanceSquaredTo(Vector3 pos);
}
