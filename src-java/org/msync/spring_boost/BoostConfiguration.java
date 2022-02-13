package org.msync.spring_boost;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Objects;

import static org.springframework.web.servlet.function.RequestPredicates.POST;

@Configuration
@ConfigurationProperties(prefix = "clojure-component")
public class BoostConfiguration {
    private int nreplPort;
    private String rootPath;
    private String wsPath;
    private boolean nreplStart = false;
    private String initSymbol;
    @Autowired
    private ApplicationContext applicationContext;

    int getNreplPort() {
        return nreplPort;
    }

    void setNreplPort(int nreplPort) {
        this.nreplPort = nreplPort;
    }

    String getRootPath() {
        return rootPath;
    }

    void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getWsPath() {
        return wsPath;
    }

    public void setWsPath(String wsPath) {
        this.wsPath = wsPath;
    }

    boolean isNreplStart() {
        return nreplStart;
    }

    void setNreplStart(boolean nreplStart) {
        this.nreplStart = nreplStart;
    }

    public String getInitSymbol() {
        return initSymbol;
    }

    public void setInitSymbol(String initSymbol) {
        this.initSymbol = initSymbol;
    }

    @Bean
    RequestHandler requestHandler(Boost boost) {
        return new RequestHandler(this.rootPath, boost);
    }

    @Bean
    Boost createBoost() {
        return new Boost(this.applicationContext, this.nreplPort, this.nreplStart, this.initSymbol);
    }

    private String expandedPath(String path) {
        Objects.requireNonNull(path);
        return rootPath + path;
    }

    @Bean
    public RouterFunction<ServerResponse> route(RequestHandler requestHandler, Boost boost) {
        return RouterFunctions
            .route(POST(expandedPath("/stop-nrepl")), requestHandler::stopNreplHandler)
            .andRoute(POST(expandedPath("/start-nrepl")), requestHandler::startNreplHandler)
            .andRoute(RequestPredicates.path(expandedPath("/**")), requestHandler::httpRequestHandler);
    }
}
