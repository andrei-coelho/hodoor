package br.com.hodoor;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class HTTPRefresh {

    public interface CallBackRequester {
        void callBack(String callback);
        void errorCall(int type);
        String getUrl();
        long getDelay();
        boolean isContinuous();
        int getAttempts();
    }

    private static HTTPRefresh httpRefresh;
    private static RequestQueue queue;
    private static Handler handler;

    public static HTTPRefresh getInstance(){
        if(httpRefresh == null){
            httpRefresh = new HTTPRefresh();
        }
        return httpRefresh;
    }

    private HTTPRefresh(){

    }

    public Requester getRequester(CallBackRequester requester){
        return new Requester(requester);
    }

    public static void runner(Requester requester, Context context, int status) {
        if(status == 0){
            runVolley(requester, context);
        } else {
            runTryReach(requester, context, 0);
        }
    }

    private static void runVolley(final Requester requester, final Context context){
        if(queue == null){
            queue = Volley.newRequestQueue(context);
        }
        final String url = requester.url;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() { // aletar aqui
            @Override
            public void onResponse(String response) {
                requester.requester.callBack(response);
                if(requester.requester.isContinuous()){
                    runContinuous(requester, context);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                runner(requester, context, 500);
            }
        });
        queue.add(request);
    }

    private static void runTryReach(final Requester requester, final Context context, int inicial){
        inicial++;
        if(queue == null){
            queue = Volley.newRequestQueue(context);
        }
        final String url = requester.url;
        handler = new Handler();
        final int finalInicial = inicial;
        final Runnable runnable = new Runnable() {
            final Runnable runner = this;
            @Override
            public void run() {
                StringRequest request = new StringRequest(url, new Response.Listener<String>() { // aletar aqui
                    @Override
                    public void onResponse(String response) {
                        requester.requester.callBack(response);
                        if(requester.requester.isContinuous()){
                            runContinuous(requester, context);
                        }
                        handler.removeCallbacks(runner);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(finalInicial <= requester.requester.getAttempts()){
                            Log.i("Tentativa - > ", "" + finalInicial);
                            runTryReach(requester, context, finalInicial);
                        } else {
                            requester.requester.errorCall(500);
                            handler.removeCallbacks(runner);
                        }
                    }
                });
                queue.add(request);
            }
        };
        handler.postDelayed(runnable, 500);
    }

    private static void runContinuous(final Requester requester, final Context context){
        if(queue == null){
            queue = Volley.newRequestQueue(context);
        }
        final String url = requester.url;
        handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                StringRequest request = new StringRequest(url, new Response.Listener<String>() { // aletar aqui
                    @Override
                    public void onResponse(String response) {
                        requester.requester.callBack(response);
                        runContinuous(requester, context);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        runner(requester, context, 500);
                    }
                });
                queue.add(request);
            }
        };
        handler.postDelayed(runnable, requester.delay);
    }

    private class Requester {

        private CallBackRequester requester;
        private boolean coninuous, stage = true;
        private String url;
        private long delay;
        private double hashCode = Math.random() * 1001; // apenas para testes

        public Requester(CallBackRequester requester){
            this.requester = requester;
            this.coninuous = requester.isContinuous();
            this.delay = requester.getDelay();
            this.url = requester.getUrl();
        }

    }

}