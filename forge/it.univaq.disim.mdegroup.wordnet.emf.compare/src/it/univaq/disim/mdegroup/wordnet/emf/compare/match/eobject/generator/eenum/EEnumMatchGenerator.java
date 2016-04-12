package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.eenum;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.compare.CompareFactory;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EPackage;

import it.univaq.disim.mdegroup.wordnet.emf.compare.impl.SimilarityMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.MatchGenerator;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.eenum.eenumliteral.EEnumLiteralMatchGenerator;

public class EEnumMatchGenerator implements MatchGenerator<SimilarityMatch, EEnum> {
	
//	private Double contentDistanceWeight = 0.025d; 
//	private Double containerDistanceWeight = 0.025d; 
//	private Double semanticDistanceWeight = 0.95d; 
//	private Double distanceThreshold = 0.3d;

	private Double contentDistanceWeight = 0.2d; 
	private Double containerDistanceWeight = 0.0d; 
	private Double semanticDistanceWeight = 0.8d; 
	private Double distanceThreshold = 0.2d;

	@Override
	public void generateMatches(Set<SimilarityMatch> generatorContext, SimilarityMatch rootSimilarityMatch, List<EEnum> leftElements, List<EEnum> rightElements) {
		for(EEnum leftEEnum : leftElements){
			for(EEnum rightEEnum : rightElements){
				SimilarityMatch similarityMatch = new SimilarityMatch();
				similarityMatch.setContainerDistanceWeight(this.containerDistanceWeight);
				similarityMatch.setContentDistanceWeight(this.contentDistanceWeight);
				similarityMatch.setSemanticDistanceWeight(this.semanticDistanceWeight);
				similarityMatch.setDistanceThreshold(this.distanceThreshold);
				similarityMatch.setMatch(CompareFactory.eINSTANCE.createMatch());
				similarityMatch.getMatch().setLeft(leftEEnum);
				similarityMatch.getMatch().setRight(rightEEnum);
				similarityMatch.getMatch().setOrigin(null);
				List<SimilarityMatch> existingSimilarityMatches = generatorContext.stream()
						.filter(existingSimilarityMatch -> existingSimilarityMatch.equals(similarityMatch))
						.collect(Collectors.toList());
				if(existingSimilarityMatches.size() > 0){
					SimilarityMatch existingSimilarityMatch = existingSimilarityMatches.get(0);
					/* EPackage */
					if(!existingSimilarityMatch.equals(similarityMatch)
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
					/* EEnum Literals */
					List<EEnumLiteral> leftEEnumLiterals = leftEEnum.eContents().stream().filter(element -> element instanceof EEnumLiteral).map(EEnumLiteral.class::cast).collect(Collectors.toList()); 
					List<EEnumLiteral> rightEEnumLiterals = rightEEnum.eContents().stream().filter(element -> element instanceof EEnumLiteral).map(EEnumLiteral.class::cast).collect(Collectors.toList());
					
					if(leftEEnumLiterals.size() > 0 && rightEEnumLiterals.size() > 0){
						new EEnumLiteralMatchGenerator().generateMatches(generatorContext, similarityMatch, leftEEnumLiterals, rightEEnumLiterals);
					}
				}
			}
		}
	}

}
