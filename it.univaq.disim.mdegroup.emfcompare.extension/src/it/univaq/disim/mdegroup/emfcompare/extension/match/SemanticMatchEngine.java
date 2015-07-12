package it.univaq.disim.mdegroup.emfcompare.extension.match;

import it.univaq.disim.mdegroup.emfcompare.extension.match.eobject.SemanticDistanceFunction;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.emf.compare.Comparison;
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
import org.eclipse.emf.compare.match.resource.IResourceMatcher;
import org.eclipse.emf.compare.scope.IComparisonScope;
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
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * The Match engine orchestrates the matching process : it takes an
 * {@link IComparisonScope scope} as input, iterates over its
 * {@link IComparisonScope#getLeft() left}, {@link IComparisonScope#getRight()
 * right} and {@link IComparisonScope#getOrigin() origin} roots and delegates to
 * {@link IResourceMatcher}s and {@link IEObjectMatcher}s in order to create the
 * result {@link Comparison} model for this scope.
 * 
 * @author <a href="mailto:lorenzo.addazi@student.univaq.it">Lorenzo Addazi</a>
 *         <p style="margin-top:1px;">
 *         <i>Derivative work from the default implementation
 *         {@link DefaultMatchEngine} written by <a
 *         href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>.</i>
 */
public class SemanticMatchEngine extends DefaultMatchEngine {

	public SemanticMatchEngine(IEObjectMatcher matcher,
			IComparisonFactory comparisonFactory) {
		super(matcher, comparisonFactory);
	}

	public static IMatchEngine create() {
		return create(UseIdentifiers.WHEN_AVAILABLE,
				WeightProviderDescriptorRegistryImpl.createStandaloneInstance());
	}

	public static IMatchEngine create(UseIdentifiers useIDs) {
		return create(useIDs,
				WeightProviderDescriptorRegistryImpl.createStandaloneInstance());
	}

	public static IMatchEngine create(UseIdentifiers useIDs,
			WeightProvider.Descriptor.Registry weightProviderRegistry) {
		return new SemanticMatchEngine(SemanticMatchEngine.createDefaultEObjectMatcher(useIDs, weightProviderRegistry), new DefaultComparisonFactory(new DefaultEqualityHelperFactory()));
	}

	public static IEObjectMatcher createDefaultEObjectMatcher(
			UseIdentifiers useIDs) {
		return createDefaultEObjectMatcher(useIDs,
				WeightProviderDescriptorRegistryImpl.createStandaloneInstance());
	}

	public static IEObjectMatcher createDefaultEObjectMatcher(UseIdentifiers useIDs, WeightProvider.Descriptor.Registry weightProviderRegistry) {
		try {
			switch (useIDs) {
			case NEVER:
				String wordnetDictionaryPath = "WordNet-3.1" + File.separator + "dict"; 
				URIFactory uriFactory = URIFactoryMemory.getSingleton();
				URI wordnetGraphURI = uriFactory.getURI("http://graph/wordnet/"); 
				G wordnetGraph = new GraphMemory(wordnetGraphURI);
				GraphLoader_Wordnet wordnetGraphLoader = new GraphLoader_Wordnet();
				GDataConf dataNoun = new GDataConf(GFormat.WORDNET_DATA, wordnetDictionaryPath + File.separator + "data.noun");
				GDataConf dataVerb = new GDataConf(GFormat.WORDNET_DATA, wordnetDictionaryPath + File.separator + "data.verb");
				wordnetGraphLoader.populate(dataNoun, wordnetGraph);
				wordnetGraphLoader.populate(dataVerb, wordnetGraph);
				GAction addRoot = new GAction(GActionType.REROOTING);
				GraphActionExecutor.applyAction(addRoot, wordnetGraph); 
				String indexNoun = wordnetDictionaryPath + File.separator + "index.noun"; 
				String indexVerb = wordnetDictionaryPath + File.separator + "index.verb"; 
				IndexerWordNetBasic wordnetNounIndexer = new IndexerWordNetBasic(uriFactory, wordnetGraph, indexNoun);
				IndexerWordNetBasic wordnetVerbIndexer = new IndexerWordNetBasic(uriFactory, wordnetGraph, indexVerb);
				SM_Engine similarityComparationEngine = new SM_Engine(wordnetGraph);
				ICconf informationContentConfiguration = new IC_Conf_Topo(SMConstants.FLAG_ICI_SECO_2004);
				SMconf similarityMeasureConfiguration = new SMconf(SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998);
				//SMconf similarityMeasureConfiguration = new SMconf(SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_JIANG_CONRATH_1997_NORM);
				similarityMeasureConfiguration.setICconf(informationContentConfiguration);
				RAMDictionary wordnetDictionary = new RAMDictionary(new URL("file", null, wordnetDictionaryPath), ILoadPolicy.IMMEDIATE_LOAD);
				wordnetDictionary.setLoadPolicy(ILoadPolicy.NO_LOAD);
				WordnetStemmer wordnetStemmer = new WordnetStemmer(wordnetDictionary);
				MaxentTagger maxentTagger = new MaxentTagger("tagger" + File.separator + "english" + File.separator + "english-left3words-distsim.tagger");
				return new ProximityEObjectMatcher(new SemanticDistanceFunction(similarityComparationEngine, similarityMeasureConfiguration, wordnetNounIndexer, wordnetVerbIndexer, maxentTagger, wordnetStemmer, wordnetDictionary));
			case ONLY:
				return new IdentifierEObjectMatcher();
			case WHEN_AVAILABLE:
			default:
				String wordnetDictionaryPath_Beta = "WordNet-3.1" + File.separator + "dict"; 
				URIFactory uriFactory_Beta = URIFactoryMemory.getSingleton();
				URI wordnetGraphURI_Beta = uriFactory_Beta.getURI("http://graph/wordnet/"); 
				G wordnetGraph_Beta = new GraphMemory(wordnetGraphURI_Beta);
				GraphLoader_Wordnet wordnetGraphLoader_Beta = new GraphLoader_Wordnet();
				GDataConf dataNoun_Beta = new GDataConf(GFormat.WORDNET_DATA, wordnetDictionaryPath_Beta + File.separator + "data.noun");
				GDataConf dataVerb_Beta = new GDataConf(GFormat.WORDNET_DATA, wordnetDictionaryPath_Beta + File.separator + "data.verb");
				wordnetGraphLoader_Beta.populate(dataNoun_Beta, wordnetGraph_Beta);
				wordnetGraphLoader_Beta.populate(dataVerb_Beta, wordnetGraph_Beta);
				GAction addRoot_Beta = new GAction(GActionType.REROOTING);
				GraphActionExecutor.applyAction(addRoot_Beta, wordnetGraph_Beta); 
				String indexNoun_Beta = wordnetDictionaryPath_Beta + File.separator + "index.noun"; 
				String indexVerb_Beta = wordnetDictionaryPath_Beta + File.separator + "index.verb"; 
				IndexerWordNetBasic wordnetNounIndexer_Beta = new IndexerWordNetBasic(uriFactory_Beta, wordnetGraph_Beta, indexNoun_Beta);
				IndexerWordNetBasic wordnetVerbIndexer_Beta = new IndexerWordNetBasic(uriFactory_Beta, wordnetGraph_Beta, indexVerb_Beta);
				SM_Engine similarityComparationEngine_Beta = new SM_Engine(wordnetGraph_Beta);
				ICconf informationContentConfiguration_Beta = new IC_Conf_Topo(SMConstants.FLAG_ICI_SECO_2004);
				SMconf similarityMeasureConfiguration_Beta = new SMconf(SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998);
				similarityMeasureConfiguration_Beta.setICconf(informationContentConfiguration_Beta);
				RAMDictionary wordnetDictionary_Beta = new RAMDictionary(new URL("file", null, wordnetDictionaryPath_Beta), ILoadPolicy.IMMEDIATE_LOAD);
				wordnetDictionary_Beta.setLoadPolicy(ILoadPolicy.NO_LOAD);
				WordnetStemmer wordnetStemmer_Beta = new WordnetStemmer(wordnetDictionary_Beta);
				MaxentTagger maxentTagger_Beta = new MaxentTagger("tagger" + File.separator + "english" + File.separator + "english-left3words-distsim.tagger");
				return new IdentifierEObjectMatcher(
						new ProximityEObjectMatcher(
								new SemanticDistanceFunction(similarityComparationEngine_Beta, similarityMeasureConfiguration_Beta, wordnetNounIndexer_Beta, wordnetVerbIndexer_Beta, maxentTagger_Beta, wordnetStemmer_Beta, wordnetDictionary_Beta)));

			}
		} catch (MalformedURLException | SLIB_Exception e) {
			e.printStackTrace();
			return new IdentifierEObjectMatcher();
		}
	}

}