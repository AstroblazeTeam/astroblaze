package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpParametersUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class HiscoresController {
    public static abstract class RunnableResponseHandler<T> implements Runnable {
        public T response;
    }

    public static void fetchBoard(RunnableResponseHandler<ArrayList<HiscoresEntry>> r) {
        Gdx.app.log("HiscoresController", "Started fetchBoard");
        Net.HttpRequest httpGet = new Net.HttpRequest(Net.HttpMethods.GET);
        httpGet.setUrl("https://services.karmaflux.com/api/Astroblaze/");
        r.response = new ArrayList<>();

        Gdx.net.sendHttpRequest(httpGet, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                try {
                    JSONArray scoresRaw = new JSONArray(httpResponse.getResultAsString());
                    int rank = 0;
                    float rankScore = 1000000000f;

                    for (int i = 0; i < scoresRaw.length(); i++) {
                        JSONObject obj = scoresRaw.getJSONObject(i);
                        float score = (float) obj.getDouble("score");
                        if (rankScore > score) { // don't increase rank for equal score
                            rankScore = score;
                            rank++;
                        }
                        r.response.add(new HiscoresEntry(
                                rank,
                                obj.getString("id"),
                                obj.getString("name"),
                                (float) obj.getDouble("score"),
                                obj.getInt("maxLevel")));
                    }

                    Gdx.app.log("HiscoresController", "fetchBoard finished");
                    r.run();
                } catch (Exception e) {
                    Gdx.app.error("HiscoresController", e.toString());
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("HiscoresController", t.toString());
            }

            @Override
            public void cancelled() {
                Gdx.app.error("HiscoresController", "Cancelled");
            }
        });
    }

    public static void fetchRank(RunnableResponseHandler<Integer> r) {
        Gdx.app.log("HiscoresController", "fetchRank started");
        PlayerState state = AstroblazeGame.getPlayerState();

        Net.HttpRequest httpGet = new Net.HttpRequest(Net.HttpMethods.GET);
        httpGet.setUrl("https://services.karmaflux.com/api/Astroblaze/?score="
                + state.getPlayerScore());
        r.response = 0;

        Gdx.net.sendHttpRequest(httpGet, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                try {
                    r.response = Integer.parseInt(httpResponse.getResultAsString()) + 1; // 0 based
                } catch (Exception e) {
                    Gdx.app.error("HiscoresController", e.toString());
                }
                Gdx.app.log("HiscoresController", "fetchRank finished");
                r.run();
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("HiscoresController", t.toString());
            }

            @Override
            public void cancelled() {
                Gdx.app.error("HiscoresController", "Cancelled");
            }
        });
    }

    public static void submitRank(RunnableResponseHandler<Integer> r, boolean soft) {
        Gdx.app.log("HiscoresController", "submitRank started");
        PlayerState state = AstroblazeGame.getPlayerState();

        if (soft && !state.shouldSubmitScore()) {
            Gdx.app.log("HiscoresController", "submitRank soft abort - fetching instead");
            fetchRank(r);
            return;
        }

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("Id", state.getId());
        parameters.put("Name", state.getName());
        parameters.put("Score", String.valueOf((int) state.getPlayerScore()));
        parameters.put("MaxLevel", String.valueOf(state.getMaxLevel()));
        Net.HttpRequest httpGet = new Net.HttpRequest(Net.HttpMethods.POST);

        httpGet.setContent(HttpParametersUtils.convertHttpParameters(parameters));
        httpGet.setUrl("https://services.karmaflux.com/api/Astroblaze/");

        r.response = 0;
        Gdx.net.sendHttpRequest(httpGet, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                state.submittedScore();
                try {
                    r.response = Integer.parseInt(httpResponse.getResultAsString()) + 1; // 0 based
                } catch (Exception e) {
                    Gdx.app.error("HiscoresController", e.toString());
                }
                Gdx.app.log("HiscoresController", "submitRank finished");
                r.run();
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("HiscoresController", t.toString());
            }

            @Override
            public void cancelled() {
                Gdx.app.error("HiscoresController", "Cancelled");
            }
        });
    }
}
