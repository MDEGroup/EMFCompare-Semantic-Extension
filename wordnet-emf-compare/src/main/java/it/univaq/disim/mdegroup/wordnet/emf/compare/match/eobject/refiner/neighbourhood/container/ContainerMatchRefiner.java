package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.refiner.neighbourhood.container;

import java.util.Set;

import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.SimilarityMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.refiner.MatchRefiner;

public class ContainerMatchRefiner implements MatchRefiner<SimilarityMatch> {

	@Override
	public void refineMatches(Set<SimilarityMatch> sourceComparison) {
		for(SimilarityMatch similarityMatch : sourceComparison){
			Double incomingScore = 1.0d;
			if(similarityMatch.getContainerSimilarityMatches().size() > 0){
				for(SimilarityMatch incomingSimilarityMatch : similarityMatch.getContainerSimilarityMatches()){
					if(incomingScore > incomingSimilarityMatch.getSemanticDistanceScore()){
						incomingScore = incomingSimilarityMatch.getSemanticDistanceScore(); 
					}
				}
			}
			similarityMatch.setContainerSimilarityMatchesDistanceScore(incomingScore);
		}
	}

}
