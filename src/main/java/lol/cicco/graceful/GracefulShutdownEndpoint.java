package lol.cicco.graceful;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

@Endpoint(id = "graceful-shutdown")
public class GracefulShutdownEndpoint implements ApplicationContextAware {
    private static final Map<String, String> NO_CONTEXT_MESSAGE = Collections.unmodifiableMap(Collections.singletonMap("message", "No context to shutdown."));
    private static final Map<String, String> SHUTDOWN_MESSAGE = Collections.unmodifiableMap(Collections.singletonMap("message", "Shutting down, bye..."));

    private ConfigurableApplicationContext applicationContext;
    private GracefulShutdownTask task;

    @WriteOperation
    public Map<String, String> shutdown() {
        if (applicationContext == null) {
            return NO_CONTEXT_MESSAGE;
        }
        try {
            return SHUTDOWN_MESSAGE;
        } finally {
            Thread thread = new Thread(() -> task.shutdown());
            thread.setContextClassLoader(getClass().getClassLoader());
            thread.start();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if (context instanceof ConfigurableApplicationContext) {
            this.applicationContext = (ConfigurableApplicationContext) context;

            task = applicationContext.getBean(GracefulShutdownTask.class);
            task.init(applicationContext);
        }
    }

}
