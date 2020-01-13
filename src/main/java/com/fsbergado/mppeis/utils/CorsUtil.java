package com.fsbergado.mppeis.utils;

import java.util.HashSet;
import java.util.Set;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.handler.CorsHandler;

/**
 * CorsUtil
 */
public class CorsUtil {

    public static CorsHandler handler() {
        final Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("x-requested-with");
        allowedHeaders.add("Access-Control-Allow-Origin");
        allowedHeaders.add("origin");
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("accept");

        final Set<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.GET);
        allowedMethods.add(HttpMethod.POST);
        allowedMethods.add(HttpMethod.OPTIONS);
        allowedMethods.add(HttpMethod.DELETE);
        allowedMethods.add(HttpMethod.PATCH);
        allowedMethods.add(HttpMethod.PUT);

        return CorsHandler.create("*").allowedHeaders(allowedHeaders)
                .allowedMethods(allowedMethods);        
    }
}