package org.tortiepoint.pachyderm.response;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.mozilla.javascript.*;

import javax.servlet.http.HttpServletResponse;
import java.io.FileReader;

public class ResponseRenderer {
    private ScriptableObject scope;
    private String path;
    private static final String _ = ResponseRenderer.class.getResource("/underscore.js").getFile();

    public ResponseRenderer(ScriptableObject scope, String path) {
        this.scope = scope;
        this.path = path;
    }

    public ResponseData renderStatus(int statusCode) {
        return new ResponseData(statusCode);
    }

    private ResponseData renderJson(Object responseBody) throws Exception {
        Context context = Context.enter();
        ResponseData responseData = new ResponseData(responseBody instanceof NativeObject
                ? NativeJSON.stringify(context, scope, responseBody, null, null).toString()
                : new ObjectMapper().writeValueAsString(responseBody), "application/json");
        Context.exit();

        return responseData;
    }

    private ResponseData renderXml(Object responseBody) throws Exception {
        String contentType = "application/xml";
        XStream xStream = new XStream();

        if (responseBody instanceof NativeObject) {
            throw new UnsupportedOperationException("JSON to XML conversion not yet supported");
        }

        String body = xStream.toXML(responseBody);

        return new ResponseData(body, contentType);
    }

    public ResponseData render(NativeObject response) throws Exception {
        ResponseData responseData = new ResponseData(200);

        if (response.containsKey("text")) {
            responseData = new ResponseData(response.get("text").toString(), "text");
        } else if (response.containsKey("json")) {
            responseData = renderJson(response.get("json"));
        } else if (response.containsKey("xml")) {
            responseData = renderXml(response.get("xml"));
        } else if (response.containsKey("view")) {
            responseData = renderView(response);
        } else if (response.containsKey("status")) {
            responseData = renderStatus((Integer) response.get("status"));
        }

        return responseData;
    }

    private ResponseData renderView(NativeObject model) throws Exception {
        Object data = model.get("model");
        String view = model.get("view").toString();
        String template = IOUtils.toString(new FileReader(String.format("%s/views/%s", path, view)));
        Context context = Context.enter();

        context.evaluateReader(scope, new FileReader(_), "underscore.js", 1, null);

        Function function = (Function) ((ScriptableObject) scope.get("_")).get("template");

        ResponseData responseData = new ResponseData(function.call(context, scope,
                scope, new Object[]{template, data}).toString(), "text/html");

        Context.exit();

        return responseData;
    }

}
