package org.tortiepoint.pachyderm.response;

public class ResponseData {

    private String body = "";
    private String contentType = "";
    private int statusCode = 200;

    public ResponseData(String body, String contentType, int statusCode) {
        this.body = body;
        this.contentType = contentType;
        this.statusCode = statusCode;
    }

    public ResponseData(String body, String contentType) {
        this(body, contentType, 200);
    }

    public ResponseData(int statusCode) {
        this("", "", statusCode);
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
