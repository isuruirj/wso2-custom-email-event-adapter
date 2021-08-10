package org.wso2.custom.event.output.adapter.email;

import org.apache.axis2.transport.mail.MailConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The Email event adapter sends mail using an SMTP server configuration defined
 * in output-event-adapters.xml email adapter sender definition.
 */

public class CustomEmailEventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(CustomEmailEventAdapter.class);
    private static ThreadPoolExecutor threadPoolExecutor;
    private Session session;
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;
    private int tenantId;

    /**
     * Default from address for outgoing messages.
     */
    private InternetAddress smtpFromAddress = null;

    /**
     * Optional replyTO address for outgoing messages.
     */
    private InternetAddress[] smtpReplyToAddress = null;

    /**
     * Optional Signature of sender address for outgoing messages.
     */
    private String signature = null;

    public CustomEmailEventAdapter(
            OutputEventAdapterConfiguration eventAdapterConfiguration,
            Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    /**
     * Initialize the thread pool to send emails.
     *
     * @throws OutputEventAdapterException on error.
     */

    @Override
    public void init() throws OutputEventAdapterException {

        tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        //ThreadPoolExecutor will be assigned  if it is null.
        if (threadPoolExecutor == null) {
            int minThread;
            int maxThread;
            long defaultKeepAliveTime;
            int jobQueSize;

            //If global properties are available those will be assigned else constant values will be assigned
            if (globalProperties.get(CustomEmailEventAdapterConstants.MIN_THREAD_NAME) != null) {
                minThread = Integer.parseInt(globalProperties.get(CustomEmailEventAdapterConstants.MIN_THREAD_NAME));
            } else {
                minThread = CustomEmailEventAdapterConstants.MIN_THREAD;
            }

            if (globalProperties.get(CustomEmailEventAdapterConstants.MAX_THREAD_NAME) != null) {
                maxThread = Integer.parseInt(globalProperties.get(CustomEmailEventAdapterConstants.MAX_THREAD_NAME));
            } else {
                maxThread = CustomEmailEventAdapterConstants.MAX_THREAD;
            }

            if (globalProperties.get(CustomEmailEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME) != null) {
                defaultKeepAliveTime = Integer.parseInt(globalProperties.get(
                        CustomEmailEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME));
            } else {
                defaultKeepAliveTime = CustomEmailEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_IN_MILLS;
            }

            if (globalProperties.get(CustomEmailEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME) != null) {
                jobQueSize = Integer.parseInt(globalProperties.get(
                        CustomEmailEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME));
            } else {
                jobQueSize = CustomEmailEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE;
            }

            threadPoolExecutor = new ThreadPoolExecutor(minThread, maxThread, defaultKeepAliveTime,
                    TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(jobQueSize));
        }

    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("Test connection is not available");
    }

    /**
     * Initialize the Email SMTP session and be ready to send emails.
     *
     * @throws ConnectionUnavailableException on error.
     */

    @Override
    public void connect() throws ConnectionUnavailableException {

        if (session == null) {

            /**
             * Default SMTP properties for outgoing messages.
             */
            String smtpFrom;
            String smtpHost;
            String smtpPort;

            /**
             *  Default from username and password for outgoing messages.
             */
            final String smtpUsername;
            final String smtpPassword;

            // initialize SMTP session.
            Properties props = new Properties();
            props.putAll(globalProperties);

            //Verifying default SMTP properties of the SMTP server.

            smtpFrom = props.getProperty(MailConstants.MAIL_SMTP_FROM);
            smtpHost = props.getProperty(CustomEmailEventAdapterConstants.MAIL_SMTP_HOST);
            smtpPort = props.getProperty(CustomEmailEventAdapterConstants.MAIL_SMTP_PORT);
            signature = props.getProperty(CustomEmailEventAdapterConstants.MAIL_SMTP_SIGNATURE);
            if (smtpFrom == null) {
                String msg = "failed to connect to the mail server due to null smtpFrom value";
                throw new ConnectionUnavailableException("The adapter " +
                        eventAdapterConfiguration.getName() + " " + msg);

            }

            if (smtpHost == null) {
                String msg = "failed to connect to the mail server due to null smtpHost value";
                throw new ConnectionUnavailableException
                        ("The adapter " + eventAdapterConfiguration.getName() + " " + msg);
            }

            if (smtpPort == null) {
                String msg = "failed to connect to the mail server due to null smtpPort value";
                throw new ConnectionUnavailableException
                        ("The adapter " + eventAdapterConfiguration.getName() + " " + msg);
            }

            String replyTo = props.getProperty(CustomEmailEventAdapterConstants.MAIL_SMTP_REPLY_TO);
            if (replyTo != null) {

                try {
                    smtpReplyToAddress = InternetAddress.parse(replyTo);
                } catch (AddressException e) {
                    log.error("Error in retrieving smtp replyTo address : " + smtpFrom, e);
                    String msg =
                            "failed to connect to the mail server due to error in retrieving " + "smtp replyTo address";
                    throw new ConnectionUnavailableException(
                            "The adapter " + eventAdapterConfiguration.getName() + " " + msg, e);
                }
            }

            try {
                smtpFromAddress = new InternetAddress(smtpFrom);
            } catch (AddressException e) {
                String msg = "failed to connect to the mail server due to error in retrieving " +
                        "smtp from address";
                throw new ConnectionUnavailableException
                        ("The adapter " + eventAdapterConfiguration.getName() + " " + msg, e);
            }

            //Retrieving username and password of SMTP server.
            smtpUsername = props.getProperty(MailConstants.MAIL_SMTP_USERNAME);
            smtpPassword = props.getProperty(MailConstants.MAIL_SMTP_PASSWORD);

            //initializing SMTP server to create session object.
            if (smtpUsername != null && smtpPassword != null && !smtpUsername.isEmpty() && !smtpPassword.isEmpty()) {
                session = Session.getInstance(props, new Authenticator() {
                    public PasswordAuthentication
                    getPasswordAuthentication() {

                        return new PasswordAuthentication(smtpUsername, smtpPassword);
                    }
                });
            } else {
                session = Session.getInstance(props);
                log.info("Connecting adapter " + eventAdapterConfiguration.getName() +
                        "without user authentication for tenant " + tenantId);
            }
        }
    }

    /**
     * This will be invoked upon a successful trigger of
     * a data stream.
     *
     * @param message           the event stream data.
     * @param dynamicProperties the dynamic attributes of the email.
     */

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {

        //Get subject and emailIds from dynamic properties
        String subject = dynamicProperties.get(CustomEmailEventAdapterConstants.ADAPTER_MESSAGE_EMAIL_SUBJECT);
        String[] emailIds = dynamicProperties.get(CustomEmailEventAdapterConstants.ADAPTER_MESSAGE_EMAIL_ADDRESS)
                .replaceAll(" ", "").split(CustomEmailEventAdapterConstants.EMAIL_SEPARATOR);
        String emailType = dynamicProperties.get(CustomEmailEventAdapterConstants.APAPTER_MESSAGE_EMAIL_TYPE);
        String ccEmail = null;

        if (CustomEmailEventAdapterConstants.APPLICABLE_SUBJECT.equalsIgnoreCase(subject)){
            //You need to implement the logic here. Implement the required logic to identify CC email address.

        }

        //Send email for each emailId
        for (String email : emailIds) {
            try {
                threadPoolExecutor.submit(new CustomEmailSender(email, subject, message.toString(), emailType, ccEmail));
            } catch (RejectedExecutionException e) {
                EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message, "Job queue is full", e, log,
                        tenantId);
            }
        }
    }

    @Override
    public void disconnect() {
        //not required
    }

    @Override
    public void destroy() {
        //not required
    }

    @Override
    public boolean isPolled() {
        return false;
    }

    class CustomEmailSender implements Runnable {

        String to;
        String subject;
        String body;
        String type;
        String ccEmail;

        CustomEmailSender(String to, String subject, String body, String type, String ccEmail) {

            this.to = to;
            this.subject = subject;
            this.body = body;
            this.type = type;
            this.ccEmail = ccEmail;
        }

        /**
         * Sending emails to the corresponding Email IDs'.
         */
        @Override
        public void run() {

            if (log.isDebugEnabled()) {
                log.debug("Format of the email:" + " " + to + "->" + type);
            }

            //Creating MIME object using initiated session.
            MimeMessage message = new MimeMessage(session);

            //Setting up the Email attributes and Email payload.
            try {
                if (signature != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Email Signature is configured as: " + signature);
                    }
                    message.setFrom(new InternetAddress(smtpFromAddress.getAddress(), signature));
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Email Signature is not configured.");
                    }
                    message.setFrom(smtpFromAddress);
                }
                if (smtpReplyToAddress != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Email reply to address is configured as: " + smtpReplyToAddress[0].getAddress());
                    }
                    message.setReplyTo(smtpReplyToAddress);
                }
                message.addRecipient(Message.RecipientType.TO,
                        new InternetAddress(to));

                if (!StringUtils.isBlank(ccEmail)){
                    message.addRecipient(Message.RecipientType.CC, new InternetAddress(ccEmail));
                }

                message.setSubject(subject);
                message.setSentDate(new Date());
                message.setContent(body, type);

                if (log.isDebugEnabled()) {
                    log.debug("Meta data of the email configured successfully");
                }

                Transport.send(message);

                if (log.isDebugEnabled()) {
                    log.debug("Mail sent to the EmailID" + " " + to + " " + "Successfully");
                }
            } catch (MessagingException e) {
                EventAdapterUtil
                        .logAndDrop(eventAdapterConfiguration.getName(), message, "Error in message format", e, log,
                                tenantId);
            } catch (Exception e) {
                EventAdapterUtil
                        .logAndDrop(eventAdapterConfiguration.getName(), message, "Error sending email to '" + to + "'",
                                e, log, tenantId);
            }
        }
    }

}
