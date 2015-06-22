package it.univaq.disim.mdegroup.emfcompare.extension.match;

import java.net.MalformedURLException;

import it.univaq.disim.mdegroup.emfcompare.extension.match.eobject.SemanticDistanceFunction;
import it.univaq.disim.mdegroup.emfcompare.extension.match.eobject.SemanticDistanceFunction;
import it.univaq.disim.mdegroup.emfcompare.extension.match.resource.SemanticResourceMatcher;

import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.match.DefaultComparisonFactory;
import org.eclipse.emf.compare.match.DefaultEqualityHelperFactory;
import org.eclipse.emf.compare.match.DefaultMatchEngine;
import org.eclipse.emf.compare.match.IComparisonFactory;
import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.match.eobject.IEObjectMatcher;
import org.eclipse.emf.compare.match.eobject.IdentifierEObjectMatcher;
import org.eclipse.emf.compare.match.eobject.ProximityEObjectMatcher;
import org.eclipse.emf.compare.match.eobject.WeightProvider;
import org.eclipse.emf.compare.match.eobject.WeightProviderDescriptorRegistryImpl;
import org.eclipse.emf.compare.match.resource.IResourceMatcher;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.utils.UseIdentifiers;

/**
 * The Match engine orchestrates the matching process : it takes an
 * {@link IComparisonScope scope} as input, iterates over its
 * {@link IComparisonScope#getLeft() left}, {@link IComparisonScope#getRight()
 * right} and {@link IComparisonScope#getOrigin() origin} roots and delegates to
 * {@link IResourceMatcher}s and {@link IEObjectMatcher}s in order to create the
 * result {@link Comparison} model for this scope.
 * 
 * @author <a href="mailto:lorenzo.addazi@student.univaq.it">Lorenzo Addazi</a>
 *         <p style="margin-top:1px;">
 *         <i>Derivative work from the default implementation
 *         {@link DefaultMatchEngine} written by <a
 *         href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>.</i>
 */
public class SemanticMatchEngine extends DefaultMatchEngine {

	public SemanticMatchEngine(IEObjectMatcher matcher,
			IComparisonFactory comparisonFactory) {
		super(matcher, comparisonFactory);
	}

	public static IMatchEngine create() {
		return create(UseIdentifiers.WHEN_AVAILABLE,
				WeightProviderDescriptorRegistryImpl.createStandaloneInstance());
	}

	public static IMatchEngine create(UseIdentifiers useIDs) {
		return create(useIDs,
				WeightProviderDescriptorRegistryImpl.createStandaloneInstance());
	}

	public static IMatchEngine create(UseIdentifiers useIDs,
			WeightProvider.Descriptor.Registry weightProviderRegistry) {
		return new SemanticMatchEngine(
				SemanticMatchEngine.createDefaultEObjectMatcher(useIDs,
						weightProviderRegistry), new DefaultComparisonFactory(
						new DefaultEqualityHelperFactory()));
	}

	protected IResourceMatcher createResourceMatcher() {
		return new SemanticResourceMatcher();
	}

	public static IEObjectMatcher createDefaultEObjectMatcher(
			UseIdentifiers useIDs) {
		return createDefaultEObjectMatcher(useIDs,
				WeightProviderDescriptorRegistryImpl.createStandaloneInstance());
	}

	public static IEObjectMatcher createDefaultEObjectMatcher(
			UseIdentifiers useIDs,
			WeightProvider.Descriptor.Registry weightProviderRegistry) {
		try {
			switch (useIDs) {
			case NEVER:
				return new ProximityEObjectMatcher(
						new SemanticDistanceFunction());
			case ONLY:
				return new IdentifierEObjectMatcher();
			case WHEN_AVAILABLE:
			default:
				return new IdentifierEObjectMatcher(
						new ProximityEObjectMatcher(
								new SemanticDistanceFunction()));

			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return new IdentifierEObjectMatcher();
		}
	}

}