package org.tortiepoint.pachyderm;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import java.io.File;
import java.io.FileNotFoundException;

public class Pachyderm {

    public static void main(String[] args) {
        try {
            File file = new File(args[0]);
            PachydermApp app = new PachydermApp(file);
            Server server = new Server(app.getConfig().getPort());
            Context root = new Context(server, "/", Context.SESSIONS);

            root.setResourceBase(app.getConfig().getPublicFolder());
            root.addServlet(new ServletHolder(app.getServlet()), "/*");

            server.start();
        } catch (FileNotFoundException fnfe) {
            System.out.println(String.format("Could not locate file ", args[0]));
        } catch (IndexOutOfBoundsException ioobe) {
            System.out.println("Usage: pachyderm {app}");
            System.out.println("Example: pachyderm /Users/rfast/test.js");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
