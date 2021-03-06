package com.github.kahlkn.artoria.jdbc;

import com.github.kahlkn.artoria.aop.Enhancer;
import com.github.kahlkn.artoria.aop.Interceptor;
import com.github.kahlkn.artoria.exception.UncheckedException;
import com.github.kahlkn.artoria.util.Assert;
import com.github.kahlkn.artoria.util.PropUtils;
import com.github.kahlkn.artoria.util.StringUtils;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

/**
 * Simple data source.
 * @author Kahle
 */
public class SimpleDataSource implements DataSource {
    private static final String CLASS_NAME = SimpleDataSource.class.getName();
    private static final String UNSUPPORTED_OPERATION = "In " + CLASS_NAME + " this operation is unsupported. ";
    private static final String DEFAULT_CONFIG_NAME = "jdbc.properties";
    private BlockingQueue<Connection> queue;
    private String driverClass = "com.mysql.jdbc.Driver";
    private String jdbcUrl;
    private String user;
    private String password;
    private int maxPoolSize = 8;
    private int minPoolSize = 2;

    public SimpleDataSource() {
        this(PropUtils.create(DEFAULT_CONFIG_NAME).getProperties());
    }

    public SimpleDataSource(Properties prop) {
        this(
                prop.getProperty("driverClass"),
                prop.getProperty("jdbcUrl"),
                prop.getProperty("user"),
                prop.getProperty("password")
        );
    }

    public SimpleDataSource(String driverClass, String jdbcUrl, String user, String password) {
        this(driverClass, jdbcUrl, user, password, -1, -1);
    }

    public SimpleDataSource(String driverClass, String jdbcUrl, String user, String password, int maxPoolSize, int minPoolSize) {
        if (StringUtils.isNotBlank(driverClass)) { this.driverClass = driverClass; }
        Assert.notBlank(jdbcUrl, "Parameter \"jdbcUrl\" must not blank. ");
        this.jdbcUrl = jdbcUrl;
        Assert.notBlank(user, "Parameter \"user\" must not blank. ");
        this.user = user;
        Assert.notBlank(password, "Parameter \"password\" must not blank. ");
        this.password = password;
        if (maxPoolSize > 0) { this.maxPoolSize = maxPoolSize; }
        Assert.state(minPoolSize <= this.maxPoolSize
                , "Parameter \"minPoolSize\" must less than or equal to \"maxPoolSize\" " + this.maxPoolSize + ". ");
        if (minPoolSize > 0) { this.minPoolSize = minPoolSize; }
        this.queue = new ArrayBlockingQueue<Connection>(this.maxPoolSize);
        try {
            Class.forName(this.driverClass);
            for (int i = 0; i < this.minPoolSize; i++) {
                queue.offer(this.createConnection());
            }
        }
        catch (Exception e) {
            throw new UncheckedException(e);
        }
    }

    public Connection createConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
        ConnectionInterceptor intp = new ConnectionInterceptor(queue, conn);
        return (Connection) Enhancer.enhance(Connection.class, intp);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection conn = queue.poll();
        if (conn == null) {
            conn = this.createConnection();
        }
        return conn;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    private static class ConnectionInterceptor implements Interceptor {
        private static final String PROXY_METHOD = "close";
        private BlockingQueue<Connection> queue;
        private Connection connection;

        public ConnectionInterceptor(BlockingQueue<Connection> queue, Connection connection) {
            this.queue = queue;
            this.connection = connection;
        }

        @Override
        public Object intercept(Object proxyObject, Method method, Object[] args) throws Throwable {
            boolean offer = false;
            if (PROXY_METHOD.equals(method.getName())) {
                offer = queue.offer(connection);
            }
            if (!offer) {
                return method.invoke(connection, args);
            }
            return null;
        }

    }

}
