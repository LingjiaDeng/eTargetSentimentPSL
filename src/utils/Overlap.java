package utils;

public final class Overlap {
	
	private Overlap(){
	}
	
	public static boolean subStringOverlap(String s1,String s2){
		if (s1.contains(s2) || s2.contains(s1))
			return true;
		
		return false;
	}
	

}
