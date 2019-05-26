package br.com.hodoor;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;


public class Hodoor<T> {

    private Context context;
    private String url;
    private JSON json;
    private HashMap<String,String> post;
    private int id = 1;
    private Class classResponse;
    private Response response;
    private boolean returnJson = true;

    private static RequestQueue queue;
    private static Handler handler;
    private static Integer delay = 500;

    public interface Response {
        public void response(Object o, int id, String error);
    }

    public Hodoor (Context context, String url, Class classResponse){
        this.context = context;
        this.url = url;
        this.classResponse = classResponse;
    }

    public void send(Object o) {
        try {
            ObjectToJson oj = new ObjectToJson(o, true);
            json = oj.toJson();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void send(){
        if(queue == null)
            queue = Volley.newRequestQueue(context);
        getData(0);
    }

    public void send(HashMap<String, String> map) {

    }

    public void send(List<?> list){
        // transformar os objetos em json
    }

    public void setResponse(Response response){
        this.response = response;
    }

    public void setId(int id){
        this.id = id;
    }

    public void returnJson(boolean b){
        returnJson = b;
    }

    private void getData(final int ini){
        handler = new Handler();
        final Runnable runnable = new Runnable() {
            final Runnable runner = this;
            @Override
            public void run() {
                StringRequest request = new StringRequest(url, new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        responseHttp(response);
                        handler.removeCallbacks(runner);
                    }
                }, new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(ini < 2){
                            int i = ini + 1;
                            getData(i);
                        } else {
                            responseHttp(null);
                            handler.removeCallbacks(runner);
                        }
                    }
                });
                queue.add(request);
            }
        };
        handler.postDelayed(runnable, delay);
    }

    private void responseHttp(String resp){

        if(this.response == null) {
            Log.e("Error", "Can not return received value because interface method has not been configured on activity or fragment");
            return;
        }
        if(resp == null){
            this.response.response(null, this.id, null);
            return;
        }
        if(!returnJson){
            this.response.response(resp, this.id, null);
            return;
        }
        try {
            JsonToObject<T> jo =  new JsonToObject<>(resp,classResponse);
            List<T> listO = jo.getList();
            if(listO.size() == 1){
                this.response.response(listO.get(0),this.id, null);
                return;
            }
            this.response.response(listO,this.id, null);
        } catch (NoSuchMethodException e) {
            this.response.response(null,this.id, "NoSuchMethodException");
        } catch (InstantiationException e) {
            this.response.response(null,this.id, "InstantiationException");
        } catch (IllegalAccessException e) {
            this.response.response(null,this.id, "IllegalAccessException");
        } catch (InvocationTargetException e) {
            this.response.response(null,this.id, "InvocationTargetException");
        }

    }

}
