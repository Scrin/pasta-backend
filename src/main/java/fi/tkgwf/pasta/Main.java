package fi.tkgwf.pasta;

import fi.tkgwf.pasta.handler.Handler;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;

import fi.tkgwf.pasta.config.Config;

public class Main {

    private static final Logger LOG = LogManager.getLogger();
    private static final int DEFAULT_HTTP_PORT = 8080;

    public static void main(String[] args) throws Exception {
        System.setProperty("user.timezone", "UTC");
        TimeZone.setDefault(null);
        LOG.info("Initializing...");
        Main main = new Main();
        LOG.info("Starting Jetty...");
        main.startJetty();
    }

    private void startJetty() throws Exception {
        Server server = new Server(Config.getInt("http_port", DEFAULT_HTTP_PORT));
        server.setHandler(new Handler());
        server.start();
        server.join();
    }
}
