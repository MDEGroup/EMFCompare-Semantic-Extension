package it.univaq.disim.mdegroup.emfcompare.extension;

import it.univaq.disim.mdegroup.emfcompare.extension.match.impl.SemanticMatchEngineFactoryRegistryImpl;

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

//		String ecore = "metamodels/Ecore.xmi";
//		String er = "metamodels/ER.xmi";
//		String uml_1_4_2 = "metamodels/UML_1.4.2_CD.xmi";
//		String uml_2_0 = "metamodels/UML_2.0_CD.xmi";
//		String webml = "metamodels/webml.xmi";
//
//		double semanticMatching = 0.0;
//		long semanticStart = 0;
//		long semanticEnd = 0;
//		double defaultMatching = 0.0;
//		long defaultStart = 0;
//		long defaultEnd = 0;
//
//		/* ECORE - ER */
//		semanticStart = System.currentTimeMillis();
//		semanticMatching = unidirectionalComparation(ecore, er, true);
//		semanticEnd = System.currentTimeMillis();
//		defaultStart = System.currentTimeMillis();
//		defaultMatching = unidirectionalComparation(ecore, er, false);
//		defaultEnd = System.currentTimeMillis();
//		System.out.printf("[SEMANTIC] ECORE - ER : %.2f [ %.2f s ]\n", semanticMatching, (semanticEnd - semanticStart) / 1000.0);
//		System.out.printf("[DEFAULT] ECORE - ER : %.2f [ %.2f s ]\n\n", defaultMatching, (defaultEnd - defaultStart) / 1000.0);
//		System.in.read();
//		 /* ECORE - UML 1.4.2 */
//		 semanticStart = System.currentTimeMillis();
//		 semanticMatching = unidirectionalComparation(uml_1_4_2, ecore, true);
//		 semanticEnd = System.currentTimeMillis();
//		 System.out.println();
//		 defaultStart = System.currentTimeMillis();
//		 defaultMatching = unidirectionalComparation(uml_1_4_2, ecore, false);
//		 defaultEnd = System.currentTimeMillis();
//		 System.out.printf("[SEMANTIC] ECORE - UML 1.4.2 : %.2f [ %.2f s ]\n", semanticMatching, (semanticEnd - semanticStart)/1000.0);
//		 System.out.printf("[DEFAULT] ECORE - UML 1.4.2 : %.2f [ %.2f s ]\n\n", defaultMatching, (defaultEnd - defaultStart)/1000.0);
//		 System.in.read();
//		 /* ECORE - UML 2.0 */
//		 semanticStart = System.currentTimeMillis();
//		 semanticMatching = unidirectionalComparation(uml_2_0, ecore, true);
//		 semanticEnd = System.currentTimeMillis();
//		 defaultStart = System.currentTimeMillis();
//		 defaultMatching = unidirectionalComparation(uml_2_0, ecore, false);
//		 defaultEnd = System.currentTimeMillis();
//		 System.out.printf("[SEMANTIC] ECORE - UML 2.0 : %.2f [ %.2f s ]\n", semanticMatching, (semanticEnd - semanticStart)/1000.0);
//		 System.out.printf("[DEFAULT] ECORE - UML 2.0 : %.2f [ %.2f s ]\n\n", defaultMatching, (defaultEnd - defaultStart)/1000.0);
//		 System.in.read();
//		/* ECORE - WEBML */
//		 semanticStart = System.currentTimeMillis();
//		 semanticMatching = unidirectionalComparation(webml, ecore, true);
//		 semanticEnd = System.currentTimeMillis();
//		 defaultStart = System.currentTimeMillis();
//		 defaultMatching = unidirectionalComparation(webml, ecore, false);
//		 defaultEnd = System.currentTimeMillis();
//		 System.out.printf("[SEMANTIC] ECORE - WEBML : %.2f [ %.2f s ]\n", semanticMatching, (semanticEnd - semanticStart)/1000.0);
//		 System.out.printf("[DEFAULT] ECORE - WEBML : %.2f [ %.2f s ]\n\n", defaultMatching, (defaultEnd - defaultStart)/1000.0);
//		 System.in.read();
//		 /* ER - UML 1.4.2 */
//		 semanticStart = System.currentTimeMillis();
//		 semanticMatching = unidirectionalComparation(uml_1_4_2, er, true);
//		 semanticEnd = System.currentTimeMillis();
//		 defaultStart = System.currentTimeMillis();
//		 defaultMatching = unidirectionalComparation(uml_1_4_2, er, false);
//		 defaultEnd = System.currentTimeMillis();
//		 System.out.printf("[SEMANTIC] ER - UML 1.4.2 : %.2f [ %.2f s ]\n", semanticMatching, (semanticEnd - semanticStart)/1000.0);
//		 System.out.printf("[DEFAULT] ER - UML 1.4.2 : %.2f [ %.2f s ]\n\n", defaultMatching, (defaultEnd - defaultStart)/1000.0);
//		 System.in.read();
//		 /* ER - UML 2.0 */
//		 semanticStart = System.currentTimeMillis();
//		 semanticMatching = unidirectionalComparation(uml_2_0, er, true);
//		 semanticEnd = System.currentTimeMillis();
//		 defaultStart = System.currentTimeMillis();
//		 defaultMatching = unidirectionalComparation(uml_2_0, er, false);
//		 defaultEnd = System.currentTimeMillis();
//		 System.out.printf("[SEMANTIC] ER - UML 2.0 : %.2f [ %.2f s ]\n", semanticMatching, (semanticEnd - semanticStart)/1000.0);
//		 System.out.printf("[DEFAULT] ER - UML 2.0 : %.2f [ %.2f s ]\n\n", defaultMatching, (defaultEnd - defaultStart)/1000.0);
//		 System.in.read();
//		 /* ER - WEBML */
//		 semanticStart = System.currentTimeMillis();
//		 semanticMatching = unidirectionalComparation(webml, er, true);
//		 semanticEnd = System.currentTimeMillis();
//		 defaultStart = System.currentTimeMillis();
//		 defaultMatching = unidirectionalComparation(webml, er, false);
//		 defaultEnd = System.currentTimeMillis();
//		 System.out.printf("[SEMANTIC] ER - WEBML : %.2f [ %.2f s ]\n", semanticMatching, (semanticEnd - semanticStart)/1000.0);
//		 System.out.printf("[DEFAULT] ER - WEBML : %.2f [ %.2f s ]\n\n", defaultMatching, (defaultEnd - defaultStart)/1000.0);
//		 System.in.read();
//		 /* UML 1.4.2 - UML 2.0 */
//		 semanticStart = System.currentTimeMillis();
//		 semanticMatching = unidirectionalComparation(uml_2_0, uml_1_4_2, true);
//		 semanticEnd = System.currentTimeMillis();
//		 defaultStart = System.currentTimeMillis();
//		 defaultMatching = unidirectionalComparation(uml_2_0, uml_1_4_2, false);
//		 defaultEnd = System.currentTimeMillis();
//		 System.out.printf("[SEMANTIC] UML 1.4.2 - UML 2.0 : %.2f [ %.2f s ]\n", semanticMatching, (semanticEnd - semanticStart)/1000.0);
//		 System.out.printf("[DEFAULT] UML 1.4.2 - UML 2.0 : %.2f [ %.2f s ]\n\n", defaultMatching, (defaultEnd - defaultStart)/1000.0);
//		 System.in.read();
//		 /* UML 1.4.2 - WEBML */
//		 semanticStart = System.currentTimeMillis();
//		 semanticMatching = unidirectionalComparation(webml, uml_1_4_2, true);
//		 semanticEnd = System.currentTimeMillis();
//		 defaultStart = System.currentTimeMillis();
//		 defaultMatching = unidirectionalComparation(webml, uml_1_4_2, false);
//		 defaultEnd = System.currentTimeMillis();
//		 System.out.printf("[SEMANTIC] UML 1.4.2 - WEBML : %.2f [ %.2f s ]\n", semanticMatching, (semanticEnd - semanticStart)/1000.0);
//		 System.out.printf("[DEFAULT] UML 1.4.2 - WEBML : %.2f [ %.2f s ]\n\n", defaultMatching, (defaultEnd - defaultStart)/1000.0);
//		 System.in.read();
//		 /* UML 2.0 - WEBML */
//		 semanticStart = System.currentTimeMillis();
//		 semanticMatching = unidirectionalComparation(webml, uml_2_0, true);
//		 semanticEnd = System.currentTimeMillis();
//		 defaultStart = System.currentTimeMillis();
//		 defaultMatching = unidirectionalComparation(webml, uml_2_0, false);
//		 defaultEnd = System.currentTimeMillis();
//		 System.out.printf("[SEMANTIC] UML 2.0 - WEBML : %.2f [ %.2f s ]\n", semanticMatching, (semanticEnd - semanticStart)/1000.0);
//		 System.out.printf("[DEFAULT] UML 2.0 - WEBML : %.2f [ %.2f s ]\n\n", defaultMatching, (defaultEnd - defaultStart)/1000.0);
//		 System.in.read();
		
		 Boolean semanticFlag = true;  
		 long semanticStart = System.currentTimeMillis(); 
		 long semanticEnd = System.currentTimeMillis();
		 long defaultStart = System.currentTimeMillis();
		 long defaultEnd = System.currentTimeMillis();
		 String bibliographyRoot = "metamodels" + File.separator + "Bibliography"; 
		 String BIBTEX = bibliographyRoot + File.separator + "BIBTEX_.xmi"; 
		 String BibTeX = bibliographyRoot + File.separator + "BibTeX.xmi"; 
		 String BibTeXML = bibliographyRoot + File.separator + "BibTeXML.xmi";
		 String Book = bibliographyRoot + File.separator + "Book.xmi"; 
		 String DocBook = bibliographyRoot + File.separator + "DocBook.xmi"; 
		 String LaTeX = bibliographyRoot + File.separator + "LaTeX.xmi"; 
		 String Publication = bibliographyRoot + File.separator + "Publication.xmi"; 
		 String SWRC = bibliographyRoot + File.separator + "SWRC.xmi";  
		 String HAL = bibliographyRoot + File.separator + "HAL.xmi"; 
		 
		 /* Publication 1.1 - BibTeX 1.1 */
		 semanticStart = System.currentTimeMillis();
		 double semanticPublicationBibTeX = unidirectionalComparation(Publication, BibTeX, semanticFlag);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 double defaultPublicationBibTeX = unidirectionalComparation(Publication, BibTeX, !semanticFlag);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] Publication 1.1 - BibTeX 1.1 : %.2f [ %.2f s ]\n", semanticPublicationBibTeX, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] Publication 1.1 - BibTeX 1.1 : %.2f [ %.2f s ]\n\n", defaultPublicationBibTeX, (defaultEnd - defaultStart)/1000.0);

		 /* Publication 1.1 - Book 1.1 */
		 semanticStart = System.currentTimeMillis();
		 double semanticPublicationBook = unidirectionalComparation(Publication, Book, semanticFlag);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 double defaultPublicationBook = unidirectionalComparation(Publication, Book, !semanticFlag);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] Publication 1.1 - Book 1.1 : %.2f [ %.2f s ]\n", semanticPublicationBook, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] Publication 1.1 - Book 1.1 : %.2f [ %.2f s ]\n\n", defaultPublicationBook, (defaultEnd - defaultStart)/1000.0);

		 /* BibTeX 1.1 - BIBTEX 1 */
		 semanticStart = System.currentTimeMillis();
		 double semanticBibTeXBIBTEX = unidirectionalComparation(BibTeX, BIBTEX, semanticFlag);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 double defaultBibTeXBIBTEX = unidirectionalComparation(BibTeX, BIBTEX, !semanticFlag);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] BibTeX 1.1 - BIBTEX 1 : %.2f [ %.2f s ]\n", semanticBibTeXBIBTEX, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] BibTeX 1.1 - BIBTEX 1 : %.2f [ %.2f s ]\n\n", defaultBibTeXBIBTEX, (defaultEnd - defaultStart)/1000.0);

		 /* BibTeX 1.1 - BibTeXML 1.2 */
		 semanticStart = System.currentTimeMillis();
		 double semanticBibTeXBibTeXML = unidirectionalComparation(BibTeX, BibTeXML, semanticFlag);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 double defaultBibTeXBibTeXML = unidirectionalComparation(BibTeX, BibTeXML, !semanticFlag);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] BibTeX 1.1 - BibTeXML 1.2 : %.2f [ %.2f s ]\n", semanticBibTeXBibTeXML, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] BibTeX 1.1 - BibTeXML 1.2 : %.2f [ %.2f s ]\n\n", defaultBibTeXBibTeXML, (defaultEnd - defaultStart)/1000.0);

		 /* BibTeX 1.1 - DocBook 1.1 */
		 semanticStart = System.currentTimeMillis();
		 double semanticBibTeXDocBook = unidirectionalComparation(BibTeX, DocBook, semanticFlag);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 double defaultBibTeXDocBook = unidirectionalComparation(BibTeX, DocBook, !semanticFlag);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] BibTeX 1.1 - DocBook 1.1 : %.2f [ %.2f s ]\n", semanticBibTeXDocBook, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] BibTeX 1.1 - DocBook 1.1 : %.2f [ %.2f s ]\n\n", defaultBibTeXDocBook, (defaultEnd - defaultStart)/1000.0);

		 /* BibTeX 1.1 - Book 1.1 */
		 semanticStart = System.currentTimeMillis();
		 double semanticBibTeXBook = unidirectionalComparation(BibTeX, Book, semanticFlag);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 double defaultBibTeXBook = unidirectionalComparation(BibTeX, Book, !semanticFlag);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] BibTeX 1.1 - Book 1.1 : %.2f [ %.2f s ]\n", semanticBibTeXBook, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] BibTeX 1.1 - Book 1.1 : %.2f [ %.2f s ]\n\n", defaultBibTeXBook, (defaultEnd - defaultStart)/1000.0);
		 System.in.read();
		 /* BibTeX 1.1 - HAL 1 */
		 semanticStart = System.currentTimeMillis();
		 double semanticBibTeXHAL = unidirectionalComparation(BibTeX, HAL, semanticFlag);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 double defaultBibTeXHAL = unidirectionalComparation(BibTeX, HAL, !semanticFlag);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] BibTeX 1.1 - HAL 1 : %.2f [ %.2f s ]\n", semanticBibTeXHAL, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] BibTeX 1.1 - HAL 1 : %.2f [ %.2f s ]\n\n", defaultBibTeXHAL, (defaultEnd - defaultStart)/1000.0);
		 System.in.read();
		 /* BibTeX 1.1 - SWRC 1 */
		 semanticStart = System.currentTimeMillis();
		 double semanticBibTeXSWRC = unidirectionalComparation(BibTeX, SWRC, semanticFlag);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 double defaultBibTeXSWRC = unidirectionalComparation(BibTeX, SWRC, !semanticFlag);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] BibTeX 1.1 - SWRC 1 : %.2f [ %.2f s ]\n", semanticBibTeXSWRC, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] BibTeX 1.1 - SWRC 1 : %.2f [ %.2f s ]\n\n", defaultBibTeXSWRC, (defaultEnd - defaultStart)/1000.0);
		 System.in.read();
		 /* Book 1.1 - DocBook 1.1 */
		 semanticStart = System.currentTimeMillis();
		 double semanticBookDocBook = unidirectionalComparation(Book, DocBook, semanticFlag);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 double defaultBookDocBook = unidirectionalComparation(Book, DocBook, !semanticFlag);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] Book 1.1 - DocBook 1.1 : %.2f [ %.2f s ]\n", semanticBookDocBook, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] Book 1.1 - DocBook 1.1 : %.2f [ %.2f s ]\n\n", defaultBookDocBook, (defaultEnd - defaultStart)/1000.0);
		 System.in.read();
		 /* BIBTEX 1 - LaTeX 1.0 */
		 semanticStart = System.currentTimeMillis();
		 double semanticBIBTEXLaTeX = unidirectionalComparation(BIBTEX, LaTeX, semanticFlag);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 double defaultBIBTEXLaTeX = unidirectionalComparation(BIBTEX, LaTeX, !semanticFlag);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] BIBTEX 1 - LaTeX 1.0 : %.2f [ %.2f s ]\n", semanticBIBTEXLaTeX, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] BIBTEX 1 - LaTeX 1.0 : %.2f [ %.2f s ]\n\n", defaultBIBTEXLaTeX, (defaultEnd - defaultStart)/1000.0);
		 System.in.read();
		 /* BIBTEX 1 - HAL 1 */
		 semanticStart = System.currentTimeMillis();
		 double semanticBIBTEXHAL = unidirectionalComparation(BIBTEX, HAL, semanticFlag);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 double defaultBIBTEXHAL = unidirectionalComparation(BIBTEX, HAL, !semanticFlag);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] BIBTEX 1 - HAL 1 : %.2f [ %.2f s ]\n", semanticBIBTEXHAL, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] BIBTEX 1 - HAL 1 : %.2f [ %.2f s ]\n\n", defaultBIBTEXHAL, (defaultEnd - defaultStart)/1000.0);
		 System.in.read();
		 /* BIBTEX 1 - SWRC 1 */
		 semanticStart = System.currentTimeMillis();
		 double semanticBIBTEXSWRC = unidirectionalComparation(BIBTEX, SWRC, semanticFlag);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 double defaultBIBTEXSWRC = unidirectionalComparation(BIBTEX, SWRC, !semanticFlag);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] BIBTEX 1 - SWRC 1 : %.2f [ %.2f s ]\n", semanticBIBTEXSWRC, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] BIBTEX 1 - SWRC 1 : %.2f [ %.2f s ]\n\n", defaultBIBTEXSWRC, (defaultEnd - defaultStart)/1000.0);
		 System.in.read();
		 /* BIBTEX 1 - BibTeXML 1.2 */
		 semanticStart = System.currentTimeMillis();
		 double semanticBIBTEXBibTeXML = unidirectionalComparation(BIBTEX, BibTeXML, semanticFlag);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 double defaultBIBTEXBibTeXML = unidirectionalComparation(BIBTEX, BibTeXML, !semanticFlag);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] BIBTEX 1 - BibTeXML 1.2 : %.2f [ %.2f s ]\n", semanticBIBTEXBibTeXML, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] BIBTEX 1 - BibTeXML 1.2 : %.2f [ %.2f s ]\n\n", defaultBIBTEXBibTeXML, (defaultEnd - defaultStart)/1000.0);
		 System.in.read();
		 /* SWRC 1 - HAL 1 */
		 semanticStart = System.currentTimeMillis();
		 double semanticSWRCHAL = unidirectionalComparation(SWRC, HAL, semanticFlag);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 double defaultSWRCHAL = unidirectionalComparation(SWRC, HAL, !semanticFlag);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] SWRC 1 - HAL 1 : %.2f [ %.2f s ]\n", semanticSWRCHAL, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] SWRC 1 - HAL 1 : %.2f [ %.2f s ]\n\n", defaultSWRCHAL, (defaultEnd - defaultStart)/1000.0);
		 System.in.read();
		 /* SWRC 1 - BibTeXML 1.2 */
		 semanticStart = System.currentTimeMillis();
		 double semanticSWRCBibTeXML = unidirectionalComparation(SWRC, BibTeXML, semanticFlag);
		 semanticEnd = System.currentTimeMillis();
		 defaultStart = System.currentTimeMillis();
		 double defaultSWRCBibTeXML = unidirectionalComparation(SWRC, BibTeXML, !semanticFlag);
		 defaultEnd = System.currentTimeMillis();
		 System.out.printf("[SEMANTIC] SWRC 1 - BibTeXML 1.2 : %.2f [ %.2f s ]\n", semanticSWRCBibTeXML, (semanticEnd - semanticStart)/1000.0);
		 System.out.printf("[DEFAULT] SWRC 1 - BibTeXML 1.2 : %.2f [ %.2f s ]\n\n", defaultSWRCBibTeXML, (defaultEnd - defaultStart)/1000.0);
		 System.err.println("OVER");

// N.B. the metamodels prefix has been changed 		 
//		 boolean semanticFlag = true;
//		 long generalStart = System.currentTimeMillis();
//		
//		 long semanticStart = System.currentTimeMillis();
//		 double semanticDocBookPublication =
//		 unidirectionalComparation(docBook, publication, semanticFlag);
//		 long semanticEnd = System.currentTimeMillis();
//		 long defaultStart = System.currentTimeMillis();
//		 double defaultDocBookPublication = unidirectionalComparation(docBook,
//				 publication, !semanticFlag);
//		 long defaultEnd = System.currentTimeMillis();
//		 System.out.printf("[SEMANTIC] DOCBOOK - PUBLICATION : %.2f [ %.2f s ]\n",
//				 semanticDocBookPublication, (semanticEnd - semanticStart)/1000.0);
//		 System.out.printf("[DEFAULT] DOCBOOK - PUBLICATION : %.2f [ %.2f s ]\n\n", 
//				 defaultDocBookPublication, (defaultEnd - defaultStart)/1000.0);
//		 
//		 semanticStart = System.currentTimeMillis();
//		 double semanticDocBookBook = unidirectionalComparation(docBook, book,
//		 semanticFlag);
//		 semanticEnd = System.currentTimeMillis();
//		 defaultStart = System.currentTimeMillis();
//		 double defaultDocBookBook = unidirectionalComparation(docBook, book, !semanticFlag);
//		 defaultEnd = System.currentTimeMillis();
//		 System.out.printf("[SEMANTIC] DOCBOOK - BOOK : %.2f [ %.2f s ]\n", semanticDocBookBook, (semanticEnd - semanticStart)/1000.0);
//		 System.out.printf("[DEFAULT] DOCBOOK - BOOK : %.2f [ %.2f s ]\n\n", defaultDocBookBook, (defaultEnd - defaultStart)/1000.0);
//		 System.in.read();
//		 semanticStart = System.currentTimeMillis();
//		 double semanticBibTeXBook = unidirectionalComparation(bibtex, book, semanticFlag);
//		 semanticEnd = System.currentTimeMillis();
//		 defaultStart = System.currentTimeMillis();
//		 double defaultBibTeXBook = unidirectionalComparation(bibtex, book, !semanticFlag);
//		 defaultEnd = System.currentTimeMillis();
//		 System.out.printf("[SEMANTIC] BIBTEX - BOOK : %.2f [ %.2f s ]\n", semanticBibTeXBook, (semanticEnd - semanticStart)/1000.0);
//		 System.out.printf("[DEFAULT] BIBTEX - BOOK : %.2f [ %.2f s ]\n\n", defaultBibTeXBook, (defaultEnd - defaultStart)/1000.0);
//		 System.in.read();
//		 double semanticPublicationBook =
//		 unidirectionalComparation(publication,
//		 book, semanticFlag);
//		 double defaultPublicationBook = unidirectionalComparation(bibtex, book, !semanticFlag);
//		 System.out.printf("[SEMANTIC] PUBLICATION - BOOK : %.2f\n", semanticPublicationBook);
//		 System.out.printf("[DEFAULT] PUBLICATION - BOOK : %.2f\n\n", defaultPublicationBook);
//		 double semanticBibTeXPublication = unidirectionalComparation(bibtex,
//		 publication, semanticFlag);
//		 double defaultBibTeXPublication = unidirectionalComparation(bibtex,
//		 publication, !semanticFlag);
//		 System.out.printf("[SEMANTIC] BibTeX - Publication : %.2f\n",
//		 semanticBibTeXPublication);
//		 System.out.printf("[DEFAULT] BibTeX - Publication : %.2f\n\n",
//		 defaultBibTeXPublication);
		
//		 System.out.println();
//		 System.out.println("Total Time : " + (System.currentTimeMillis() -generalStart)/1000.0 + " s");

	}
}
