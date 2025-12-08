FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml ./

RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
COPY frontend ./frontend
COPY openapitools.json ./openapitools.json
COPY README.md ./README.md

RUN mvn -q -DskipTests -Djava.version=21 package


FROM quay.io/wildfly/wildfly:35.0.1.Final-jdk21 AS runtime

ENV WF_HOME=/opt/jboss/wildfly \
    JBOSS_HOME=/opt/jboss/wildfly \
    DB_HOST=postgres \
    DB_PORT=5432 \
    DB_NAME=persondb \
    DB_USER=person \
    DB_PASSWORD=person

USER root

RUN mkdir -p ${WF_HOME}/modules/system/layers/base/org/postgresql/main

COPY --from=build target/person-service-1.0.0/WEB-INF/lib/postgresql-42.7.8.jar ${WF_HOME}/modules/system/layers/base/org/postgresql/main/postgresql-42.7.8.jar

COPY --from=build docker/wildfly/module.xml ${WF_HOME}/modules/system/layers/base/org/postgresql/main/module.xml

RUN chown -R jboss:jboss ${WF_HOME} /opt/jboss/add-postgres-datasource.cli

USER jboss

RUN ${WF_HOME}/bin/jboss-cli.sh --echo-command --timeout=120000 --connect \
    --commands=\
"module add --name=org.postgresql --resources=${WF_HOME}/modules/system/layers/base/org/postgresql/main/postgresql-42.7.8.jar --dependencies=jakarta.transaction.api,javax.api,java.naming,ibm.jakarta.jakartaee-api;" \
    "embed-server --server-config=standalone.xml --std-out=echo;" \
    "if (outcome == success) of /subsystem=datasources/jdbc-driver=postgresql:read-resource() then echo Driver exists else /subsystem=datasources/jdbc-driver=postgresql:add(driver-name=postgresql,driver-module-name=org.postgresql) end-if;" \
    "if (outcome == success) of /subsystem=datasources/data-source=PostgresDS:read-resource() then echo DS exists else /subsystem=datasources/data-source=PostgresDS:add(jndi-name=java:/jboss/datasources/PostgresDS, driver-name=postgresql, connection-url=jdbc:postgresql://${env.DB_HOST:${env.POSTGRES_HOST:postgres}}:${env.DB_PORT:5432}/${env.DB_NAME:persondb}, user-name=${env.DB_USER:person}, password=${env.DB_PASSWORD:person}, min-pool-size=5, max-pool-size=20, use-ccm=true, enabled=true) end-if;" \
    "stop-embedded-server;"

COPY --from=build /workspace/target/person-service-1.0.0.war ${WF_HOME}/standalone/deployments/app.war

EXPOSE 8080

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]


