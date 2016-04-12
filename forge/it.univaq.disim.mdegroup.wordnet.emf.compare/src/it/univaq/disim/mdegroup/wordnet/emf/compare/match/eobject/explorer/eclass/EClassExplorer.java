package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.eclass;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EGenericTypeImpl;

import com.google.common.collect.Lists;

import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.WordNetMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.EObjectExplorer;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.eattribute.EAttributeExplorer;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.ereference.EReferenceExplorer;

public class EClassExplorer implements EObjectExplorer<WordNetMatch, EClass> {

	@Override
	public void generateMatches(Set<WordNetMatch> context,
			WordNetMatch rootMatch, List<EClass> leftElements,
			List<EClass> rightElements) {
		for(EClass leftEClass : leftElements){
			for(EClass rightEClass : rightElements){
				WordNetMatch currentMatch = new WordNetMatch();
				currentMatch.setLeft(leftEClass);
				currentMatch.setRight(rightEClass);
				currentMatch.setOrigin(null);
				List<WordNetMatch> existingMatches = context.stream()
						.filter(existingMatch -> existingMatch.equals(currentMatch))
						.collect(Collectors.toList());
				if(existingMatches.size() > 0){
					WordNetMatch existingMatch = existingMatches.get(0); 
					/* EPackage */
					if(!existingMatch.equals(rootMatch) 
							&& ((rootMatch.getLeft() != null && rootMatch.getLeft() instanceof EPackage) || (rootMatch.getLeft() == null))
							&& ((rootMatch.getRight() != null && rootMatch.getRight() instanceof EPackage) || (rootMatch.getRight() == null))){
						existingMatch.addContainerMatch(rootMatch);
						rootMatch.addContentMatch(existingMatch);
					}
				} else {
					/* EPackage */
					if(((rootMatch.getLeft() != null && rootMatch.getLeft() instanceof EPackage)||(rootMatch.getLeft() == null)) 
							&& ((rootMatch.getRight() != null && rootMatch.getRight() instanceof EPackage) || (rootMatch.getRight() == null))){
						currentMatch.addContainerMatch(rootMatch);
						rootMatch.addContentMatch(currentMatch);
					}
					context.add(currentMatch);
					/* EAttributes, EReferences */
					List<EAttribute> leftEAttributes = Lists.newArrayList();
					List<EReference> leftEReferences = Lists.newArrayList();
					this.collectContent(leftEClass.eContents(), leftEAttributes, leftEReferences);
					List<EAttribute> rightEAttributes = Lists.newArrayList();
					List<EReference> rightEReferences = Lists.newArrayList();
					this.collectContent(rightEClass.eContents(), rightEAttributes, rightEReferences);
					if(leftEAttributes.size() > 0 && rightEAttributes.size() > 0){
						new EAttributeExplorer().generateMatches(context, rootMatch, leftEAttributes, rightEAttributes);
					}
					if(leftEReferences.size() > 0 && rightEReferences.size() > 0){
						new EReferenceExplorer().generateMatches(context, rootMatch, leftEReferences, rightEReferences);
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
