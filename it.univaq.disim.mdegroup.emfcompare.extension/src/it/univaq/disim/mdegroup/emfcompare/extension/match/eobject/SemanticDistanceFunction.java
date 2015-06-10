package it.univaq.disim.mdegroup.emfcompare.extension.match.eobject;

import it.univaq.disim.mdegroup.emfcompare.extension.utilities.HungarianAlgorithm;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URL;
import java.util.List;

import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.match.eobject.ProximityEObjectMatcher.DistanceFunction;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EAttributeImpl;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.impl.EDataTypeImpl;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.impl.EReferenceImpl;

import com.google.common.collect.Lists;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.morph.WordnetStemmer;

/** TODO : Documentation */

public class SemanticDistanceFunction implements DistanceFunction {

	private static List<String> tokenizeString(String string) {
		List<String> result = Lists
				.newArrayList(string.split("(?=[A-Z][a-z])"));
		for (int i = 0; i < result.size(); i++) {
			result.set(i,
					result.get(i).toLowerCase().replaceAll("[^a-zA-Z']", ""));
		}
		return result;
	}

	private static double wordDistance(IDictionary dictionary, IWordID source,
			IWordID target) throws IOException {
		ISynsetID sourceSynsetID = dictionary.getWord(source).getSynset()
				.getID();
		List<ISynsetID> sourceSynsetIDs = Lists.newArrayList();
		while (sourceSynsetID != null) {
			dictionary.open();
			List<ISynsetID> relatedSynsetsIDs = dictionary.getSynset(
					sourceSynsetID).getRelatedSynsets(Pointer.HYPERNYM);
			if (relatedSynsetsIDs.size() != 0) {
				sourceSynsetID = relatedSynsetsIDs.get(0);
				sourceSynsetIDs.add(sourceSynsetID);
			} else {
				sourceSynsetID = null;
			}
		}
		ISynsetID targetSynsetID = dictionary.getWord(target).getSynset()
				.getID();
		List<ISynsetID> targetSynsetIDs = Lists.newArrayList();
		while (targetSynsetID != null) {
			List<ISynsetID> relatedSynsetsIDs = dictionary.getSynset(
					targetSynsetID).getRelatedSynsets(Pointer.HYPERNYM);
			if (relatedSynsetsIDs.size() != 0) {
				targetSynsetID = relatedSynsetsIDs.get(0);
				targetSynsetIDs.add(targetSynsetID);
			} else {
				targetSynsetID = null;
			}
		}
		for (int i = 0; i < sourceSynsetIDs.size(); i++) {
			for (int j = 0; j < targetSynsetIDs.size(); j++) {
				if (sourceSynsetIDs.get(i).toString()
						.equals(targetSynsetIDs.get(j).toString())) {
					double sourceValue = new BigDecimal((double) i
							/ sourceSynsetIDs.size())
							.round(new MathContext(10)).doubleValue();
					double targetValue = new BigDecimal((double) j
							/ targetSynsetIDs.size())
							.round(new MathContext(10)).doubleValue();
					double resultValue = new BigDecimal(
							(sourceValue + targetValue) / 2.00).round(
							new MathContext(10)).doubleValue();
					return resultValue;
				}
			}
		}
		return 1.00;
	}

	private static double tokenDistance(String source, String target)
			throws IOException {
		if (source.equals(target)) {
			return 0.00;
		} else {
			IDictionary dictionary = new Dictionary(new URL("file", null,
					"wordnet" + File.separator + "dict"));
			dictionary.open();
			WordnetStemmer wordnetStemmer = new WordnetStemmer(dictionary);
			IIndexWord sourceIndexWord = null;
			IIndexWord targetIndexWord = null;
			List<String> sourceStems = wordnetStemmer.findStems(source,
					POS.NOUN);
			List<String> targetStems = wordnetStemmer.findStems(target,
					POS.NOUN);
			if (sourceStems.size() > 0 && targetStems.size() > 0) {
				sourceIndexWord = dictionary.getIndexWord(sourceStems.get(0),
						POS.NOUN);
				targetIndexWord = dictionary.getIndexWord(targetStems.get(0),
						POS.NOUN);
			} else {
				sourceStems = wordnetStemmer.findStems(source, POS.VERB);
				targetStems = wordnetStemmer.findStems(target, POS.VERB);
				if (sourceStems.size() > 0 && targetStems.size() > 0) {
					sourceIndexWord = dictionary.getIndexWord(
							sourceStems.get(0), POS.VERB);
					targetIndexWord = dictionary.getIndexWord(
							targetStems.get(0), POS.VERB);
				} else {
					sourceStems = wordnetStemmer.findStems(source,
							POS.ADJECTIVE);
					targetStems = wordnetStemmer.findStems(target,
							POS.ADJECTIVE);
					if (sourceStems.size() > 0 && targetStems.size() > 0) {
						sourceIndexWord = dictionary.getIndexWord(
								sourceStems.get(0), POS.ADJECTIVE);
						targetIndexWord = dictionary.getIndexWord(
								targetStems.get(0), POS.ADJECTIVE);
					} else {
						sourceStems = wordnetStemmer.findStems(source,
								POS.ADVERB);
						targetStems = wordnetStemmer.findStems(target,
								POS.ADVERB);
						if (sourceStems.size() > 0 && targetStems.size() > 0) {
							sourceIndexWord = dictionary.getIndexWord(
									sourceStems.get(0), POS.ADVERB);
							targetIndexWord = dictionary.getIndexWord(
									targetStems.get(0), POS.ADVERB);
						}
					}
				}
			}
			dictionary.close();
			if (sourceIndexWord != null && targetIndexWord != null) {
				dictionary.open();
				List<IWordID> sourceWordIDs = sourceIndexWord.getWordIDs();
				List<IWordID> targetWordIDs = targetIndexWord.getWordIDs();
				double minWordDistance = 1.00;
				for (IWordID sourceWordID : sourceWordIDs) {
					for (IWordID targetWordID : targetWordIDs) {
						double currentWordDistance = wordDistance(dictionary,
								sourceWordID, targetWordID);
						if (currentWordDistance < minWordDistance) {
							minWordDistance = currentWordDistance;
						}
					}
				}
				dictionary.close();
				return new BigDecimal(minWordDistance).round(
						new MathContext(10)).doubleValue();
			} else {
				dictionary.close();
				return source.equals(target) ? 0.00 : 1.00;
			}
		}
	}

	private static double computeStringDistance(double[][] tokenDistancesMatrix) {
		double result = 0.00;
		HungarianAlgorithm hungarianAlgorithm = new HungarianAlgorithm(
				tokenDistancesMatrix);
		int[] minTokenDistances = hungarianAlgorithm.execute();
		for (int i = 0; i < minTokenDistances.length; i++) {
			if (minTokenDistances[i] != -1) {
				result += new BigDecimal(
						(double) tokenDistancesMatrix[i][minTokenDistances[i]])
						.round(new MathContext(10)).doubleValue();
			} else {
				result += 1.0;
			}
		}
		return new BigDecimal((double) result / minTokenDistances.length)
				.round(new MathContext(10)).doubleValue();
	}

	private double computeDistance(String first, String second)
			throws IOException {
		List<String> sourceTokenList = tokenizeString(first);
		List<String> targetTokenList = tokenizeString(second);
		double[][] tokenDistancesMatrix = new double[sourceTokenList.size()][targetTokenList
				.size()];
		for (int i = 0; i < sourceTokenList.size(); i++) {
			for (int j = 0; j < targetTokenList.size(); j++) {
				double tokenDistanceValue = tokenDistance(
						sourceTokenList.get(i), targetTokenList.get(j));
				tokenDistancesMatrix[i][j] = new BigDecimal(tokenDistanceValue)
						.round(new MathContext(10)).doubleValue();
			}
		}
		double stringDistance = computeStringDistance(tokenDistancesMatrix);
		return stringDistance;
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
				result = firstName.equals("PrimitiveTypes")
						|| secondName.equals("PrimitiveTypes") ? firstName
						.equals(secondName) ? 0.0 : 1.0 : computeDistance(
						firstName, secondName);
			} else if (first instanceof EClassImpl
					&& second instanceof EClassImpl) {
				String firstName = ((EClassImpl) first).getName();
				String secondName = ((EClassImpl) second).getName();
				result = computeDistance(firstName, secondName);
			} else if (first instanceof EReferenceImpl
					&& second instanceof EReferenceImpl) {
				String firstName = ((EReferenceImpl) first).getName();
				String secondName = ((EReferenceImpl) second).getName();
				result = computeDistance(firstName, secondName);
			} else if (first instanceof EAttributeImpl
					&& second instanceof EAttributeImpl) {
				String firstName = ((EAttributeImpl) first).getName();
				String secondName = ((EAttributeImpl) second).getName();
				result = computeDistance(firstName, secondName);
			} else if (first instanceof EDataTypeImpl
					&& second instanceof EDataTypeImpl) {
				String firstName = ((EDataTypeImpl) first).getName();
				String firstPackageName = ((EDataTypeImpl) first).getEPackage()
						.getName();
				String secondName = ((EDataTypeImpl) second).getName();
				String secondPackageName = ((EDataTypeImpl) second)
						.getEPackage().getName();
				if (firstPackageName.equals("PrimitiveTypes")
						|| secondPackageName.equals("PrimitiveTypes")) {
					if (firstPackageName.equals(secondPackageName)) {
						result = firstName.equals(secondName) ? 0.0 : 1.0;
					} else {
						result = 1.0;
					}
				} else {
					result = computeDistance(firstName, secondName);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result <= 0.75 ? result : Double.MAX_VALUE;
	}

}
