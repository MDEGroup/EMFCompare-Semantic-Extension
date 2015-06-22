package it.univaq.disim.mdegroup.emfcompare.extension;

import it.univaq.disim.mdegroup.emfcompare.extension.match.impl.SemanticMatchEngineFactoryRegistryImpl;

import java.io.IOException;
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
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import com.google.common.collect.Lists;

/** TODO : Documentation */
/** TODO : Documentation */

public class Test {

	private static IComparisonScope buildComparisonScope(String uri1,
			String uri2) {
		ResourceSet resourceSet1 = new ResourceSetImpl();
		ResourceSet resourceSet2 = new ResourceSetImpl();
		resourceSet1.getResource(URI.createFileURI(uri1), true);
		resourceSet2.getResource(URI.createFileURI(uri2), true);
		return new DefaultComparisonScope(resourceSet1, resourceSet2, null);
	}

	private static IMatchEngine.Factory.Registry buildMatchEngineFactoryRegistry(
			boolean semanticFlag) {
		return semanticFlag ? SemanticMatchEngineFactoryRegistryImpl
				.createStandaloneInstance() : MatchEngineFactoryRegistryImpl
				.createStandaloneInstance();
	}

	private static Comparison executeComparison(
			IMatchEngine.Factory.Registry registry, IComparisonScope scope) {
		return EMFCompare.builder().setMatchEngineFactoryRegistry(registry)
				.build().compare(scope);
	}

	private static int countMatches(Match match) {
		List<Match> submatches = match.getSubmatches();
		int totalMatches = submatches.size();
		for (Match submatch : submatches) {
			if (submatch.getLeft() != null && submatch.getRight() != null) {
				System.out.print(" " + submatch.getLeft().getClass().getName()
						+ "." + ((ENamedElement) submatch.getLeft()).getName()
						+ " - ");
				System.out
						.println(submatch.getRight().getClass().getName()
								+ "."
								+ ((ENamedElement) submatch.getRight())
										.getName());
			}
			totalMatches += countMatches(submatch);
		}
		return totalMatches;
	}

	private static void countMatches(Comparison comparison) {
		List<Match> matches = comparison.getMatches();
		int totalMatches = matches.size();
		for (Match match : matches) {
			if (match.getLeft() != null && match.getRight() != null) {
				System.out.print(match.getLeft().getClass().getName() + "."
						+ ((ENamedElement) match.getLeft()).getName() + " - ");
				System.out.println(match.getRight().getClass().getName() + "."
						+ ((ENamedElement) match.getRight()).getName());
			}
			totalMatches += countMatches(match);
		}
		System.out.println("Total Matches : " + totalMatches);
	}

	private static double evaluateComparisonResult(Comparison comparison) {
		countMatches(comparison);
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
			if (match.getLeft() != null && match.getRight() != null) {
				counter++;
			}
		}
		return total != 0 ? (double) counter / total : counter;
	}

	private static double unidirectionalComparation(String first,
			String second, boolean semanticFlag) {
		IComparisonScope comparisonScope = buildComparisonScope(first, second);
		IMatchEngine.Factory.Registry registry = buildMatchEngineFactoryRegistry(semanticFlag);
		Comparison comparison = executeComparison(registry, comparisonScope);
		return evaluateComparisonResult(comparison);
	}

	// private static double bidirectionalComparation(String first, String
	// second,
	// boolean semanticFlag) {
	// return new BigDecimal((unidirectionalComparation(first, second,
	// semanticFlag) + unidirectionalComparation(second, first,
	// semanticFlag)) / 2.0).round(new MathContext(10)).doubleValue();
	// }

	public final static void main(String[] args) throws IOException {

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				"xmi", new XMIResourceFactoryImpl());

		String docBook = "metamodels/DocBook.xmi";
		String book = "metamodels/Book.xmi";
		String bibtex = "metamodels/BibTeX.xmi";
		String publication = "metamodels/Publication.xmi";

		boolean semanticFlag = true;

		double semanticDocBookPublication = unidirectionalComparation(docBook,
				publication, semanticFlag);
		double defaultDocBookPublication = unidirectionalComparation(docBook,
				publication, !semanticFlag);
		System.out.printf("[SEMANTIC] DOCBOOK - PUBLICATION : %.2f\n",
				semanticDocBookPublication);
		System.out.printf("[DEFAULT] DOCBOOK - PUBLICATION : %.2f\n\n",
				defaultDocBookPublication);

		// 0.47, 0.22
		double semanticDocBookBook = unidirectionalComparation(docBook, book,
				semanticFlag);
		double defaultDocBookBook = unidirectionalComparation(docBook, book,
				!semanticFlag);
		System.out.printf("[SEMANTIC] DOCBOOK - BOOK : %.2f\n",
				semanticDocBookBook);
		System.out.printf("[DEFAULT] DOCBOOK - BOOK : %.2f\n\n",
				defaultDocBookBook);

		// 32, 16
		double semanticBibTeXBook = unidirectionalComparation(bibtex, book,
				semanticFlag);
		double defaultBibTeXBook = unidirectionalComparation(bibtex, book,
				!semanticFlag);
		System.out.printf("[SEMANTIC] BIBTEX - BOOK : %.2f\n",
				semanticBibTeXBook);
		System.out.printf("[DEFAULT] BIBTEX - BOOK : %.2f\n\n",
				defaultBibTeXBook);

		// [S:0.69,D:0.22]
		double semanticPublicationBook = unidirectionalComparation(publication,
				book, semanticFlag);
		double defaultPublicationBook = unidirectionalComparation(bibtex, book,
				!semanticFlag);
		System.out.printf("[SEMANTIC] PUBLICATION - BOOK : %.2f\n",
				semanticPublicationBook);
		System.out.printf("[DEFAULT] PUBLICATION - BOOK : %.2f\n\n",
				defaultPublicationBook);
		// 12, 21
		double semanticBibTeXPublication = unidirectionalComparation(bibtex,
				publication, semanticFlag);
		double defaultBibTeXPublication = unidirectionalComparation(bibtex,
				publication, !semanticFlag);
		System.out.printf("[SEMANTIC] BibTeX - Publication : %.2f\n",
				semanticBibTeXPublication);
		System.out.printf("[DEFAULT] BibTeX - Publication : %.2f\n\n",
				defaultBibTeXPublication);
	}
}
