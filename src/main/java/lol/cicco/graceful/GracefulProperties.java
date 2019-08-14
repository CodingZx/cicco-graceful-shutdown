package lol.cicco.graceful;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cicco.graceful")
public class GracefulProperties {

    private Timeout timeout = new Timeout();


    public static class Timeout {

        /**
         * Timeout (in ms) to wait for running Requests to complete
         */
        private Integer request = 10000;

        /**
         * Timeout (in ms) to wait until all containers are shutdown. Bust be
         * greater than http timeout
         */
        private Integer container = 15000;

        Integer getRequest() {
            return request;
        }

        public void setRequest(Integer request) {
            this.request = request;
        }

        Integer getContainer() {
            return container;
        }

        public void setContainer(Integer container) {
            this.container = container;
        }

    }

    Timeout getTimeout() {
        return timeout;
    }

    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

}
