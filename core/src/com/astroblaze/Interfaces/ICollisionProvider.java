package com.astroblaze.Interfaces;

import com.badlogic.gdx.math.Vector3;

/**
 * This represents a collide-able with object
 */
public interface ICollisionProvider {
    /**
     * Checks collision in a sphere centered on 'pos' with radius 'radius'
     * @param pos position to check for collision
     * @param radius radius to check for collision
     * @return true if sphere centered at 'pos' with radius 'radius' touches the implementing object
     */
    boolean checkCollision(Vector3 pos, float radius);

    /**
     * @param damage Amount of damage to apply
     * @param isPlayer True if damage came from the player
     */
    void applyDamage(float damage, boolean isPlayer);
}
