package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.eattribute;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;

import com.google.common.collect.Lists;

import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.WordNetMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.EObjectExplorer;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.edatatype.EDataTypeExplorer;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.eenum.EEnumExplorer;

public class EAttributeExplorer implements EObjectExplorer<WordNetMatch, EAttribute>{

	@Override
	public void generateMatches(Set<WordNetMatch> context,
			WordNetMatch rootMatch, List<EAttribute> leftElements,
			List<EAttribute> rightElements) {
		for(EAttribute leftEAttribute : leftElements){
			for(EAttribute rightEAttribute : rightElements){
				WordNetMatch currentMatch = new WordNetMatch();
				currentMatch.setLeft(leftEAttribute);
				currentMatch.setRight(rightEAttribute);
				currentMatch.setOrigin(null);
				List<WordNetMatch> existingMatches = context.stream()
						.filter(existingMatch -> existingMatch.equals(currentMatch))
						.collect(Collectors.toList());
				if(existingMatches.size() > 0){
					WordNetMatch existingMatch = existingMatches.get(0); 
					/* EClass */
					if(!existingMatch.equals(rootMatch) 
							&& ((rootMatch.getLeft() != null && rootMatch.getLeft() instanceof EClass) || (rootMatch.getLeft() == null))
							&& ((rootMatch.getRight() != null && rootMatch.getRight() instanceof EClass) || (rootMatch.getRight() == null))){
						existingMatch.addContainerMatch(rootMatch);
						rootMatch.addContentMatch(existingMatch);
					}
				} else {
					/* EClass */
					if(((rootMatch.getLeft() != null && rootMatch.getLeft() instanceof EClass)||(rootMatch.getLeft() == null)) 
							&& ((rootMatch.getRight() != null && rootMatch.getRight() instanceof EClass) || (rootMatch.getRight() == null))){
						currentMatch.addContainerMatch(rootMatch);
						rootMatch.addContentMatch(currentMatch);
					}
					context.add(currentMatch);
					/* EEnum, EDataType */
					if(leftEAttribute.getEType() instanceof EEnum && rightEAttribute.getEType() instanceof EEnum){
						new EEnumExplorer().generateMatches(context, rootMatch, Lists.newArrayList((EEnum)leftEAttribute.getEType()), Lists.newArrayList((EEnum)rightEAttribute.getEType()));
					} else if(leftEAttribute.getEType() instanceof EDataType && rightEAttribute.getEType() instanceof EDataType){
						new EDataTypeExplorer().generateMatches(context, rootMatch, Lists.newArrayList((EDataType)leftEAttribute.getEType()), Lists.newArrayList((EDataType)rightEAttribute.getEType()));
					}
				}
			}
		}
	}
}
