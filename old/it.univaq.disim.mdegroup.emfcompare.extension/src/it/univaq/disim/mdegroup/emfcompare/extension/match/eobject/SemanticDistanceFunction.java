package it.univaq.disim.mdegroup.emfcompare.extension.match.eobject;

import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.match.eobject.ProximityEObjectMatcher.DistanceFunction;
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
	
	public SemanticDistanceFunction(SM_Engine similarityComparationEngine, SMconf similarityMeasureConfiguration, IndexerWordNetBasic wordnetNounIndexer, IndexerWordNetBasic wordnetVerbIndexer, MaxentTagger maxentTagger, WordnetStemmer wordnetStemmer, IDictionary wordnetDictionary){
		this.similarityComparationEngine = similarityComparationEngine; 
		this.similarityMeasureConfiguration = similarityMeasureConfiguration; 
		this.wordnetNounIndexer = wordnetNounIndexer; 
		this.wordnetVerbIndexer = wordnetVerbIndexer; 
		this.maxentTagger = maxentTagger;
		this.wordnetStemmer = wordnetStemmer; 
		this.wordnetDictionary = wordnetDictionary; 
	}
	
	@Override
	public boolean areIdentic(Comparison comparison, EObject first, EObject second) {
		return distance(comparison, first, second) == 0.0; 
	}

	@Override
	public double distance(Comparison comparison, EObject first, EObject second) {
		double semanticDistanceThreshold = 0.26; 
		Class<?> firstClass = first.getClass();
		Class<?> secondClass = second.getClass();
		double result = Double.MAX_VALUE;
		if (firstClass.toString().equals(secondClass.toString())) {
			try {
				String firstName = (String) firstClass.getMethod("getName", new Class<?>[] {}).invoke(first, new Object[] {});
				String secondName = (String) secondClass.getMethod("getName", new Class<?>[] {}).invoke(second, new Object[] {});
				this.wordnetDictionary.open();
				result = new SemanticDistanceEvaluator(firstName, secondName, this.similarityComparationEngine, this.similarityMeasureConfiguration, this.wordnetNounIndexer, this.wordnetVerbIndexer, this.maxentTagger, this.wordnetStemmer).call();
				this.wordnetDictionary.close();
				//System.out.println(firstName);
				//System.out.println(secondName);
				//System.out.println(result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//System.out.println(result <= semanticDistanceThreshold ? result : Double.MAX_VALUE);
		return (double) result <= semanticDistanceThreshold ? result : Double.MAX_VALUE;
	}
	
}
