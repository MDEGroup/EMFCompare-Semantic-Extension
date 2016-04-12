package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.filterer.threshold;

import java.util.Set;

import org.eclipse.emf.compare.CompareFactory;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Match;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.SimilarityMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.filterer.MatchFilterer;

public class HybridThresholdMatchFilterer implements MatchFilterer<SimilarityMatch> {

	public void filterMatches(Set<SimilarityMatch> sourceComparison, Comparison targetComparison) {
		for(SimilarityMatch similarityMatch : sourceComparison){
			Double similarityMatchScore = 
					(similarityMatch.getContainerDistanceWeight() * similarityMatch.getContainerSimilarityMatchesDistanceScore()) + 
					(similarityMatch.getContentDistanceWeight() * similarityMatch.getContentSimilarityMatchesDistanceScore()) + 
					(similarityMatch.getSemantictDistanceWeight() * similarityMatch.getSemanticDistanceScore());
			if(similarityMatchScore <= similarityMatch.getDistanceThreshold()){
				targetComparison.getMatches().add(similarityMatch.getMatch());
			} else {
				if(similarityMatch.getMatch().getLeft() != null){
					Match leftMatch = CompareFactory.eINSTANCE.createMatch(); 
					leftMatch.setLeft(similarityMatch.getMatch().getLeft());
					leftMatch.setOrigin(null);
					leftMatch.setRight(null);
					targetComparison.getMatches().add(leftMatch);
				}
				if(similarityMatch.getMatch().getRight() != null){
					Match rightMatch = CompareFactory.eINSTANCE.createMatch();
					rightMatch.setLeft(null);
					rightMatch.setRight(similarityMatch.getMatch().getRight());
					rightMatch.setOrigin(null);
					targetComparison.getMatches().add(rightMatch); 
				}
			}
		}
	}

}
