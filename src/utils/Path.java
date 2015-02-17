package utils;

public final class Path {
	private static String bishanRoot = "/afs/cs.pitt.edu/usr0/lid29/Documents/Bishan/";
	private static String gate_annoRoot = "/afs/cs.pitt.edu/projects/wiebe/opin/database/gate_anns/";
	private static String docRoot = "/afs/cs.pitt.edu/projects/wiebe/opin/database/docs/";;
	private static String gfbfLexiconFile = "/afs/cs.pitt.edu/usr0/lid29/Downloads/effectwordnet/EffectWordNet.tff";
	private static String doclistFile = "/afs/cs.pitt.edu/usr0/lid29/Downloads/man_anns/doclist";
	private static String featureRoot = "/afs/cs.pitt.edu/projects/wiebe/opin/database/auto_anns/";
	private static String PSLRoot = "/afs/cs.pitt.edu/projects/wiebe/opin/database/auto_anns/";
	
	private Path(){
	}
	
	public static String getBishanRoot(){
		return bishanRoot;
	}
	
	public static String getGate_annoRoot(){
		return gate_annoRoot;
	}
	
	public static String getGFBFLexiconFile(){
		return gfbfLexiconFile;
	}
	
	public static String getDocRoot(){
		return docRoot;
	}
	
	public static String getDoclistFile(){
		return doclistFile;
	}
	
	public static String getFeatureRoot(){
		return featureRoot;
	}
	
	public static String getPSLRoot(){
		return PSLRoot;
	}
}
