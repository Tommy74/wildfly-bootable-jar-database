package org.wildfly.plugins.demo.jaxrs;


import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import java.sql.Connection;
import java.sql.SQLException;

@ApplicationScoped
@Path("/api")
public class DatabaseEndpoint {

    @Resource(name = "java:jboss/datasources/PostgreSQLDS")
    DataSource dataSource;

    @GET
    @Path("datasource")
    @Produces("text/plain")
    public Response doGetDatasource() throws SQLException {
        String schema = null;
        try (Connection conn = dataSource.getConnection()) {
            schema = conn.getSchema();
        }
        return Response.ok("Hello from WildFly bootable jar - current database schema '" + schema + "'!").build();
    }

    @GET
    @Path("ping")
    @Produces("text/plain")
    public Response doGet() {
        return Response.ok("Hello from WildFly bootable jar!").build();
    }
}
