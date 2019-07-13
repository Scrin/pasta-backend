package fi.tkgwf.pasta.handler;

import com.google.gson.Gson;
import fi.tkgwf.pasta.bean.ErrorResponse;
import fi.tkgwf.pasta.bean.Meta;
import fi.tkgwf.pasta.config.Config;
import fi.tkgwf.pasta.util.Generator;
import fi.tkgwf.pasta.util.Validator;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.lang3.StringUtils;

public class ApiHandler {

    private final StatefulRedisConnection<String, String> redisText;
    private final StatefulRedisConnection<String, String> redisMeta;

    public ApiHandler(StatefulRedisConnection<String, String> redisText, StatefulRedisConnection<String, String> redisMeta) {
        this.redisText = redisText;
        this.redisMeta = redisMeta;
    }

    public int handle(String target, HttpServletRequest request, HttpServletResponse response) throws Exception {
        int status = 404;
        String[] pathParts = target.split("/", -1);
        String base = pathParts[0];
        pathParts = Arrays.copyOfRange(pathParts, 1, pathParts.length);
        switch (base) {
            case "meta":
                status = handleMeta(pathParts, response);
                break;
            case "new":
                status = handleNew(pathParts, request, response);
                break;
        }
        return status;

    }

    private int handleMeta(String[] pathParts, HttpServletResponse response) throws IOException {
        if (pathParts.length >= 1) {
            Meta meta = new Gson().fromJson(redisMeta.sync().get(pathParts[0]), Meta.class);
            if (meta != null) {
                if (pathParts.length < 2 || !meta.secret.equals(pathParts[1])) {
                    meta.secret = null; // Hide the secret if wrong or no secret supplied
                }
                meta.expiry = redisMeta.sync().ttl(pathParts[0]);
                response.setContentType("application/json");
                response.getWriter().write(new Gson().toJson(meta));
                return 200;
            }
        }
        return 404;
    }

    private int handleNew(String[] pathParts, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String id = null;
        String secret = null;
        String expiry = null;
        String mime = null;
        if (pathParts.length == 5) {
            id = pathParts[0];
            secret = pathParts[1];
            expiry = pathParts[2];
            mime = pathParts[3] + '/' + pathParts[4];
        }
        response.setContentType("application/json");
        return add(id, secret, expiry, mime, request, response);
    }

    /**
     * Validate and add a new paste. In case the paste is invalid, 400 is
     * returned and the errors written to the response, otherwise 200 is
     * returned and the generated meta is written to the response
     *
     * @param id ID of the paste, optional
     * @param secret Secret of the paste, optional
     * @param expiryString Expiry time, optional
     * @param mime MIME type of the paste, optional
     * @param request Original request where to read the text from
     * @param response Response where to write the meta or errors to
     * @return 400 in case of errors, 200 otherwise
     * @throws IOException
     */
    private int add(String id, String secret, String expiryString, String mime, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String text = IOUtils.toString(new BoundedInputStream(request.getInputStream(), Config.getLong("max_size", 4194304) + 1), "utf-8");
        if (StringUtils.isEmpty(id)) {
            int length = 5;
            int retry = 0;
            do {
                id = Generator.generateID(length);
                if (retry++ > 1000) {
                    retry = 0;
                    length++;
                }
            } while (redisMeta.sync().exists(id) != 0);
        }
        if (StringUtils.isEmpty(secret)) {
            secret = Generator.generateID(10);
        }
        long expiry = 2592000;
        if (StringUtils.isNotEmpty(expiryString)) {
            try {
                expiry = Long.parseLong(expiryString);
            } catch (NumberFormatException ex) {
                sendErrors(Collections.singletonList("Invalid expiry time"), response);
                return 400;
            }
        }
        if (StringUtils.isEmpty(mime)) {
            mime = "text/plain";
        }
        Meta meta = new Meta();
        meta.id = id;
        meta.secret = secret;
        meta.expiry = expiry;
        meta.mime = mime;
        List<String> errors = Validator.validate(text, meta, new Gson().fromJson(redisMeta.sync().get(id), Meta.class));
        if (!errors.isEmpty()) {
            sendErrors(errors, response);
            return 400;
        }
        String metaJson = new Gson().toJson(meta);
        redisText.sync().set(id, text, new SetArgs().ex(expiry));
        redisMeta.sync().set(id, metaJson, new SetArgs().ex(expiry));
        response.setContentType("application/json");
        response.getWriter().write(metaJson);
        return 200;
    }

    /**
     * Writes the supplied errors as json to the response while setting the
     * appropriate Content-Type header
     *
     * @param errors List of errors to be written
     * @param response The response where to write the errors to
     * @throws IOException
     */
    private void sendErrors(List<String> errors, HttpServletResponse response) throws IOException {
        ErrorResponse error = new ErrorResponse();
        error.errors = errors;
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(error));
    }
}
