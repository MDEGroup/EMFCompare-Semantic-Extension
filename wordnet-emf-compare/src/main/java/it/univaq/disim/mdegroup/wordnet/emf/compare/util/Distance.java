package it.univaq.disim.mdegroup.wordnet.emf.compare.util;

public class Distance {
	
	/* Computes the levenshtein distance among two strings */
    public static Double computeLevenshteinDistance(String str1,String str2) {      
        int[][] distance = new int[str1.length() + 1][str2.length() + 1];        
        for (int i = 0; i <= str1.length(); i++)                                 
            distance[i][0] = i;                                                  
        for (int j = 1; j <= str2.length(); j++)                                 
            distance[0][j] = j;                                                  
        for (int i = 1; i <= str1.length(); i++)                                 
            for (int j = 1; j <= str2.length(); j++)                             
                distance[i][j] = minimum(                                        
                        distance[i - 1][j] + 1,                                  
                        distance[i][j - 1] + 1,                                  
                        distance[i - 1][j - 1] + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1));
        return new Double(distance[str1.length()][str2.length()]);                           
    }
	
	/* Returns the minimum among three numbers */
    private static int minimum(int a, int b, int c) {                            
        return Math.min(Math.min(a, b), c);                                      
    }                                                                            

    
	public static Double levenshtein(String s1, String s2){
		if (s1.equals(s2)){
            return new Double(0);
        }
        if (s1.length() == 0) {
            return new Double(s2.length());
        }
        if (s2.length() == 0) {
            return new Double(s1.length());
        }
        // create two work vectors of integer distances
        int[] v0 = new int[s2.length() + 1];
        int[] v1 = new int[s2.length() + 1];
        int[] vtemp;
        // initialize v0 (the previous row of distances)
        // this row is A[0][i]: edit distance for an empty s
        // the distance is just the number of characters to delete from t
        for (int i = 0; i < v0.length; i++) {
            v0[i] = i;
        }
        for (int i = 0; i < s1.length(); i++) {
            // calculate v1 (current row distances) from the previous row v0
            // first element of v1 is A[i+1][0]
            //   edit distance is delete (i+1) chars from s to match empty t
            v1[0] = i + 1;
            // use formula to fill in the rest of the row
            for (int j = 0; j < s2.length(); j++) {
                int cost = (s1.charAt(i) == s2.charAt(j)) ? 0 : 1;
                v1[j + 1] = Math.min(
                        v1[j] + 1,              // Cost of insertion
                        Math.min(
                                v0[j + 1] + 1,  // Cost of remove
                                v0[j] + cost)); // Cost of substitution
            }
            // copy v1 (current row) to v0 (previous row) for next iteration
            //System.arraycopy(v1, 0, v0, 0, v0.length);
            // Flip references to current and previous row
            vtemp = v0;
            v0 = v1;
            v1 = vtemp;
                
        }
        return new Double(v0[s2.length()]);
    }
	
	public static Double normalizedLevenshtein(String s1, String s2){
		String[] strings = align(s1, s2);
		return new Double(Distance.computeLevenshteinDistance(strings[0], strings[1]) / Math.max(s1.length(), s2.length()));
	}
	
	public static String[] align(String a, String b) {
        int[][] T = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++)
            T[i][0] = i;

        for (int i = 0; i <= b.length(); i++)
            T[0][i] = i;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1))
                    T[i][j] = T[i - 1][j - 1];
                else
                    T[i][j] = Math.min(T[i - 1][j], T[i][j - 1]) + 1;
            }
        }

        StringBuilder aa = new StringBuilder(), bb = new StringBuilder();

        for (int i = a.length(), j = b.length(); i > 0 || j > 0; ) {
            if (i > 0 && T[i][j] == T[i - 1][j] + 1) {
                aa.append(a.charAt(--i));
                bb.append("-");
            } else if (j > 0 && T[i][j] == T[i][j - 1] + 1) {
                bb.append(b.charAt(--j));
                aa.append("-");
            } else if (i > 0 && j > 0 && T[i][j] == T[i - 1][j - 1]) {
                aa.append(a.charAt(--i));
                bb.append(b.charAt(--j));
            }
        }

        return new String[]{aa.reverse().toString(), bb.reverse().toString()};
    }
}