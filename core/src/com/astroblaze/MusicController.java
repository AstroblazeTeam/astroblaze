package com.astroblaze;

import com.astroblaze.Interfaces.*;
import com.astroblaze.Utils.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;

import java.util.HashMap;

class MusicController implements ILoadingFinishedListener {
    public enum MusicTrackType {None, UI, Game}

    private final HashMap<MusicTrackType, Music> musicTracks = new HashMap<>(4);
    private final PlayerState state;
    private final float fadeSpeed = 1f / 2f; // music tracks crossfade time
    private final float intervalUpdate = 0.1f;

    private MusicTrackType currentTrack = MusicTrackType.None;
    private float targetVolume = 1f;
    private float time = 0f;

    MusicController(AstroblazeGame game) {
        game.addOnLoadingFinishedListener(this);
        state = AstroblazeGame.getPlayerState();
    }

    public void loadLoadingScreenAssets() {
        if (!Assets.getInstance().isLoaded(Assets.uiMusic)) {
            Gdx.app.error("MusicManager", "uiMusic assets is not loaded");
            return;
        }

        assignTrack(MusicTrackType.UI, Assets.uiMusic);
        targetVolume = AstroblazeGame.getPlayerState().getMusicVolume();
    }

    @Override
    public void finishedLoadingAssets() {
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

            float end = trackType == currentTrack ? targetVolume : 0f;

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

    public float getMusicVolume() {
        return targetVolume;
    }

    public void setMusicVolume(float volume) {
        state.setMusicVolume(volume);
        targetVolume = volume;
        Gdx.app.log("MusicManager", "Set target volume to " + targetVolume);
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
