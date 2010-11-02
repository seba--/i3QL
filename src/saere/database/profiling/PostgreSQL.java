package saere.database.profiling;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Helper to ease the working with a PostgreSQL database.
 * 
 * @author David Sullivan
 * @version 1.01, 10/26/2010
 */
public final class PostgreSQL {
	
	private Connection conn;
	private Statement st;
	
	private String url;
	private String user;
	private String pass;
	
	public PostgreSQL() {
		
		// Default values
		url = "jdbc:postgresql://localhost:5432/sae";
		user = "postgres";
		pass = "5gks"; // XXX Well...
		
		// Load PostgreSQL driver
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			System.err.println("Unable to load PostgreSQL driver");
			e.printStackTrace();
		}
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public void setPassword(String password) {
		this.pass = password;
	}
	
	public String getFullUrl() {
		return url + "?user=" + user + "&password=" + pass;
	}
	
	public void connect() {
		try {
			conn = DriverManager.getConnection(getFullUrl());
			st = conn.createStatement();
		} catch (SQLException e) {
			System.err.println("Unable to open connection/statement to PostgreSQL database");
			e.printStackTrace();
		}
	}
	
	public int modify(String sql) {
		assert conn != null : "Unable to modify without connection";
		int r = 0;
		try {
			r = st.executeUpdate(sql);
		} catch (SQLException e) {
			System.err.println("Unable to modify: " + sql);
			e.printStackTrace();
		}
		return r;
	}
	
	public int insert(String tableName, String[] columnNames, String[] values) {
		assert tableName != null && tableName.length() > 0 && columnNames.length == values.length && columnNames.length > 0 : "Invalid parameter(s)";
		
		String sql = "INSERT INTO " + tableName + " (";
		boolean first = true;
		for (String columnName : columnNames) {
			if (first) {
				first = false;
			} else {
				sql += ",";
			}
			sql += columnName;
		}
		sql += ") VALUES (";
		first = true;
		for (String value : values) {
			if (first) {
				first = false;
			} else {
				sql += ",";
			}
			sql += "'" + value + "'"; // XXX We always wrap values with quotation marks
		}
		sql += ");";
		
		return modify(sql);
	}
	
	public ResultSet query(String query) {
		assert conn != null : "Unable to execute query without connection";
		ResultSet rs = null;
		try {
			rs = st.executeQuery(query);
		} catch (SQLException e) {
			System.err.println("Unable to execute query: " + query);
			e.printStackTrace();
		}
		return rs;
	}
	
	public void disconnect() {
		try {
			st.close();
			conn.close();
			conn = null;
		} catch (SQLException e) {
			System.err.println("Unable to close connection/statement to PostgreSQL database");
			e.printStackTrace();
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (conn != null) {
			System.err.println("PostgreSQL automatic shutdown...");
			disconnect();
		}
	}
}
