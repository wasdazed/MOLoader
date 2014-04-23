package main;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import util.config.ConnectionManager;

public class LocusSeqLoader {
	
	public static String loadAASeq(int id,int version) throws SQLException{
		Connection conn = ConnectionManager.getConnection(0);
		String seq = null;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(" SELECT sequence from AASeq where locusId = "+id+" and version = "+version+" ;");
		while (rs.next()){
			seq = rs.getString(1);
		}
		rs.close();
		stmt.close();
		return seq;		
	}
	
}
