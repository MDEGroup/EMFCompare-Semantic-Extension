package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.eattribute;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.compare.CompareFactory;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;

import com.google.common.collect.Lists;

import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.SimilarityMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.MatchGenerator;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.edatatype.EDataTypeMatchGenerator;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.eenum.EEnumMatchGenerator;

public class EAttributeMatchGenerator implements MatchGenerator<SimilarityMatch, EAttribute> {
	
//	private Double contentDistanceWeight = 0.0d; 
//	private Double containerDistanceWeight = 0.0d; 
//	private Double semanticDistanceWeight = 1.0d; 
//	private Double distanceThreshold = 0.25d;

	private Double contentDistanceWeight = 0.0d; 
	private Double containerDistanceWeight = 0.3d; 
	private Double semanticDistanceWeight = 0.7d; 
	private Double distanceThreshold = 0.1d;
	
	@Override
	public void generateMatches(Set<SimilarityMatch> generatorContext, SimilarityMatch rootSimilarityMatch, List<EAttribute> leftElements, List<EAttribute> rightElements) {
		for(EAttribute leftEAttribute : leftElements){
			for(EAttribute rightEAttribute : rightElements){
				SimilarityMatch similarityMatch = new SimilarityMatch();
				similarityMatch.setContainerDistanceWeight(this.containerDistanceWeight);
				similarityMatch.setContentDistanceWeight(this.contentDistanceWeight);
				similarityMatch.setSemanticDistanceWeight(this.semanticDistanceWeight);
				similarityMatch.setDistanceThreshold(this.distanceThreshold);
				similarityMatch.setMatch(CompareFactory.eINSTANCE.createMatch());
				similarityMatch.getMatch().setLeft(leftEAttribute); 
				similarityMatch.getMatch().setRight(rightEAttribute);
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
					if(leftEAttribute.getEType() instanceof EEnum && rightEAttribute.getEType() instanceof EEnum){
						new EEnumMatchGenerator().generateMatches(generatorContext, similarityMatch, Lists.newArrayList((EEnum)leftEAttribute.getEType()), Lists.newArrayList((EEnum)rightEAttribute.getEType()));
					} else if(leftEAttribute.getEType() instanceof EDataType && rightEAttribute.getEType() instanceof EDataType){
						new EDataTypeMatchGenerator().generateMatches(generatorContext, similarityMatch, Lists.newArrayList((EDataType)leftEAttribute.getEType()), Lists.newArrayList((EDataType)rightEAttribute.getEType()));
					}
				}
			}
		}
	}

}
