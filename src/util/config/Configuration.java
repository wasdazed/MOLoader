package util.config;

import java.util.*;
import java.io.*;

public class Configuration {

	public static final String ConnHost = "Host";
	public static final String ConnPort = "Port";
	public static final String ConnDB = "DB";
	public static final String ConnUser = "User";
	public static final String ConnPwd = "Pwd";
	public static final String MysqlPath = "MysqlPath";
	public static final String ConnTestQuery = "TestQuery";

	private static Properties[] props = new Properties[]{null,null};
//	private static String cfg_file_name = "/configuration2.properties";
	private static String[] cfg_file_name =new String[]{"/MicrobeOnline.properties"};

	public static boolean isPropsNull(int i){
		return (props[i]==null);
	}	
	
	public static String getConnHost(int i) {
		Properties prpos = getProps(i);
		String host = prpos.getProperty(ConnHost);
		if (host == null)
			host = "localhost";
		return host;
	}

	public static String getConnPort(int i) {
		Properties prpos = getProps(i);
		String port = prpos.getProperty(ConnPort);
		if (port == null)
			port = "3306";
		return port;
	}

	public static String getMysqlPath(int i) {
		Properties prpos = getProps(i);
		String mysql_path = prpos.getProperty(MysqlPath);
		if (mysql_path == null)
			mysql_path = "mysql";
		return mysql_path;
	}

	public static String getConnDB(int i) {
		Properties prpos = getProps(i);
		return prpos.getProperty(ConnDB);
	}

	public static String getConnUser(int i) {
		Properties prpos = getProps(i);
		return prpos.getProperty(ConnUser);
	}

	public static String getConnPwd(int i) {
		Properties prpos = getProps(i);
		return prpos.getProperty(ConnPwd);
	}

	public static String getConnTestQuery(int i) {
		Properties prpos = getProps(i);
		String testQuery = prpos.getProperty(ConnTestQuery);
		if (testQuery == null)
			testQuery = "select 1";
		return testQuery;
	}

	public static Properties getProps(int i) {
		if (props[i] == null) {
			try {
				props[i] = loadConfigProps(i);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException();
			}
		}
		return props[i];
	}

	public static Properties loadConfigProps(int i) {
		Properties p = new Properties();
		try {
			Reader fr = new InputStreamReader(Configuration.class
					.getResourceAsStream(cfg_file_name[i]));
			p.load(fr);
			fr.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Can't read Configurations file((");
			// TODO: handle exception
		}
		return p;
	}	
}
