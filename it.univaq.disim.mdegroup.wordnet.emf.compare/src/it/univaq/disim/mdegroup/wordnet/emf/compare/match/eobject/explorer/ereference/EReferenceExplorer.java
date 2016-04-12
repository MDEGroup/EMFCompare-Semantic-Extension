package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.ereference;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import com.google.common.collect.Lists;

import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.WordNetMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.EObjectExplorer;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.eclass.EClassExplorer;

public class EReferenceExplorer implements EObjectExplorer<WordNetMatch, EReference>{

	@Override
	public void generateMatches(Set<WordNetMatch> context,
			WordNetMatch rootMatch, List<EReference> leftElements,
			List<EReference> rightElements) {
		for(EReference leftEReference : leftElements){
			for(EReference rightEReference : rightElements){
				WordNetMatch currentMatch = new WordNetMatch();
				currentMatch.setLeft(leftEReference);
				currentMatch.setRight(rightEReference);
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
					if(leftEReference.getEType() instanceof EClass && rightEReference.getEType() instanceof EClass){
						new EClassExplorer().generateMatches(context, rootMatch, Lists.newArrayList((EClass)leftEReference.getEType()), Lists.newArrayList((EClass)rightEReference.getEType()));
					}
				}
			}
		}
	}
}