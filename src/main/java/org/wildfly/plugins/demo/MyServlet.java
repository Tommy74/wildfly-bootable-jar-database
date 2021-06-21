package org.wildfly.plugins.demo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@WebServlet("/datasource")
public class MyServlet extends HttpServlet {

    @Resource(lookup = "java:jboss/datasources/PostgreSQLDS")
    DataSource dataSource;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String schema = null;
        try (Connection conn = dataSource.getConnection()) {
            schema = conn.getSchema();
        } catch (SQLException throwables) {
            throwables.printStackTrace(response.getWriter());
        }
        response.getWriter().println("schema="+schema);
    }
}
