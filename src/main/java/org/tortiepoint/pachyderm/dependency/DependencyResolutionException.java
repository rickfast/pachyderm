package org.tortiepoint.pachyderm.dependency;

/**
 * Created with IntelliJ IDEA.
 * User: rickfast
 * Date: 9/10/12
 * Time: 5:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class DependencyResolutionException extends Exception {

    public DependencyResolutionException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
