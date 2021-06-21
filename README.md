# WildFly Bootable Jar connecting to PostgreSQL Database

This repo contains a working example of a WildFly Bootable Jar that connects to an PostgreSQL Database;

WildFly Bootable Jar is an alternative to Spring Boot which uses WildFly as the embedded server;

WildFly Bootable Jar allows you to trim the server and keep just the pieces you are interested in: as we know WildFly 
comes with a ton of features like clustering, messaging, EJBs, JBatch, Microprofile etc... and with Bootable jar you can
keep just the pieces you need (the "pieces" are called layers - you can see the list of all available layers here
https://docs.wildfly.org/23/Bootable_Guide.html#wildfly_layers);

## PostgreSQL DB

First you need an PostgreSQL database: I used the containerized version of PostgreSQL version 13
[bitnami/postgresql](https://quay.io/repository/bitnami/postgresql?tag=latest&tab=tags);

You can start it using Podman:

```shell
podman run -d --rm --network=host \
  --name postgresql \
  -e POSTGRES_PASSWORD=mysecretpassword \
  -e POSTGRES_USER=postgres \
  quay.io/bitnami/postgresql:13
```

or using Docker:

```shell
docker run -d --rm --network=host \
  --name postgresql \
  -e POSTGRES_PASSWORD=mysecretpassword \
  -e POSTGRES_USER=postgres \
  quay.io/bitnami/postgresql:13
```

This way you have a running PostgreSQL DB on your laptop; you can connect to it using the following info:

```shell
URL: jdbc:postgresql://localhost:5432/postgres
USERNAME: postgres
PASSWORD: mysecretpassword
```

## PostgreSQL Layers

WildFly Bootable Jar needs a couple of layers that provide:
- the PostgreSQL JDBC driver 
- the WildFly Database connection

You can obtain these layers by cloning and building the following repository:

```shell
git clone https://github.com/wildfly-extras/wildfly-datasources-galleon-pack.git
cd wildfly-datasources-galleon-pack
mvn install -DskipTests -Denforcer.skip=true
```

The repository actually provides layers for all most common databases (MariaDB, Microsoft SQL Server, MySQL, Oracle, PostgreSQL);


## WildFly Bootable Jar

After you have a working PostgreSQL Database, and the layers to connect to it, you can create the WildFly Bootable Jar; 

You can just clone this repository and build it:

```shell
git clone https://github.com/Tommy74/wildfly-bootable-jar-database.git
cd wildfly-bootable-jar-database
mvn package -DskipTests -Denforcer.skip=true
```

Now we can set the environment variables that tell the Bootable Jar how-to connect to the PostgreSQL Database and start the
Bootable Jar (see [doc/postgresql/README.md](https://github.com/wildfly-extras/wildfly-datasources-galleon-pack/blob/master/doc/postgresql/README.md)):

```shell
export POSTGRESQL_USER=postgres
export POSTGRESQL_PASSWORD=mysecretpassword
export POSTGRESQL_SERVICE_HOST=localhost
export POSTGRESQL_SERVICE_PORT=5432
export POSTGRESQL_DATABASE=postgres
export POSTGRESQL_DATASOURCE=PostgreSQLDS
java -jar target/wildfly-bootable-jar-database-bootable.jar
```

Invoke the following URL and see that the response tells you the PostgreSQL schema you are connected to:

```shell
curl http://localhost:8080/api/datasource
Hello from WildFly bootable jar - PostgreSQL schema SYSTEM!
```

if you prefer using a simple servlet rather that a JAX-RS endpoint:

```shell
curl http://localhost:8080/datasource
schema=SYSTEM
```

## WildFly Bootable Jar Datasource

This paragraph gives a little explanation about what happens under the hood;

If you look into the `pom.xml` file you can see the two layers that allows us to connect to PostgreSQL:

```xml
<layer>postgresql-driver</layer>
<layer>postgresql-datasource</layer>
```

The `postgresql-driver` provides the PostgreSQL JDBC driver to the WildFly Bootable Jar;

The `postgresql-datasource` provides a parametric connection to the PostgreSQL Database to the WildFly Bootable Jar; if you look
inside the `/standalone/configuration/standalone.xml` file inside the file `wildfly.zip` which is inside 
`target/wildfly-bootable-jar-database-bootable.jar`, you find the following:

```xml
        <subsystem xmlns="urn:jboss:domain:datasources:6.0">
            <datasources>
                <datasource jndi-name="java:jboss/datasources/${env.POSTGRESQL_DATASOURCE,env.OPENSHIFT_POSTGRESQL_DATASOURCE:PostgreSQLDS}" pool-name="PostgreSQLDS" enabled="true" use-java-context="true" use-ccm="true" statistics-enabled="${wildfly.datasources.statistics-enabled:${wildfly.statistics-enabled:false}}">
                    <connection-url>jdbc:postgresql://${env.POSTGRESQL_SERVICE_HOST, env.OPENSHIFT_POSTGRESQL_DB_HOST}:${env.POSTGRESQL_SERVICE_PORT, env.OPENSHIFT_POSTGRESQL_DB_PORT}/${env.POSTGRESQL_DATABASE, env.OPENSHIFT_POSTGRESQL_DB_NAME}</connection-url>
                    <driver>postgresql</driver>
                    <pool>
                        <flush-strategy>IdleConnections</flush-strategy>
                    </pool>
                    <security>
                        <user-name>${env.POSTGRESQL_USER, env.OPENSHIFT_POSTGRESQL_DB_USERNAME}</user-name>
                        <password>${env.POSTGRESQL_PASSWORD, env.OPENSHIFT_POSTGRESQL_DB_PASSWORD}</password>
                    </security>
                    <validation>
                        <check-valid-connection-sql>SELECT 1</check-valid-connection-sql>
                        <background-validation>true</background-validation>
                        <background-validation-millis>60000</background-validation-millis>
                    </validation>
                </datasource>
                <drivers>
                    <driver name="postgresql" module="org.postgresql.jdbc">
                        <xa-datasource-class>org.postgresql.xa.PGXADataSource</xa-datasource-class>
                    </driver>
                </drivers>
            </datasources>
        </subsystem>
```

this is exactly a connection to the PostgreSQL Database which picks the database url, username, password and JNDI name from 
environment variables that you can set before starting the bootable jar;

This is very useful because you can use the same `wildfly-bootable-jar-database-bootable.jar` and deploy it to your test,
production, cloud etc... environment without recompiling it;



