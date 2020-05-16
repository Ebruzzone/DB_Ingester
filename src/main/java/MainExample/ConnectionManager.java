package MainExample;

import org.apache.commons.dbcp.BasicDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionManager {
	private BasicDataSource basicDataSource;
	private Map<String, Connection> connections;
	private Map<String, AtomicInteger> times;
	private int reset;

	public ConnectionManager(String fileName, int reset) throws ClassNotFoundException {
		this.basicDataSource = new BasicDataSource();

		try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {

			if (input == null) {
				throw new IllegalArgumentException("fileName must be the name of a file in resources.");
			}

			Properties prop = new Properties();
			prop.load(input);

			Class.forName(prop.getProperty("DriverClassName"));

			basicDataSource.setDriverClassName(prop.getProperty("DriverClassName"));
			basicDataSource.setUrl(prop.getProperty("Url"));
			basicDataSource.setUsername(prop.getProperty("Username"));
			basicDataSource.setPassword(prop.getProperty("Password"));
			basicDataSource.setMaxIdle(Integer.parseInt(prop.getProperty("MaxIdle")));
			basicDataSource.setMinIdle(Integer.parseInt(prop.getProperty("MinIdle")));
			basicDataSource.setMaxActive(Integer.parseInt(prop.getProperty("MaxActive")));
			basicDataSource.setMaxWait(Long.parseLong(prop.getProperty("MaxWait")));

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		connections = new HashMap<>();
		times = new HashMap<>();
		this.reset = reset;
	}

	public synchronized PreparedStatement getPreparedStatement(String name, String sql) throws SQLException {
		if (connections.containsKey(name)) {
			if (times.get(name).incrementAndGet() > reset) {
				restartConnection(name);
			}
			return connections.get(name).prepareStatement(sql);
		} else {
			Connection connection = basicDataSource.getConnection();
			connections.put(name, connection);
			times.put(name, new AtomicInteger(0));
			return connection.prepareStatement(sql);
		}
	}

	private void restartConnection(String name) {
		if (connections.containsKey(name)) {
			try {
				connections.remove(name).close();
				Connection connection = basicDataSource.getConnection();
				connections.put(name, connection);
				times.get(name).set(0);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				connections.put(name, basicDataSource.getConnection());
				times.put(name, new AtomicInteger(0));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void close() {

		try {
			for (Connection connection : connections.values()) {
				connection.close();
			}
			connections.clear();
			times.clear();
			basicDataSource.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
