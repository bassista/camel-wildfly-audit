# camel-wildfly-audit

## Requirements

- [Apache Maven 3.x](http://maven.apache.org)
- [MySQL 5.7.18](https://www.mysql.com/oem/)
  - [Docker Image](https://hub.docker.com/r/mysql/mysql-server/)

## Preparing

Install and run MySQL [https://dev.mysql.com/doc/refman/5.7/en/installing.html]

_Note: For my tests, I chose to run the docker image [https://hub.docker.com/r/mysql/mysql-server/]. You can run it using the command `docker run --name mysql -e MYSQL_DATABASE=example -e MYSQL_ROOT_PASSWORD=Abcd1234 -e MYSQL_ROOT_HOST=172.17.0.1 -p 3306:3306 -d mysql/mysql-server:5.7`._

Build the project source code

```
cd $PROJECT_ROOT
mvn clean install
```

## Deploying to a standalone WidlFly server

```
cd $PROJECT_ROOT
mvn wildfly:deploy
```

## Running the example in OpenShift

It is assumed that:

- OpenShift platform is already running, if not you can find details how to [Install OpenShift at your site](https://docs.openshift.com/container-platform/3.9/install_config/index.html).
- Your system is configured for Fabric8 Maven Workflow, if not you can find a [Get Started Guide](https://access.redhat.com/documentation/en-us/red_hat_fuse/7.0/html/fuse_on_openshift_guide/)

Issue the following commands:

```
oc login
oc new-project fuse
oc create -f src/main/kube/serviceaccount.yml
oc create -f src/main/kube/configmap.yml
oc create -f src/main/kube/secret.yml
oc secrets add sa/camel-wildfly-audit-sa secret/camel-wildfly-audit-secret
oc policy add-role-to-user view system:serviceaccount:fuse:camel-wildfly-audit-sa
mvn -Popenshift fabric8:deploy
```

## Testing the code

There is a SoapUI project located in the `src/test/soapui` folder that can be used to send in requests. The project also contains a mock Notification Service that you can use. So either start the mock server, or configure the application's properties to point to the real [Notification Service](https://github.com/joshdreagan/camel-wildfly-notification).
