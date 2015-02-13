package structure;

public class Feature {
	public int inOpinionSpan;
	public int inTargetSpan;
	public int deptOfParent;
	public int lengthOfDep;
	public int containsSthInDep;
	public int[] unigramCon;
	public int[] bigramCon;
	public int[] unigramDep;
	public int[] bigramDep;
	
	
	public Feature(){
		this.inOpinionSpan = 0;
		this.inTargetSpan = 0;
		this.deptOfParent = 0;
		this.lengthOfDep = 0;
		this.containsSthInDep = 0;
		
	}

}
