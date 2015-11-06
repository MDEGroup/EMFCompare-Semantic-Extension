package it.univaq.disim.mdegroup.semantic.emf.compare.match.eobject.evaluator;

import java.util.concurrent.Callable;

public class SyntacticDistanceEvaluator implements Callable<Double> {
	
	private String first; 
	private String second; 
	
	/* Constructor */
	public SyntacticDistanceEvaluator(String first, String second){
		this.first = first; 
		this.second = second; 
	}
	
	/* Evaluator Entry Point */
	@Override
	public Double call() throws Exception {
		return computeLevenshteinDistance(this.first, this.second);
	}
	
	/* Returns the minimum among three numbers */
    private int minimum(int a, int b, int c) {                            
        return Math.min(Math.min(a, b), c);                                      
    }                                                                            
                                               
    /* Computes the levenshtein distance among two strings */
    private double computeLevenshteinDistance(String str1,String str2) {      
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
        return distance[str1.length()][str2.length()];                           
    }
	
}
