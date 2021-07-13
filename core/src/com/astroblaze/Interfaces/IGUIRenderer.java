package com.astroblaze.Interfaces;

import com.astroblaze.Rendering.EnemyType;

/**
 * This class provides an interface from LibGDX package into the Android package
 * Allows for things like getting translated strings, sending NavGraph calls into
 * Android part of the project, detecting right-to-left layout etc.
 */
public interface IGUIRenderer {
    void renderText(int id, String text, float fontSize, float x, float y);

    String getTranslatedString(TranslatedStringId id);

    void navigateToGameOver();

    boolean isRightToLeft();

    void navigateToLevelComplete();

    String getTranslatedEnemyName(EnemyType type);

    boolean isDebuggable();
}
