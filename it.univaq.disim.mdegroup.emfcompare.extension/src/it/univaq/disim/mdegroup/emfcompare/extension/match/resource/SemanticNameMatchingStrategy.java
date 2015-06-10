package it.univaq.disim.mdegroup.emfcompare.extension.match.resource;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.match.resource.NameMatchingStrategy;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * This implementation of a matching strategy will use String semantic equivalence on the resource names to try and
 * find resource mappings.
 * @author <a href="mailto:lorenzo.addazi@student.univaq.it">Lorenzo Addazi</a>
 * <p style="margin-top:1px;">
 * <i>Derivative work from the default implementation {@link NameMatchingStrategy} written by <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>.</i>
 */
public class SemanticNameMatchingStrategy extends NameMatchingStrategy {

	protected Resource findMatch(Resource reference, Iterable<Resource> candidates) {
		final URI referenceURI = reference.getURI();
		System.out.println(referenceURI.lastSegment().toUpperCase());
		for (Resource candidate : candidates) {
			if (referenceURI == candidate.getURI()
					|| referenceURI != null
					&& candidate.getURI() != null
					&& semanticallyEquivalent(referenceURI.lastSegment(), candidate.getURI().lastSegment())) {
				return candidate;
			}
		}
		return null;
	}
	
	private boolean semanticallyEquivalent(String source, String target){
		return source.equals(target);
	}

}
