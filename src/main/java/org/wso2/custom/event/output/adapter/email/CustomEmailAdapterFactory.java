package org.wso2.custom.event.output.adapter.email;

import org.wso2.carbon.event.output.adapter.core.MessageType;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.Property;
import org.wso2.carbon.event.output.adapter.email.EmailEventAdapterFactory;

import java.util.*;

/**
 * The email event adapter factory class to create an email output adapter
 */
public class CustomEmailAdapterFactory extends EmailEventAdapterFactory {

    private ResourceBundle resourceBundle =
            ResourceBundle.getBundle("org.wso2.custom.event.output.adapter.email.i18n.Resources", Locale.getDefault());

    @Override
    public String getType() {

        return CustomEmailEventAdapterConstants.CUSTOM_ADAPTER_TYPE_EMAIL;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportedMessageFormats = new ArrayList<String>();
        supportedMessageFormats.add(MessageType.TEXT);
        supportedMessageFormats.add(MessageType.XML);
        supportedMessageFormats.add(MessageType.JSON);
        return supportedMessageFormats;
    }

    @Override
    public List<Property> getStaticPropertyList() {

        List<Property> staticpropertyList = new ArrayList<>();

        Property smtplUserName = new Property(CustomEmailEventAdapterConstants.ADAPTER_EMAIL_SMTP_USER);
        smtplUserName.setDisplayName(resourceBundle.getString(CustomEmailEventAdapterConstants.ADAPTER_EMAIL_SMTP_USER));
        smtplUserName.setRequired(false);

        Property smtpPassword = new Property(CustomEmailEventAdapterConstants.ADAPTER_EMAIL_SMTP_PASSWORD);
        smtpPassword.setDisplayName(resourceBundle.getString(CustomEmailEventAdapterConstants.ADAPTER_EMAIL_SMTP_PASSWORD));
        smtpPassword.setRequired(false);

        Property smtpAuth = new Property(CustomEmailEventAdapterConstants.ADAPTER_EMAIL_SMTP_AUTH);
        smtpAuth.setDisplayName(resourceBundle.getString(CustomEmailEventAdapterConstants.ADAPTER_EMAIL_SMTP_AUTH));
        smtpAuth.setRequired(false);

        Property smtpFrom = new Property(CustomEmailEventAdapterConstants.ADAPTER_EMAIL_SMTP_FROM);
        smtpFrom.setDisplayName(resourceBundle.getString(CustomEmailEventAdapterConstants.ADAPTER_EMAIL_SMTP_FROM));
        smtpFrom.setRequired(false);

        Property smtpHost = new Property(CustomEmailEventAdapterConstants.ADAPTER_EMAIL_SMTP_HOST);
        smtpHost.setDisplayName(resourceBundle.getString(CustomEmailEventAdapterConstants.ADAPTER_EMAIL_SMTP_HOST));
        smtpHost.setRequired(false);

        Property smtpPort = new Property(CustomEmailEventAdapterConstants.ADAPTER_EMAIL_SMTP_PORT);
        smtpPort.setDisplayName(resourceBundle.getString(CustomEmailEventAdapterConstants.ADAPTER_EMAIL_SMTP_PORT));
        smtpPort.setRequired(false);

        Property startTLSEnable = new Property(CustomEmailEventAdapterConstants.ADAPTER_EMAIL_SMTP_STARTTLS_ENABLE);
        startTLSEnable.setDisplayName(
                resourceBundle.getString(CustomEmailEventAdapterConstants.ADAPTER_EMAIL_SMTP_STARTTLS_ENABLE));
        startTLSEnable.setRequired(false);

        Property senderSignature = new Property(CustomEmailEventAdapterConstants.MAIL_SMTP_SIGNATURE);
        startTLSEnable.setDisplayName(resourceBundle.getString(CustomEmailEventAdapterConstants.MAIL_SMTP_SIGNATURE));
        startTLSEnable.setRequired(false);

        Property replyToAddress = new Property(CustomEmailEventAdapterConstants.MAIL_SMTP_REPLY_TO);
        startTLSEnable.setDisplayName(resourceBundle.getString(CustomEmailEventAdapterConstants.MAIL_SMTP_REPLY_TO));
        startTLSEnable.setRequired(false);

        staticpropertyList.add(smtplUserName);
        staticpropertyList.add(smtpPassword);
        staticpropertyList.add(smtpAuth);
        staticpropertyList.add(smtpFrom);
        staticpropertyList.add(smtpHost);
        staticpropertyList.add(smtpPort);
        staticpropertyList.add(startTLSEnable);
        staticpropertyList.add(senderSignature);
        staticpropertyList.add(replyToAddress);

        return staticpropertyList;
    }

    @Override
    public List<Property> getDynamicPropertyList() {
        List<Property> dynamicPropertyList = new ArrayList<Property>();

        // set email address
        Property emailAddress = new Property(CustomEmailEventAdapterConstants.ADAPTER_MESSAGE_EMAIL_ADDRESS);
        emailAddress.setDisplayName(
                resourceBundle.getString(CustomEmailEventAdapterConstants.ADAPTER_MESSAGE_EMAIL_ADDRESS));
        emailAddress.setRequired(true);
        emailAddress.setHint(resourceBundle.getString(CustomEmailEventAdapterConstants.ADAPTER_MESSAGE_EMAIL_ADDRESS_HINT));

        // set email subject
        Property subject = new Property(CustomEmailEventAdapterConstants.ADAPTER_MESSAGE_EMAIL_SUBJECT);
        subject.setDisplayName(
                resourceBundle.getString(CustomEmailEventAdapterConstants.ADAPTER_MESSAGE_EMAIL_SUBJECT));
        subject.setRequired(true);


        //set format of the email
        Property format = new Property(CustomEmailEventAdapterConstants.APAPTER_MESSAGE_EMAIL_TYPE);
        format.setDisplayName
                (resourceBundle.getString(CustomEmailEventAdapterConstants.APAPTER_MESSAGE_EMAIL_TYPE));
        format.setRequired(false);
        format.setOptions(new String[]{CustomEmailEventAdapterConstants.MAIL_TEXT_PLAIN, CustomEmailEventAdapterConstants.MAIL_TEXT_HTML});
        format.setDefaultValue(CustomEmailEventAdapterConstants.MAIL_TEXT_PLAIN);
        format.setHint(resourceBundle.getString(CustomEmailEventAdapterConstants.ADAPTER_MESSAGE_EMAIL_TYPE_HINT));

        Property ccAddress = new Property(CustomEmailEventAdapterConstants.ADAPTER_MESSAGE_CC_EMAIL);
        ccAddress.setDisplayName(resourceBundle.getString(CustomEmailEventAdapterConstants.ADAPTER_MESSAGE_CC_EMAIL));
        ccAddress.setRequired(false);

        dynamicPropertyList.add(emailAddress);
        dynamicPropertyList.add(subject);
        dynamicPropertyList.add(format);
        dynamicPropertyList.add(ccAddress);

        return dynamicPropertyList;
    }

    @Override
    public String getUsageTips() {
        return null;
    }

    @Override
    public OutputEventAdapter createEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration, Map<String,
            String> globalProperties) {

        return new CustomEmailEventAdapter(eventAdapterConfiguration, globalProperties);

    }
}
