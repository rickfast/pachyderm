package org.tortiepoint.pachyderm;

import org.codehaus.jackson.map.ObjectMapper;
import sun.org.mozilla.javascript.internal.NativeObject;

import java.io.IOException;

public class PachydermResponse {

    private String body = "";
    private String contentType = "";
    private int statusCode = 200;

    public void renderStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public void renderJson(Object responseBody) throws Exception {
        contentType = "application/json";

        responseBody = responseBody instanceof NativeObject
                    ? PachydermUtils.objectToMap((NativeObject)responseBody)
                    : responseBody;
        body = new ObjectMapper().writeValueAsString(responseBody);
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
