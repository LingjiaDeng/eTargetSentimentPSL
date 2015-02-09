package utils;

public final class Path {
	private static String bishanRoot = "/afs/cs.pitt.edu/usr0/lid29/Documents/Bishan/";
	private static String gate_annoRoot = "/afs/cs.pitt.edu/projects/wiebe/opin/database/gate_anns/";
	private static String docRoot = "/afs/cs.pitt.edu/projects/wiebe/opin/database/docs/";;
	private static String gfbfLexiconFile = "/afs/cs.pitt.edu/usr0/lid29/Downloads/effectwordnet/EffectWordNet.tff";
	
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
}
