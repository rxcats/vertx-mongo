package io.github.rxcats.vertx.mongo;

import static io.vertx.ext.sync.Sync.awaitResult;
import static io.vertx.ext.sync.Sync.fiberHandler;

import java.util.List;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class MongoExampleVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    private static final String COLLECTION_NAME = "test";

    private MongoClient mongoClient;

    @Suspendable
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        JsonObject config = new JsonObject()
            .put("useObjectId", true)
            .put("connection_string", "mongodb://localhost:27017/test")
            .put("connectTimeoutMS", 3000)
            .put("socketTimeoutMS", 1000)
            .put("keepAlive", true);

        mongoClient = MongoClient.createShared(vertx, config);

        router.route().handler(BodyHandler.create());
        router.post("/test").handler(fiberHandler(this::insert));
        router.get("/test/:id").handler(fiberHandler(this::findOne));
        router.get("/test").handler(fiberHandler(this::findAll));
        server.requestHandler(router::accept).listen(8089);
    }

    @Suspendable
    private void insert(RoutingContext ctx) {

        JsonObject request = ctx.getBodyAsJson();
        log.info("request:{}", request);

        String id = awaitResult(handler -> mongoClient.insert(COLLECTION_NAME, request, handler));
        log.info("id:{}", id);

        response(ctx, id);

    }

    @Suspendable
    private void findOne(RoutingContext ctx) {

        String id = ctx.request().getParam("id");

        JsonObject query = new JsonObject()
            .put("_id", new ObjectId(id));

        JsonObject result = awaitResult(handler -> mongoClient.findOne(COLLECTION_NAME, query, new JsonObject(), handler));

        response(ctx, result);
    }

    @Suspendable
    private void findAll(RoutingContext ctx) {

        List<JsonObject> result = awaitResult(handler -> mongoClient.find(COLLECTION_NAME, new JsonObject(), handler));

        response(ctx, result);
    }

    private void response(RoutingContext ctx, Object message) {
        JsonObject res = new JsonObject()
            .put("result_code", 0)
            .put("result_message", message);

        ctx.response()
            .putHeader("content-type", "application/json")
            .end(res.encode());
    }

}
