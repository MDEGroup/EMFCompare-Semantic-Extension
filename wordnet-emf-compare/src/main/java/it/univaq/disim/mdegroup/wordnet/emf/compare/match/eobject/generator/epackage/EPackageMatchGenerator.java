package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.epackage;

import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.SimilarityMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.MatchGenerator;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.eclass.EClassMatchGenerator;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.edatatype.EDataTypeMatchGenerator;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.eenum.EEnumMatchGenerator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.compare.CompareFactory;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;

import com.google.common.collect.Lists;

public class EPackageMatchGenerator implements MatchGenerator<SimilarityMatch, EPackage> {
	
//	private Double contentDistanceWeight = 0.025d; 
//	private Double containerDistanceWeight = 0.025d; 
//	private Double semanticDistanceWeight = 0.95d; 
//	private Double distanceThreshold = 0.3d;

	private Double contentDistanceWeight = 0.0d; 
	private Double containerDistanceWeight = 0.0d; 
	private Double semanticDistanceWeight = 1.0d; 
	private Double distanceThreshold = -1.0d;
	
	@Override
	public void generateMatches(Set<SimilarityMatch> generatorContext, SimilarityMatch rootSimilarityMatch, List<EPackage> leftElements, List<EPackage> rightElements) {
		
		for(EPackage leftEPackage : leftElements){
			for(EPackage rightEPackage : rightElements){
				
				SimilarityMatch similarityMatch = new SimilarityMatch();
				similarityMatch.setContainerDistanceWeight(this.containerDistanceWeight);
				similarityMatch.setContentDistanceWeight(this.contentDistanceWeight);
				similarityMatch.setSemanticDistanceWeight(this.semanticDistanceWeight);
				similarityMatch.setDistanceThreshold(this.distanceThreshold);
				similarityMatch.setMatch(CompareFactory.eINSTANCE.createMatch());
				similarityMatch.getMatch().setLeft(leftEPackage);
				similarityMatch.getMatch().setRight(rightEPackage);
				similarityMatch.getMatch().setOrigin(null);
				List<SimilarityMatch> existingSimilarityMatches = generatorContext.stream()
						.filter(existingSimilarityMatch -> existingSimilarityMatch.equals(similarityMatch))
						.collect(Collectors.toList());
				if(existingSimilarityMatches.size() > 0){
					SimilarityMatch existingSimilarityMatch = existingSimilarityMatches.get(0);
					/* EPackage */
					if(!existingSimilarityMatch.equals(rootSimilarityMatch)
							&& rootSimilarityMatch != null
							&& rootSimilarityMatch.getMatch() != null
							&& ((rootSimilarityMatch.getMatch().getLeft() != null && rootSimilarityMatch.getMatch().getLeft() instanceof EPackage) || (rootSimilarityMatch.getMatch().getLeft() == null))
							&& ((rootSimilarityMatch.getMatch().getRight() != null && rootSimilarityMatch.getMatch().getRight() instanceof EPackage) || (rootSimilarityMatch.getMatch().getRight() == null))){
						existingSimilarityMatch.addContainerSimilarityMatch(rootSimilarityMatch);
						rootSimilarityMatch.addContentSimilarityMatch(existingSimilarityMatch);
					}
				} else {
					/* EPackage */
					if((rootSimilarityMatch == null) 
							|| (rootSimilarityMatch != null && rootSimilarityMatch.getMatch() != null 
								&& ((rootSimilarityMatch.getMatch().getLeft() != null && rootSimilarityMatch.getMatch().getLeft() instanceof EPackage) || (rootSimilarityMatch.getMatch().getLeft() == null))
								&& ((rootSimilarityMatch.getMatch().getRight() != null && rootSimilarityMatch.getMatch().getRight() instanceof EPackage) || (rootSimilarityMatch.getMatch().getRight() == null)))){
						similarityMatch.addContainerSimilarityMatch(rootSimilarityMatch);
						rootSimilarityMatch.addContentSimilarityMatch(similarityMatch);
					}
					generatorContext.add(similarityMatch); 
					List<EDataType> leftEDataTypes = Lists.newArrayList();
					List<EEnum> leftEEnums = Lists.newArrayList();
					List<EClass> leftEClasses = Lists.newArrayList();
					List<EPackage> leftEPackages = Lists.newArrayList();
					leftEPackage.eContents().stream()
						.forEach(leftElement -> {
							if(leftElement instanceof EDataType){
								leftEDataTypes.add((EDataType)leftElement);
							} else if(leftElement instanceof EEnum){
								leftEEnums.add((EEnum)leftElement);
							} else if(leftElement instanceof EClass){
								leftEClasses.add((EClass)leftElement);
							} else if(leftElement instanceof EPackage){
								leftEPackages.add((EPackage)leftElement);
							}
						});
					List<EDataType> rightEDataTypes = Lists.newArrayList();
					List<EEnum> rightEEnums = Lists.newArrayList();
					List<EClass> rightEClasses = Lists.newArrayList();
					List<EPackage> rightEPackages = Lists.newArrayList();
					rightEPackage.eContents().stream()
						.forEach(rightElement -> {
							if(rightElement instanceof EDataType){
								rightEDataTypes.add((EDataType)rightElement);
							} else if(rightElement instanceof EEnum){
								rightEEnums.add((EEnum)rightElement);
							} else if(rightElement instanceof EClass){
								rightEClasses.add((EClass)rightElement);
							} else if(rightElement instanceof EPackage){
								rightEPackages.add((EPackage)rightElement);
							}
						});
					if(leftEDataTypes.size() > 0 && rightEDataTypes.size() > 0){
						new EDataTypeMatchGenerator().generateMatches(generatorContext, similarityMatch, leftEDataTypes, rightEDataTypes);
					}
					if(leftEEnums.size() > 0 && rightEEnums.size() > 0){
						new EEnumMatchGenerator().generateMatches(generatorContext, similarityMatch, leftEEnums, rightEEnums);
					}
					if(leftEClasses.size() > 0 && rightEClasses.size() > 0){
						new EClassMatchGenerator().generateMatches(generatorContext, similarityMatch, leftEClasses, rightEClasses);
					}
					if(leftEPackages.size() > 0){
						if(rightEPackages.size() > 0){
							leftEPackages.add(leftEPackage);
							rightEPackages.add(rightEPackage);
							this.generateMatches(generatorContext, similarityMatch, leftEPackages, rightEPackages);
						} else {
							rightEPackages.add(rightEPackage);
							this.generateMatches(generatorContext, similarityMatch, leftEPackages, rightEPackages);
						}
					} else {
						if(rightEPackages.size() > 0){
							leftEPackages.add(leftEPackage);
							this.generateMatches(generatorContext, similarityMatch, leftEPackages, rightEPackages);
						}
					}
					
				}
			}
		}
	}

}
