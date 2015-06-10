package it.univaq.disim.mdegroup.emfcompare.extension.match.resource;

import org.eclipse.emf.compare.match.resource.IResourceMatchingStrategy;
import org.eclipse.emf.compare.match.resource.RootIDMatchingStrategy;
import org.eclipse.emf.compare.match.resource.StrategyResourceMatcher;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * A {@link SemanticResourceMatcher} will be used to match two or three {@link Resource}s together; depending
 * on whether we are doing a two or three way comparison.
 * <p>
 * Do take note that the match engine expects ResourceMatchers to return matching resources as well as
 * resources that do not match.
 * </p>
 * @author <a href="mailto:lorenzo.addazi@student.univaq.it">Lorenzo Addazi</a>
 *  * <p style="margin-top:1px;">
 * <i>Derivative work from the default implementation {@link StrategyResourceMatcher} written by <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>.</i>
 */
public class SemanticResourceMatcher extends StrategyResourceMatcher {
	
	protected IResourceMatchingStrategy[] getResourceMatchingStrategies() {
		final IResourceMatchingStrategy idStrategy = new RootIDMatchingStrategy();
		final IResourceMatchingStrategy semanticNameStrategy = new SemanticNameMatchingStrategy();
		return new IResourceMatchingStrategy[] { idStrategy, semanticNameStrategy };
	}

}
