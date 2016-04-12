package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.filterer.threshold;

import java.io.IOException;
import java.util.Set;

import org.eclipse.emf.compare.CompareFactory;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.ecore.ENamedElement;

import it.univaq.disim.mdegroup.wordnet.emf.compare.impl.SimilarityMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.filterer.MatchFilterer;

public class HybridThresholdMatchFilterer implements MatchFilterer<SimilarityMatch> {

	@Override
	public void filterMatches(Set<SimilarityMatch> sourceComparison, Comparison targetComparison) {
		for(SimilarityMatch similarityMatch : sourceComparison){
			Double similarityMatchScore = 
					(similarityMatch.getContainerDistanceWeight() * similarityMatch.getContainerSimilarityMatchesDistanceScore()) + 
					(similarityMatch.getContentDistanceWeight() * similarityMatch.getContentSimilarityMatchesDistanceScore()) + 
					(similarityMatch.getSemantictDistanceWeight() * similarityMatch.getSemanticDistanceScore());
//			if(((ENamedElement)similarityMatch.getMatch().getLeft()).getName().equals("eAttributeType") && 
//					((ENamedElement)similarityMatch.getMatch().getRight()).getName().equals("attribute")){
//				System.out.println(similarityMatchScore);
//				System.out.println(similarityMatch.getContainerSimilarityMatchesDistanceScore() + "[" + similarityMatch.getContainerDistanceWeight() + "]");
//				System.out.println(similarityMatch.getContentSimilarityMatchesDistanceScore() + "[" + similarityMatch.getContentDistanceWeight() + "]");
//				System.out.println(similarityMatch.getSemanticDistanceScore() + "[" + similarityMatch.getSemantictDistanceWeight() + "]");
//				try {
//					System.in.read();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
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
