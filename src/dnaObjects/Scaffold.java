package dnaObjects;

public class Scaffold {
	
	public int scaffoldId;
	public int taxonomyId;
	public byte isCircular;
	public int length;
	public int gi;
	
	public Scaffold(int scaffoldId, int taxonomyId, int gi, byte isCircular, int length) {
		super();
		this.scaffoldId = scaffoldId;
		this.taxonomyId = taxonomyId;
		this.gi = gi;
		this.isCircular = isCircular;
		this.length = length;
	}
	
	public String toString(){
		return scaffoldId+" "+taxonomyId+" "+gi+" "+isCircular+" "+length;
	}
	
	
}
