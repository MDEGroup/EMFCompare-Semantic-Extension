package it.univaq.disim.mdegroup.semantic.emf.compare.match.eobject.evaluator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.openrdf.model.URI;

import slib.indexer.wordnet.IndexerWordNetBasic;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.utils.SMconf;

import com.google.common.base.CaseFormat;
import com.google.common.base.Splitter;

import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class SemanticDistanceEvaluator implements Callable<Double> {
	
	String first; 
	String second; 
	private SM_Engine similarityComparationEngine; 
	private SMconf similarityMeasureConfiguration; 
	private IndexerWordNetBasic wordnetNounIndexer; 
	private IndexerWordNetBasic wordnetVerbIndexer; 
	private MaxentTagger maxentTagger; 
	private WordnetStemmer wordnetStemmer; 
	
	/* Constructor */
	public SemanticDistanceEvaluator(String first, String second, SM_Engine similarityComparationEngine, SMconf similarityMeasureConfiguration, IndexerWordNetBasic wordnetNounIndexer, IndexerWordNetBasic wordnetVerbIndexer, MaxentTagger maxentTagger, WordnetStemmer wordnetStemmer){
		this.first = first; 
		this.second = second; 
		this.similarityComparationEngine = similarityComparationEngine; 
		this.similarityMeasureConfiguration = similarityMeasureConfiguration; 
		this.wordnetNounIndexer = wordnetNounIndexer; 
		this.wordnetVerbIndexer = wordnetVerbIndexer; 
		this.maxentTagger = maxentTagger; 
		this.wordnetStemmer = wordnetStemmer; 
	}
	
	/* Retrieve Wordnet POS from Wordnet Stemmer format */
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
	
	/* String Tokenization */
	private List<String> tokenizeString(String string) throws IOException {
		return Splitter.on("_").omitEmptyStrings().splitToList(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, string.replaceAll("\\P{Alpha}", "")));
	}
	
	/* Token List Part-of-Speech (POS) Tagging */
	private List<TaggedWord> retrieveTokenListPOSTags(List<String> stringTokenList){
		return this.maxentTagger.tagSentence(Sentence.toWordList((String[]) stringTokenList.toArray(new String[0]))); 
	}
	
	/* Token Stemming (null if not found) */
	private List<TaggedWord> retrieveTaggedTokenListStems(List<TaggedWord> taggedStringTokenList){
		List<TaggedWord> stemmedTaggedStringTokenList = new ArrayList<TaggedWord>();
		for(TaggedWord taggedToken : taggedStringTokenList){
			POS taggedTokenPOS = retrieveWordnetPOS(taggedToken.tag());
			List<String> taggedTokenStems = this.wordnetStemmer.findStems(taggedToken.word(), taggedTokenPOS);
			if(taggedTokenStems != null && taggedTokenStems.size() > 0){
				stemmedTaggedStringTokenList.add(new TaggedWord(taggedTokenStems.get(0), taggedToken.tag()));
			} else {
				stemmedTaggedStringTokenList.add(new TaggedWord(taggedToken.word(), null));
			}
		}
		return stemmedTaggedStringTokenList; 
	}
	
	/* String semantic distance evaluation */
	private Double evaluateSemanticDistance() throws IOException {
		Double groupwiseSemanticDistance = 1.0; 
		if(first != null && second != null){
			/* Tokenization */
			List<String> firstTokenList = tokenizeString(first);
			List<String> secondTokenList = tokenizeString(second);
			if(firstTokenList.size() > 0 && secondTokenList.size() > 0){
				/* Part-of-Speech (POS) Tagging */
				List<TaggedWord> taggedFirstTokenList = retrieveTokenListPOSTags(firstTokenList); 
				List<TaggedWord> taggedSecondTokenList = retrieveTokenListPOSTags(secondTokenList); 
				if(taggedFirstTokenList.size() > 0 && taggedSecondTokenList.size() > 0){
					/* Token Stemming */
					List<TaggedWord> stemmedTaggedFirstTokenList = retrieveTaggedTokenListStems(taggedFirstTokenList); 
					List<TaggedWord> stemmedTaggedSecondTokenList = retrieveTaggedTokenListStems(taggedSecondTokenList);
					if(stemmedTaggedFirstTokenList.size() > 0 && stemmedTaggedSecondTokenList.size() > 0){
						/* Start Pairwise Semantic Distance Comparation */
						ExecutorService pairwiseSemanticDistancesExecutor = Executors.newCachedThreadPool();
						List<List<Future<Double>>> futurePairwiseSemanticDistances = new ArrayList<List<Future<Double>>>();
						for(TaggedWord stemmedTaggedFirstTokenListElement : stemmedTaggedFirstTokenList.size() >= stemmedTaggedSecondTokenList.size() ? stemmedTaggedFirstTokenList : stemmedTaggedSecondTokenList){
							List<Future<Double>> futurePairwiseSemanticDistancesRow = new ArrayList<Future<Double>>();
							for(TaggedWord stemmedTaggedSecondTokenListElement : stemmedTaggedFirstTokenList.size() >= stemmedTaggedSecondTokenList.size() ? stemmedTaggedSecondTokenList : stemmedTaggedFirstTokenList){
								futurePairwiseSemanticDistancesRow.add(pairwiseSemanticDistancesExecutor
								.submit(new PairwiseSemanticDistanceEvaluator(
										stemmedTaggedFirstTokenListElement, stemmedTaggedSecondTokenListElement, 
										this.similarityComparationEngine, this.similarityMeasureConfiguration, 
										this.wordnetNounIndexer, this.wordnetVerbIndexer)));
							}
							futurePairwiseSemanticDistances.add(futurePairwiseSemanticDistancesRow);
						}
						pairwiseSemanticDistancesExecutor.shutdown();
						/* Retrieve Semantic Distance Comparation Results */
						double[][] pairwiseSemanticDistances = new double[stemmedTaggedFirstTokenList.size() >= stemmedTaggedSecondTokenList.size() ? stemmedTaggedFirstTokenList.size() : stemmedTaggedSecondTokenList.size()][stemmedTaggedFirstTokenList.size() >= stemmedTaggedSecondTokenList.size() ? stemmedTaggedSecondTokenList.size() : stemmedTaggedFirstTokenList.size()];
						double[][] pairwiseTranslatedSemanticDistances = new double[stemmedTaggedFirstTokenList.size() >= stemmedTaggedSecondTokenList.size() ? stemmedTaggedSecondTokenList.size() : stemmedTaggedFirstTokenList.size()][stemmedTaggedFirstTokenList.size() >= stemmedTaggedSecondTokenList.size() ? stemmedTaggedFirstTokenList.size() : stemmedTaggedSecondTokenList.size()]; 
						for(int i = 0; i < pairwiseSemanticDistances.length; i++){
							for(int j = 0; j < pairwiseSemanticDistances[i].length; j++){
									try {
										double currentPairwiseSemanticDistance = futurePairwiseSemanticDistances.get(i).get(j).get();
										pairwiseSemanticDistances[i][j] = currentPairwiseSemanticDistance;
										pairwiseTranslatedSemanticDistances[j][i] = currentPairwiseSemanticDistance; 
									} catch (ExecutionException | InterruptedException e) {
										/* In case of interruption, we assign the highest possible semantic distance */
										pairwiseSemanticDistances[i][j] = 1.0;  
										pairwiseTranslatedSemanticDistances[j][i] = 1.0; 
									}
							}
						}
						/* Custom Best-Match Average Result Combination */
						double pairwiseSemanticDistanceThreshold = 0.2; 
						/* First side */
						double groupwiseTokenSemanticDistance = 0.0;
						for(int i = 0; i < pairwiseSemanticDistances.length; i++){
							double minPairwiseSemanticDistance = 1.0;
							for(int j = 0; j < pairwiseSemanticDistances[i].length; j++){
								double currentPairwiseSemanticDistance = pairwiseSemanticDistances[i][j]; 
								if(currentPairwiseSemanticDistance <= minPairwiseSemanticDistance && currentPairwiseSemanticDistance <= pairwiseSemanticDistanceThreshold){
									minPairwiseSemanticDistance = currentPairwiseSemanticDistance; 
								}
							}
							groupwiseTokenSemanticDistance += minPairwiseSemanticDistance; 
						}
						groupwiseTokenSemanticDistance = groupwiseTokenSemanticDistance/pairwiseSemanticDistances.length; 
						/* Second Side */
						double groupwiseTranslatedTokenSemanticDistance = 0.0;
						for(int i = 0; i < pairwiseTranslatedSemanticDistances.length; i++){
							double minPairwiseSemanticDistance = 1.0; 
							for(int j = 0; j < pairwiseTranslatedSemanticDistances[i].length; j++){
								double currentPairwiseSemanticDistance = pairwiseTranslatedSemanticDistances[i][j]; 
								if(currentPairwiseSemanticDistance <= minPairwiseSemanticDistance && currentPairwiseSemanticDistance <= pairwiseSemanticDistanceThreshold){
									minPairwiseSemanticDistance = currentPairwiseSemanticDistance; 
								}
							}
							groupwiseTranslatedTokenSemanticDistance += minPairwiseSemanticDistance; 
						}
						groupwiseTranslatedTokenSemanticDistance = groupwiseTranslatedTokenSemanticDistance/pairwiseTranslatedSemanticDistances.length;
						/* Sides Combination */
						groupwiseSemanticDistance = (groupwiseTokenSemanticDistance + groupwiseTranslatedTokenSemanticDistance)/2.0; 
					}
				}
			}
		}
		return groupwiseSemanticDistance; 
	}
	
	/* Evaluator Entry Point */
	@Override 
	public Double call() throws Exception {
		return evaluateSemanticDistance(); 
	}
	
	/* Pairwise Distance Evaluator (among token pairs) */
	private class PairwiseSemanticDistanceEvaluator implements Callable<Double> {
		
		private TaggedWord firstToken; 
		private TaggedWord secondToken;
		private SM_Engine similarityComparationEngine; 
		private SMconf similarityMeasureConfiguration; 
		private IndexerWordNetBasic wordnetNounIndexer; 
		private IndexerWordNetBasic wordnetVerbIndexer; 

		/* Constructor */
		public PairwiseSemanticDistanceEvaluator(TaggedWord firstToken, TaggedWord secondToken, SM_Engine similarityComparationEngine, SMconf similarityMeasureConfiguration, IndexerWordNetBasic wordnetNounIndexer, IndexerWordNetBasic wordnetVerbIndexer){
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
		
		/* Evaluator Entry Point */
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
					 * Therefore, we can only rely on their possible syntactic equivalence, in which case distance is 0.0. 									 * 
					 * TODO : investigate some further extensions of this aspect. 											 */
					if(this.firstToken.word().equals(this.secondToken.word())){
						pairwiseSemanticDistance = 0.0; 
					}
				}
			}
			return pairwiseSemanticDistance; 
		}
		
	}

}
