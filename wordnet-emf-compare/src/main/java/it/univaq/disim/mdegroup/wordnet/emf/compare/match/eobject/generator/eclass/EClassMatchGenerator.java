package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.eclass;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.compare.CompareFactory;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EGenericTypeImpl;

import com.google.common.collect.Lists;

import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.SimilarityMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.MatchGenerator;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.eattribute.EAttributeMatchGenerator;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.ereference.EReferenceMatchGenerator;

public class EClassMatchGenerator implements MatchGenerator<SimilarityMatch, EClass> {
	
	private Double contentDistanceWeight = 0.05d; 
	private Double containerDistanceWeight = 0.0d; 
	private Double semanticDistanceWeight = 0.95d; 
	private Double distanceThreshold = 0.3d;

	@Override
	public void generateMatches(Set<SimilarityMatch> generatorContext, SimilarityMatch rootSimilarityMatch, List<EClass> leftElements, List<EClass> rightElements) {
		for(EClass leftEClass : leftElements){
			for(EClass rightEClass : rightElements){
				SimilarityMatch similarityMatch = new SimilarityMatch();
				
				similarityMatch.setContainerDistanceWeight(this.containerDistanceWeight);
				similarityMatch.setContentDistanceWeight(this.contentDistanceWeight);
				similarityMatch.setSemanticDistanceWeight(this.semanticDistanceWeight);
				similarityMatch.setDistanceThreshold(this.distanceThreshold);
				
				similarityMatch.setMatch(CompareFactory.eINSTANCE.createMatch());
				similarityMatch.getMatch().setLeft(leftEClass);
				similarityMatch.getMatch().setRight(rightEClass);
				similarityMatch.getMatch().setOrigin(null);
				List<SimilarityMatch> existingSimilarityMatches = generatorContext.stream()
						.filter(existingSimilarityMatch -> existingSimilarityMatch.equals(similarityMatch))
						.collect(Collectors.toList());
				if(existingSimilarityMatches.size() > 0){
					SimilarityMatch existingSimilarityMatch = existingSimilarityMatches.get(0);
					/* EPackages */
					if(!existingSimilarityMatch.equals(rootSimilarityMatch) 
							&& ((rootSimilarityMatch.getMatch().getLeft() != null && rootSimilarityMatch.getMatch().getLeft() instanceof EPackage) || (rootSimilarityMatch.getMatch().getLeft() == null))
							&& ((rootSimilarityMatch.getMatch().getRight() != null && rootSimilarityMatch.getMatch().getRight() instanceof EPackage) || (rootSimilarityMatch.getMatch().getRight() == null))){
						existingSimilarityMatch.addContainerSimilarityMatch(rootSimilarityMatch);
						rootSimilarityMatch.addContentSimilarityMatch(existingSimilarityMatch);
					}
				} else {
					/* EPackages */
					if(((rootSimilarityMatch.getMatch().getLeft() != null && rootSimilarityMatch.getMatch().getLeft() instanceof EPackage) || (rootSimilarityMatch.getMatch().getLeft() == null))
							&& ((rootSimilarityMatch.getMatch().getRight() != null && rootSimilarityMatch.getMatch().getRight() instanceof EPackage) || (rootSimilarityMatch.getMatch().getRight() == null))){
						similarityMatch.addContainerSimilarityMatch(rootSimilarityMatch);
						rootSimilarityMatch.addContentSimilarityMatch(similarityMatch);
					}
					generatorContext.add(similarityMatch);
					/* EAttributes, EReferences */
					List<EAttribute> leftEAttributes = Lists.newArrayList();
					List<EReference> leftEReferences = Lists.newArrayList();
					this.collectContent(leftEClass.eContents(), leftEAttributes, leftEReferences);
					List<EAttribute> rightEAttributes = Lists.newArrayList();
					List<EReference> rightEReferences = Lists.newArrayList();
					this.collectContent(rightEClass.eContents(), rightEAttributes, rightEReferences);
					if(leftEAttributes.size() > 0 && rightEAttributes.size() > 0){
						new EAttributeMatchGenerator().generateMatches(generatorContext, similarityMatch, leftEAttributes, rightEAttributes);
					}
					if(leftEReferences.size() > 0 && rightEReferences.size() > 0){
						new EReferenceMatchGenerator().generateMatches(generatorContext, similarityMatch, leftEReferences, rightEReferences);
					}
				}
			}
		}
	}
	
	private void collectContent(List<EObject> source, List<EAttribute> eAttributes, List<EReference> eReferences){
		source.stream()
			.forEach(sourceElement -> {
				if(sourceElement instanceof EAttribute){
					eAttributes.add((EAttribute)sourceElement);
				} else if(sourceElement instanceof EReference){
					eReferences.add((EReference)sourceElement);
				} else if(sourceElement instanceof EGenericTypeImpl){
					if(((EGenericTypeImpl)sourceElement).basicGetEClassifier() instanceof EClass){
						collectContent((((EGenericTypeImpl)sourceElement).basicGetEClassifier()).eContents(), eAttributes, eReferences);
					}
				}
			});
	}

}
