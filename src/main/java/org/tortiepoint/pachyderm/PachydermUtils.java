package org.tortiepoint.pachyderm;

import sun.org.mozilla.javascript.internal.NativeObject;

import javax.servlet.ServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class PachydermUtils {

    public static Map<String, String> extractParams(ServletRequest request) {
        Map<String, String> result = new HashMap<String, String>();
        Enumeration params = request.getParameterNames();

        while(params.hasMoreElements()) {
            String name = params.nextElement().toString();

            result.put(name, request.getParameter(name));
        }

        return result;
    }

    /**
     * TODO handle complex objects
     */
    public static Map<String,Object> objectToMap( NativeObject obj ) {

        HashMap<String,Object> map = new HashMap<String,Object>();

        for( Object id: obj.getIds() ) {
            String key;
            Object value;

            if (id instanceof String ) {
                key = (String) id;
                value = obj.get(key,obj);
            } else if (id instanceof Integer) {
                key = id.toString();
                value = obj.get( ((Integer)id).intValue(), obj);
            } else {
                throw new IllegalArgumentException();
            }

            map.put( key, value );
        }

        return map;
    }
}
