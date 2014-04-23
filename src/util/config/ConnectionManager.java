package util.config;

import java.sql.*;

public class ConnectionManager {
	private static Connection[] conn=new Connection[]{null,null};
	public static final int RECONNECT_NUMBER = 3;
	public static final String DRIVER_CLASS = "org.gjt.mm.mysql.Driver";

	public static Connection getConnection(int i) {
		try {
			if (conn[i] == null || conn[i].isClosed()) {
				conn[i] = createNewConnection(i);
			}
			if (!checkConnection(i))
				reconnect(i);
			return conn[i];
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Connection createNewConnection(int i) throws Exception {
		String host = Configuration.getConnHost(i);
		String port = Configuration.getConnPort(i);
		String url = "jdbc:mysql://" + host + ":" + port + "/";
		String db = Configuration.getConnDB(i);
		if (db != null)
			url += db;
		String usr = Configuration.getConnUser(i);
		String pwd = Configuration.getConnPwd(i);
		Class.forName(DRIVER_CLASS);
		return DriverManager.getConnection(url, usr, pwd);
	}

	private static boolean checkConnection(int i) {
		if (conn[i] == null)
			return false;
		boolean res = false;
		String testQuery = Configuration.getConnTestQuery(i);
		try {
			Statement stmt = conn[i].createStatement();
			if (stmt.execute(testQuery))
				res = true;
			stmt.close();
		} catch (Exception e) {
			System.err.println("Bad connection.");
		}
		return res;
	}

	private static void reconnect(int j) throws Exception {
		System.out.println("Reconnectiong..");
		int i = 0;
		while (i < RECONNECT_NUMBER) {
			closeConnection(j);
			conn[i] = createNewConnection(j);
			if (checkConnection(j))
				break;
			i++;
		}
		if (i == RECONNECT_NUMBER)
			throw new Exception();
	}

	public static void closeConnection(int i) {
		try {
			if ((conn[i] != null) && (!conn[i].isClosed()))
				conn[i].close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
