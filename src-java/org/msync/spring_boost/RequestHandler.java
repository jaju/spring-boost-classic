package org.msync.spring_boost;

import clojure.lang.Keyword;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.msync.spring_boost.Utils.*;

public class RequestHandler {

    private final String rootPath;
    private final Boost boost;
    private static final Logger logger = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(String rootPath, Boost boost) {
        this.rootPath = rootPath;
        this.boost = boost;
    }

    private String prunePath(String path) {
        return path.substring(rootPath.length());
    }

    static private final Map<MediaType, Class<?>> mediaTypeToClass = Map.of(
            MediaType.APPLICATION_JSON, Map.class,
            MediaType.APPLICATION_FORM_URLENCODED, MultiValueMap.class
    );

    private static Class<?> contentTypeToJavaType(MediaType mt) {
        Class<?> known = mediaTypeToClass.get(mt);

        if (Objects.isNull(known)) {
            known = String.class;
        }

        return known;
    }

    private static final Set<HttpMethod> httpMethodsWithBody = Set.of(
            HttpMethod.POST,
            HttpMethod.PUT,
            HttpMethod.PATCH
    );

    /**
     * Endpoint to request starting of the nrepl-server
     *
     * @param request - The request object
     * @return void
     */
    public ServerResponse startNreplHandler(ServerRequest request) {
        try {
            boost.startNrepl();
            return ServerResponse
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("status", "started"));
        } catch (Exception e) {
            return ServerResponse
                    .status(HttpStatus.CONFLICT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("status", "error"));
        }
    }

    /**
     * Endpoint to request stopping of the nrepl-server
     *
     * @param request - The request object
     * @return void
     */
    public ServerResponse stopNreplHandler(ServerRequest request) {
        try {
            boost.stopNrepl();
            return ServerResponse
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("status", "stopped"));
        } catch (Exception e) {
            return ServerResponse
                    .status(HttpStatus.CONFLICT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("status", "error"));
        }
    }

    private ServerResponse updateResponse(Map<Keyword, Object> clojureResponse) {

        Long status = (Long) clojureResponse.get(keyword("status"));
        var headers = (Map<Object, String>) clojureResponse.get(keyword("headers"));
        Object body = clojureResponse.get(keyword("body"));

        logger.log(Level.FINE, () -> "Response Status = " + status);
        logger.log(Level.FINE, () -> "Response Headers = " + headers);
        logger.log(Level.FINE, () -> "Response Body = " + body);
        return ServerResponse.status(status.intValue())
            .headers(h -> {
                for (var key : headers.keySet()) {
                    h.add(name(key), headers.get(key));
                }
            })
            .body(stringifyKeysFn.invoke(body));
    }

    /**
     * @param request - ServerRequest object as initialized by Spring
     * @return - The response
     */
    public ServerResponse httpRequestHandler(ServerRequest request) throws ServletException, IOException {
        String uri = prunePath(request.path());

        logger.log(Level.FINE, () -> "We have a request for uri = " + uri);

        final var clojureRequest = (Map<Keyword, Object>) toRingSpecFn.invoke(uri, request);

        if (httpMethodsWithBody.contains(request.method())) {
            return handleRequestWithBody(request, clojureRequest);
        }

        logger.log(Level.FINE, () -> "We have a clojure request: " + clojureRequest);
        Map<Keyword, Object> response = (Map<Keyword, Object>) httpHandlerFn.invoke(clojureRequest);
        logger.log(Level.FINE, () -> "We have a response: " + response);
        return updateResponse(response);
    }

    private ServerResponse handleRequestWithBody(ServerRequest request, Map<Keyword, Object> clojureRequest) throws ServletException, IOException {
        var headers = (Map<String, String>) clojureRequest.get(keyword("headers"));
        var contentType = (String) headers.get("content-type");
        var mediaType = MediaType.valueOf(contentType);

        var payload = request.body(contentTypeToJavaType(mediaType));

        var payloadType = "body";
        var kwPayloadType = keyword(payloadType);
        var updatedRequest = assocFn.invoke(clojureRequest, kwPayloadType, payload);
        Map<Keyword, Object> response = (Map<Keyword, Object>) httpHandlerFn.invoke(updatedRequest);
        return updateResponse(response);
    }

}
