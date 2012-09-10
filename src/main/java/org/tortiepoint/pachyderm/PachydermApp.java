package org.tortiepoint.pachyderm;

import org.springframework.util.AntPathMatcher;
import org.tortiepoint.pachyderm.dependency.DependencyResolver;

import javax.script.*;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class PachydermApp {

    private Map<String, Object> endpoints = new HashMap<String, Object>();
    private ScriptEngine scriptEngine;

    public PachydermApp(String sourceFile) {
        this.scriptEngine = new ScriptEngineManager().getEngineByExtension("js");
        try {
            this.scriptEngine.getContext().setAttribute("app", this,
                    ScriptContext.GLOBAL_SCOPE);
            this.scriptEngine.getContext().setAttribute("maven", new DependencyResolver(), ScriptContext.GLOBAL_SCOPE);
            this.scriptEngine.getContext().setAttribute("out", System.out, ScriptContext.GLOBAL_SCOPE);
            this.scriptEngine.eval(new InputStreamReader(PachydermApp.class.getResourceAsStream(sourceFile)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PachydermApp(ScriptEngine scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

    public void get(String pattern, Object function) {
        endpoints.put(pattern, function);
    }

    public PachydermResponse getResponse(String uri, HttpServletRequest request) {
        Object function = null;
        AntPathMatcher pathMatcher = new AntPathMatcher();
        Map<String, String> params = PachydermUtils.extractParams(request);

        for(String pattern : endpoints.keySet()) {
            if(pathMatcher.match(pattern, uri)) {
                params.putAll(pathMatcher.extractUriTemplateVariables(pattern, uri));
                function = endpoints.get(pattern);
            }
        }

        PachydermRequest req = new PachydermRequest(request);
        PachydermResponse res = new PachydermResponse();

        req.setParams(params);

        if(function != null) {
            try {
                ((Invocable)scriptEngine).invokeMethod(function, "call", function, req, res);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(true) throw new RuntimeException("powpers");

        return res;
    }

    public static void main(String[] args) {
        ScriptEngine js = new ScriptEngineManager().getEngineByExtension("js");
        PachydermApp app;
        try {
            js.getContext().setAttribute("app", app = new PachydermApp(js),
                    ScriptContext.GLOBAL_SCOPE);
            js.getContext().setAttribute("out", System.out, ScriptContext.GLOBAL_SCOPE);
            js.eval(new InputStreamReader(PachydermApp.class.getResourceAsStream("/app.js")));
            System.out.println(app.getResponse("/", null));
        } catch (ScriptException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
