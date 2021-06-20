package com.astroblaze.Bonuses;

import com.astroblaze.*;
import com.astroblaze.Interfaces.*;
import com.astroblaze.Rendering.*;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class PlayerBonusMissiles implements IPlayerBonus {
    @Override
    public TextureAtlas.AtlasRegion getDecalTexture() {
        return Assets.asset(Assets.atlas).findRegion("missile-bonus");
    }

    @Override
    public float getDecalScale() {
        return 0.15f;
    }

    @Override
    public void applyBonus(Ship ship) {
        ship.modMissileSalvos(+3);
    }
}
