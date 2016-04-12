package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.generator;

import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.SimilarityMatch;

import java.util.List;
import java.util.Set;

import org.eclipse.emf.compare.Match;
import org.eclipse.emf.ecore.EObject;

public interface MatchGenerator<S extends Match, T extends EObject> {
	
	public void generateMatches(Set<S> generatorContext, SimilarityMatch rootSimilarityMatch, List<T> leftElements, List<T> rightElements);
	
}
