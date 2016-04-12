package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.eenum;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EPackage;

import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.WordNetMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.EObjectExplorer;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.eenum.eenumliteral.EEnumLiteralExplorer;

public class EEnumExplorer implements EObjectExplorer<WordNetMatch, EEnum>{

	@Override
	public void generateMatches(Set<WordNetMatch> context,
			WordNetMatch rootMatch, List<EEnum> leftElements,
			List<EEnum> rightElements) {
		for(EEnum leftEEnum : leftElements){
			for(EEnum rightEEnum : rightElements){
				WordNetMatch currentMatch = new WordNetMatch();
				currentMatch.setLeft(leftEEnum);
				currentMatch.setRight(rightEEnum);
				currentMatch.setOrigin(null);
				List<WordNetMatch> existingMatches = context.stream().filter(existingMatch -> existingMatch.equals(currentMatch)).collect(Collectors.toList());
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
					/* EEnumLiterals */
					List<EEnumLiteral> leftEEnumLiterals = leftEEnum.eContents().stream().filter(element -> element instanceof EEnumLiteral).map(EEnumLiteral.class::cast).collect(Collectors.toList());
					List<EEnumLiteral> rightEEnumLiterals = rightEEnum.eContents().stream().filter(element -> element instanceof EEnumLiteral).map(EEnumLiteral.class::cast).collect(Collectors.toList());
					if(leftEEnumLiterals.size() > 0 && rightEEnumLiterals.size() > 0){
						new EEnumLiteralExplorer().generateMatches(context, rootMatch, leftEEnumLiterals, rightEEnumLiterals);;
					}
				}
			}
		}
	}
}