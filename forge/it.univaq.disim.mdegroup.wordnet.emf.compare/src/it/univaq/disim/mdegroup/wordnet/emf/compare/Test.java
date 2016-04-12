package it.univaq.disim.mdegroup.wordnet.emf.compare;

import it.univaq.disim.mdegroup.wordnet.emf.compare.match.impl.SemanticMatchEngineFactoryRegistryImpl;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

public class Test {
	
	
	/* IMPOSTAZIONI */
	private static final boolean WIMMER_BENCHMARK = false; 
	private static final boolean THESIS_MANAGEMENT_EXAMPLE = true; 
	private static final boolean DEFAULT = true; 
	private static final boolean SEMANTIC = false; 
	
	private static IMatchEngine.Factory.Registry matchEngineFactoryRegistry = null; 
	
	private static IComparisonScope buildComparisonScope(String uri1, String uri2) {
		ResourceSet resourceSet1 = new ResourceSetImpl();
		ResourceSet resourceSet2 = new ResourceSetImpl();
		resourceSet1.getResource(URI.createFileURI(uri1), true);
		resourceSet2.getResource(URI.createFileURI(uri2), true);
		return new DefaultComparisonScope(resourceSet1, resourceSet2, null);
	}

	private static IMatchEngine.Factory.Registry buildMatchEngineFactoryRegistry(boolean semanticFlag) {
		return semanticFlag ? 
				matchEngineFactoryRegistry == null? 
						SemanticMatchEngineFactoryRegistryImpl.createStandaloneInstance() 
						: matchEngineFactoryRegistry
			    : matchEngineFactoryRegistry == null ? 
			    		MatchEngineFactoryRegistryImpl.createStandaloneInstance() 
			    		: matchEngineFactoryRegistry;
	}

	private static Comparison executeComparison(IMatchEngine.Factory.Registry registry, IComparisonScope scope) {
		return EMFCompare.builder().setMatchEngineFactoryRegistry(registry).build().compare(scope);
	}
	
	private static void printComparisonResults(Comparison comparison){
		System.out.println(comparison.getMatches().size());	
		for(Match match : comparison.getMatches()){
				if(match.getLeft() != null && match.getRight() != null){
					System.out.println((match.getLeft().eContainer() != null ? ((ENamedElement)match.getLeft().eContainer()).getName() + "." : "" ) 
									 + ((ENamedElement)match.getLeft()).getName() + " <---> " 
									 + (match.getRight().eContainer() != null ? ((ENamedElement)match.getRight().eContainer()).getName() + "." : "" ) 
									 + ((ENamedElement)match.getRight()).getName());
				}
				for(Match submatch : match.getAllSubmatches()){
					if(submatch.getLeft() != null && submatch.getRight() != null){
						System.out.println(" " + ((ENamedElement)submatch.getLeft().eContainer()).getName() + "." + ((ENamedElement)submatch.getLeft()).getName() + " <---> " + ((ENamedElement)submatch.getRight().eContainer()).getName() + "." + ((ENamedElement)submatch.getRight()).getName());
					}
				}
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

		Instant start = Instant.now();

		IComparisonScope comparisonScope = buildComparisonScope(first, second);
		IMatchEngine.Factory.Registry registry = buildMatchEngineFactoryRegistry(semanticFlag);
		Comparison comparison = executeComparison(registry, comparisonScope);
		if(semanticFlag){
			System.out.println("[SEMANTIC EMF COMPARE]");
		} else {
			System.out.println("[DEFAULT EMF COMPARE]");
		}
		
		Instant end = Instant.now();
		System.err.println(Duration.between(start, end));
//		try {
//			System.in.read();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		printComparisonResults(comparison); 
		return evaluateComparisonResult(comparison);
	}
	
	public static void runWimmerBenchmark() throws IOException{
		/* Benchamrk Metamodels */
		String ecore = "metamodels/Benchmark/Ecore.xmi";
		String er = "metamodels/Benchmark/ER.xmi";
		String uml_1_4_2 = "metamodels/Benchmark/UML_1.4.2_CD.xmi";
		String uml_2_0 = "metamodels/Benchmark/UML_2.0_CD.xmi";
		String webml = "metamodels/Benchmark/webml.xmi";
		
		/* ECore - ER */
		if(SEMANTIC)
			System.out.printf("[SEMANTIC] Ecore - ER : %.2f\n", unidirectionalComparation(ecore, er, true));
		if(DEFAULT)
			System.out.printf("[DEFAULT] Ecore - ER : %.2f\n", unidirectionalComparation(ecore, er, false));
//		System.out.println();
//		System.in.read();
		/* ECore - UML 1.4.2 */
		if(SEMANTIC)
			System.out.printf("[SEMANTIC] Ecore - UML 1.4.2 : %.2f\n", unidirectionalComparation(ecore, uml_1_4_2, true));
		if(DEFAULT)
			System.out.printf("[DEFAULT] Ecore - UML 1.4.2 : %.2f\n", unidirectionalComparation(ecore, uml_1_4_2, false));
//		System.out.println();
//		System.in.read();
		/* ECore - UML 2.0 */
		if(SEMANTIC)
			System.out.printf("[SEMANTIC] Ecore - UML 2.0 : %.2f\n", unidirectionalComparation(ecore, uml_2_0, true));
		if(DEFAULT)
			System.out.printf("[DEFAULT] Ecore - UML 2.0 : %.2f\n", unidirectionalComparation(ecore, uml_2_0, false));
//		System.out.println();
//		System.in.read();
		/* ECore - WebML */
		if(SEMANTIC)
			System.out.printf("[SEMANTIC] Ecore - WebML : %.2f\n", unidirectionalComparation(ecore, webml, true));
		if(DEFAULT)
			System.out.printf("[DEFAULT] Ecore - WebML : %.2f\n", unidirectionalComparation(ecore, webml, false));
		System.out.println();
//		System.in.read();
		/* ER - UML 1.4.2 */
		if(SEMANTIC)
			System.out.printf("[SEMANTIC] ER - UML 1.4.2 : %.2f\n", unidirectionalComparation(er, uml_1_4_2, true));
		if(DEFAULT)
			System.out.printf("[DEFAULT] ER - UML 1.4.2 : %.2f\n", unidirectionalComparation(er, uml_1_4_2, false));
//		System.out.println();
//		System.in.read();
		/* ER - UML 2.0 */
		if(SEMANTIC)
			System.out.printf("[SEMANTIC] ER - UML 2.0 : %.2f\n", unidirectionalComparation(er, uml_2_0, true));
		if(DEFAULT)
			System.out.printf("[DEFAULT] ER - UML 2.0 : %.2f\n", unidirectionalComparation(er, uml_2_0, false));
//		System.out.println();
//		System.in.read();
		/* ER - WebML */
		if(SEMANTIC)
			System.out.printf("[SEMANTIC] ER - WebML : %.2f\n", unidirectionalComparation(er, webml, true));
		if(DEFAULT)
			System.out.printf("[DEFAULT] ER - WebML : %.2f\n", unidirectionalComparation(er, webml, false));
//		System.out.println();
//		System.in.read();
		/* UML 1.4.2 - UML 2.0 */
		if(SEMANTIC)
			System.out.printf("[SEMANTIC] UML 1.4.2 - UML 2.0 : %.2f\n", unidirectionalComparation(uml_1_4_2, uml_2_0, true));
		if(DEFAULT)
			System.out.printf("[DEFAULT] UML 1.4.2 - UML 2.0 : %.2f\n", unidirectionalComparation(uml_1_4_2, uml_2_0, false));
//		System.out.println();
//		System.in.read();
		/* UML 1.4.2 - WebML */
		if(SEMANTIC)
			System.out.printf("[SEMANTIC] UML 1.4.2 - WebML : %.2f\n", unidirectionalComparation(uml_1_4_2, webml, true));
		if(DEFAULT)
			System.out.printf("[DEFAULT] UML 1.4.2 - WebML : %.2f\n", unidirectionalComparation(uml_1_4_2, webml, false));
//		System.out.println();
//		System.in.read();
		/* UML 2.0 - WebML */
		if(SEMANTIC)
			System.out.printf("[SEMANTIC] UML 2.0 - WebML : %.2f\n", unidirectionalComparation(uml_2_0, webml, true));
		if(DEFAULT)
			System.out.printf("[DEFAULT] UML 2.0 - WebML : %.2f\n", unidirectionalComparation(uml_2_0, webml, false));
//		System.out.println();
//		System.in.read();
	}
	
	public static void runThesisManagementExample(){
		/** Thesis Management - First Version - Path **/
		String thesisManagement_v1 = "metamodels/ThesisManagement/ThesisManagement_v1.xmi";
		/** Thesis Management - Second Version - Path **/
		String thesisManagement_v2 = "metamodels/ThesisManagement/ThesisManagement_v2.xmi"; 
		/** Default **/
		if(DEFAULT)
			System.out.printf("[DEFAULT] ThesisManagement_v1 - ThesisManagement_v2 : %.2f\n", unidirectionalComparation(thesisManagement_v1, thesisManagement_v2, false));
		/** Semantic **/
		if(SEMANTIC)
			System.out.printf("[SEMANTIC] ThesisManagement_v1 - ThesisManagement_v2 : %.2f\n", unidirectionalComparation(thesisManagement_v1, thesisManagement_v2, true));
	}
	
	public final static void main(String[] args) throws IOException {
		/* Register XMI extension */
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		if(THESIS_MANAGEMENT_EXAMPLE){
			runThesisManagementExample();
		}
		if(WIMMER_BENCHMARK){
			runWimmerBenchmark();
		}

	}
}
