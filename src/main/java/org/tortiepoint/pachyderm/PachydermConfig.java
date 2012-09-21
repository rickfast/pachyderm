package org.tortiepoint.pachyderm;

public class PachydermConfig {

    private int port = Integer.valueOf(System.getProperty("port", "8080"));
    private String viewFolder, publicFolder;

    public PachydermConfig(String viewFolder, String publicFolder) {
        this.viewFolder = viewFolder;
        this.publicFolder = publicFolder;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getViewFolder() {
        return viewFolder;
    }

    public void setViewFolder(String viewFolder) {
        this.viewFolder = viewFolder;
    }

    public String getPublicFolder() {
        return publicFolder;
    }

    public void setPublicFolder(String publicFolder) {
        this.publicFolder = publicFolder;
    }
}
