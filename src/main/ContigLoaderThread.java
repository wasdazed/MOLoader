package main;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.SQLException;

import dnaObjects.Scaffold;

public class ContigLoaderThread extends Thread {

	String dir;
	COGLoader loader;
	int id;
	
	public ContigLoaderThread(int i,COGLoader loader,String dir) {
		this.loader = loader;
		this.dir = dir;
		this.id = i;
	}
	
	public void run(){
		try{
			Scaffold scaffold = loader.getNextScaffold();
			while(scaffold!=null){
				String f = dir+"/"+scaffold.taxonomyId+"_contig"+scaffold.scaffoldId+".fasta";
				PrintWriter pw = new PrintWriter(f);
				String seq = ContigSeqLoader.loadContigSeq(scaffold.scaffoldId);
				pw.println(">"+scaffold.taxonomyId+"|"+scaffold.scaffoldId+"|"+scaffold.gi);
				pw.println(seq);
				pw.close();
				System.out.println("Thread "+id+"says: contig "+scaffold.scaffoldId+" done.");
				scaffold = loader.getNextScaffold();
			}
		}
		catch (SQLException e) {
			System.out.println("Thread "+id+"says SQL EXCEPTION:");
			e.printStackTrace();			
		} catch (FileNotFoundException e) {
			System.out.println("Thread "+id+"says FILE NOT FOUND EXCEPTION:");
			e.printStackTrace();
		}
	}
}