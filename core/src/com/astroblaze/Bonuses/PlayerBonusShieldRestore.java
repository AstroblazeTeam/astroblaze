package com.astroblaze.Bonuses;

import com.astroblaze.*;
import com.astroblaze.Interfaces.*;
import com.astroblaze.Rendering.*;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class PlayerBonusShieldRestore implements IPlayerBonus {
    @Override
    public TextureAtlas.AtlasRegion getDecalTexture() {
        return Assets.asset(Assets.atlas).findRegion("shield-restore");
    }

    @Override
    public float getDecalScale() {
        return 0.1f;
    }

    @Override
    public void applyBonus(PlayerShip playerShip) {
        AstroblazeGame.getSoundController().playBonusSound();
        playerShip.modHp(playerShip.getMaxHitpoints());
    }
}
