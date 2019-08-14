package lol.cicco.graceful;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

public class GracefulShutdownTask {

    private static final Logger logger = LoggerFactory.getLogger(GracefulShutdownTask.class);
    private final List<WebServer> embeddedContainers = new ArrayList<>();
    private ConfigurableApplicationContext applicationContext;
    private int timeout;

    private static volatile boolean SHUT_DOWN_STATUS = false;

    @Autowired
    private GracefulHttpFilter filter;

    /**
     * Initializes the Application Context
     */
    public void init(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        logger.debug("ApplicationContext initialized");
    }

    public void shutdown() {
        if (SHUT_DOWN_STATUS) {
            return;
        }
        try {
            SHUT_DOWN_STATUS = true;
            logger.info("Graceful shutdown triggered");
            // Shutdown HTTP
            ExecutorService httpExecutor = Executors.newSingleThreadExecutor();
            Future<?> httpFuture = httpExecutor.submit(() -> {
                shutdownHttpFilter();
                shutdownHTTPConnector();
            });
            try {
                httpFuture.get(timeout, TimeUnit.MILLISECONDS);
                logger.info("HTTP shutdown finished");
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.error("HTTP graceful shutdown failed", e);
            }
            httpExecutor.shutdownNow();
        } finally {
            logger.info("Shutdown ApplicationContext");
            shutdownApplication();
        }
    }

    /**
     * Set returncode 503 for new requests. And wait for running requests to
     * complete.
     */
    private void shutdownHttpFilter() {
        try {
            logger.debug("Trigger HTTPFilter shutdown");
            filter.shutdown();
            logger.debug("Trigger HTTPFilter finished");
        } catch (InterruptedException e) {
            logger.error("shutdownHttpFilter failed", e);
        }
    }

    /**
     * Stops the HTTP Connector
     */
    private void shutdownHTTPConnector() {
        logger.debug("Shutting down embedded containers");
        for (WebServer embeddedServletContainer : embeddedContainers) {
            embeddedServletContainer.stop();
        }
        logger.debug("Shutting down embedded containers finishes");
    }

    /**
     * Close the SpringBoot Application Context
     */
    private void shutdownApplication() {
        applicationContext.close();
    }

    @EventListener
    public synchronized void onContainerInitialized(WebServerInitializedEvent event) {
        embeddedContainers.add(event.getWebServer());
        logger.debug("EmbeddedServletContainer registered");
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

}
