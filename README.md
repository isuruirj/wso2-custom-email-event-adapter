# wso2-custom-email-event-adapter

Custom Email Output Event Adapter to add users in the cc list based on the subject.

Note: This is implemented for WSO2IS-5.10.0. If you want to use it in some other WSO2 server, you can simply modify the pom.xml file’s project dependency versions matching the same version packed in the product

### Steps to deploy
- Build the component by running "mvn clean install"
- Copy following jar file which can be found in target directory into <IS_HOME>/repository/components/dropins/
    - org.wso2.custom.event.output.adapter.email-1.0.jar
- Configure the Output Adapter Event by adding following lines into deployment.toml file.
```
[output_adapter.customEmail] 
from_address= "xxxxxxxx" 
username= "xxxxxxxx" 
password= "xxxxxxxx" 
hostname= "smtp.gmail.com" 
port= 587 
enable_start_tls= true 
enable_authentication= true
```
- Open the <IS-HOME>/repository/deployment/server/eventpublishers/EmailPublisher.xml file and update it as below,
```
<?xml version="1.0" encoding="UTF-8"?>
<eventPublisher name="EmailPublisher" statistics="disable"
  trace="disable" xmlns="http://wso2.org/carbon/eventpublisher">
  <from streamName="id_gov_notify_stream" version="1.0.0"/>
  <mapping customMapping="enable" type="text">
    <inline>{{body}}{{footer}}</inline>
  </mapping>
  <to eventAdapterType="customEmail">
    <property name="email.address">{{send-to}}</property>
    <property name="email.type">{{content-type}}</property>
    <property name="email.subject">{{subject}}</property>
  </to>
</eventPublisher>

```
- Restart WSO2 IS.
