package saere.database.profiling;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public final class PostgreSQL {
	
	private Connection conn;
	private Statement st;
	
	private String url;
	private String user;
	private String pass;
	
	public PostgreSQL() {
		
		// Default values
		url = "jdbc:postgresql://localhost:5432/sae";
		user = "<insert-real-username-here>";
		pass = "<insert-real-password-here>"; // XXX Well...
		
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
		} catch (SQLException e) {
			System.err.println("Unable to close connection/statement to PostgreSQL database");
			e.printStackTrace();
		}
	}
}
