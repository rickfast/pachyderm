package org.tortiepoint.pachyderm;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: rickfast
 * Date: 9/7/12
 * Time: 8:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class PachydermServlet extends HttpServlet {

    private PachydermApp app = new PachydermApp("/app.js");

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        PachydermResponse response = app.getResponse(req.getPathInfo(), req);

        res.setStatus(response.getStatusCode());
        res.setContentType(response.getContentType());

        if(res.getWriter() != null) {
            res.getWriter().write(response.getBody());
        }
    }
}
