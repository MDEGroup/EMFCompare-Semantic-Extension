package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.epackage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;

import com.google.common.collect.Lists;

import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.WordNetMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.EObjectExplorer;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.eclass.EClassExplorer;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.edatatype.EDataTypeExplorer;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer.eenum.EEnumExplorer;

public class EPackageExplorer implements EObjectExplorer<WordNetMatch, EPackage> {

	@Override
	public void generateMatches(Set<WordNetMatch> context,
			WordNetMatch rootMatch, List<EPackage> leftElements,
			List<EPackage> rightElements) {
		for(EPackage leftEPackage : leftElements){
			for(EPackage rightEPackage : rightElements){
				WordNetMatch currentMatch = new WordNetMatch();
				currentMatch.setLeft(leftEPackage);
				currentMatch.setRight(rightEPackage);
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
						new EDataTypeExplorer().generateMatches(context, rootMatch, leftEDataTypes, rightEDataTypes);
					}
					if(leftEEnums.size() > 0 && rightEEnums.size() > 0){
						new EEnumExplorer().generateMatches(context, rootMatch, leftEEnums, rightEEnums);
					}
					if(leftEClasses.size() > 0 && rightEClasses.size() > 0){
						new EClassExplorer().generateMatches(context, rootMatch, leftEClasses, rightEClasses);
					}
					if(leftEPackages.size() > 0){
						if(rightEPackages.size() > 0){
							leftEPackages.add(leftEPackage);
							rightEPackages.add(rightEPackage);
							this.generateMatches(context, rootMatch, leftEPackages, rightEPackages);
						} else {
							rightEPackages.add(rightEPackage);
							this.generateMatches(context, rootMatch, leftEPackages, rightEPackages);
						}
					} else {
						if(rightEPackages.size() > 0){
							leftEPackages.add(leftEPackage);
							this.generateMatches(context, rootMatch, leftEPackages, rightEPackages);
						}
					}
				}
			}
		}
	}
}