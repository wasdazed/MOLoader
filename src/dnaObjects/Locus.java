package dnaObjects;

public class Locus {
	
	public int locusId;
	public int version;
	public int cogInfoId;
	public DnaPosition pos;
	public Scaffold sc;
	
	public Locus(int locusId, int version, int cogInfoId,DnaPosition pos,Scaffold sc) {
		super();
		this.locusId = locusId;
		this.version = version;
		this.cogInfoId = cogInfoId;
		this.pos = pos;
		this.sc = sc;
	}	
}
