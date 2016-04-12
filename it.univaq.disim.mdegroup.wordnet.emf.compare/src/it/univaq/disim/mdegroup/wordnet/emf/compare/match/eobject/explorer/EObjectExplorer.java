package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.explorer;

import java.util.List;
import java.util.Set;

import org.eclipse.emf.compare.Match;
import org.eclipse.emf.ecore.EObject;

public interface EObjectExplorer<S extends Match, T extends EObject> {
	
	public void generateMatches(Set<S> context, S rootMatch, List<T> leftElements, List<T> rightElements);
	
}
