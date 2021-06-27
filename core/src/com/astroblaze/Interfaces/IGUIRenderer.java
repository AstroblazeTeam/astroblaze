package com.astroblaze.Interfaces;

public interface IGUIRenderer {
    void renderText(int id, String text, float fontSize, float x, float y);

    void renderText(int id, int textId, float fontSize, float x, float y);

    String getTranslatedString(TranslatedStringId id);

    void backToLevelSelect();

    void navigateToGameOver();

    boolean isRightToLeft();

    void navigateToLevelComplete();
}
