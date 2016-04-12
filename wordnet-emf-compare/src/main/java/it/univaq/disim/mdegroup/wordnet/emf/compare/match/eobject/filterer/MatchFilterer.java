package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.filterer;

import java.util.Set;

import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Match;

public interface MatchFilterer<T extends Match> {
	
	public void filterMatches(Set<T> sourceComparison, Comparison targetComparison);
	
}
