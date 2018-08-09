package io.github.rxcats.vertx.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;

public class Launcher {

    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {

        Vertx.vertx().deployVerticle(MongoExampleVerticle.class.getName(), result -> {
            if (result.succeeded()) {
                log.info("success: {}", result.result());
            } else {
                log.error("failure: {}", result.cause());
            }
        });

    }
}
