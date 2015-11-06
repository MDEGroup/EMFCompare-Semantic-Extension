package it.univaq.disim.mdegroup.semantic.emf.compare.match;

import it.univaq.disim.mdegroup.semantic.emf.compare.SemanticEMFCompareRCPPlugin;
import it.univaq.disim.mdegroup.semantic.emf.compare.match.eobject.SemanticDistanceFunction;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.compare.match.DefaultComparisonFactory;
import org.eclipse.emf.compare.match.DefaultEqualityHelperFactory;
import org.eclipse.emf.compare.match.DefaultMatchEngine;
import org.eclipse.emf.compare.match.IComparisonFactory;
import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.match.eobject.IEObjectMatcher;
import org.eclipse.emf.compare.match.eobject.IdentifierEObjectMatcher;
import org.eclipse.emf.compare.match.eobject.ProximityEObjectMatcher;
import org.eclipse.emf.compare.match.eobject.WeightProvider;
import org.eclipse.emf.compare.match.eobject.WeightProviderDescriptorRegistryImpl;
import org.eclipse.emf.compare.utils.UseIdentifiers;
import org.openrdf.model.URI;

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
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Exception;
//import edu.mit.jwi.CachingDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;


public class SemanticMatchEngine extends DefaultMatchEngine {
	
	/* Constructor */
	public SemanticMatchEngine(IEObjectMatcher eObjectMatcher, IComparisonFactory comparisonFactory){
		super(eObjectMatcher, comparisonFactory); 
	}
	
	/* Create a Semantic Match Engine instance - No Parameters */
	public static IMatchEngine create(){
		return create(UseIdentifiers.WHEN_AVAILABLE, WeightProviderDescriptorRegistryImpl.createStandaloneInstance()); 
	}
	
	/* Create a Semantic Match Engine instance - UseIdentifier parameter only */
	public static IMatchEngine create(UseIdentifiers useIDs){
		return create(useIDs, WeightProviderDescriptorRegistryImpl.createStandaloneInstance());
	}
	
	/* Create a Semantic Match Engine instance - Complete Parameters */
	public static IMatchEngine create(UseIdentifiers useIDs, WeightProvider.Descriptor.Registry weightProviderRegistry){
		return new SemanticMatchEngine(SemanticMatchEngine.createDefaultEObjectMatcher(useIDs, weightProviderRegistry), new DefaultComparisonFactory(new DefaultEqualityHelperFactory()));
	}
	
	/* Create Default EObject Matcher Instance (Delegate Below) */
	public static IEObjectMatcher createDefaultEObjectMatcher(UseIdentifiers useIDs){
		return createDefaultEObjectMatcher(useIDs, WeightProviderDescriptorRegistryImpl.createStandaloneInstance());
	}
	
	/* Create Default EObject Matcher Instance (Semantic-based Proximity Matching) */
	public static IEObjectMatcher createDefaultEObjectMatcher(UseIdentifiers useIDs, WeightProvider.Descriptor.Registry weightProviderRegistry){
		try{
			if(useIDs.equals(UseIdentifiers.ONLY)){
				/** Default Match Engine behaviour in case the user wants to consider them only */
				return new IdentifierEObjectMatcher();
			} else {
				/* Wordnet Dictionary Folder Path */
				String wordnetDictionaryPath = "WordNet-3.1" + File.separator + "dict"; 
				if(SemanticEMFCompareRCPPlugin.getDefault() != null) {
					URL wordnetDictionaryURL = FileLocator.resolve(FileLocator.find(SemanticEMFCompareRCPPlugin.getDefault().getBundle(), new Path("WordNet-3.1" + File.separator + "dict"), null));
					String wordnetDictionaryURLExternalForm = wordnetDictionaryURL.toExternalForm();
					wordnetDictionaryPath = wordnetDictionaryURLExternalForm.substring(wordnetDictionaryURLExternalForm.lastIndexOf(".." + File.separator) + 2);
				}
				System.out.println(wordnetDictionaryPath);
				/* Wordnet Graph - Initialization (SMLib) */
				URIFactory uriFactory = URIFactoryMemory.getSingleton();
				URI wordnetGraphURI = uriFactory.getURI("http://graph/wordnet");
				G wordnetGraph = new GraphMemory(wordnetGraphURI);
				/* Wordnet Graph - Population (SMLib) */
				GraphLoader_Wordnet wordnetGraphLoader = new GraphLoader_Wordnet();
				GDataConf dataNoun = new GDataConf(GFormat.WORDNET_DATA, wordnetDictionaryPath + File.separator + "data.noun");
				wordnetGraphLoader.populate(dataNoun, wordnetGraph);
				GDataConf dataVerb = new GDataConf(GFormat.WORDNET_DATA, wordnetDictionaryPath + File.separator + "data.verb");
				wordnetGraphLoader.populate(dataVerb, wordnetGraph);
				/* Wordnet Graph - Orientation (this is made in order to allow comparations among terms which do not share any synset)  (SMLib) */
				GAction addRoot = new GAction(GActionType.REROOTING);
				GraphActionExecutor.applyAction(addRoot, wordnetGraph);
				/* Wordnet Graph - Content Indexing  (SMLib) */
				String indexNoun = wordnetDictionaryPath + File.separator + "index.noun"; 
				IndexerWordNetBasic wordnetNounIndexer = new IndexerWordNetBasic(uriFactory, wordnetGraph, indexNoun);
				String indexVerb = wordnetDictionaryPath + File.separator + "index.verb"; 
				IndexerWordNetBasic wordnetVerbIndexer = new IndexerWordNetBasic(uriFactory, wordnetGraph, indexVerb);
				/* Wordnet Graph - Comparation Engines  (SMLib) */
				SM_Engine similarityComparationEngine = new SM_Engine(wordnetGraph);
				ICconf informationContentConfiguration = new IC_Conf_Topo(SMConstants.FLAG_ICI_SECO_2004);
				SMconf similarityMeasureConfiguration = new SMconf(SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998);
				similarityMeasureConfiguration.setICconf(informationContentConfiguration);
				/* Wordnet RAM Dictionary (JWI, POS Tagging, Token Stemming) */
				RAMDictionary wordnetDictionary = new RAMDictionary(new URL("file", null, wordnetDictionaryPath), ILoadPolicy.IMMEDIATE_LOAD);
				//CachingDictionary wordnetDictionary = new CachingDictionary();
				wordnetDictionary.setLoadPolicy(ILoadPolicy.NO_LOAD);
				/* Wordnet Stemmer (Token Stemming) */
				WordnetStemmer wordnetStemmer = new WordnetStemmer(wordnetDictionary);
				/* Maxent Tagger (POS Tagging) */
				String maxentTaggerPath = "tagger" + File.separator + "english" + File.separator + "english-left3words-distsim.tagger"; 
				if(SemanticEMFCompareRCPPlugin.getDefault() != null){
					URL maxentTaggerURL = FileLocator.resolve(FileLocator.find(SemanticEMFCompareRCPPlugin.getDefault().getBundle(), new Path("tagger" + File.separator + "english" + File.separator + "english-left3words-distsim.tagger"), null));
					String maxentTaggerURLExternalForm = maxentTaggerURL.toExternalForm();
					maxentTaggerPath = maxentTaggerURLExternalForm.substring(maxentTaggerURLExternalForm.lastIndexOf(".." + File.separator) + 2);
				}
				MaxentTagger maxentTagger = new MaxentTagger(maxentTaggerPath);
				/** Semantic Match Engine behaviour in case the user does not want to consider IDs */
				if(useIDs.equals(UseIdentifiers.NEVER)){
					return new ProximityEObjectMatcher(new SemanticDistanceFunction(similarityComparationEngine, similarityMeasureConfiguration, wordnetNounIndexer, wordnetVerbIndexer, maxentTagger, wordnetStemmer, wordnetDictionary));
				} else {
				/** Default Match Engine with Semantic Fallback in case the user wants to use IDs if possible */
					return new IdentifierEObjectMatcher(
							new ProximityEObjectMatcher(
									new SemanticDistanceFunction(similarityComparationEngine, similarityMeasureConfiguration, wordnetNounIndexer, wordnetVerbIndexer, maxentTagger, wordnetStemmer, wordnetDictionary))); 
				}
			}
		} catch(MalformedURLException | SLIB_Exception e){
			e.printStackTrace();
			return new IdentifierEObjectMatcher();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return new IdentifierEObjectMatcher();
		} 
	}
	
}
