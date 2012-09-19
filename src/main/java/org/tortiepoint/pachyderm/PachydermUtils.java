package org.tortiepoint.pachyderm;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJSON;
import org.mozilla.javascript.NativeObject;
import org.tortiepoint.pachyderm.dependency.DependencyResolver;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.ServletRequest;
import java.util.AbstractMap;
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
