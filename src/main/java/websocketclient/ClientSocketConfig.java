package websocketclient;

import config.ConfigProperties;

import javax.websocket.ClientEndpointConfig;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ClientSocketConfig extends ClientEndpointConfig.Configurator {
    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        headers.put("Authorization", Collections.singletonList(ConfigProperties.getProperty("authorization")));
        headers.put("Accept-Language", Collections.singletonList(ConfigProperties.getProperty("accept-language")));
        super.beforeRequest(headers);
    }
}
