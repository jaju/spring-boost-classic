package org.msync.spring_boost;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import org.springframework.stereotype.Component;

@Component
public class Utils {

    public static final IFn require;
    public static final IFn nameFn;
    public static final IFn assocFn;
    public static final IFn dissocFn;
    public static final IFn stringifyKeysFn;
    public static final IFn toRingSpecFn;
    public static final IFn httpHandlerFn;
    public static final IFn websocketHandlerFn;
    public static final IFn setHandlerFn;
    public static final IFn setWebSocketHandlerFn;

    static {
        require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("org.msync.spring-boost"));
        require.invoke(Clojure.read("org.msync.spring-boost.ring-like"));
        require.invoke(Clojure.read("clojure.walk"));

        nameFn = Clojure.var("clojure.core", "name");
        assocFn = Clojure.var("clojure.core", "assoc");
        dissocFn = Clojure.var("clojure.core", "dissoc");
        stringifyKeysFn = Clojure.var("clojure.walk", "stringify-keys");
        toRingSpecFn = Clojure.var("org.msync.spring-boost.ring-like", "to-ring-spec");
        httpHandlerFn = Clojure.var("org.msync.spring-boost", "-http-handler");
        websocketHandlerFn = Clojure.var("org.msync.spring-boost", "-websocket-handler");
        setHandlerFn = Clojure.var("org.msync.spring-boost", "set-handler!");
        setWebSocketHandlerFn = Clojure.var("org.msync.spring-boost", "set-websocket-handler!");
    }

    public static Keyword keyword(String s) {
        return Keyword.intern(s);
    }

    public static String name(Object k) {
        return (String) nameFn.invoke(k);
    }

}
