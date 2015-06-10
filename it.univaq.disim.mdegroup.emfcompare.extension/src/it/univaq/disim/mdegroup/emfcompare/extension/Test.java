package it.univaq.disim.mdegroup.emfcompare.extension;

import it.univaq.disim.mdegroup.emfcompare.extension.match.impl.SemanticMatchEngineFactoryRegistryImpl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryRegistryImpl;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import com.google.common.collect.Lists;

/** TODO : Documentation */

public class Test {

	private static IComparisonScope buildComparisonScope(String uri1, String uri2){
		ResourceSet resourceSet1 = new ResourceSetImpl();
		ResourceSet resourceSet2 = new ResourceSetImpl();
		resourceSet1.getResource(URI.createFileURI(uri1), true);
		resourceSet2.getResource(URI.createFileURI(uri2), true);
		return new DefaultComparisonScope(resourceSet1, resourceSet2, null);
	}
	
	private static IMatchEngine.Factory.Registry buildMatchEngineFactoryRegistry(boolean semanticFlag){
		return semanticFlag ? SemanticMatchEngineFactoryRegistryImpl.createStandaloneInstance() : MatchEngineFactoryRegistryImpl.createStandaloneInstance();
	}
	
	private static Comparison executeComparison(IMatchEngine.Factory.Registry registry, IComparisonScope scope){
		return EMFCompare.builder().setMatchEngineFactoryRegistry(registry).build().compare(scope);
	}
	
	private static double evaluateComparisonResult(Comparison comparison){
		List<Match> matches = comparison.getMatches();
		int total = matches.size();
		int counter = 0;
		for (Match match : matches) {
			List<Match> lm = Lists.newArrayList(match.getAllSubmatches());
			total += lm.size();
			for (Match match2 : lm) {
				if (match2.getLeft() != null && match2.getRight() != null) {
					counter++;
				}
			}
			if (match.getLeft() != null && match.getRight() != null)
				counter++;
		}
		double resultValue = total != 0 ? new BigDecimal(((double)counter/total)).round(new MathContext(10)).doubleValue() : 0.0; 
		return resultValue; 
	}
	
	private static double unidirectionalComparation(String first, String second, boolean semanticFlag){
		IComparisonScope comparisonScope = buildComparisonScope(first, second);
		IMatchEngine.Factory.Registry registry = buildMatchEngineFactoryRegistry(semanticFlag);
		Comparison comparison = executeComparison(registry, comparisonScope);
		return evaluateComparisonResult(comparison);
	}
	
	private static double bidirectionalComparation(String first, String second, boolean semanticFlag){
		 return new BigDecimal((unidirectionalComparation(first, second, semanticFlag) + unidirectionalComparation(second, first, semanticFlag)) / 2.0).round(new MathContext(10)).doubleValue();
	}
	
	public final static void main(String[] args) {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		String bibtex = "metamodels/BibTeX.xmi";
		String publication = "metamodels/Publication.xmi";
		boolean semanticFlag = true; 
		/* BibTeX - Publication */
		double semanticBidirectionalComparationValue = bidirectionalComparation(bibtex, publication, semanticFlag);
		double defaultBidirectionalComparationValue = bidirectionalComparation(bibtex, publication, !semanticFlag); 
		System.out.printf("[SEMANTIC] BibTeX - Publication : %.2f\n", semanticBidirectionalComparationValue);
		System.out.printf("[DEFAULT] BibTeX - Publication : %.2f\n\n", defaultBidirectionalComparationValue); 
		/* Publication - Publication */
		semanticBidirectionalComparationValue = bidirectionalComparation(publication, publication, semanticFlag);
		defaultBidirectionalComparationValue = bidirectionalComparation(publication, publication, !semanticFlag); 
		System.out.printf("[SEMANTIC] Publication - Publication : %.2f\n", semanticBidirectionalComparationValue);
		System.out.printf("[DEFAULT] Publication - Publication : %.2f\n\n", defaultBidirectionalComparationValue);
		/* BibTeX - BibTeX */
		semanticBidirectionalComparationValue = bidirectionalComparation(bibtex, bibtex, semanticFlag);
		defaultBidirectionalComparationValue = bidirectionalComparation(bibtex, bibtex, !semanticFlag);
		System.out.printf("[SEMANTIC] BibTeX - BibTeX : %.2f\n", semanticBidirectionalComparationValue);
		System.out.printf("[DEFAULT] BibTeX - BibTeX : %.2f\n", defaultBidirectionalComparationValue);
	}
	
	
}
 