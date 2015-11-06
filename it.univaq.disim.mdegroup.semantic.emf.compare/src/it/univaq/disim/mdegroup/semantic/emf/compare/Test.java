package it.univaq.disim.mdegroup.semantic.emf.compare;

import it.univaq.disim.mdegroup.semantic.emf.compare.match.impl.SemanticMatchEngineFactoryRegistryImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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

public class Test {

	private static IComparisonScope buildComparisonScope(String uri1,
			String uri2) {
		ResourceSet resourceSet1 = new ResourceSetImpl();
		ResourceSet resourceSet2 = new ResourceSetImpl();
		resourceSet1.getResource(URI.createFileURI(uri1), true);
		resourceSet2.getResource(URI.createFileURI(uri2), true);
		return new DefaultComparisonScope(resourceSet1, resourceSet2, null);
	}

	private static IMatchEngine.Factory.Registry buildMatchEngineFactoryRegistry(boolean semanticFlag) {
		return semanticFlag ? SemanticMatchEngineFactoryRegistryImpl.createStandaloneInstance() : MatchEngineFactoryRegistryImpl.createStandaloneInstance();
	}

	private static Comparison executeComparison(IMatchEngine.Factory.Registry registry, IComparisonScope scope) {
		return EMFCompare.builder().setMatchEngineFactoryRegistry(registry).build().compare(scope);
	}
	
	private static void printComparisonResults(Comparison comparison){
		try {
			FileOutputStream fos = new FileOutputStream("produced" + File.separator + System.currentTimeMillis() + ".txt", true);
			PrintStream printStream = new PrintStream(fos);
			for(Match match : comparison.getMatches()){
				if(match.getLeft() != null && match.getRight() != null){
					System.out.println(((ENamedElement)match.getLeft()).getName() + " [" + match.getLeft().getClass() + "]");
					System.out.println(((ENamedElement)match.getRight()).getName() + " [" + match.getRight().getClass() + "]"); 
					System.out.println();
					
					printStream.println(((ENamedElement)match.getLeft()).getName() + " [" + match.getLeft().getClass() + "]");
					printStream.println(((ENamedElement)match.getRight()).getName() + " [" + match.getRight().getClass() + "]");
					printStream.println();
				}
				for(Match submatch : match.getAllSubmatches()){
					if(submatch.getLeft() != null && submatch.getRight() != null){
						System.out.println("\t" + ((ENamedElement)submatch.getLeft().eContainer()).getName() + " [" + submatch.getLeft().eContainer().getClass() + "]");
						System.out.println("\t\t" + ((ENamedElement)submatch.getLeft()).getName() + " [" + submatch.getLeft().getClass() + "]");
						System.out.println("\t" + ((ENamedElement)submatch.getRight().eContainer()).getName() + "[" + submatch.getRight().eContainer().getClass() + "]");
						System.out.println("\t\t" + ((ENamedElement)submatch.getRight()).getName() + " [" + submatch.getRight().getClass() + "]");
						System.out.println();
						
						printStream.println("\t" + ((ENamedElement)submatch.getLeft().eContainer()).getName() + " [" + submatch.getLeft().eContainer().getClass() + "]");
						printStream.println("\t\t" + ((ENamedElement)submatch.getLeft()).getName() + " [" + submatch.getLeft().getClass() + "]");
						printStream.println("\t" + ((ENamedElement)submatch.getRight().eContainer()).getName() + " [" + submatch.getRight().eContainer().getClass() + "]");
						printStream.println("\t\t" + ((ENamedElement)submatch.getRight()).getName() + " [" + submatch.getRight().getClass() + "]");
						printStream.println();
					}
				}
			}
			printStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static double evaluateComparisonResult(Comparison comparison) {
		List<Match> matches = comparison.getMatches();
		int total = matches.size();
		int counter = 0;
		for (Match match : matches) {
			List<Match> lm = Lists.newArrayList(match.getAllSubmatches());
			total += lm.size();
			for (Match match2 : lm)
				if (match2.getLeft() != null && match2.getRight() != null)
					counter++;
			if (match.getLeft() != null && match.getRight() != null)
				counter++;
		}
		return (counter * 1.0) / total;
	}

	private static double unidirectionalComparation(String first, String second, boolean semanticFlag) {
		IComparisonScope comparisonScope = buildComparisonScope(first, second);
		IMatchEngine.Factory.Registry registry = buildMatchEngineFactoryRegistry(semanticFlag);
		Comparison comparison = executeComparison(registry, comparisonScope);
		if(semanticFlag){
			printComparisonResults(comparison);
		}
		return evaluateComparisonResult(comparison);
	}
	
	
	
	public final static void main(String[] args) throws IOException {

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

		String ecore = "metamodels/Benchmark/Ecore.xmi";
		String er = "metamodels/Benchmark/ER.xmi";
		String uml_1_4_2 = "metamodels/Benchmark/UML_1.4.2_CD.xmi";
		String uml_2_0 = "metamodels/Benchmark/UML_2.0_CD.xmi";
		String webml = "metamodels/Benchmark/webml.xmi";

		double semanticMatching = 0.0;
		long semanticStart = 0;
		long semanticEnd = 0;
		double defaultMatching = 0.0;
		long defaultStart = 0;
		long defaultEnd = 0;
		long generalStart = System.currentTimeMillis();		

		/* ECORE - ER */
		semanticStart = System.currentTimeMillis();
		semanticMatching = unidirectionalComparation(ecore, er, true);
		semanticEnd = System.currentTimeMillis();
		defaultStart = System.currentTimeMillis();
		defaultMatching = unidirectionalComparation(ecore, er, false);
		defaultEnd = System.currentTimeMillis();
		System.out.printf("[SEMANTIC] ECORE - ER : %.2f [ %.2f s ]\n", semanticMatching, (semanticEnd - semanticStart) / 1000.0);
		System.out.printf("[DEFAULT] ECORE - ER : %.2f [ %.2f s ]\n\n", defaultMatching, (defaultEnd - defaultStart) / 1000.0);
		System.in.read();
		 
		/* ECORE - UML 1.4.2 */
		 semanticStart = System.currentTimeMillis();
		 semanticMatching = unidirectionalComparation(uml_1_4_2, ecore, true);
		 semanticEnd = System.currentTimeMillis();
		 System.out.println();
		 defaultStart = System.currentTimeMillis();
		 defaultMatching = unidirectionalComparation(uml_1_4_2, ecore, false);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] ECORE - UML 1.4.2 : %.2f [ %.2f s ]\n", semanticMatching, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] ECORE - UML 1.4.2 : %.2f [ %.2f s ]\n\n", defaultMatching, (defaultEnd - defaultStart)/1000.0);
		 System.in.read();
		 
		 /* ECORE - UML 2.0 */
		 semanticStart = System.currentTimeMillis();
		 semanticMatching = unidirectionalComparation(uml_2_0, ecore, true);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 defaultMatching = unidirectionalComparation(uml_2_0, ecore, false);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] ECORE - UML 2.0 : %.2f [ %.2f s ]\n", semanticMatching, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] ECORE - UML 2.0 : %.2f [ %.2f s ]\n\n", defaultMatching, (defaultEnd - defaultStart)/1000.0);
		 System.in.read();
		
		 /* ECORE - WEBML */
		 semanticStart = System.currentTimeMillis();
		 semanticMatching = unidirectionalComparation(webml, ecore, true);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 defaultMatching = unidirectionalComparation(webml, ecore, false);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] ECORE - WEBML : %.2f [ %.2f s ]\n", semanticMatching, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] ECORE - WEBML : %.2f [ %.2f s ]\n\n", defaultMatching, (defaultEnd - defaultStart)/1000.0);
		 System.in.read();
		 
		 /* ER - UML 1.4.2 */
		 semanticStart = System.currentTimeMillis();
		 semanticMatching = unidirectionalComparation(uml_1_4_2, er, true);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 defaultMatching = unidirectionalComparation(uml_1_4_2, er, false);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] ER - UML 1.4.2 : %.2f [ %.2f s ]\n", semanticMatching, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] ER - UML 1.4.2 : %.2f [ %.2f s ]\n\n", defaultMatching, (defaultEnd - defaultStart)/1000.0);
		 System.in.read();
		 
		 /* ER - UML 2.0 */
		 semanticStart = System.currentTimeMillis();
		 semanticMatching = unidirectionalComparation(uml_2_0, er, true);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 defaultMatching = unidirectionalComparation(uml_2_0, er, false);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] ER - UML 2.0 : %.2f [ %.2f s ]\n", semanticMatching, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] ER - UML 2.0 : %.2f [ %.2f s ]\n\n", defaultMatching, (defaultEnd - defaultStart)/1000.0);
		 System.in.read();
		 
		 /* ER - WEBML */
		 semanticStart = System.currentTimeMillis();
		 semanticMatching = unidirectionalComparation(webml, er, true);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 defaultMatching = unidirectionalComparation(webml, er, false);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] ER - WEBML : %.2f [ %.2f s ]\n", semanticMatching, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] ER - WEBML : %.2f [ %.2f s ]\n\n", defaultMatching, (defaultEnd - defaultStart)/1000.0);
		 System.in.read();
		 
		 /* UML 1.4.2 - UML 2.0 */
		 semanticStart = System.currentTimeMillis();
		 semanticMatching = unidirectionalComparation(uml_2_0, uml_1_4_2, true);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 defaultMatching = unidirectionalComparation(uml_2_0, uml_1_4_2, false);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] UML 1.4.2 - UML 2.0 : %.2f [ %.2f s ]\n", semanticMatching, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] UML 1.4.2 - UML 2.0 : %.2f [ %.2f s ]\n\n", defaultMatching, (defaultEnd - defaultStart)/1000.0);
		 System.in.read();
		 
		 /* UML 1.4.2 - WEBML */
		 semanticStart = System.currentTimeMillis();
		 semanticMatching = unidirectionalComparation(webml, uml_1_4_2, true);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 defaultMatching = unidirectionalComparation(webml, uml_1_4_2, false);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] UML 1.4.2 - WEBML : %.2f [ %.2f s ]\n", semanticMatching, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] UML 1.4.2 - WEBML : %.2f [ %.2f s ]\n\n", defaultMatching, (defaultEnd - defaultStart)/1000.0);
		 System.in.read();
		 
		 /* UML 2.0 - WEBML */
		 semanticStart = System.currentTimeMillis();
		 semanticMatching = unidirectionalComparation(webml, uml_2_0, true);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 defaultMatching = unidirectionalComparation(webml, uml_2_0, false);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] UML 2.0 - WEBML : %.2f [ %.2f s ]\n", semanticMatching, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] UML 2.0 - WEBML : %.2f [ %.2f s ]\n\n", defaultMatching, (defaultEnd - defaultStart)/1000.0);
		 System.in.read();
		 System.out.println();
		 
		 System.out.println("Total Benchmark Time : " + (System.currentTimeMillis() -generalStart)/1000.0 + " s");
		 
//		 
//		 generalStart = System.currentTimeMillis(); 
//		 System.in.read();
//		
//		 Boolean semanticFlag = true;  
//		 semanticStart = System.currentTimeMillis(); 
//		 semanticEnd = System.currentTimeMillis();
//		 defaultStart = System.currentTimeMillis();
//		 defaultEnd = System.currentTimeMillis();
//		 String bibliographyRoot = "metamodels" + File.separator + "Bibliography"; 
//		 String Book = bibliographyRoot + File.separator + "Book.xmi"; 
//		 String DocBook = bibliographyRoot + File.separator + "DocBook.xmi"; 
//		 String Publication = bibliographyRoot + File.separator + "Publication.xmi"; 
//
//		 /* Publication 1.1 - Book 1.1 */
//		 semanticStart = System.currentTimeMillis();
//		 double semanticPublicationBook = unidirectionalComparation(Publication, Book, semanticFlag);
//		 semanticEnd = System.currentTimeMillis();
//		 defaultStart = System.currentTimeMillis();
//		 double defaultPublicationBook = unidirectionalComparation(Publication, Book, !semanticFlag);
//		 defaultEnd = System.currentTimeMillis();
//		 System.out.printf("[SEMANTIC] Publication 1.1 - Book 1.1 : %.2f [ %.2f s ]\n", semanticPublicationBook, (semanticEnd - semanticStart)/1000.0);
//		 System.out.printf("[DEFAULT] Publication 1.1 - Book 1.1 : %.2f [ %.2f s ]\n\n", defaultPublicationBook, (defaultEnd - defaultStart)/1000.0);
//
//		 /* Publication 1.1 - DocBook 1.1 */
//		 semanticStart = System.currentTimeMillis();
//		 double semanticPublicationDocBook = unidirectionalComparation(Publication, DocBook, semanticFlag);
//		 semanticEnd = System.currentTimeMillis();
//		 defaultStart = System.currentTimeMillis();
//		 double defaultPublicationDocBook = unidirectionalComparation(Publication, DocBook, !semanticFlag);
//		 defaultEnd = System.currentTimeMillis();
//		 System.out.printf("[SEMANTIC] Publication 1.1 - DocBook 1.1 : %.2f [ %.2f s ]\n", semanticPublicationDocBook, (semanticEnd - semanticStart)/1000.0);
//		 System.out.printf("[DEFAULT] Publication 1.1 - DocBook 1.1 : %.2f [ %.2f s ]\n\n", defaultPublicationDocBook, (defaultEnd - defaultStart)/1000.0);
//
//		 /* Book 1.1 - DocBook 1.1 */
//		 semanticStart = System.currentTimeMillis();
//		 double semanticBookDocBook = unidirectionalComparation(Book, DocBook, semanticFlag);
//		 semanticEnd = System.currentTimeMillis();
//		 defaultStart = System.currentTimeMillis();
//		 double defaultBookDocBook = unidirectionalComparation(Book, DocBook, !semanticFlag);
//		 defaultEnd = System.currentTimeMillis();
//		 System.out.printf("[SEMANTIC] Book 1.1 - DocBook 1.1 : %.2f [ %.2f s ]\n", semanticBookDocBook, (semanticEnd - semanticStart)/1000.0);
//		 System.out.printf("[DEFAULT] Book 1.1 - DocBook 1.1 : %.2f [ %.2f s ]\n\n", defaultBookDocBook, (defaultEnd - defaultStart)/1000.0);
//		 System.in.read();
//
//		 System.out.println();
//		 System.out.println("Total Time : " + (System.currentTimeMillis() -generalStart)/1000.0 + " s");

	}
}
