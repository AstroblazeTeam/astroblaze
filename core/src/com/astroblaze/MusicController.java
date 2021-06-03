package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;

import java.util.HashMap;

public class MusicController {
    public enum MusicTrackType {None, UI, Game}

    public final HashMap<MusicTrackType, Music> musicTracks = new HashMap<>(4);
    private MusicTrackType currentTrack = MusicTrackType.None;
    private float targetVolume = 1f;
    private final float fadeSpeed = 1f / 2f; // crossfade for 3 seconds

    public void loadLoadingScreenAssets() {
        if (!Assets.getInstance().isLoaded(Assets.uiMusic)) {
            Gdx.app.error("MusicManager", "uiMusic assets is not loaded");
            return;
        }

        assignTrack(MusicTrackType.UI, Assets.uiMusic);
        targetVolume = AstroblazeGame.getPrefs().getFloat("musicVolume", 1f);
    }

    public void assignOtherAssets() { // loads the rest of the assets
        assignTrack(MusicTrackType.Game, Assets.gameMusic);
    }

    private void assignTrack(MusicTrackType type, AssetDescriptor<Music> music) {
        Music track = Assets.asset(music);
        musicTracks.put(type, track);
        track.setVolume(0f);
        track.setLooping(true);
        track.play();
        track.pause();
    }

    public void update(float delta) {
        for (MusicTrackType trackType : musicTracks.keySet()) {
            Music music = musicTracks.get(trackType);
            if (music == null) // skip unassigned tracks
                continue;

            float end = trackType == currentTrack ? targetVolume : 0f;

            float current = music.getVolume();
            float target = moveTowards(current, end, delta * fadeSpeed);
            if (current == 0f && target == 0f && music.isPlaying()) {
                Gdx.app.log("MusicManager", "Track " + trackType + ": playing -> stopped");
                music.pause();
            } else if (current != target && target > 0f && !music.isPlaying()) {
                Gdx.app.log("MusicManager", "Track " + trackType + " stopped -> playing.");
                music.play();
            }
            music.setVolume(target);
        }
    }

    private static float moveTowards(float current, float target, float maxDelta) {
        if (Math.abs(target - current) <= maxDelta) return target;
        return current + Math.signum(target - current) * maxDelta;
    }

    public void setTargetVolume(float volume) {
        AstroblazeGame.getPrefs().putFloat("musicVolume", volume);
        AstroblazeGame.getPrefs().flush();
        targetVolume = volume;
        Gdx.app.log("MusicManager", "Set target volume to " + targetVolume);
    }

    public float getMusicVolume() {
        return targetVolume;
    }

    public void setTrack(MusicTrackType track) {
        if (this.currentTrack == track) {
            Gdx.app.log("MusicManager", "Music track already was " + currentTrack + ", skipping.");
            return;
        }
        this.currentTrack = track;
        Gdx.app.log("MusicManager", "Set music track to " + currentTrack);
    }

    public MusicTrackType getTrack() {
        return this.currentTrack;
    }
}
