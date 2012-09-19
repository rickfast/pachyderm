package org.tortiepoint.pachyderm.handler;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Function;
import org.springframework.util.AntPathMatcher;

import java.util.HashMap;
import java.util.Map;

public class RequestHandlerManager {
    private Map<String, Map<String, Object>> handlers = new HashMap<String, Map<String, Object>>();

    private static final Logger log = Logger.getLogger(RequestHandlerManager.class);

    {
        for (String verb : new String[]{"get", "post", "delete", "put"}) {
            handlers.put(verb, new HashMap<String, Object>());
        }
    }

    public void mapHandler(String verb, String pattern, Object function) {
        log.info(String.format("Mapped (%s) %s", verb, pattern));
        handlers.get(verb).put(pattern, function);
    }

    public Function matchHandler(String verb, String uri, Map<String, String> params) {
        Function function = null;
        AntPathMatcher pathMatcher = new AntPathMatcher();

        for (String pattern : handlers.get(verb).keySet()) {
            if (pathMatcher.match(pattern, uri)) {
                params.putAll(pathMatcher.extractUriTemplateVariables(pattern, uri));
                function = (Function) handlers.get(verb).get(pattern);

                break;
            }
        }

        return function;
    }
}
