package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.refiner.neighbourhood;

import java.util.Set;

import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.SimilarityMatch;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.refiner.MatchRefiner;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.refiner.neighbourhood.container.ContainerMatchRefiner;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.refiner.neighbourhood.content.ContentMatchRefiner;

public class NeighbourhoodMatchRefiner implements MatchRefiner<SimilarityMatch> {
	
	private ContentMatchRefiner contentMatchRefiner = new ContentMatchRefiner();
	private ContainerMatchRefiner containerMatchRefiner = new ContainerMatchRefiner();
	
	@Override
	public void refineMatches(Set<SimilarityMatch> sourceComparison) {
		this.contentMatchRefiner.refineMatches(sourceComparison);
		this.containerMatchRefiner.refineMatches(sourceComparison);
	}
	
}
