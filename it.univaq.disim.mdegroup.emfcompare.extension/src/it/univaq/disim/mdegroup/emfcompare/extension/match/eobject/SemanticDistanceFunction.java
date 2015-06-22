package it.univaq.disim.mdegroup.emfcompare.extension.match.eobject;

import it.univaq.disim.mdegroup.emfcompare.extension.utilities.HungarianAlgorithm;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.match.eobject.ProximityEObjectMatcher.DistanceFunction;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EAttributeImpl;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.impl.EDataTypeImpl;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.impl.EReferenceImpl;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.sussex.nlp.jws.AdaptedLeskTanimoto;

public class SemanticDistanceFunction implements DistanceFunction {

	private MaxentTagger maxentTagger;
	private IDictionary wordnetDictionary;

	public SemanticDistanceFunction() throws MalformedURLException {
		this.maxentTagger = new MaxentTagger("tagger" + File.separator
				+ "english" + File.separator
				+ "english-bidirectional-distsim.tagger");
		this.wordnetDictionary = new Dictionary(new URL("file", null, "wordnet"
				+ File.separator + "dict"));
	}

	/**
	 * [STEM DISTANCE] Evaluate the distance among two given stems (token root
	 * words)
	 * 
	 * @throws IOException
	 * */
	private double stemDistance(TaggedWord firstStem, TaggedWord secondStem)
			throws IOException {
		double result = 1.0;
		if (firstStem != null && secondStem != null && firstStem.tag() != null
				&& secondStem.tag() != null
				&& firstStem.tag().equals(secondStem.tag())) {
			IIndexWord firstStemIndexWord = this.wordnetDictionary
					.getIndexWord(firstStem.word(),
							retrieveWordnetPOS(firstStem.tag()));
			IIndexWord secondStemIndexWord = this.wordnetDictionary
					.getIndexWord(secondStem.word(),
							retrieveWordnetPOS(secondStem.tag()));
			if (firstStemIndexWord != null && secondStemIndexWord != null) {
				List<IWordID> firstStemWordIDs = firstStemIndexWord
						.getWordIDs();
				List<IWordID> secondStemWordIDs = secondStemIndexWord
						.getWordIDs();
				for (int i = 0; i < firstStemWordIDs.size(); i++) {
					for (int j = 0; j < secondStemWordIDs.size(); j++) {
						if (firstStemWordIDs.get(i).toString()
								.equals(secondStemWordIDs.get(j).toString())) {
							result = 0.0;
						} else {
							/*
							 * This is just to stop the adapted lesk code
							 * printing on the console
							 */
							PrintStream oldOut = System.out;
							System.setOut(new PrintStream(new OutputStream() {
								@Override
								public void write(int arg0) throws IOException {
									/* Shut up */
								}
							}));
							/*
							 * Normalized Lesk Distance Algorithm (Min : 0.0 -
							 * Max 1.0)
							 */
							AdaptedLeskTanimoto alg = new AdaptedLeskTanimoto(
									wordnetDictionary);
							System.setOut(oldOut);
							double currentStemDistance = 1.0 - alg.lesk(
									firstStem.word(), i + 1, secondStem.word(),
									j + 1, firstStem.tag());
							if (currentStemDistance < result) {
								result = currentStemDistance;
							}
						}
						if (result == 0.0) {
							System.out.println("Stem Distance : " + result);
							return result;
						}
					}
				}
			}
		}
		System.out.println("Stem Distance : " + result);
		return result;
	}

	/**
	 * [RETRIEVE WORDNET POS] Translate a given POS produced by the Maxent
	 * Tagger into another one compatible with Wordnet (Reference :
	 * https://catalog.ldc.upenn.edu/docs/LDC99T42/tagguid1.pdf
	 * */
	private POS retrieveWordnetPOS(String maxentTaggerPOS) {
		if (maxentTaggerPOS.equals("a")) {
			return POS.ADJECTIVE;
		} else if (maxentTaggerPOS.equals("r")) {
			return POS.ADVERB;
		} else if (maxentTaggerPOS.equals("v")) {
			return POS.VERB;
		} else if (maxentTaggerPOS.equals("n")) {
			return POS.NOUN;
		} else {
			return null;
		}
	}

	/**
	 * [TOKEN DISTANCE] Evaluate the distance among two given tokens
	 * 
	 * @throws IOException
	 * */
	private double tokenDistance(TaggedWord firstToken, TaggedWord secondToken)
			throws IOException {
		double result = 1.0;
		if (firstToken != null && secondToken != null
				&& firstToken.tag() != null && secondToken.tag() != null
				&& firstToken.tag().equals(secondToken.tag())) {
			if (firstToken.word().equals(secondToken.word())) {
				result = 0.0;
			} else {
				WordnetStemmer wordnetStemmer = new WordnetStemmer(
						this.wordnetDictionary);
				List<String> firstTokenStems = retrieveWordnetPOS(firstToken
						.tag()) != null ? wordnetStemmer
						.findStems(firstToken.word(),
								retrieveWordnetPOS(firstToken.tag())) : Lists
						.newArrayList();
				List<String> secondTokenStems = retrieveWordnetPOS(secondToken
						.tag()) != null ? wordnetStemmer.findStems(
						secondToken.word(),
						retrieveWordnetPOS(secondToken.tag())) : Lists
						.newArrayList();
				if (firstTokenStems.size() > 0 && secondTokenStems.size() > 0) {
					for (String firstTokenStem : firstTokenStems) {
						System.out.println("First Stem : "
								+ firstTokenStem.toUpperCase());
						for (String secondTokenStem : secondTokenStems) {
							System.out.println("Second Stem : "
									+ secondTokenStem.toUpperCase());
							if (firstTokenStem.equals(secondTokenStem)) {
								result = 0.0;
							} else {
								double stemDistance = stemDistance(
										new TaggedWord(firstTokenStem,
												firstToken.tag()),
										new TaggedWord(secondTokenStem,
												secondToken.tag()));
								if (stemDistance < result) {
									result = stemDistance;
								}
							}
							if (result == 0.0) {
								System.out
										.println("Token Distance : " + result);
								return result;
							}
						}
					}
				}
			}
		}
		System.out.println("Token Distance : " + result);
		return result;
	}

	/**
	 * [TAG TOKEN LIST] Assign a POS (i.e. Part-of-Speech) tag to each of the
	 * tokens belonging to a given list (Maxent Tagger Algorithm)
	 * */
	private List<TaggedWord> tagTokenList(List<String> tokenList) {
		List<HasWord> tokenListSentence = Sentence
				.toWordList((String[]) tokenList.toArray(new String[0]));
		List<TaggedWord> taggedTokenListSentence = this.maxentTagger
				.tagSentence(tokenListSentence, false);
		for (int i = 0; i < taggedTokenListSentence.size(); i++) {
			String currentTokenTag = taggedTokenListSentence.get(i).tag()
					.toUpperCase();
			if (currentTokenTag.toUpperCase().startsWith("JJ")) { // adjective
				taggedTokenListSentence.get(i).setTag("a");
			} else if (currentTokenTag.toUpperCase().startsWith("RB")
					|| currentTokenTag.toUpperCase().equals("WRB")) { // adverb
				taggedTokenListSentence.get(i).setTag("r");
			} else if (currentTokenTag.toUpperCase().startsWith("VB")
					|| currentTokenTag.toUpperCase().equals("MD")) { // verb
				taggedTokenListSentence.get(i).setTag("v");
			} else if (currentTokenTag.toUpperCase().startsWith("NN")) { // noun
				taggedTokenListSentence.get(i).setTag("n");
			} else {
				taggedTokenListSentence.get(i).setTag(null);
			}
		}
		return taggedTokenListSentence;
	}

	/**
	 * [TOKENIZE STRING] Split a given string into a token list
	 **/
	private List<String> tokenizeString(String string) {
		List<String> result = Lists.newArrayList(string.replaceAll(
				"\\P{Alnum}|(?=[A-Z][a-z])", " ").split(" "));
		Iterables.removeIf(result, new Predicate<String>() {
			@Override
			public boolean apply(String input) {
				return input.length() == 0;
			}
		});
		for (int i = 0; i < result.size(); i++) {
			result.set(i, result.get(i).toLowerCase());
		}
		return result;
	}

	/**
	 * [STRING DISTANCE] Compute the distance among two strings (we already know
	 * that these two are not the same)
	 * 
	 * @throws IOException
	 **/
	private double stringDistance(String first, String second)
			throws IOException {
		double result = 1.0;
		/* Tokenization */
		List<String> firstTokens = tokenizeString(first);
		List<String> secondTokens = tokenizeString(second);
		if (firstTokens.size() > 0 && secondTokens.size() > 0) {
			/* POS Tagging */
			List<TaggedWord> firstTaggedTokens = tagTokenList(firstTokens);
			List<TaggedWord> secondTaggedTokens = tagTokenList(secondTokens);
			double[][] tokenDistancesMatrix = new double[firstTaggedTokens
					.size() >= secondTaggedTokens.size() ? firstTaggedTokens
					.size() : secondTaggedTokens.size()][firstTaggedTokens
					.size() >= secondTaggedTokens.size() ? secondTaggedTokens
					.size() : firstTaggedTokens.size()];
			for (double[] row : tokenDistancesMatrix) {
				Arrays.fill(row, 1.0);
			}
			this.wordnetDictionary.open();
			for (int i = 0; firstTaggedTokens.size() >= secondTaggedTokens
					.size() ? i < firstTaggedTokens.size()
					: i < secondTaggedTokens.size(); i++) {
				for (int j = 0; firstTaggedTokens.size() >= secondTaggedTokens
						.size() ? j < secondTaggedTokens.size()
						: j < firstTaggedTokens.size(); j++) {
					/* Evaluate Token Distance (Min) */
					tokenDistancesMatrix[i][j] = tokenDistance(
							firstTaggedTokens.size() >= secondTaggedTokens.size() ? firstTaggedTokens.get(i)
									: secondTaggedTokens.get(i),
							firstTaggedTokens.size() >= secondTaggedTokens
									.size() ? secondTaggedTokens.get(j)
									: firstTaggedTokens.get(j));
					System.out.println("Token Matrix Entry [" + i + "][" + j
							+ "] : " + tokenDistancesMatrix[i][j]);
				}
			}
			this.wordnetDictionary.close();
			/* Min Weight Matching (Hungarian Algorithm) */
			HungarianAlgorithm hungarianAlgorithm = new HungarianAlgorithm(
					tokenDistancesMatrix);
			int[] tokenMatch = hungarianAlgorithm.execute();
			/* Result Computation */
			double tokenMatchValue = 0.0;
			double tokenMatchSize = 0.0;
			for (int i = 0; i < tokenMatch.length; i++) {
				if (tokenMatch[i] != -1) {
					tokenMatchValue += tokenDistancesMatrix[i][tokenMatch[i]];
					tokenMatchSize += 1.0;
				}
			}
			result = ((2.0 * tokenMatchValue) + (firstTokens.size()
					+ secondTokens.size() - (2.0 * tokenMatchSize)))
					/ (firstTokens.size() + secondTokens.size());
		}
		return result;
	}

	@Override
	public boolean areIdentic(Comparison comparison, EObject first,
			EObject second) {
		return distance(comparison, first, second) == 0.0;
	}

	@Override
	public double distance(Comparison comparison, EObject first, EObject second) {
		double result = 1.0;
		try {
			if (first instanceof EPackageImpl && second instanceof EPackageImpl) {
				String firstName = ((EPackageImpl) first).getName();
				String secondName = ((EPackageImpl) second).getName();
				System.out.println(firstName + " [" + first.getClass() + "]");
				System.out.println(secondName + " [" + second.getClass() + "]");
				result = firstName.equals(secondName) ? 0.0 : stringDistance(
						firstName, secondName);
			} else if (first instanceof EClassImpl
					&& second instanceof EClassImpl) {
				String firstName = ((EClassImpl) first).getName();
				String secondName = ((EClassImpl) second).getName();
				System.out.println(firstName + " [" + first.getClass() + "]");
				System.out.println(secondName + " [" + second.getClass() + "]");
				result = firstName.equals(secondName) ? 0.0 : stringDistance(
						firstName, secondName);
			} else if (first instanceof EReferenceImpl
					&& second instanceof EReferenceImpl) {
				String firstName = ((EReferenceImpl) first).getName();
				String secondName = ((EReferenceImpl) second).getName();
				System.out.println(firstName + " [" + first.getClass() + "]");
				System.out.println(secondName + " [" + second.getClass() + "]");
				result = firstName.equals(secondName) ? 0.0 : stringDistance(
						firstName, secondName);
			} else if (first instanceof EAttributeImpl
					&& second instanceof EAttributeImpl) {
				String firstName = ((EAttributeImpl) first).getName();
				String secondName = ((EAttributeImpl) second).getName();
				System.out.println(firstName + " [" + first.getClass() + "]");
				System.out.println(secondName + " [" + second.getClass() + "]");
				result = firstName.equals(secondName) ? 0.0 : stringDistance(
						firstName, secondName);
			} else if (first instanceof EDataTypeImpl
					&& second instanceof EDataTypeImpl) {
				String firstName = ((EDataTypeImpl) first).getName();
				String secondName = ((EDataTypeImpl) second).getName();
				System.out.println(firstName + " [" + first.getClass() + "]");
				System.out.println(secondName + " [" + second.getClass() + "]");
				result = firstName.equals(secondName) ? 0.0 : stringDistance(
						firstName, secondName);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Result : " + (double) result * Double.MAX_VALUE);
		System.out.println();
		return (double) result * Double.MAX_VALUE;
	}

}
