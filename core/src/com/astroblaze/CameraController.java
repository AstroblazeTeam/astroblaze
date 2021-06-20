package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;

import java.util.Random;

public class CameraController extends PerspectiveCamera {
    private static final Random rng = new Random();

    private float[] samples;
    private float timer = 0f;
    private float duration = 0f;
    private int amplitude = 0;
    private int frequency = 0;
    private boolean isFading = true;
    private boolean shake = false;
    private final PlayerState state;

    public CameraController() {
        state = AstroblazeGame.getPlayerState();
    }

    public CameraController(float fieldOfViewY, float viewportWidth, float viewportHeight) {
        super(fieldOfViewY, viewportWidth, viewportHeight);
        state = AstroblazeGame.getPlayerState();
    }

    public void shake() {
        shake(1f);
    }

    public void shake(float time) {
        shake(time, 4);
    }

    public void shake(float time, int amp) {
        shake(time, amp, 35, true);
    }

    public void shake(float time, int amp, int freq) {
        shake(time, amp, freq, true);
    }

    public void shake(float time, int amp, int freq, boolean fade) {
        Gdx.app.log("CustomCamera", "shake(" + time + ", " + amp + ", " + freq + ", " + fade + ")");
        shake = true;
        timer = 0f;
        duration = time;
        amplitude = amp;
        frequency = freq;
        isFading = fade;
        samples = new float[frequency];
        for (int i = 0; i < frequency; i++) {
            samples[i] = rng.nextFloat() * 2f - 1f;
        }
    }

    @Override
    public void update() {
        super.update();

        if (!shake || !state.getScreenShake()) {
            return;
        }

        // shaking translated from Kotlin https://gamedev.stackexchange.com/a/163663
        try {
            if (timer > duration) shake = false;
            float dt = Gdx.graphics.getDeltaTime();
            timer += dt;
            if (duration > 0f) {
                duration -= dt;
                float shakeTime = timer * frequency;
                int first = (int) shakeTime;
                int second = (first + 1) % frequency;
                int third = (first + 2) % frequency;
                float deltaT = shakeTime - (int) shakeTime;
                float deltaX = samples[first] * deltaT + samples[second] * (1f - deltaT);
                float deltaY = samples[second] * deltaT + samples[third] * (1f - deltaT);

                position.x = deltaX * amplitude * (isFading ? Math.min(duration, 1f) : 1f);
                position.z = 100f + deltaY * amplitude * (isFading ? Math.min(duration, 1f) : 1f);
            }
        } catch (Exception e) {
            // noop
        }
    }
}
