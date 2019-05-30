package br.com.hodoor;

/**
 * @author Andrei Coelho
 * @version 1.4
 *
 * 2019-03-19
 * https://github.com/andrei-coelho
 * andreifcoelho@gmail.com
 *
 * @descr
 *
 */


import android.opengl.GLException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.hodoor.annotation.BLOCK;
import br.com.hodoor.annotation.CHANGE;


public class JsonToObject<T> {

    private List<T> list = new ArrayList<>();

    JsonToObject(String json, Class<?> c) throws GLException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        try {
            JSONObject jo = new JSONObject(json);
            setList(jo, c);
        } catch (JSONException e) {
            try {
                JSONArray ja = new JSONArray(json);
                setList(ja, c);
            } catch (JSONException e1) {
                this.list = null;
                e1.printStackTrace();
            }
        }

    }

    @SuppressWarnings("unchecked")
    private void setList(JSONObject j, Class<?> c) throws GLException, InvocationTargetException, NoSuchMethodException, InstantiationException, JSONException, IllegalAccessException {

        Object obj;
        obj = readJson(j,c);
        try {
            list.add((T) obj);
        } catch (GLException e) {
            throw new GLException(50,"Could not create object. The class referenced in type is not the same as the class passed as argument.");
        }

    }
    private void setList(JSONArray j, Class<?> c) throws GLException, JSONException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

        for (int i = 0; i < j.length(); i++) {
            if(j.get(i) instanceof JSONObject) {
                JSONObject jo = j.getJSONObject(i);
                setList(jo, c);
            }
        }

    }


    private Object readJson(JSONArray j, Field f) throws JSONException, GLException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        if(j.get(0) == null) return null;
        boolean isObject = j.get(0) instanceof JSONObject;

        Collection<Object> c = getCollectionByType(f);
        if(c == null) return null;

        for(int i = 0; i < j.length(); i++) {
            if(isObject) {
                ParameterizedType stringListType = (ParameterizedType) f.getGenericType();
                Class<?> classObj = (Class<?>) stringListType.getActualTypeArguments()[0];
                c.add(readJson((JSONObject) j.get(i), classObj));
                continue;
            }
            c.add(j.get(i));
        }

        return c;
    }

    private Object readJson(JSONObject j, Class<?> c) throws GLException, JSONException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {

        Object obj;
        try {
            Constructor<?> cons = c.getDeclaredConstructor();
            cons.setAccessible(true);
            obj = cons.newInstance();
        } catch (GLException e) {
            throw new GLException(100, "This is not a valid constructor. Create empty private or public constructor in " + c.getCanonicalName());
        }

        Field[] fields = obj.getClass().getDeclaredFields();
        HashMap<String, String> fieldsAlter = new HashMap<>();
        HashMap<String, Boolean> fieldsBlock = new HashMap<>();

        for (Field f : fields) {
            f.setAccessible(true);
            if (f.isAnnotationPresent(BLOCK.class)) {
                BLOCK block = f.getAnnotation(BLOCK.class);
                if (block.set()) fieldsBlock.put(f.getName(), true);
            }

            if (f.isAnnotationPresent(CHANGE.class)) {
                CHANGE change = f.getAnnotation(CHANGE.class);
                if (!change.paramSet().equals("DFAULT"))
                    fieldsAlter.put(change.paramSet(), f.getName());
            }
        }

        Iterator<String> i = j.keys();

        while(i.hasNext()) {
            String data = i.next();
            Object n = j.get(data);
            Field f;
            if(fieldsBlock.containsKey(data))
                continue;
            if(fieldsAlter.containsKey(data))
                data = fieldsAlter.get(data);

            try {
                f = obj.getClass().getDeclaredField(data);
                f.setAccessible(true);
            } catch(GLException e) {
                continue;
            } catch (NoSuchFieldException e) {
                continue;
            }

            if(n instanceof JSONObject) {
                char m = isMap(f);
                if(m != 'N') {
                    f.set(obj, getMapByType((JSONObject) n, f, m));
                    continue;
                }
                if(ObjectToJson.isNotObjectJava(f.getClass())) {
                    f.set(obj, readJson((JSONObject) n, f.getClass()));
                    continue;
                }
                if(ObjectToJson.isNotObjectJava(f.getType())) {
                    f.set(obj, readJson((JSONObject) n, f.getType()));
                    continue;
                }
                f.set(obj, null);
                continue;
            }
            if(n instanceof JSONArray) {
                f.set(obj, readJson((JSONArray) n, f));
                continue;
            }

            f.set(obj, n);
        }
        return obj;
    }


    private Collection<Object> getCollectionByType(Field f){

        Collection<Object> c = null;
        Type t = f.getType();
        if(t.equals(Queue.class) || t.equals(LinkedList.class)){
            c = new LinkedList<>();
        } else
        if(t.equals(List.class) || t.equals(ArrayList.class)){
            c = new ArrayList<>();
        } else
        if(t.equals(PriorityQueue.class)) {
            c = new PriorityQueue<>();
        } else
        if(t.equals(Vector.class)) {
            c = new Vector<>();
        } else
        if(t.equals(Set.class) || t.equals(HashSet.class)) {
            c = new HashSet<>();
        } else
        if(t.equals(LinkedHashSet.class)) {
            c = new LinkedHashSet<>();
        } else
        if(t.equals(TreeSet.class)) {
            c = new ArrayDeque<>();
        }
        return c;
    }


    private Map<String, Object> getMapByType(JSONObject j, Field f, char m) throws GLException, JSONException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        ParameterizedType stringListType = (ParameterizedType) f.getGenericType();
        Class<?> key = (Class<?>) stringListType.getActualTypeArguments()[0];
        Class<?> val = (Class<?>) stringListType.getActualTypeArguments()[1];

        if(!key.equals(String.class))
            throw new GLException(200, "The Map< **String**, Object > keys can only be string");

        boolean status = ObjectToJson.isNotObjectJava(val);
        Map<String, Object> map = null;
        switch(m) {
            case 'H': map = new HashMap<>(); break;
            case 'T': map = new TreeMap<>(); break;
            case 'L': map = new LinkedHashMap<>(); break;
        }

        Iterator<String> i = j.keys();
        if(map != null)
        while(i.hasNext()) {

            String data = i.next();
            Object n = j.get(data);

            if(status) {
                if(n instanceof JSONObject) {
                    n = readJson((JSONObject) n, f.getClass());
                } else if(n instanceof JSONArray) {
                    n = readJson((JSONArray) n, f);
                } else {
                    throw new GLException(300, "The object was not identified");
                }
            }

            map.put(data, n);
        }

        return map;
    }


    private char isMap(Field f) {

        Type t = f.getType();
        if(t.equals(Map.class)){
            return 'H';
        } else
        if(t.equals(HashMap.class)){
            return 'H';
        } else
        if(t.equals(TreeMap.class)) {
            return 'T';
        } else
        if(t.equals(LinkedHashMap.class)) {
            return 'L';
        } else {
            return 'N';
        }

    }

    public List<T> getList() {
        return list;
    }

}
