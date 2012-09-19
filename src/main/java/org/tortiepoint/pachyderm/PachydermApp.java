package org.tortiepoint.pachyderm;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.mozilla.javascript.*;
import org.springframework.util.AntPathMatcher;
import org.tortiepoint.pachyderm.dependency.DependencyResolver;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class PachydermApp {

    private Map<String, Map<String, Object>> handlers = new HashMap<String, Map<String, Object>>();

    private static final Logger log = Logger.getLogger(PachydermApp.class);
    private static final String _ = PachydermApp.class.getResource("/underscore.js").getFile();

    private Context context;
    private ScriptableObject scope;
    private String path;

    {
        for (String verb : new String[]{"get", "post", "delete", "put"}) {
            handlers.put(verb, new HashMap<String, Object>());
        }
    }

    PachydermApp(File file) throws FileNotFoundException, PachydermInitException {
        this(new FileReader(file), file.getParentFile().getPath());
    }

    PachydermApp(Reader reader, String path) throws PachydermInitException {
        try {
            this.path = path;
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

        for (String pattern : handlers.get(verb).keySet()) {
            if (pathMatcher.match(pattern, uri)) {
                params.putAll(pathMatcher.extractUriTemplateVariables(pattern, uri));
                function = (Function) handlers.get(verb).get(pattern);

                break;
            }
        }

        return function;
    }

    public void getResponse(String verb, String uri, HttpServletRequest request,
                                         HttpServletResponse response) throws Exception {
        Map<String, String> params = PachydermUtils.extractParams(request);
        PachydermRequest req = new PachydermRequest(request);
        PachydermResponse res = new PachydermResponse();
        Function function = matchHandler(verb, uri, params);

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
        }

        render(response, res.getData());
    }

    public PachydermResponseData renderStatus(int statusCode) {
        return new PachydermResponseData(statusCode);
    }

    private PachydermResponseData renderJson(Object responseBody) throws Exception {
        return new PachydermResponseData(responseBody instanceof NativeObject
                ? NativeJSON.stringify(context, scope, responseBody, null, null).toString()
                : new ObjectMapper().writeValueAsString(responseBody), "application/json");
    }

    private PachydermResponseData renderXml(Object responseBody) throws Exception {
        String contentType = "application/xml";
        XStream xStream = new XStream();

        if (responseBody instanceof NativeObject) {
            throw new UnsupportedOperationException("JSON to XML conversion not yet supported");
        }

        String body = xStream.toXML(responseBody);

        return new PachydermResponseData(body, contentType);
    }

    private void render(HttpServletResponse servletResponse, NativeObject response) throws Exception {
        PachydermResponseData responseData = new PachydermResponseData(200);

        if (response.containsKey("json")) {
            responseData = renderJson(response.get("json"));
        } else if (response.containsKey("xml")) {
            responseData = renderXml(response.get("xml"));
        } else if (response.containsKey("view")) {
            responseData = renderView(response);
        } else if (response.containsKey("status")) {
            responseData = renderStatus((Integer) response.get("status"));
        }

        int statusCode = responseData.getStatusCode();

        servletResponse.setStatus(statusCode);
        servletResponse.setContentType(responseData.getContentType());

        if (servletResponse.getWriter() != null) {
            servletResponse.getWriter().write(responseData.getBody());
        }
    }

    private PachydermResponseData renderView(NativeObject model) throws Exception {
        Object data = model.get("model");
        String view = model.get("view").toString();
        String template = IOUtils.toString(new FileReader(String.format("%s/views/%s", path, view)));
        Context context = Context.enter();

        context.evaluateReader(scope, new FileReader(_), "underscore.js", 1, null);

        Function function = (Function) ((ScriptableObject) scope.get("_")).get("template");

        PachydermResponseData responseData = new PachydermResponseData(function.call(context, scope,
                scope, new Object[]{template, data}).toString(), "text/html");

        Context.exit();

        return responseData;
    }

    private void render(HttpServletResponse response, String body, String contentType, int statusCode) {

    }
}
