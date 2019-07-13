package fi.tkgwf.pasta.handler;

import io.lettuce.core.api.StatefulRedisConnection;
import javax.servlet.http.HttpServletResponse;

public class RawHandler {

    private final StatefulRedisConnection<String, String> redisText;

    public RawHandler(StatefulRedisConnection<String, String> redisText) {
        this.redisText = redisText;
    }

    public int handle(String target, HttpServletResponse response) throws Exception {
        String data = redisText.sync().get(target);
        if (data != null) {
            response.setContentType("text/plain;charset=utf-8");
            response.getWriter().write(data);
            return 200;
        }
        return 404;
    }
}
