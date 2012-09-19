package org.tortiepoint.pachyderm;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.mozilla.javascript.*;

import java.io.FileReader;

public class PachydermResponse {

    private String body = "";
    private String contentType = "";
    private int statusCode = 200;
    private final Context context;
    private final ScriptableObject scope;
    private final String _ = PachydermApp.class.getResource("/underscore.js").getFile();

    public PachydermResponse(Context context, ScriptableObject scope) {
        this.context = context;
        this.scope = scope;
    }

    public void renderStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public void renderJson(Object responseBody) throws Exception {
        contentType = "application/json";
        body = responseBody instanceof NativeObject
                ? NativeJSON.stringify(context, scope, responseBody, null, null).toString()
                : new ObjectMapper().writeValueAsString(responseBody);
    }

    public void renderXml(Object responseBody) throws Exception {
        contentType = "application/xml";
        XStream xStream = new XStream();

        if(responseBody instanceof NativeObject) {
            throw new UnsupportedOperationException("JSON to XML conversion not yet supported");
        }

        body = xStream.toXML(responseBody);
    }

    public void render(NativeObject response) throws Exception {
        if(response.containsKey("json")) {
            renderJson(response.get("json"));
        } else if(response.containsKey("xml")) {
            renderXml(response.get("xml"));
        } else if(response.containsKey("view")) {
            renderView(response);
        }

        if(response.containsKey("status")) {
            renderStatus((Integer) response.get("status"));
        }
    }

    public void renderView(NativeObject model) throws Exception {
        Object data = model.get("model");
        String view = model.get("view").toString();
        String template = IOUtils.toString(new FileReader(view));

        context.evaluateReader(scope, new FileReader(_), "underscore.js", 1, null);

        Function function = (Function) ((ScriptableObject) scope.get("_")).get("template");

        body = function.call(context, scope, scope, new Object[] { template, data }).toString();
        contentType = "text/html";
    }

    public String getBody() {
        return body;
    }

    public String getContentType() {
        return contentType;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String toString() {
        return body;
    }
}
