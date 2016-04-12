package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.eenum.eenumliteral;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;

import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.WordNetMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.EObjectExplorer;

public class EEnumLiteralExplorer implements EObjectExplorer<WordNetMatch, EEnumLiteral>{

	@Override
	public void generateMatches(Set<WordNetMatch> context,
			WordNetMatch rootMatch, List<EEnumLiteral> leftElements,
			List<EEnumLiteral> rightElements) {
		for(EEnumLiteral leftEEnumLiteral : leftElements){
			for(EEnumLiteral rightEEnumLiteral : rightElements){
				WordNetMatch currentMatch = new WordNetMatch();
				currentMatch.setLeft(leftEEnumLiteral);
				currentMatch.setRight(rightEEnumLiteral);
				currentMatch.setOrigin(null);
				List<WordNetMatch> existingMatches = context.stream()
						.filter(existingMatch -> existingMatch.equals(currentMatch))
						.collect(Collectors.toList());
				if(existingMatches.size() > 0){
					WordNetMatch existingMatch = existingMatches.get(0); 
					/* EEnum */
					if(!existingMatch.equals(rootMatch) 
							&& ((rootMatch.getLeft() != null && rootMatch.getLeft() instanceof EEnum) || (rootMatch.getLeft() == null))
							&& ((rootMatch.getRight() != null && rootMatch.getRight() instanceof EEnum) || (rootMatch.getRight() == null))){
						existingMatch.addContainerMatch(rootMatch);
						rootMatch.addContentMatch(existingMatch);
					}
				} else {
					/* EEnum */
					if(((rootMatch.getLeft() != null && rootMatch.getLeft() instanceof EEnum)||(rootMatch.getLeft() == null)) 
							&& ((rootMatch.getRight() != null && rootMatch.getRight() instanceof EEnum) || (rootMatch.getRight() == null))){
						currentMatch.addContainerMatch(rootMatch);
						rootMatch.addContentMatch(currentMatch);
					}
					context.add(currentMatch);
				}
			}
		}
	}
}