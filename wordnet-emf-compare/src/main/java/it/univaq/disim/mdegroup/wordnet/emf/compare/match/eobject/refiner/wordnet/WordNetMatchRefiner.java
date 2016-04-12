package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.refiner.wordnet;

import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.SimilarityMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.refiner.MatchRefiner;
import it.univaq.disim.mdegroup.wordnet.emf.compare.util.Distance;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.emf.ecore.ENamedElement;
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
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;

import com.google.common.base.CaseFormat;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import edu.mit.jwi.CachingDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class WordNetMatchRefiner implements MatchRefiner<SimilarityMatch>{
	
	private static final Map<String, List<TaggedWord>> matchPreprocessingHistory = new HashMap<String, List<TaggedWord>>();
	private static final Map<TaggedWord, Map<TaggedWord, Double>> matchDistanceEvaluationHistory = new HashMap<TaggedWord, Map<TaggedWord, Double>>();
	private static final String wordnetDictionaryPath = "WordNet-3.1" + File.separator + "dict"; 
	private static final String maxentTaggerPath = "tagger" + File.separator + "english" + File.separator + "english-left3words-distsim.tagger";
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
			wordnetGraphLoader.populate(new GDataConf(GFormat.WORDNET_DATA, wordnetDictionaryPath + File.separator + "data.adj"), wordnetGraph);
			GraphActionExecutor.applyAction(new GAction(GActionType.REROOTING), wordnetGraph);
			wordnetNounIndexer = new IndexerWordNetBasic(uriFactory, wordnetGraph, wordnetDictionaryPath + File.separator + "index.noun");
			wordnetVerbIndexer = new IndexerWordNetBasic(uriFactory, wordnetGraph, wordnetDictionaryPath + File.separator + "index.verb");
			similarityComparisonEngine = new SM_Engine(wordnetGraph);
			similarityMeasureConfiguration = new SMconf(SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998);
			similarityMeasureConfiguration.setICconf(new IC_Conf_Topo(SMConstants.FLAG_ICI_SECO_2004));
		} catch(Exception e){
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
	
	public void preprocessMatches(Set<SimilarityMatch> sourceComparison) {
		try{
			wordnetDictionary.open(); 
			for(SimilarityMatch similarityMatch : sourceComparison){
				ENamedElement leftElement = similarityMatch.getMatch().getLeft() != null ? 
						(ENamedElement) similarityMatch.getMatch().getLeft() : null;
				ENamedElement rightElement = similarityMatch.getMatch().getRight() != null ? 
						(ENamedElement) similarityMatch.getMatch().getRight() : null; 
				List<TaggedWord> leftTaggedStemmedTokenList = Lists.newArrayList();
				if(leftElement != null){
					leftTaggedStemmedTokenList = matchPreprocessingHistory.get(leftElement.getName());
					if(leftTaggedStemmedTokenList == null){
						leftTaggedStemmedTokenList = Lists.newArrayList();
						List<String> leftTokenList = this.tokenizeString(leftElement.getName()); 
						if(leftTokenList.size() > 0){
							List<TaggedWord> leftTaggedTokenList = this.retrieveTokenListPOSTags(leftTokenList); 
							if(leftTaggedTokenList.size() > 0){
								leftTaggedStemmedTokenList = this.retrieveTaggedTokenListStems(leftTaggedTokenList);
							}
						}
						matchPreprocessingHistory.put(leftElement.getName(), leftTaggedStemmedTokenList); 
					}
				}
				similarityMatch.setLeftTaggedStemmedTokenList(leftTaggedStemmedTokenList);
				List<TaggedWord> rightTaggedStemmedTokenList = Lists.newArrayList();
				if(rightElement != null){
					rightTaggedStemmedTokenList = matchPreprocessingHistory.get(rightElement.getName()); 
					if(rightTaggedStemmedTokenList == null){
						rightTaggedStemmedTokenList = Lists.newArrayList();
						List<String> rightTokenList = this.tokenizeString(rightElement.getName()); 
						if(rightTokenList.size() > 0){
							List<TaggedWord> rightTaggedTokenList = this.retrieveTokenListPOSTags(rightTokenList); 
							if(rightTaggedTokenList.size() > 0){
								rightTaggedStemmedTokenList = this.retrieveTaggedTokenListStems(rightTaggedTokenList);
							}
						}
						matchPreprocessingHistory.put(rightElement.getName(), rightTaggedStemmedTokenList); 
					}
				}
				similarityMatch.setRightTaggedStemmedTokenList(rightTaggedStemmedTokenList);
			}
			wordnetDictionary.close(); 
		} catch(Exception e){
			wordnetDictionary.close(); 
			e.printStackTrace();
		}
	}
	
	public void evaluateMatchDistances(Set<SimilarityMatch>  sourceComparison){
		for(SimilarityMatch similarityMatch : sourceComparison){
			 if(similarityMatch.getMatch() != null && similarityMatch.getMatch().getLeft() != null && similarityMatch.getMatch().getRight() != null){
				 ENamedElement leftElement = (ENamedElement) similarityMatch.getMatch().getLeft(); 
				 ENamedElement rightElement = (ENamedElement) similarityMatch.getMatch().getRight(); 
				 if(leftElement.getName().toLowerCase().equals(rightElement.getName().toLowerCase())){
					 similarityMatch.setSemanticDistanceScore(0.0d); 
				 } else {
					 List<TaggedWord> leftTokenList = similarityMatch.getLeftTaggedStemmedTokenList(); 
					 List<TaggedWord> rightTokenList = similarityMatch.getRightTaggedStemmedTokenList(); 
					 if(leftTokenList.size() > 0 && rightTokenList.size() > 0){
							double[][] pairwiseSemanticDistances = new double[leftTokenList.size() >= rightTokenList.size() ? leftTokenList.size() : rightTokenList.size()][leftTokenList.size() >= rightTokenList.size() ? rightTokenList.size() : leftTokenList.size()];
							double[][] pairwiseTranslatedSemanticDistances = new double[leftTokenList.size() >= rightTokenList.size() ? rightTokenList.size() : leftTokenList.size()][leftTokenList.size() >= rightTokenList.size() ? leftTokenList.size() : rightTokenList.size()]; 
							ExecutorService pairwiseSemanticDistanceExecutor = Executors.newCachedThreadPool();
							List<List<Future<Double>>> futurePairwiseSemanticDistances = new ArrayList<List<Future<Double>>>();
							for(int i = 0; i < (leftTokenList.size() >= rightTokenList.size() ? leftTokenList.size() : rightTokenList.size()); i++){
								List<Future<Double>> futurePairwiseSemanticDistancesRow = new ArrayList<Future<Double>>();
								for(int j = 0; j < (leftTokenList.size() >= rightTokenList.size() ? rightTokenList.size() : leftTokenList.size()); j++){
									if(matchDistanceEvaluationHistory.containsKey((leftTokenList.size() >= rightTokenList.size() ? leftTokenList : rightTokenList).get(i))){
										if(matchDistanceEvaluationHistory.get((leftTokenList.size() >= rightTokenList.size() ? leftTokenList : rightTokenList).get(i))
												.containsKey((leftTokenList.size() >= rightTokenList.size() ? rightTokenList : leftTokenList).get(j))){
											pairwiseSemanticDistances[i][j] = matchDistanceEvaluationHistory
													.get((leftTokenList.size() >= rightTokenList.size() ? leftTokenList : rightTokenList).get(i))
													.get((leftTokenList.size() >= rightTokenList.size() ? rightTokenList : leftTokenList).get(j)); 
											pairwiseTranslatedSemanticDistances[j][i] = matchDistanceEvaluationHistory
													.get((leftTokenList.size() >= rightTokenList.size() ? leftTokenList : rightTokenList).get(i))
													.get((leftTokenList.size() >= rightTokenList.size() ? rightTokenList : leftTokenList).get(j));
											futurePairwiseSemanticDistancesRow.add(null);
										} else {
											futurePairwiseSemanticDistancesRow
											.add(pairwiseSemanticDistanceExecutor
													.submit(new PairwiseSemanticDistanceEvaluator((leftTokenList.size() >= rightTokenList.size() ? leftTokenList : rightTokenList).get(i), 
															(leftTokenList.size() >= rightTokenList.size() ? rightTokenList : leftTokenList).get(j), 
															similarityComparisonEngine, similarityMeasureConfiguration, wordnetNounIndexer, wordnetVerbIndexer)));
 
										}
									} else {
										futurePairwiseSemanticDistancesRow
										.add(pairwiseSemanticDistanceExecutor
												.submit(new PairwiseSemanticDistanceEvaluator((leftTokenList.size() >= rightTokenList.size() ? leftTokenList : rightTokenList).get(i), 
														(leftTokenList.size() >= rightTokenList.size() ? rightTokenList : leftTokenList).get(j), 
														similarityComparisonEngine, similarityMeasureConfiguration, wordnetNounIndexer, wordnetVerbIndexer)));
									}
								}
								futurePairwiseSemanticDistances.add(futurePairwiseSemanticDistancesRow);
							}
							pairwiseSemanticDistanceExecutor.shutdown();
							for(int i = 0; i < (leftTokenList.size() >= rightTokenList.size() ? leftTokenList.size() : rightTokenList.size()); i++){
								for(int j = 0; j < (leftTokenList.size() >= rightTokenList.size() ? rightTokenList.size() : leftTokenList.size()); j++){
									if(futurePairwiseSemanticDistances.get(i).get(j) != null){
										try {
											double currentPairwiseSemanticDistance = futurePairwiseSemanticDistances.get(i).get(j).get();
											pairwiseSemanticDistances[i][j] = currentPairwiseSemanticDistance;
											pairwiseTranslatedSemanticDistances[j][i] = currentPairwiseSemanticDistance;
											if(matchDistanceEvaluationHistory.containsKey((leftTokenList.size() >= rightTokenList.size() ? leftTokenList : rightTokenList).get(i))){
												matchDistanceEvaluationHistory
													.get((leftTokenList.size() >= rightTokenList.size() ? leftTokenList : rightTokenList).get(i))
													.put((leftTokenList.size() >= rightTokenList.size() ? rightTokenList : leftTokenList).get(j), currentPairwiseSemanticDistance); 
											} else {
												Map<TaggedWord, Double> distanceEvaluationHistoryValue = new HashMap<TaggedWord, Double>();
												distanceEvaluationHistoryValue.put((leftTokenList.size() >= rightTokenList.size() ? rightTokenList : leftTokenList).get(j), currentPairwiseSemanticDistance); 
												matchDistanceEvaluationHistory.put((leftTokenList.size() >= rightTokenList.size() ? leftTokenList : rightTokenList).get(i), distanceEvaluationHistoryValue);  
											}
										} catch (ExecutionException | InterruptedException e) {
											pairwiseSemanticDistances[i][j] = 1.0d;  
											pairwiseTranslatedSemanticDistances[j][i] = 1.0d;
										}
									}
								}
							}
							double groupwiseTokenSemanticDistance = 0.0d;
							for(int i = 0; i < pairwiseSemanticDistances.length; i++){
								double minPairwiseSemanticDistance = 1.0d;
								for(int j = 0; j < pairwiseSemanticDistances[i].length; j++){
									double currentPairwiseSemanticDistance = pairwiseSemanticDistances[i][j]; 
									if(currentPairwiseSemanticDistance <= minPairwiseSemanticDistance){
										minPairwiseSemanticDistance = currentPairwiseSemanticDistance; 
									}
								}
								groupwiseTokenSemanticDistance += minPairwiseSemanticDistance; 
							}
							groupwiseTokenSemanticDistance = groupwiseTokenSemanticDistance/pairwiseSemanticDistances.length; 
							double groupwiseTranslatedTokenSemanticDistance = 0.0d;
							for(int i = 0; i < pairwiseTranslatedSemanticDistances.length; i++){
								double minPairwiseSemanticDistance = 1.0d; 
								for(int j = 0; j < pairwiseTranslatedSemanticDistances[i].length; j++){
									double currentPairwiseSemanticDistance = pairwiseTranslatedSemanticDistances[i][j]; 
									if(currentPairwiseSemanticDistance <= minPairwiseSemanticDistance){
										minPairwiseSemanticDistance = currentPairwiseSemanticDistance; 
									}
								}
								groupwiseTranslatedTokenSemanticDistance += minPairwiseSemanticDistance; 
							}
							groupwiseTranslatedTokenSemanticDistance = groupwiseTranslatedTokenSemanticDistance/pairwiseTranslatedSemanticDistances.length;
							Double semanticDistance = (groupwiseTokenSemanticDistance + groupwiseTranslatedTokenSemanticDistance)/2.0d; 
							similarityMatch.setSemanticDistanceScore(semanticDistance); 
					}
				 }
			 }
		}
	}
	
	@Override
	public void refineMatches(Set<SimilarityMatch> sourceComparison) {
		this.preprocessMatches(sourceComparison);
		this.evaluateMatchDistances(sourceComparison);
	}
	
	private POS retrieveWordnetPOS(String pos){
		if(pos.toUpperCase().startsWith("VB") || pos.toUpperCase().equals("MD")){
			return POS.VERB; 
		} else if(pos.toUpperCase().startsWith("NN")){
			return POS.NOUN; 
		} else if(pos.toUpperCase().startsWith("RB") || pos.toUpperCase().equals("WRB")){
			return POS.ADVERB; 
		} else if(pos.toUpperCase().startsWith("JJ")){
			return POS.ADJECTIVE; 
		} else {
			return null; 
		}
	}
	
	private List<String> tokenizeString(String string) throws IOException {
		return Splitter.on("_").omitEmptyStrings().splitToList(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, string.replaceAll("\\P{Alpha}", "")));
	}
	
	private List<TaggedWord> retrieveTokenListPOSTags(List<String> stringTokenList){
		return posTagger.tagSentence(Sentence.toWordList((String[]) stringTokenList.toArray(new String[0]))); 
	}
	
	private List<TaggedWord> retrieveTaggedTokenListStems(List<TaggedWord> taggedStringTokenList){
		List<TaggedWord> stemmedTaggedStringTokenList = new ArrayList<TaggedWord>();
		for(TaggedWord taggedToken : taggedStringTokenList){
			POS taggedTokenPOS = retrieveWordnetPOS(taggedToken.tag());
			List<String> taggedTokenStems = tokenStemmer.findStems(taggedToken.word(), taggedTokenPOS);
			if(taggedTokenStems != null && taggedTokenStems.size() > 0){
				stemmedTaggedStringTokenList.add(new TaggedWord(taggedTokenStems.get(0), taggedToken.tag()));
			} else {
				stemmedTaggedStringTokenList.add(new TaggedWord(taggedToken.word(), null));
			}
		}
		return stemmedTaggedStringTokenList; 
	}

	// [DISTANZA]
	private class PairwiseSemanticDistanceEvaluator implements Callable<Double> {
		
		private TaggedWord firstToken; 
		private TaggedWord secondToken;
		private SM_Engine similarityComparationEngine; 
		private SMconf similarityMeasureConfiguration; 
		private IndexerWordNetBasic wordnetNounIndexer; 
		private IndexerWordNetBasic wordnetVerbIndexer;

		/* Constructor */
		public PairwiseSemanticDistanceEvaluator(TaggedWord firstToken, TaggedWord secondToken, SM_Engine similarityComparationEngine, SMconf similarityMeasureConfiguration, 
				IndexerWordNetBasic wordnetNounIndexer, IndexerWordNetBasic wordnetVerbIndexer){
			this.firstToken = firstToken; 
			this.secondToken = secondToken; 
			this.similarityComparationEngine = similarityComparationEngine; 
			this.similarityMeasureConfiguration = similarityMeasureConfiguration; 
			this.wordnetNounIndexer = wordnetNounIndexer; 
			this.wordnetVerbIndexer = wordnetVerbIndexer; 
		}
		
		/* Check if the given tokens have comparable POS tags */
		private Boolean haveEquivalentPOSTags(TaggedWord firstToken, TaggedWord secondToken){
			if(firstToken != null && secondToken != null && firstToken.tag() != null && secondToken.tag() != null){
				if(firstToken.tag().toUpperCase().startsWith("VB") && secondToken.tag().toUpperCase().startsWith("VB")){
					return true; 
				} else if(firstToken.tag().toUpperCase().equals("MD") && secondToken.tag().toUpperCase().equals("MD")){
					return true; 
				} else if(firstToken.tag().toUpperCase().startsWith("NN") && secondToken.tag().toUpperCase().startsWith("NN")){
					return true; 
				} else if(firstToken.tag().toUpperCase().startsWith("RB") && secondToken.tag().toUpperCase().startsWith("RB")){
					return true; 
				} else if(firstToken.tag().toUpperCase().equals("WRB") && secondToken.tag().toUpperCase().equals("WRB")){
					return true; 
				} else if(firstToken.tag().toUpperCase().startsWith("JJ") && secondToken.tag().toUpperCase().startsWith("JJ")){
					return true; 
				}
			}
			return false; 
		}
		
		/* Check if the given tokens allow the execution of the Lin Algorithm (i.e. either both nouns or both verbs) */
		private Boolean haveComparablePOSTags(TaggedWord firstToken, TaggedWord secondToken){
			if(firstToken != null && secondToken != null && firstToken.tag() != null && secondToken.tag() != null){
				if(firstToken.tag().toUpperCase().startsWith("VB") && secondToken.tag().toUpperCase().startsWith("VB")){
					return true; 
				} else if(firstToken.tag().toUpperCase().equals("MD") && secondToken.tag().toUpperCase().equals("MD")){
					return true; 
				} else if(firstToken.tag().toUpperCase().startsWith("NN") && secondToken.tag().toUpperCase().startsWith("NN")){
					return true; 
				}
			}
			return false;
		}
		
		/* Check if the given tokens have a noun tag */
		private Boolean haveNounPOSTags(TaggedWord firstToken, TaggedWord secondToken){
			if(firstToken != null && secondToken != null && firstToken.tag() != null && secondToken.tag() != null){
				if(firstToken.tag().toUpperCase().startsWith("NN") && secondToken.tag().toUpperCase().startsWith("NN")){
					return true; 
				}
			}
			return false;
		}
		
		/* Evaluator Entry Point [DISTANZA] */
		@Override
		public Double call() throws Exception {
			Double pairwiseSemanticDistance = 1.0;
			if(haveEquivalentPOSTags(this.firstToken, this.secondToken)){
				if(haveComparablePOSTags(this.firstToken, this.secondToken)){
					if(haveNounPOSTags(this.firstToken, this.secondToken)){
						Set<URI> firstTokenURIsSet = this.wordnetNounIndexer.get(this.firstToken.word());
						/* Retrieve the second token URIs set */
						Set<URI> secondTokenURIsSet = this.wordnetNounIndexer.get(this.secondToken.word());
						/* Find the best possible distance value */
						onThroughToTheOtherSide :
							for(URI firstTokenURI : firstTokenURIsSet){
								for(URI secondTokenURI : secondTokenURIsSet){
									Double currentPairwiseSimilarity = this.similarityComparationEngine.compare(this.similarityMeasureConfiguration, firstTokenURI, secondTokenURI);
									Double currentPairwiseDistance = 1.0 - currentPairwiseSimilarity;
									if(currentPairwiseDistance < pairwiseSemanticDistance){
										pairwiseSemanticDistance = currentPairwiseDistance; 
										if(pairwiseSemanticDistance == 0.0){
											/* Since we've already found the best possible result, we can stop the iteration */
											break onThroughToTheOtherSide;  
										}
									}
								}
							}
					} else {
						/* Retrieve the first token URIs set */
						Set<URI> firstTokenURIsSet = this.wordnetVerbIndexer.get(this.firstToken.word());
						/* Retrieve the second token URIs set */
						Set<URI> secondTokenURIsSet = this.wordnetVerbIndexer.get(this.secondToken.word());
						/* Find the best possible distance value */
						onThroughToTheOtherSide : 
							for(URI firstTokenURI : firstTokenURIsSet){
								for(URI secondTokenURI : secondTokenURIsSet){
									Double currentPairwiseSimilarity = this.similarityComparationEngine.compare(this.similarityMeasureConfiguration, firstTokenURI, secondTokenURI); 
									Double currentPairwiseDistance = 1.0 - currentPairwiseSimilarity;
									if(currentPairwiseDistance < pairwiseSemanticDistance){
										pairwiseSemanticDistance = currentPairwiseDistance; 
										if(pairwiseSemanticDistance == 0.0){
											/* Since we've already found the best possible result, we can stop the iteration */
											break onThroughToTheOtherSide;  
										}
									}
								}
							}
					}
				} else {
					/* These two tokens have an equivalent POS tag, still they are not comparable by using the Lin Algorithm. *
					 * Therefore, we can only rely on their possible syntactic distance. 									 * 
					 * TODO : investigate some further extensions of this aspect. 											 */
					pairwiseSemanticDistance = Distance.normalizedLevenshtein(this.firstToken.word(), this.secondToken.word());
				}
			}
			return pairwiseSemanticDistance; 
		}
		
	}
	
}