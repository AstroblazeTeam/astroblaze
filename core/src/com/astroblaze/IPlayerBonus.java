package com.astroblaze;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public interface IPlayerBonus {
    TextureAtlas.AtlasRegion getDecalTexture();
    void applyBonus(Ship ship);
}
