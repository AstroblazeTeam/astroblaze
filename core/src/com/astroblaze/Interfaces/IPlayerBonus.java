package com.astroblaze.Interfaces;

import com.astroblaze.Rendering.*;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/**
 * This represents a player bonus item, used to display texture with specific scale
 * and apply the bonus onto the player
 */
public interface IPlayerBonus {
    TextureAtlas.AtlasRegion getDecalTexture();
    float getDecalScale();
    void applyBonus(PlayerShip playerShip);
}
