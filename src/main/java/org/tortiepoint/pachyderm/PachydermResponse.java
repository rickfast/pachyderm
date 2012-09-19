package org.tortiepoint.pachyderm;

import org.mozilla.javascript.NativeObject;

public class PachydermResponse {
    private NativeObject data;

    public void render(NativeObject data) {
        this.data = data;
    }

    public NativeObject getData() {
        return data;
    }
}
