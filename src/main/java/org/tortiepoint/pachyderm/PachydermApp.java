package org.tortiepoint.pachyderm;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;
import org.springframework.util.AntPathMatcher;
import org.tortiepoint.pachyderm.dependency.DependencyResolver;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class PachydermApp {

    private Map<String, Map<String, Object>> handlers = new HashMap<String, Map<String, Object>>();

    private static final Logger log = Logger.getLogger(PachydermApp.class);

    private Context context;
    private ScriptableObject scope;

    {
        for(String verb : new String[] {"get", "post", "delete", "put"}) {
            handlers.put(verb, new HashMap<String, Object>());
        }
    }

    PachydermApp(File file) throws FileNotFoundException, PachydermInitException {
        this(new FileReader(file), file.getPath());
    }

    PachydermApp(Reader reader, String path) throws PachydermInitException {
        try {
            context = Context.enter();
            scope = context.initStandardObjects();

            ScriptableObject.putProperty(scope, "app", Context.javaToJS(this, scope));
            ScriptableObject.putProperty(scope, "maven", Context.javaToJS(new DependencyResolver(), scope));
            ScriptableObject.putProperty(scope, "out", Context.javaToJS(System.out, scope));

            context.evaluateReader(scope, reader, "app.js", 1, null);
            log.info(String.format("Working directory: %s", path));
        } catch (Exception e) {
            throw new PachydermInitException("Error initializing application", e);
        }
    }

    Servlet getServlet() throws Exception {
        return new PachydermServlet(this);
    }

    public void get(String pattern, Object function) {
        mapHandler("get", pattern, function);
    }

    public void post(String pattern, Object function) {
        mapHandler("get", pattern, function);
    }

    public void put(String pattern, Object function) {
        mapHandler("put", pattern, function);
    }

    public void delete(String pattern, Object function) {
        mapHandler("delete", pattern, function);
    }

    private void mapHandler(String verb, String pattern, Object function) {
        log.info(String.format("Mapped (%s) %s", verb, pattern));
        handlers.get(verb).put(pattern, function);
    }

    private Function matchHandler(String verb, String uri, Map<String, String> params) {
        Function function = null;
        AntPathMatcher pathMatcher = new AntPathMatcher();

        for(String pattern : handlers.get(verb).keySet()) {
            if(pathMatcher.match(pattern, uri)) {
                params.putAll(pathMatcher.extractUriTemplateVariables(pattern, uri));
                function = (Function)handlers.get(verb).get(pattern);

                break;
            }
        }

        return function;
    }

    public PachydermResponse getResponse(String verb, String uri, HttpServletRequest request) {
        Map<String, String> params = PachydermUtils.extractParams(request);
        PachydermRequest req = new PachydermRequest(request);
        PachydermResponse res = new PachydermResponse(context, scope);
        Function function = matchHandler(verb, uri, params);

        req.setParams(params);

        if(function != null) {
            try {
                Context context = Context.enter();

                function.call(context, scope, scope, new Object[] {req, res});
            } catch (Exception e) {
                log.error("Error invoking handler", e);
            } finally {
                Context.exit();
            }
        }

        return res;
    }
}
