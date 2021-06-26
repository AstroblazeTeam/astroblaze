package com.astroblaze.Interfaces;

import com.astroblaze.Rendering.*;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public interface IPlayerBonus {
    TextureAtlas.AtlasRegion getDecalTexture();
    float getDecalScale();
    void applyBonus(PlayerShip playerShip);
}
