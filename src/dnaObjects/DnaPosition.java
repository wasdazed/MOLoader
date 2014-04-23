package dnaObjects;

public class DnaPosition {

	public Scaffold scaffold;
	public boolean dir_PLUS;
	public int begin;
	public int end;
	
	public DnaPosition(Scaffold scaffold, String strand, int begin, int end) {
		super();
		this.scaffold = scaffold;
		this.dir_PLUS = strand.equals("+")?true:false;
		this.begin = begin;
		this.end = end;
	}
	public String toString(){
		return "["+scaffold.toString()+"] "+dir_PLUS+" "+begin+" "+end+" ";
	}
	
}
