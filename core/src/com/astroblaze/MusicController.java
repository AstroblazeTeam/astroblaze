package com.astroblaze;

import com.astroblaze.Interfaces.*;
import com.astroblaze.Utils.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;

import java.util.HashMap;

public class MusicController implements ILoadingFinishedListener {
    public enum MusicTrackType {None, UI, Game1, Game2, Game3, Ending}

    private final HashMap<MusicTrackType, Music> musicTracks = new HashMap<>(4);
    private final PlayerState state;
    private final float fadeSpeed = 3f; // music tracks crossfade time
    private final float intervalUpdate = 0.1f;

    private MusicTrackType currentTrack = MusicTrackType.None;
    private float volume = 1f;
    private float time = 0f;
    private MusicTrackType randomGameTrack;

    MusicController(AstroblazeGame game) {
        game.addOnLoadingFinishedListener(this);
        state = AstroblazeGame.getPlayerState();
        randomizeGameTrack();
    }

    public void loadLoadingScreenAssets() {
        if (!Assets.getInstance().isLoaded(Assets.musicUI)) {
            Gdx.app.error("MusicManager", "uiMusic assets is not loaded");
            return;
        }

        assignTrack(MusicTrackType.UI, Assets.musicUI);
        volume = state.getMusicVolume();
    }

    @Override
    public void finishedLoadingAssets() {
        assignTrack(MusicTrackType.Ending, Assets.musicEnding);
        assignTrack(MusicTrackType.Game1, Assets.musicLevel1);
        assignTrack(MusicTrackType.Game2, Assets.musicLevel2);
        assignTrack(MusicTrackType.Game3, Assets.musicLevel3);
    }

    private void assignTrack(MusicTrackType type, AssetDescriptor<Music> music) {
        Music track = Assets.asset(music);
        musicTracks.put(type, track);
        track.setVolume(0f);
        track.setLooping(true);
        track.play();
        track.pause();
    }

    public void update(final float delta) {
        time += delta;
        if (time < intervalUpdate) {
            return;
        }
        time -= intervalUpdate;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                updateInternal(intervalUpdate);
            }
        });
        t.start();
    }

    private void updateInternal(float delta) {
        for (MusicTrackType trackType : musicTracks.keySet()) {
            Music music = musicTracks.get(trackType);
            if (music == null) // skip unassigned tracks
                continue;

            float end = trackType == currentTrack ? volume : 0f;

            float current = music.getVolume();
            float target = MathHelper.moveTowards(current, end, delta * fadeSpeed);
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

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        state.setMusicVolume(volume);
        this.volume = volume;
        Gdx.app.log("MusicManager", "Set target volume to " + this.volume);
    }

    public void setTrackToRandomGameTrack() {
        setTrack(randomGameTrack);
    }

    public void randomizeGameTrack() {
        int roll = MathUtils.random(1, 3);
        Gdx.app.log("MusicManager", "Game track rolled " + roll);
        switch (roll) {
            case 3:
                randomGameTrack = MusicTrackType.Game3;
                break;
            case 2:
                randomGameTrack = MusicTrackType.Game2;
                break;
            case 1:
            default:
                randomGameTrack = MusicTrackType.Game1;
                break;
        }

        if (getTrack() == MusicTrackType.Game1 ||
                getTrack() == MusicTrackType.Game2 ||
                getTrack() == MusicTrackType.Game3) {
            setTrack(randomGameTrack);
        }
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
