package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import dnaObjects.DnaPosition;
import dnaObjects.Locus;
import dnaObjects.Scaffold;
import util.config.ConnectionManager;

public class COGLoader {
	
	protected Hashtable<Integer,ArrayList<Locus>> cogs;
	protected Hashtable<Integer,Scaffold> scaffolds;
	private HashSet<Integer> taxonomies;
	protected HashSet<String> starts= new HashSet<String>();
	{
		starts.add("ATG");
		starts.add("GTG");
		starts.add("TTG");
	}
	private Iterator<Integer> COGIterator;
	private Iterator<Scaffold> contigIterator;
	private int ind_COG = -1;
	private int ind_contig = -1;
	String type;
	
	public COGLoader(String type) {
		this.type = type;
	}
	
	
	private static String getQString(ArrayList<Integer> taxList,String type) throws SQLException{
		String q = "";
		for (Iterator<Integer> iterator = taxList.iterator(); iterator.hasNext();) {
			Integer integer = iterator.next();
			q+=(","+integer);		
		}
		q=q.substring(1);
		
		String qString = "";
		
		if (type.equals("MOG")) qString+=
		"select mogId,c.locusId,c.version,s.taxonomyId,s.scaffoldId,gi,isCircular,length,p.strand,p.begin,p.end from Scaffold s"+
		" inner join Locus l ON l.scaffoldId=s.scaffoldId"+
		" inner join MOGMember c ON c.locusId=l.locusId and c.version=l.version"+
		" inner join Position p ON l.posId=p.posId"+
		" where isActive=1 and isPartial=0 and isGenomic=1 and s.taxonomyId IN ("+q+") and l.priority=1"+
		" order by mogId";
		
		else if (type.equals("COG")) qString+=
				"select cogInfoId,c.locusId,c.version,taxonomyId,s.scaffoldId,gi,isCircular,length,p.strand,p.begin,p.end from Scaffold s"+
				" inner join Locus l ON l.scaffoldId=s.scaffoldId"+
				" inner join COG c ON c.locusId=l.locusId and c.version=l.version"+
				" inner join Position p ON l.posId=p.posId"+
				" where isActive=1 and isPartial=0 and isGenomic=1 and s.taxonomyId IN ("+q+") and l.priority=1"+
				" order by cogInfoId";
		else return null;
		
		return(qString);
	}
		
	private void loadMembers(ArrayList<Integer>taxList) throws SQLException{
		String qString = getQString(taxList, type);
		if (qString==null) {
			System.out.println("Unknown type "+type);
			System.exit(0);
		}
		cogs = new Hashtable<Integer,ArrayList<Locus>>();
		scaffolds = new Hashtable<Integer,Scaffold>();
		taxonomies = new HashSet<Integer>();
		Connection conn = ConnectionManager.getConnection(0);
		Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(qString);
			int prevCogInfoId=-1;
			ArrayList<Locus> genes = null;
			while(rs.next()) {
				int cogInfoId = rs.getInt(1);
				if (cogInfoId!=prevCogInfoId){
					if (prevCogInfoId!=-1) {
						if (genes.size()>1) cogs.put(prevCogInfoId, genes);
						else System.out.println(type+prevCogInfoId+" has only 1 record for specified taxonomies; not adding");
					}
					prevCogInfoId=cogInfoId;
					genes = new ArrayList<Locus>();
				}
				int locusId = rs.getInt(2);
				int version = rs.getInt(3);
				int taxonomyId = rs.getInt(4);
				int scaffoldId = rs.getInt(5);
				Scaffold sc;
				if (scaffolds.containsKey(scaffoldId)) sc=scaffolds.get(scaffoldId);
				else {
					sc = new Scaffold(scaffoldId,taxonomyId,rs.getInt(6),rs.getByte(7),rs.getInt(8));
					scaffolds.put(scaffoldId, sc);
					taxonomies.add(taxonomyId);
				}
				DnaPosition pos = new DnaPosition(sc, rs.getString(9),rs.getInt(10),rs.getInt(11));
				genes.add(new Locus(locusId, version, cogInfoId, pos, sc));
			}
			if (genes.size()>1) cogs.put(prevCogInfoId, genes);
			else System.out.println(type+prevCogInfoId+" has only 1 record for specified taxonomies; not adding");
			rs.close();
			stmt.close();
	}
	
	protected synchronized Integer getNextCOG(){
		if (ind_COG<0) {
			this.COGIterator = cogs.keySet().iterator();
			ind_COG = 0;
		}
		if (COGIterator.hasNext()) return  COGIterator.next();
		else return null;	
	}
	
	protected synchronized Scaffold getNextScaffold(){
		if (ind_contig<0) {
			this.contigIterator = scaffolds.values().iterator();
			ind_contig = 0;
		}
		if (contigIterator.hasNext()) return contigIterator.next();
		else return null;	
	}
		
	public void writeCOGs(int threadNumber, String dir,String type,int upstream){
		long time = System.currentTimeMillis();
		
		File f = new File(dir);
		if (!f.exists()) f.mkdir();
		else{
			File[] ff = f.listFiles();
			for (int i = 0; i < ff.length; i++) {
				ff[i].delete();
			}
		}
		
		COGLoaderThread[] threads = new COGLoaderThread[threadNumber];
		for (int j = 0; j < threadNumber; j++) {
			COGLoaderThread clt = new COGLoaderThread(j,this, dir,type,upstream);
			clt.start();
			threads[j]=clt;
			System.out.println("thread "+j+" started");
		}
		
		for (int i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				System.out.println("Can't join thread "+i);
				e.printStackTrace();
			}
		}
		
		System.out.println(type+"s loaded. Total working time: "+(System.currentTimeMillis()-time)+" ms");		
	}
	
	public void writeContigs(int threadNumber, String dir){
		long time = System.currentTimeMillis();
		
		File f = new File(dir);
		if (!f.exists()) f.mkdir();
		else{
			File[] ff = f.listFiles();
			for (int i = 0; i < ff.length; i++) {
				ff[i].delete();
			}
		}
		
		ContigLoaderThread[] threads = new ContigLoaderThread[threadNumber];
		for (int j = 0; j < threadNumber; j++) {
			ContigLoaderThread clt = new ContigLoaderThread(j,this, dir);
			clt.start();
			threads[j]=clt;
			System.out.println("thread "+j+" started");
		}
		
		for (int i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				System.out.println("Can't join thread "+i);
				e.printStackTrace();
			}
		}		
		System.out.println("Contigs loadded. Total working time: "+(System.currentTimeMillis()-time)+" ms");		
	}
	
	public static void launch(String f,String type,int upstream,boolean withContig){
		System.out.println("Reading taxonomy list..");
		ArrayList<Integer> taxList = new ArrayList<Integer>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = br.readLine();
			while (line!=null) {
				if (!line.trim().equals("")) {
					int taxa = Integer.parseInt(line);
					taxList.add(taxa);
					System.out.println(taxa);
				}
				line = br.readLine();
			}
			br.close();	
		}
		catch (IOException e) {
			System.out.println("IO EXCEPTION READING "+f);
			e.printStackTrace();
			System.exit(0);
		}

		System.out.println("Loading "+type+" data..");
		
		COGLoader loader = new COGLoader(type);		
		try {
			loader.loadMembers(taxList);
		} catch (SQLException e) {
			System.out.println("FAILED TO LOAD "+type+" DATA");
			e.printStackTrace();
			System.exit(0);
		}
				
		System.out.println("ALL "+type+" NUMBER FOR SPECIFIED TAXONOMIES: "+loader.cogs.size());
		System.out.println("SCAFFOLDS NUMBER:" + loader.scaffolds.size());
		System.out.println("TAXONOMIES NUMBER:" + loader.taxonomies.size());
		for (Iterator<Integer> iterator = loader.taxonomies.iterator(); iterator.hasNext();) {
			System.out.println(iterator.next());			
		}	
		
		int MAX_PROCESSORS_NUMBER = 24;
		int processorsNumber = Runtime.getRuntime().availableProcessors();
		System.out.println("processors number:"+processorsNumber);
		int threadNumber = Math.min(processorsNumber, MAX_PROCESSORS_NUMBER);
		
		System.out.println("Loading loading "+type+" fastas..");
		String dir = "MO_"+type;
		loader.writeCOGs(threadNumber, dir,type,upstream);
		
		if (withContig){
			System.out.println("Loading corresponding contigs..");
			loader.writeContigs(threadNumber, "MO_Contig");
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException {
	/*	String f = "genomes_list.txt";
		String type = "MOG";
		int upstream = 201;*/
		String f = args[0];
		String type = args[1];
		int upstream = Integer.parseInt(args[2]);
		boolean withContig = false;
		try{
			if (args[3].toLowerCase().equals("withcontig")) withContig = true;
		}
		catch(ArrayIndexOutOfBoundsException e){			
		}
		System.out.println(withContig);
		launch(f,type,upstream,withContig);
	}
	
}


