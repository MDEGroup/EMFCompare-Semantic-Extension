package it.univaq.disim.mdegroup.emfcompare.extension.match.impl;

import it.univaq.disim.mdegroup.emfcompare.extension.match.SemanticMatchEngine;

import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.match.eobject.WeightProvider;
import org.eclipse.emf.compare.match.eobject.WeightProviderDescriptorRegistryImpl;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryImpl;
import org.eclipse.emf.compare.utils.UseIdentifiers;

/**
 * The semantic implementation of the {@link IMatchEngine.Factory}.
 * 
 * @author <a href="mailto:lorenzo.addazi@student.univaq.it">Lorenzo Addazi</a>
 *         <p style="margin-top:1px;">
 *         <i>Derivative work from the default implementation
 *         {@link MatchEngineFactoryImpl} written by <a
 *         href="mailto:axel.richard@obeo.fr">Axel Richard</a>.</i>
 */
public class SemanticMatchEngineFactoryImpl extends MatchEngineFactoryImpl {

	public SemanticMatchEngineFactoryImpl() {
		this(UseIdentifiers.NEVER,
				WeightProviderDescriptorRegistryImpl.createStandaloneInstance());
	}

	public SemanticMatchEngineFactoryImpl(UseIdentifiers useIDs) {
		this(useIDs, WeightProviderDescriptorRegistryImpl
				.createStandaloneInstance());
	}

	public SemanticMatchEngineFactoryImpl(UseIdentifiers useIDs,
			WeightProvider.Descriptor.Registry weightProviderRegistry) {
		this.matchEngine = SemanticMatchEngine.create(useIDs,
				weightProviderRegistry);
	}

}