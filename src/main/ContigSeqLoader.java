package main;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import util.config.ConnectionManager;

public class ContigSeqLoader {
	
	public static String loadContigSeq(int id) throws SQLException{
		Connection conn = ConnectionManager.getConnection(0);
		String seq = null;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(" SELECT sequence from ScaffoldSeq where scaffoldId = "+id+";");
		while (rs.next()){
			seq = rs.getString(1);
		}
		rs.close();
		stmt.close();
		return seq;		
	}
}
