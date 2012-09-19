package org.tortiepoint.pachyderm;

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
}
