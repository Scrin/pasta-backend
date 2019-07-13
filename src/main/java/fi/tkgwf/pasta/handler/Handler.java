package fi.tkgwf.pasta.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import fi.tkgwf.pasta.config.Config;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;

public class Handler extends AbstractHandler {

    private static final Logger LOG = LogManager.getLogger();

    private static final String RAW_BASE_URL = "/raw/";
    private static final String API_BASE_URL = "/api/";

    private final StatefulRedisConnection<String, String> redisText;
    private final StatefulRedisConnection<String, String> redisMeta;

    private final RawHandler rawHandler;
    private final ApiHandler apiHandler;

    public Handler() {
        redisText = RedisClient.create("redis://" + Config.get("redis_host") + "/0").connect();
        redisMeta = RedisClient.create("redis://" + Config.get("redis_host") + "/1").connect();
        rawHandler = new RawHandler(redisText);
        apiHandler = new ApiHandler(redisText, redisMeta);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        int status = 404;
        try {
            if (target.startsWith(RAW_BASE_URL)) {
                status = rawHandler.handle(target.substring(RAW_BASE_URL.length()), response);
            } else if (target.startsWith(API_BASE_URL)) {
                status = apiHandler.handle(target.substring(API_BASE_URL.length()), request, response);
            }
        } catch (Exception ex) {
            LOG.error("Uncaught exception while handling request to " + target, ex);
            status = 500;
        }
        response.setStatus(status);
        baseRequest.setHandled(true);
    }
}
