package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.ereference;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.compare.CompareFactory;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import com.google.common.collect.Lists;

import it.univaq.disim.mdegroup.wordnet.emf.compare.impl.SimilarityMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.MatchGenerator;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.eclass.EClassMatchGenerator;

public class EReferenceMatchGenerator implements MatchGenerator<SimilarityMatch, EReference> {
	
//	private Double contentDistanceWeight = 0.025d; 
//	private Double containerDistanceWeight = 0.025d; 
//	private Double semanticDistanceWeight = 0.95d; 
//	private Double distanceThreshold = 0.3d;

	private Double contentDistanceWeight = 0.0d; 
	private Double containerDistanceWeight = 0.3d; 
	private Double semanticDistanceWeight = 0.7d; 
	private Double distanceThreshold = 0.1d;
	
	@Override
	public void generateMatches(Set<SimilarityMatch> generatorContext, SimilarityMatch rootSimilarityMatch, List<EReference> leftElements, List<EReference> rightElements) {
		for(EReference leftEReference : leftElements){
			for(EReference rightEReference : rightElements){
				SimilarityMatch similarityMatch = new SimilarityMatch();
				
				similarityMatch.setContainerDistanceWeight(this.containerDistanceWeight);
				similarityMatch.setContentDistanceWeight(this.contentDistanceWeight);
				similarityMatch.setSemanticDistanceWeight(this.semanticDistanceWeight);
				similarityMatch.setDistanceThreshold(this.distanceThreshold);
				
				similarityMatch.setMatch(CompareFactory.eINSTANCE.createMatch());
				similarityMatch.getMatch().setLeft(leftEReference);
				similarityMatch.getMatch().setRight(rightEReference);
				similarityMatch.getMatch().setOrigin(null);
				List<SimilarityMatch> existingSimilarityMatches = generatorContext.stream()
						.filter(existingSimilarityMatch -> existingSimilarityMatch.equals(similarityMatch))
						.collect(Collectors.toList());
				if(existingSimilarityMatches.size() > 0){
					SimilarityMatch existingSimilarityMatch = existingSimilarityMatches.get(0);
					if(!existingSimilarityMatch.equals(rootSimilarityMatch) 
							&& ((rootSimilarityMatch.getMatch().getLeft() != null && rootSimilarityMatch.getMatch().getLeft() instanceof EClass) || (rootSimilarityMatch.getMatch().getLeft() == null))
							&& ((rootSimilarityMatch.getMatch().getRight() != null && rootSimilarityMatch.getMatch().getRight() instanceof EClass) || (rootSimilarityMatch.getMatch().getRight() == null))){
						existingSimilarityMatch.addContainerSimilarityMatch(rootSimilarityMatch);
						rootSimilarityMatch.addContentSimilarityMatch(existingSimilarityMatch);
					}
				} else {
					if(((rootSimilarityMatch.getMatch().getLeft() != null && rootSimilarityMatch.getMatch().getLeft() instanceof EClass) || (rootSimilarityMatch.getMatch().getLeft() == null))
							&& ((rootSimilarityMatch.getMatch().getRight() != null && rootSimilarityMatch.getMatch().getRight() instanceof EClass) || (rootSimilarityMatch.getMatch().getRight() == null))){
						similarityMatch.addContainerSimilarityMatch(rootSimilarityMatch);
						rootSimilarityMatch.addContentSimilarityMatch(similarityMatch);
					}
					generatorContext.add(similarityMatch);
					if(leftEReference.getEType() instanceof EClass && rightEReference.getEType() instanceof EClass){
						new EClassMatchGenerator().generateMatches(generatorContext, similarityMatch, Lists.newArrayList((EClass)leftEReference.getEType()), Lists.newArrayList((EClass)rightEReference.getEType()));
					}
				}
			}
		}
	}
}
