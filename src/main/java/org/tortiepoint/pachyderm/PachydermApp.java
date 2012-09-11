package org.tortiepoint.pachyderm;

import org.apache.log4j.Logger;
import org.springframework.util.AntPathMatcher;
import org.tortiepoint.pachyderm.dependency.DependencyResolver;

import javax.script.*;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class PachydermApp {

    private Map<String, Map<String, Object>> handlers = new HashMap<String, Map<String, Object>>();
    private ScriptEngine scriptEngine;
    private static final Logger log = Logger.getLogger(PachydermApp.class);

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
            scriptEngine = new ScriptEngineManager().getEngineByExtension("js");

            scriptEngine.getContext().setAttribute("app", this,
                    ScriptContext.GLOBAL_SCOPE);
            scriptEngine.getContext().setAttribute("maven", new DependencyResolver(), ScriptContext.GLOBAL_SCOPE);
            scriptEngine.getContext().setAttribute("out", System.out, ScriptContext.GLOBAL_SCOPE);
            scriptEngine.eval(reader);

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

    public PachydermResponse getResponse(String verb, String uri, HttpServletRequest request) {
        Object function = null;
        AntPathMatcher pathMatcher = new AntPathMatcher();
        Map<String, String> params = PachydermUtils.extractParams(request);

        for(String pattern : handlers.get(verb).keySet()) {
            if(pathMatcher.match(pattern, uri)) {
                params.putAll(pathMatcher.extractUriTemplateVariables(pattern, uri));
                function = handlers.get(verb).get(pattern);
            }
        }

        PachydermRequest req = new PachydermRequest(request);
        PachydermResponse res = new PachydermResponse(this.scriptEngine);

        req.setParams(params);

        if(function != null) {
            try {
                ((Invocable)scriptEngine).invokeMethod(function, "call", function, req, res);
            } catch (Exception e) {
                log.error("Error invoking handler", e);
            }
        }

        return res;
    }
}
