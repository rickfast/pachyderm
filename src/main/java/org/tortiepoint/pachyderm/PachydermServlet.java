package org.tortiepoint.pachyderm;

import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

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
            app.getResponse(req.getMethod().toLowerCase(), req.getPathInfo(), req, res);
        } catch (Exception e) {
            Map<String, String> bindings = new HashMap<String, String>();
            bindings.put("errorMessage", e.getMessage());
            String html = this.template.make(bindings).toString();

            res.getWriter().write(html);
        }
    }
}
