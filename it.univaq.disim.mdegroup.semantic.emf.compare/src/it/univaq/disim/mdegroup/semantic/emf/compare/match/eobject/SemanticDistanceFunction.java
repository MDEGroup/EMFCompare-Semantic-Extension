package it.univaq.disim.mdegroup.semantic.emf.compare.match.eobject;

import it.univaq.disim.mdegroup.semantic.emf.compare.match.eobject.evaluator.SemanticDistanceEvaluator;
import it.univaq.disim.mdegroup.semantic.emf.compare.match.eobject.evaluator.SyntacticDistanceEvaluator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.match.eobject.ProximityEObjectMatcher.DistanceFunction;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;

import slib.indexer.wordnet.IndexerWordNetBasic;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.utils.SMconf;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class SemanticDistanceFunction implements DistanceFunction {

	private SM_Engine similarityComparationEngine; 
	private SMconf similarityMeasureConfiguration; 
	private IndexerWordNetBasic wordnetNounIndexer; 
	private IndexerWordNetBasic wordnetVerbIndexer; 
	private MaxentTagger maxentTagger; 
	private WordnetStemmer wordnetStemmer; 
	private IDictionary wordnetDictionary; 
	
	/* Constructor */
	public SemanticDistanceFunction(SM_Engine similarityComparationEngine, SMconf similarityMeasureConfiguration, IndexerWordNetBasic wordnetNounIndexer, IndexerWordNetBasic wordnetVerbIndexer, MaxentTagger maxentTagger, WordnetStemmer wordnetStemmer, IDictionary wordnetDictionary){
		this.similarityComparationEngine = similarityComparationEngine; 
		this.similarityMeasureConfiguration = similarityMeasureConfiguration; 
		this.wordnetNounIndexer = wordnetNounIndexer; 
		this.wordnetVerbIndexer = wordnetVerbIndexer; 
		this.maxentTagger = maxentTagger; 
		this.wordnetStemmer = wordnetStemmer; 
		this.wordnetDictionary = wordnetDictionary; 
	}
	
	/* Check if two elements are semantically equivalent */
	@Override 
	public boolean areIdentic(Comparison comparison, EObject first, EObject second){
		return distance(comparison, first, second) == 0.0; 
	}
	
	/* Computes the semantic distance among two given elements */
	@Override 
	public double distance(Comparison comparison, EObject first, EObject second){
		double semanticDistanceThreshold = 0.2525; 
		Class<?> firstClass = first.getClass();
		Class<?> secondClass = second.getClass();
		double result = Double.MAX_VALUE;
		if(firstClass.toString().equals(secondClass.toString())){
			try{
				/* Retrieve elements' names */
				String firstName = (String) firstClass.getMethod("getName", new Class<?>[]{}).invoke(first, new Object[]{});
				String secondName = (String) secondClass.getMethod("getName", new Class<?>[]{}).invoke(second, new Object[]{});
				/* Retrieve elements' contexts */
				String firstContextName = first.eContainer() != null && first.eContainer().eContainer() != null ? ((ENamedElement)first.eContainer()).getName() : "";
				String secondContextName = second.eContainer() != null && second.eContainer().eContainer() != null ? ((ENamedElement)second.eContainer()).getName() : ""; 
				/* Distances Executor Service */
				ExecutorService distancesExecutor = Executors.newCachedThreadPool();
				double semanticElementsDistance = 1.0; 
				double syntacticContextsDistance = 1.0; 
				/* Compute Context Syntactic Distance */
				if(!firstContextName.toLowerCase().equals(secondContextName.toLowerCase())){
					Future<Double> futureContextSyntacticDistance = distancesExecutor.submit(new SyntacticDistanceEvaluator(firstContextName, secondContextName));
					/* Compute Elements Semantic Distance */
					if(!firstName.toLowerCase().equals(secondName.toLowerCase())){
						this.wordnetDictionary.open();
						Future<Double> futureElementsSemanticDistance = distancesExecutor.submit(new SemanticDistanceEvaluator(firstName, secondName, this.similarityComparationEngine, this.similarityMeasureConfiguration, this.wordnetNounIndexer, this.wordnetVerbIndexer, this.maxentTagger, this.wordnetStemmer));
						semanticElementsDistance = futureElementsSemanticDistance.get();
						this.wordnetDictionary.close();
					} else {
						semanticElementsDistance = 0.0; 
					}
					syntacticContextsDistance = futureContextSyntacticDistance.get()/Math.max(firstContextName.length(), secondContextName.length());
				} else {
					syntacticContextsDistance = 0.0; 
					if(!firstName.toLowerCase().equals(secondName.toLowerCase())){
						/* Compute Elements Semantic Distance */
						this.wordnetDictionary.open();
						Future<Double> futureElementsSemanticDistance = distancesExecutor.submit(new SemanticDistanceEvaluator(firstName, secondName, this.similarityComparationEngine, this.similarityMeasureConfiguration, this.wordnetNounIndexer, this.wordnetVerbIndexer, this.maxentTagger, this.wordnetStemmer)); 
						semanticElementsDistance = futureElementsSemanticDistance.get();
						this.wordnetDictionary.close();
					} else {
						semanticElementsDistance = 0.0; 
					}
				}
				distancesExecutor.shutdown();
				/* Compute overall distance */
				result = 1.0 - (0.9999 * (1.0 - semanticElementsDistance)) + (0.0001 * (1.0 - syntacticContextsDistance)); 
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		return (double) result <= semanticDistanceThreshold ? result : Double.MAX_VALUE; 
	}
	
	
}
