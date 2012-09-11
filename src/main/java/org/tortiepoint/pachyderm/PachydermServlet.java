package org.tortiepoint.pachyderm;

import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: rickfast
 * Date: 9/7/12
 * Time: 8:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class PachydermServlet extends HttpServlet {

    private PachydermApp app;
    private Template template;

    {
        Reader reader = new InputStreamReader(PachydermApp.class.getResourceAsStream("/error.template"));
        SimpleTemplateEngine simpleTemplateEngine = new SimpleTemplateEngine();

        try {
            this.template = simpleTemplateEngine.createTemplate(reader);
        } catch (IOException e) {

        }
    }

    public PachydermServlet(PachydermApp app) {
        this.app = app;
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            PachydermResponse response = app.getResponse(req.getMethod().toLowerCase(), req.getPathInfo(), req);
            int statusCode = response.getStatusCode();

            res.setStatus(statusCode);
            res.setContentType(response.getContentType());

            if (res.getWriter() != null) {
                res.getWriter().write(response.getBody());
            }
        } catch (Exception e) {
            Map<String, String> bindings = new HashMap<String, String>();
            bindings.put("errorMessage", e.getMessage());
            String html = this.template.make(bindings).toString();

            res.getWriter().write(html);
        }
    }
}
