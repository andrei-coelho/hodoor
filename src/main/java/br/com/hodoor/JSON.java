/**
 * @class JSON
 * @author Andrei
 * @version 1.0	
 * @date 2019-03-19
 * @contact: https://github.com/andrei-coelho
 * 
 * @descr
 * 
 * 
 */
package br.com.hodoor;

import android.opengl.GLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSON {
	
	private JSONObject jsonO;
	private JSONArray jsonA;
	private String valueString;
	private Integer valueInt;
	private Double valueDouble;
	private Boolean valueBoolean;
	
	public JSON() {}
	
	JSON(StringBuffer formatJson) throws JSONException {
		jsonO = new JSONObject(formatJson.toString());
	}
	
	public JSON(String formatJson) throws JSONException {
		jsonO = new JSONObject(formatJson);
	}
	
	JSON(JSONArray array) {
		jsonA = array;
	}
	
	JSON(JSONObject object) {
		jsonO = object;
	}
	
	public JSON get(int i) {
		Object json = null;
		try {
			json = jsonA.get(i);
		} catch (GLException e) {
			return null;
		} catch (JSONException e) {
			return null;
		}
		if(json instanceof JSONArray) {
			return new JSON((JSONArray) json);
		}
		if(json instanceof JSONObject) {
			return new JSON((JSONObject) json);
		}
		JSON j = new JSON();
		j.setValue(json);
		return j;
	}
	
	public JSON get(String str) {
		Object json = null;
		try {
			json = jsonO.get(str);
		} catch (GLException e) {
			return null;
		} catch (JSONException e) {
			return null;
		}
		if(json instanceof JSONObject) {
			return new JSON((JSONObject) json);
		}
		if(json instanceof JSONArray) {
			return new JSON((JSONArray) json);
		}
		JSON j = new JSON();
		j.setValue(json);
		return j;
	}
	
	public String value() {
		if(valueString != null) {
			return valueString;
		}
		if(valueInt != null) {
			return String.valueOf(valueInt);
		}
		if(valueDouble != null) {
			return String.valueOf(valueDouble);
		}
		if(valueBoolean != null) {
			return String.valueOf(valueBoolean);
		}
		return null;
	}
	
	private void setValue(Object json) {
		if(json instanceof Double) {
			valueDouble = (Double) json;
			return;
		}
		if(json instanceof Integer) {
			valueInt = (Integer) json;
			return;
		}
		if(json instanceof String) {
			valueString = (String) json;
			return;
		}
		if(json instanceof Boolean) {
			valueBoolean = (Boolean) json;
			return;
		}
	}
	
	
}
