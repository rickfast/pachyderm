package org.tortiepoint.pachyderm;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;
import org.tortiepoint.pachyderm.dependency.DependencyResolver;
import org.tortiepoint.pachyderm.handler.RequestHandlerManager;
import org.tortiepoint.pachyderm.response.ResponseData;
import org.tortiepoint.pachyderm.response.ResponseRenderer;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map;

public class PachydermApp {

    private static final Logger log = Logger.getLogger(PachydermApp.class);

    private Context context;
    private ScriptableObject scope;
    private RequestHandlerManager requestHandlerManager = new RequestHandlerManager();
    private ResponseRenderer responseRenderer;
    private PachydermConfig config;

    PachydermApp(File file) throws FileNotFoundException, PachydermException {
        this(new FileReader(file), file.getParentFile().getPath());
    }

    PachydermApp(Reader reader, String path) throws PachydermException {
        try {
            context = Context.enter();
            scope = context.initStandardObjects();
            responseRenderer = new ResponseRenderer(scope, path);
            config = new PachydermConfig(path + "/views", path + "/public");

            ScriptableObject.putProperty(scope, "app", Context.javaToJS(this, scope));
            ScriptableObject.putProperty(scope, "maven", Context.javaToJS(new DependencyResolver(), scope));
            ScriptableObject.putProperty(scope, "out", Context.javaToJS(System.out, scope));
            ScriptableObject.putProperty(scope, "config", Context.javaToJS(config, scope));

            context.evaluateReader(scope, reader, "app.js", 1, null);
            log.info(String.format("Working directory: %s", path));
        } catch (Exception e) {
            throw new PachydermException("Error initializing application", e);
        }
    }

    Servlet getServlet() throws Exception {
        return new PachydermServlet(this);
    }

    public void get(String pattern, Object function) {
        requestHandlerManager.mapHandler("get", pattern, function);
    }

    public void post(String pattern, Object function) {
        requestHandlerManager.mapHandler("get", pattern, function);
    }

    public void put(String pattern, Object function) {
        requestHandlerManager.mapHandler("put", pattern, function);
    }

    public void delete(String pattern, Object function) {
        requestHandlerManager.mapHandler("delete", pattern, function);
    }

    public PachydermConfig getConfig() {
        return config;
    }

    public ResponseData getResponse(String verb, String uri, HttpServletRequest request) throws Exception {
        Map<String, String> params = PachydermUtils.extractParams(request);
        PachydermRequest req = new PachydermRequest(request);
        PachydermResponse res = new PachydermResponse();
        Function function = requestHandlerManager.matchHandler(verb, uri, params);

        req.setParams(params);

        if (function != null) {
            try {
                Context context = Context.enter();

                function.call(context, scope, scope, new Object[]{req, res});
            } catch (Exception e) {
                log.error("Error invoking handler", e);
            } finally {
                Context.exit();
            }
        } else {
            throw new PachydermException(String.format("No handler found for mapping (%s) %s", verb, uri), null);
        }

        return responseRenderer.render(res.getData());
    }
}
