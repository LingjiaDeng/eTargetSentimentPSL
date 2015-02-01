package utils;

public final class Path {
	private static String bishanRoot = "/afs/cs.pitt.edu/usr0/lid29/Documents/Bishan/";
	private static String gate_annoRoot = "/afs/cs.pitt.edu/projects/wiebe/opin/database/gate_anns/";
	
	private Path(){
	}
	
	public static String getBishanRoot(){
		return bishanRoot;
	}
	
	public static String getGate_annoRoot(){
		return gate_annoRoot;
	}
}
