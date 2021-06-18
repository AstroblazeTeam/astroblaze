package com.astroblaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;

public class HiscoresController {
    public static abstract class RunnableResponseHandler implements Runnable {
        public String response;
    }

    public static void fetch(RunnableResponseHandler r) {
        Net.HttpRequest httpGet = new Net.HttpRequest(Net.HttpMethods.GET);
        httpGet.setUrl("https://services.karmaflux.com/api/Astroblaze/?score="
                + AstroblazeGame.getPlayerState().getPlayerScore());

        Gdx.net.sendHttpRequest(httpGet, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                r.response = httpResponse.getResultAsString();
                r.run();
            }

            @Override
            public void failed(Throwable t) {
                // silently fail
            }

            @Override
            public void cancelled() {
                // silently fail
            }
        });
    }
}
