package com.astroblaze;

public interface IGUIRenderer {
    void renderText(int id, String text, float fontSize, float x, float y);

    void renderText(int id, int textId, float fontSize, float x, float y);
}
