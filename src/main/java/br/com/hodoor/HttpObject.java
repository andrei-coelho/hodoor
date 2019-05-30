package br.com.hodoor;

import android.content.Context;
import android.os.Handler;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

public class HttpObject {

    private List<String> connections = new ArrayList<>();
    private Context context;
    private List<String> responses = new ArrayList<>();

    private static RequestQueue queue;
    private static Handler handler;
    private static Integer delay = 500;

    public HttpObject(Context context){
        this.context = context;
    }
    public HttpObject(Context context, int delay){
        this.context = context;
        this.delay = delay;
    }
    public void setConnection(String url){
        connections.add(url);
    }

    public void setPost(Object o){

    }

    public void go(){
        if(queue == null)
            queue = Volley.newRequestQueue(context);
        //for (String url : connections)
            //getData(url, 0);
    }



    public List<String> getListResponse() {
        return responses;
    }

    public String getResponse(int pos){
        return this.responses.get(pos);
    }
}
