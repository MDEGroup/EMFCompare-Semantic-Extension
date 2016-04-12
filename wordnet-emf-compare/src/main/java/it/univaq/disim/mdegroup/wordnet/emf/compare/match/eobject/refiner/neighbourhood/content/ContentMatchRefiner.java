package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.refiner.neighbourhood.content;

import java.util.Set;

import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.SimilarityMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.refiner.MatchRefiner;

public class ContentMatchRefiner implements MatchRefiner<SimilarityMatch> {

	@Override
	public void refineMatches(Set<SimilarityMatch> sourceComparison) {
		for(SimilarityMatch similarityMatch : sourceComparison){
			Double outgoingScore = 0.0d; 
			if(similarityMatch.getContentSimilarityMatches().size() > 0){
				for(SimilarityMatch outgoingSimilarityMatch : similarityMatch.getContentSimilarityMatches()){
					outgoingScore += outgoingSimilarityMatch.getSemanticDistanceScore(); 
				}
				outgoingScore /= new Double(similarityMatch.getContentSimilarityMatches().size());
			}
			similarityMatch.setContentSimilarityMatchesDistanceScore(outgoingScore); 
		}
	}

}
