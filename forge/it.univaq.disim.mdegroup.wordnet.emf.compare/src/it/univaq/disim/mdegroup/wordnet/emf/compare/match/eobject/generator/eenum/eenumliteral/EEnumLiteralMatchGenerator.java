package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.eenum.eenumliteral;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.compare.CompareFactory;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;

import it.univaq.disim.mdegroup.wordnet.emf.compare.impl.SimilarityMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.MatchGenerator;

public class EEnumLiteralMatchGenerator implements MatchGenerator<SimilarityMatch, EEnumLiteral> {
	
//	private Double contentDistanceWeight = 0.025d; 
//	private Double containerDistanceWeight = 0.025d; 
//	private Double semanticDistanceWeight = 0.95d; 
//	private Double distanceThreshold = 0.3d;

	private Double contentDistanceWeight = 0.0d; 
	private Double containerDistanceWeight = 0.0d; 
	private Double semanticDistanceWeight = 1.0d; 
	private Double distanceThreshold = -1.0d;

	@Override
	public void generateMatches(Set<SimilarityMatch> generatorContext, SimilarityMatch rootSimilarityMatch, List<EEnumLiteral> leftElements, List<EEnumLiteral> rightElements) {
		for(EEnumLiteral leftEEnumLiteral : leftElements){
			for(EEnumLiteral rightEEnumLiteral : rightElements){
				SimilarityMatch similarityMatch = new SimilarityMatch();
				
				similarityMatch.setContainerDistanceWeight(this.containerDistanceWeight);
				similarityMatch.setContentDistanceWeight(this.contentDistanceWeight);
				similarityMatch.setSemanticDistanceWeight(this.semanticDistanceWeight);
				similarityMatch.setDistanceThreshold(this.distanceThreshold);
				
				similarityMatch.setMatch(CompareFactory.eINSTANCE.createMatch());
				similarityMatch.getMatch().setLeft(leftEEnumLiteral);
				similarityMatch.getMatch().setLeft(rightEEnumLiteral);
				similarityMatch.getMatch().setOrigin(null);
				List<SimilarityMatch> existingSimilarityMatches = generatorContext.stream()
						.filter(existingSimilarityMatch -> existingSimilarityMatch.equals(similarityMatch))
						.collect(Collectors.toList());
				if(existingSimilarityMatches.size() > 0){
					SimilarityMatch existingSimilarityMatch = existingSimilarityMatches.get(0);
					/* EEnum */
					if(!existingSimilarityMatch.equals(rootSimilarityMatch) 
							&& ((rootSimilarityMatch.getMatch().getLeft() != null && rootSimilarityMatch.getMatch().getLeft() instanceof EEnum) || (rootSimilarityMatch.getMatch().getLeft() == null))
							&& ((rootSimilarityMatch.getMatch().getRight() != null && rootSimilarityMatch.getMatch().getRight() instanceof EEnum) || (rootSimilarityMatch.getMatch().getRight() == null))){
						existingSimilarityMatch.addContainerSimilarityMatch(rootSimilarityMatch); 
						rootSimilarityMatch.addContentSimilarityMatch(existingSimilarityMatch); 
					}
				} else {
					/* EEnum */
					if(((rootSimilarityMatch.getMatch().getLeft() != null && rootSimilarityMatch.getMatch().getLeft() instanceof EEnum) || (rootSimilarityMatch.getMatch().getLeft() == null))
						&& ((rootSimilarityMatch.getMatch().getRight() != null && rootSimilarityMatch.getMatch().getRight() instanceof EEnum) || (rootSimilarityMatch.getMatch().getRight() == null))){
						similarityMatch.addContainerSimilarityMatch(rootSimilarityMatch); 
						rootSimilarityMatch.addContentSimilarityMatch(similarityMatch); 
					} 
					generatorContext.add(similarityMatch); 
				}
			}
		}
	}

}
