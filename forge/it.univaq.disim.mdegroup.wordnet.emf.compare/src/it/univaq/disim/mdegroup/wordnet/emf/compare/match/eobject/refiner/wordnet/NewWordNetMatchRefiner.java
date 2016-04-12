package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.refiner.wordnet;

import java.io.File;
import java.net.URL;
import java.util.Set;

import slib.graph.algo.utils.GAction;
import slib.graph.algo.utils.GActionType;
import slib.graph.algo.utils.GraphActionExecutor;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.loader.wordnet.GraphLoader_Wordnet;
import slib.graph.io.util.GFormat;
import slib.graph.model.graph.G;
import slib.graph.model.impl.graph.memory.GraphMemory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.indexer.wordnet.IndexerWordNetBasic;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import edu.mit.jwi.CachingDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.WordNetMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.refiner.MatchRefiner;

public class NewWordNetMatchRefiner implements MatchRefiner<WordNetMatch> {
	
	private static final String wordnetDictionaryPath = ""; 
	private static final String maxentTaggerPath = ""; 
	private static MaxentTagger posTagger; 
	private static WordnetStemmer tokenStemmer; 
	private static CachingDictionary wordnetDictionary; 
	private static SM_Engine similarityComparisonEngine; 
	private static SMconf similarityMeasureConfiguration; 
	private static IndexerWordNetBasic wordnetNounIndexer; 
	private static IndexerWordNetBasic wordnetVerbIndexer;
	
	static {
		try {
			wordnetDictionary = new CachingDictionary(new RAMDictionary(new URL("file", null, wordnetDictionaryPath), ILoadPolicy.IMMEDIATE_LOAD));
			((RAMDictionary)wordnetDictionary.getBackingDictionary()).setLoadPolicy(ILoadPolicy.NO_LOAD);
			tokenStemmer = new WordnetStemmer(wordnetDictionary);
			posTagger = new MaxentTagger(maxentTaggerPath);
			URIFactory uriFactory = URIFactoryMemory.getSingleton(); 
			G wordnetGraph = new GraphMemory(uriFactory.getURI("http://graph/wordnet/"));
			GraphLoader_Wordnet wordnetGraphLoader = new GraphLoader_Wordnet();
			wordnetGraphLoader.populate(new GDataConf(GFormat.WORDNET_DATA, wordnetDictionaryPath + File.separator + "data.noun"), wordnetGraph);
			wordnetGraphLoader.populate(new GDataConf(GFormat.WORDNET_DATA, wordnetDictionaryPath + File.separator + "data.verb"), wordnetGraph);
			GraphActionExecutor.applyAction(new GAction(GActionType.REROOTING), wordnetGraph);
			wordnetNounIndexer = new IndexerWordNetBasic(uriFactory, wordnetGraph, wordnetDictionaryPath + File.separator + "index.noun");
			wordnetVerbIndexer = new IndexerWordNetBasic(uriFactory, wordnetGraph, wordnetDictionaryPath + File.separator + "index.verb");
			similarityComparisonEngine = new SM_Engine(wordnetGraph);
			similarityMeasureConfiguration = new SMconf(SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998);
			similarityMeasureConfiguration.setICconf(new IC_Conf_Topo(SMConstants.FLAG_ICI_SECO_2004));
		} catch (Exception e){
			wordnetDictionary = null;
			posTagger = null; 
			tokenStemmer = null; 
			similarityComparisonEngine = null; 
			similarityMeasureConfiguration = null; 
			wordnetNounIndexer = null; 
			wordnetVerbIndexer = null;
			e.printStackTrace();
		}
	}
	
	@Override
	public void refineMatches(Set<WordNetMatch> sourceComparison) {
		try {
			wordnetDictionary.open(); 
			// per ogni wordnet match
			// - calcolo lista dei token : 
			// 		ottengo lista token
			// 		aggiungo tag per ogni lista token
			// 		aggiungo URI per ogni lista token
			wordnetDictionary.close();
		} catch (Exception e){
			wordnetDictionary.close();
			e.printStackTrace(); 
		}
	}

}
