/**
 * @author Andrei Coelho
 * @version 0.5
 *
 * 2019-03-19
 * https://github.com/andrei-coelho
 * andreifcoelho@gmail.com
 *
 * @descr
 *
 */

package br.com.hodoor;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Hodoor<T> {

    private Context context;
    private String url;
    private Map<String,String> post;
    private Integer id = 1;
    private Class classResponse;
    private Response response;
    private boolean returnObject = true;
    private Integer delay = 100;
    private Integer statusCode = 200;

    private static RequestQueue queue;
    private static Handler handler;

    public static final int HTTP_RESPONSE_ERROR = 1, NO_SUCH_METHOD = 2, INSTANTIATION = 3, ILLEGAL_ACCESS = 4, INVOCATION_TARGET = 5, JSON_CONVERTER = 6;

    public interface Response {

        void httpObjectResponse(Object o, Integer id);
        void httpListResponse(List<?> l, Integer id);
        void httpResponseError(Integer hodoorError, Integer networkResponseError, Integer id);

    }

    public Hodoor (Context context, String url, Class classResponse){
        this.context = context;
        this.url = url;
        this.classResponse = classResponse;
    }

    public void setResponse(Response response){
        this.response = response;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setDelay(Integer delay){
        this.delay = delay;
    }

    public void setPost(HashMap<String, String> map){
        post = map;
    }

    public void setPost(String key, List<?> list){
        try {
            post = new HashMap<>();
            StringBuffer sb = new StringBuffer("[");
            for (Object o: list) {
                sb.append(new ObjectToJson(o, false).toString()).append(",");
            }
            post.put(key, sb.substring(0, sb.length() -1) + "]");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void setPost(String key, Object o){
        try {
            ObjectToJson oj = new ObjectToJson(o, false);
            String jObjectPost = oj.toString();
            post = new HashMap<>();
            post.put(key, jObjectPost);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void send(){
        if(queue == null)
            queue = Volley.newRequestQueue(context);
        getData();
    }

    public void returnObject(boolean b){
        returnObject = b;
    }

    private void getData(){
        StringRequest request = new StringRequest(Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                responseHttp(response);
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getData(0);
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                return post;
            }
        };
        queue.add(request);
    }

    private void getData(final int ini){
        if(handler == null)
            handler = new Handler();
        final Runnable runnable = new Runnable() {
            final Runnable runner = this;
            @Override
            public void run() {
                StringRequest request = new StringRequest(Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        responseHttp(response);
                        handler.removeCallbacks(runner);
                    }
                }, new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(ini < 1){
                            int i = ini + 1;
                            getData(i);
                        } else {
                            if(error.networkResponse != null) {
                                statusCode = error.networkResponse.statusCode;
                            } else {
                                statusCode = 500;
                            }
                            responseHttp(null);
                        }
                        handler.removeCallbacks(runner);
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() {
                        return post;
                    }
                };
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
            this.response.httpResponseError(HTTP_RESPONSE_ERROR, this.statusCode, this.id);
            return;
        }
        if(!returnObject){
            this.response.httpObjectResponse(resp, this.id);
            return;
        }
        try {
            JsonToObject<T> jo =  new JsonToObject<>(resp,classResponse);
            List<T> listO = jo.getList();
            if(listO.size() == 1){
                this.response.httpObjectResponse(listO.get(0),this.id);
                return;
            }
            this.response.httpListResponse(listO,this.id);
        } catch (NoSuchMethodException e) {
            this.response.httpResponseError(NO_SUCH_METHOD, this.statusCode, this.id);
        } catch (InstantiationException e) {
            this.response.httpResponseError(INSTANTIATION, this.statusCode, this.id);
        } catch (IllegalAccessException e) {
            this.response.httpResponseError(ILLEGAL_ACCESS, this.statusCode, this.id);
        } catch (InvocationTargetException e) {
            this.response.httpResponseError(INVOCATION_TARGET, this.statusCode, this.id);
        } catch (NullPointerException e){
            this.response.httpResponseError(JSON_CONVERTER, this.statusCode, this.id);
        }

    }

}
