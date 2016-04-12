package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject;

import it.univaq.disim.mdegroup.wordnet.emf.compare.impl.SimilarityMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.filterer.threshold.HybridThresholdMatchFilterer;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator.epackage.EPackageMatchGenerator;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.refiner.neighbourhood.NeighbourhoodMatchRefiner;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.refiner.wordnet.WordNetMatchRefiner;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.Monitor;
import org.eclipse.emf.compare.CompareFactory;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.match.eobject.IEObjectMatcher;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.mit.jwi.CachingDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;

public class HybridEObjectMatcher implements IEObjectMatcher {
	
	private static final String wordnetDictionaryPath = "WordNet-3.1" + File.separator + "dict"; 
	private static CachingDictionary wordnetDictionary = null;  
	static {
		try {
			wordnetDictionary = new CachingDictionary(new RAMDictionary(new URL("file", null, wordnetDictionaryPath), ILoadPolicy.IMMEDIATE_LOAD));
			((RAMDictionary)wordnetDictionary.getBackingDictionary()).setLoadPolicy(ILoadPolicy.NO_LOAD);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	private EPackageMatchGenerator ePackageMatchGenerator = new EPackageMatchGenerator();
	private WordNetMatchRefiner wordnetMatchRefiner = new WordNetMatchRefiner();
	private NeighbourhoodMatchRefiner neighbourhoodMatchRefiner = new NeighbourhoodMatchRefiner();
	private HybridThresholdMatchFilterer hybridThresholdMatchFilterer = new HybridThresholdMatchFilterer();

	@Override
	public void createMatches(Comparison comparison, Iterator<? extends EObject> leftEObjects, Iterator<? extends EObject> rightEObjects, Iterator<? extends EObject> originEObjects, Monitor monitor) {
		/* Root Similarity Match */
		SimilarityMatch rootSimilarityMatch = new SimilarityMatch();
		rootSimilarityMatch.setMatch(CompareFactory.eINSTANCE.createMatch());
		rootSimilarityMatch.getMatch().setLeft(null);
		rootSimilarityMatch.getMatch().setRight(null);
		rootSimilarityMatch.getMatch().setOrigin(null);
		rootSimilarityMatch.setSemanticDistanceScore(0.0d);
		/* Similarity Matches Set */
		Set<SimilarityMatch> localComparison = Sets.newHashSet();
		/* Generate Matches */
		this.generateMatches(localComparison, rootSimilarityMatch, Lists.newArrayList(leftEObjects), Lists.newArrayList(rightEObjects));
		/* Refine Matches */
		this.refineMatches(localComparison);
		/* Filter Matches */
		this.filterMatches(localComparison, comparison);
	}
	
	/* Generate Matches */
	private void generateMatches(Set<SimilarityMatch> targetComparison, SimilarityMatch rootSimilarityMatch, List<EObject> leftEObjects, List<EObject> rightEObjects){
		List<EPackage> leftEPackages = leftEObjects.stream()
				.filter(leftElement -> leftElement instanceof EPackage)
				.map(EPackage.class::cast)
				.collect(Collectors.toList());
		List<EPackage> rightEPackages = rightEObjects.stream()
				.filter(rightElement -> rightElement instanceof EPackage)
				.map(EPackage.class::cast)
				.collect(Collectors.toList());
		this.ePackageMatchGenerator.generateMatches(targetComparison, rootSimilarityMatch, leftEPackages, rightEPackages);
	}
	
	/* Refine Matches */
	private void refineMatches(Set<SimilarityMatch> matches){
		this.wordnetMatchRefiner.refineMatches(matches);
		this.neighbourhoodMatchRefiner.refineMatches(matches);
	}
	
	/* Filter Matches */
	private void filterMatches(Set<SimilarityMatch> sourceComparison, Comparison targetComparison){
		this.hybridThresholdMatchFilterer.filterMatches(sourceComparison, targetComparison);
	}
	
}