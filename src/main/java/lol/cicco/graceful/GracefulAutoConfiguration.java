package lol.cicco.graceful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GracefulProperties.class)
public class GracefulAutoConfiguration {

    @Autowired
    private GracefulProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public GracefulHttpFilter gracefulHttpFiler() {
        GracefulHttpFilter filter = new GracefulHttpFilter();
        filter.setTimeout(properties.getTimeout().getGraceful());
        return filter;
    }

    @Bean
    @ConditionalOnMissingBean
    public GracefulShutdownTask gracefulShutdownHook() {
        GracefulShutdownTask task = new GracefulShutdownTask();
        task.setTimeout(properties.getTimeout().getContainer());
        return task;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnEnabledEndpoint
    public GracefulShutdownEndpoint graceShutdownEndPoint() {
        return new GracefulShutdownEndpoint();
    }

}
