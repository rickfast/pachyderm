package org.tortiepoint.pachyderm;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: rickfast
 * Date: 9/6/12
 * Time: 8:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class PachydermItem implements Serializable {

    private int value;

    public PachydermItem() {
    }

    public PachydermItem(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
