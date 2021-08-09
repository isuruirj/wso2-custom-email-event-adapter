package org.wso2.custom.event.output.adapter.email;

import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.email.EmailEventAdapterFactory;

import java.util.Map;

/**
 * The email event adapter factory class to create an email output adapter
 */
public class CustomEmailAdapterFactory extends EmailEventAdapterFactory {

    private static final String CUSTOM_ADAPTER_TYPE_EMAIL = "customEmail";

    @Override
    public String getType() {

        return CUSTOM_ADAPTER_TYPE_EMAIL;
    }

    @Override
    public OutputEventAdapter createEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration, Map<String,
            String> globalProperties) {

        return new CustomEmailEventAdapter(eventAdapterConfiguration, globalProperties);

    }
}
