package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.refiner;

import java.util.Set;

import org.eclipse.emf.compare.Match;

public interface MatchRefiner<T extends Match> {
	
	public void refineMatches(Set<T> sourceComparison);
	
}
