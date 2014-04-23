package main;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import util.config.ConnectionManager;
import bif.msk.ent.seq.Sequence;
import dnaObjects.DnaPosition;
import dnaObjects.Locus;
import dnaObjects.Scaffold;

public class LocusUpstreamLoader {


	private static String loadScaffoldFragment(int scaffoldId, int start,int end) throws SQLException{
		Connection conn = ConnectionManager.getConnection(0);
		String seq = null;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(" SELECT substring(sequence,"+start+","+(end-start)+") FROM"+
				" ScaffoldSeq where scaffoldId="+scaffoldId); 
		while (rs.next()){
			seq = rs.getString(1);
		}
		rs.close();
		stmt.close();
		return seq;		
	}

	public static Sequence loadGeneUpstream(Locus l, int leftCoord) throws SQLException{
		DnaPosition pos = l.pos;
		Scaffold sc = l.sc;
		//	System.out.println(pos.toString());
		int scaffoldStartPos;
		int scaffoldEndPos;
		if (pos.dir_PLUS) { 
			scaffoldStartPos = pos.begin+leftCoord;
			scaffoldEndPos = pos.end+1;
		}
		else{
			scaffoldStartPos = pos.begin;
			scaffoldEndPos = pos.end-leftCoord+1;
		}
		String seq;
		if (sc.isCircular==0){
			System.out.println("not circular");
			if (scaffoldStartPos<=1) scaffoldStartPos=1;
			if (scaffoldEndPos>=sc.length) scaffoldEndPos=sc.length;
			seq = loadScaffoldFragment(sc.scaffoldId,scaffoldStartPos,scaffoldEndPos);
		}
		else{
			if (scaffoldStartPos<=1) {
				scaffoldStartPos=sc.length+scaffoldStartPos;
				if ((scaffoldEndPos<=1)){
					scaffoldEndPos=sc.length+scaffoldEndPos;
					seq = loadScaffoldFragment(sc.scaffoldId,scaffoldStartPos,scaffoldEndPos); 
				}
				else{
					String seq1 = loadScaffoldFragment(sc.scaffoldId,scaffoldStartPos,sc.length+1);
					String seq2 = loadScaffoldFragment(sc.scaffoldId,1,scaffoldEndPos);
					seq = seq1+seq2;
				}
			}
			else if (scaffoldEndPos>=sc.length) {
				scaffoldEndPos=scaffoldEndPos-sc.length;
				if ((scaffoldStartPos>=sc.length)){
					scaffoldStartPos=scaffoldStartPos-sc.length;
					seq = loadScaffoldFragment(sc.scaffoldId,scaffoldStartPos,scaffoldEndPos);
				}
				else{
					String seq1 = loadScaffoldFragment(sc.scaffoldId,scaffoldStartPos,sc.length+1);
					String seq2 = loadScaffoldFragment(sc.scaffoldId,1,scaffoldEndPos);
					seq = seq1+seq2;
				}
			}
			else {
				seq = loadScaffoldFragment(sc.scaffoldId,scaffoldStartPos,scaffoldEndPos);
			}
		}
		byte[] res;
		if (!pos.dir_PLUS){
			res = Sequence.getReverseComplement(seq.getBytes());
		}
		else{
			res = seq.getBytes();
		}
		String name = l.sc.taxonomyId+"|"+l.sc.scaffoldId+"|"+l.sc.isCircular+"|"+l.sc.gi+"|"+l.locusId+"|"+l.version+"|"+(l.pos.dir_PLUS?1:-1)+"|"+l.pos.begin+"|"+l.pos.end+"|"+scaffoldStartPos+"|"+scaffoldEndPos; 
		return new Sequence(name,res);
	}

}
