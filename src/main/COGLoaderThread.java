package main;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import bif.msk.ent.seq.Codon;
import bif.msk.ent.seq.Sequence;
import dnaObjects.Locus;

public class COGLoaderThread extends Thread {

	String dir;
	COGLoader loader;
	int id;
	String type;
	int upstream;
	
	public COGLoaderThread(int i,COGLoader loader,String dir,String type,int upstream) {
		this.loader = loader;
		this.dir = dir;
		this.id = i;
		this.type = type;
		this.upstream=upstream;
	}
	
	public void run(){
		int strangeStartCount = 0;
		int erroneusLocusCount = 0;
		try{
			Integer cog = loader.getNextCOG();
			while(cog!=null){
				ArrayList<Locus> genes = loader.cogs.get(cog);
				String f = dir+"/"+type+cog+".fasta";
				PrintWriter pw = new PrintWriter(f);
				for (Iterator<Locus> iterator = genes.iterator(); iterator.hasNext();) {
					Locus l =  iterator.next();
					Sequence s = LocusUpstreamLoader.loadGeneUpstream(l, -upstream);
					String seq = new String (s.getSeq());
					if (!loader.starts.contains(seq.substring(upstream, upstream+3))) {
						System.out.println("Thread "+id+"says: "+type+cog+" gene "+s.getName()+":"+seq.substring(upstream, upstream+3));
						String tr_seq = new String(Codon.TransSeq(s.getSeq(), 0));
						String tr_seq_cut = tr_seq.substring((int)(upstream/3),tr_seq.length()-1);
						String locusAA = LocusSeqLoader.loadAASeq(l.locusId,l.version);
						if (!tr_seq_cut.substring(1).equals(locusAA.substring(1))) {
							System.out.println("AA seqs not equal! reporting Error");
							erroneusLocusCount++;
							System.err.println("Thread "+id+"says: "+type+cog+" gene "+s.getName()+":"+seq.substring(upstream, upstream+3));
							System.err.println("Translated from contig pos: "+tr_seq_cut);
							System.err.println("From Locus aa seq: "+locusAA);
						}
						else strangeStartCount++;
					}
					pw.println(">"+s.getName());
					pw.println(seq);
				}
				pw.close();
				System.out.println("Thread "+id+"says: "+type+cog+" done.");
				cog = loader.getNextCOG();
			}
			System.out.println("Thread "+id+"reports: "+strangeStartCount+" cases of strange start codons");
			System.out.println("Thread "+id+"reports: "+erroneusLocusCount+" cases of erroneous locus");
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
