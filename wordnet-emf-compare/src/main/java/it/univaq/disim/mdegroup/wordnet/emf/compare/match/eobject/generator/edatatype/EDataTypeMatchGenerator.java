package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.edatatype;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.compare.CompareFactory;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EPackage;

import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.SimilarityMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.MatchGenerator;

public class EDataTypeMatchGenerator implements MatchGenerator<SimilarityMatch, EDataType> {
	
//	private Double contentDistanceWeight = 0.025d; 
//	private Double containerDistanceWeight = 0.025d; 
//	private Double semanticDistanceWeight = 0.95d; 
//	private Double distanceThreshold = 0.3d;

	private Double contentDistanceWeight = 0.025d; 
	private Double containerDistanceWeight = 0.025d; 
	private Double semanticDistanceWeight = 0.95d; 
	private Double distanceThreshold = -1.0d;

	@Override
	public void generateMatches(Set<SimilarityMatch> generatorContext, SimilarityMatch rootSimilarityMatch, List<EDataType> leftElements, List<EDataType> rightElements) {
		for(EDataType leftEDataType : leftElements){
			for(EDataType rightEDataType : rightElements){
				SimilarityMatch similarityMatch = new SimilarityMatch();
				
				similarityMatch.setContainerDistanceWeight(this.containerDistanceWeight);
				similarityMatch.setContentDistanceWeight(this.contentDistanceWeight);
				similarityMatch.setSemanticDistanceWeight(this.semanticDistanceWeight);
				similarityMatch.setDistanceThreshold(this.distanceThreshold);
				
				similarityMatch.setMatch(CompareFactory.eINSTANCE.createMatch());
				similarityMatch.getMatch().setLeft(leftEDataType);
				similarityMatch.getMatch().setRight(rightEDataType);
				similarityMatch.getMatch().setOrigin(null);
				List<SimilarityMatch> existingSimilarityMatches = generatorContext.stream()
						.filter(existingSimilarityMatch -> existingSimilarityMatch.equals(similarityMatch))
						.collect(Collectors.toList()); 
				if(existingSimilarityMatches.size() > 0){
					SimilarityMatch existingSimilarityMatch = existingSimilarityMatches.get(0); 
					/* EPackage */
					if(!existingSimilarityMatch.equals(rootSimilarityMatch)
							&& ((rootSimilarityMatch.getMatch().getLeft() != null && rootSimilarityMatch.getMatch().getLeft() instanceof EPackage) || (rootSimilarityMatch.getMatch().getLeft() == null))
							&& ((rootSimilarityMatch.getMatch().getRight() != null && rootSimilarityMatch.getMatch().getRight() instanceof EPackage) || (rootSimilarityMatch.getMatch().getRight() == null))){
						existingSimilarityMatch.addContainerSimilarityMatch(rootSimilarityMatch);
						rootSimilarityMatch.addContentSimilarityMatch(existingSimilarityMatch); 
					}
				} else {
					/* EPackage */
					if(((rootSimilarityMatch.getMatch().getLeft() != null && rootSimilarityMatch.getMatch().getLeft() instanceof EPackage) || (rootSimilarityMatch.getMatch().getLeft() == null))
							&& ((rootSimilarityMatch.getMatch().getRight() != null && rootSimilarityMatch.getMatch().getRight() instanceof EPackage) || (rootSimilarityMatch.getMatch().getRight() == null))){
						similarityMatch.addContainerSimilarityMatch(rootSimilarityMatch);
						rootSimilarityMatch.addContentSimilarityMatch(similarityMatch);
					}
					generatorContext.add(similarityMatch);
				}
			}
		}
	}

}
