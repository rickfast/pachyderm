package org.tortiepoint.pachyderm;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * Created with IntelliJ IDEA.
 * User: rickfast
 * Date: 9/7/12
 * Time: 8:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class Pachyderm {

    public static void main(String[] argz) {
        Server server = new Server(8080);
        try {
            Context root = new Context(server, "/", Context.SESSIONS);
            root.addServlet(new ServletHolder(new PachydermServlet()), "/*");

            server.start();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
