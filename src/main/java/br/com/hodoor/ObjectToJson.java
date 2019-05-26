/**
 * @class ObjectToJson
 * @author Andrei
 * @version 1.2
 * @date 2019-03-19
 * @contact: https://github.com/andrei-coelho
 *
 * @descr
 * This class transform Object to Json String
 *
 */

package br.com.hodoor;

import org.json.JSONException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import br.com.hodoor.annotation.BLOCK;
import br.com.hodoor.annotation.BOUND;
import br.com.hodoor.annotation.CHANGE;
import br.com.hodoor.annotation.CLASSNAME;


public class ObjectToJson {

	private StringBuffer jsonBuffer = new StringBuffer();
	private JSON jsonObject;

	public ObjectToJson(Object o, boolean classname) throws IllegalAccessException, IllegalArgumentException {
		Class<?> c = o.getClass();
		String nameClass = c.getSimpleName();
		if(c.isAnnotationPresent(CLASSNAME.class) && classname) {
			String str = c.getAnnotation(CLASSNAME.class).name();
			if(!str.trim().equals("")) nameClass = str;
		}
		if(classname)
			jsonBuffer.append("{\"").append(nameClass).append("\":");
		if(isNotObjectJava(o.getClass()))
			readObject(o);
		else
			readElement(o);
		cleanJson();
		if(classname)
			jsonBuffer.append("}");
	}

	public JSON toJson() throws JSONException {
		if(jsonObject == null)
			jsonObject = new JSON(jsonBuffer);
		return jsonObject;
	}

	@Override
	public String toString() {
		return jsonBuffer.toString();
	}

	private void readObject(Object o) throws IllegalAccessException, IllegalArgumentException {
		Class<?> c = o.getClass();
		if(isNotObjectJava(c))
			readIfIsSuperClass(c, o);
		jsonBuffer.append("{");
		readFields(c, o);
		cleanJson();
		jsonBuffer.append("},");
	}

	private void readSuperClass(Class<?> c,  Object o) throws IllegalArgumentException, IllegalAccessException {
		readIfIsSuperClass(c, o);
		readFields(c, o);
	}

	private void readFields(Class<?> c,  Object o) throws IllegalAccessException, IllegalArgumentException {

		for(Field field : c.getDeclaredFields()){
			if(field.isAnnotationPresent(BLOCK.class))
				if(field.getAnnotation(BLOCK.class).get())
					continue;

			field.setAccessible(true);

			String key = field.getName();
			boolean isAnnotatedChange = field.isAnnotationPresent(CHANGE.class);
			CHANGE an = null;
			if(isAnnotatedChange) {
				an = field.getAnnotation(CHANGE.class);
				String param = an.paramGet();
				if(!param.equals("DFAULT"))
					key = param;
			}
			if(key.equals("$change") || key.equals("serialVersionUID")) continue;

			jsonBuffer.append("\"").append(key).append("\":");

			if(isNotObjectJava(field.getClass())) {
				Object newO = field.get(o);
				jsonBuffer.append("{");
				readObject(newO);
				cleanJson();
				jsonBuffer.append("}");
				continue;
			}
			Object val = field.get(o);

			if(isAnnotatedChange) {
				String value = an.valueGet();
				if(!value.equals("DFAULT"))
					val = value;
			}

			readElement(val);

		}
	}

	private void readElement(Object value) throws IllegalAccessException, IllegalArgumentException {
		if(value instanceof String || value instanceof StringBuffer || value instanceof StringBuilder || value instanceof Character) {
			jsonBuffer.append("\"").append(escapeValues(value.toString())).append("\",");
		} else
		if(value instanceof Number || value instanceof Boolean) {
			jsonBuffer.append(String.valueOf(value)).append(",");
		} else
		if(value instanceof List) {
			readList((List<?>) value);
		} else
		if(value instanceof Map) {
			readList((Map<?,?>) value);
		} else
		if(value instanceof Set) {
			readList((Set<?>) value);
		} else
		if(value instanceof Queue) {
			readList((Queue<?>) value);
		} else {
			jsonBuffer.append("\"\",");
		}

	}

	private void readList(Set<?> set) throws IllegalAccessException, IllegalArgumentException {
		if(set == null || set.size() == 0) {
			jsonBuffer.append("[],");
			return;
		}
		jsonBuffer.append("[");
		for(Object o : set) {
			if(isNotObjectJava(o.getClass())) {
				readObject(o);
				continue;
			}
			readElement(o);
		}
		cleanJson();
		jsonBuffer.append("],");
	}

	private void readList(Queue<?> queue) throws IllegalAccessException, IllegalArgumentException {
		if(queue == null || queue.size() == 0) {
			jsonBuffer.append("[],");
			return;
		}
		jsonBuffer.append("[");
		for(Object o : queue.toArray()) {
			if(isNotObjectJava(o.getClass())) {
				readObject(o);
				continue;
			}
			readElement(o);
		}
		cleanJson();
		jsonBuffer.append("],");
	}

	private void readList(List<?> list) throws IllegalAccessException, IllegalArgumentException {
		if(list == null || list.size() == 0) {
			jsonBuffer.append("[],");
			return;
		}
		jsonBuffer.append("[");
		for(int i = 0; i < list.size(); i++) {
			Object o = list.get(i);
			if(isNotObjectJava(o.getClass())) {
				readObject(o);
				continue;
			}
			readElement(o);
		}
		cleanJson();
		jsonBuffer.append("],");

	}

	private void readList(Map<?,?> map) throws IllegalAccessException, IllegalArgumentException {
		if(map == null || map.size() == 0) {
			jsonBuffer.append("{},");
			return;
		}
		jsonBuffer.append("{");
		for (Map.Entry<?,?> element : map.entrySet()) {
			jsonBuffer.append("\"").append(escapeValues(String.valueOf(element.getKey()))).append("\":");
			Object o = element.getValue();
			if(isNotObjectJava(o.getClass())) {
				readObject(o);
				continue;
			}
			readElement(o);
		}
		cleanJson();
		jsonBuffer.append("},");
	}

	private void readIfIsSuperClass(Class<?> c, Object o) throws IllegalArgumentException, IllegalAccessException {
		if(c.isAnnotationPresent(BOUND.class)) return;

		Class<?> superC = c.getSuperclass();
		if(superC != null && isNotObjectJava(superC))
			readSuperClass(superC, o);
	}


	private String escapeValues(String value) {
		value = value.replace("\\", "\\\\");
		value = value.replace("\"", "\\\"");
		return value;
	}


	private void cleanJson() {
		int total = jsonBuffer.length();
		jsonBuffer = jsonBuffer.replace( total - 1, total, "" );
	}


	public static boolean isNotObjectJava(Class<?> c) {
		return !c.isPrimitive() && !c.getName().startsWith("java.lang");
	}
}
