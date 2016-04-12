package it.univaq.disim.mdegroup.wordnet.emf.compare.match.impl;

import it.univaq.disim.mdegroup.wordnet.emf.compare.match.SemanticMatchEngine;

import org.eclipse.emf.compare.match.eobject.WeightProvider;
import org.eclipse.emf.compare.match.eobject.WeightProviderDescriptorRegistryImpl;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryImpl;
import org.eclipse.emf.compare.utils.UseIdentifiers;

public class SemanticMatchEngineFactoryImpl extends MatchEngineFactoryImpl {
	
	/* Constructor - No Parameters */
	public SemanticMatchEngineFactoryImpl(){
		this(UseIdentifiers.NEVER, WeightProviderDescriptorRegistryImpl.createStandaloneInstance());
	}
	
	/* Constructor - Use Identifiers Parameter */
	public SemanticMatchEngineFactoryImpl(UseIdentifiers useIDs) {
		this(useIDs, WeightProviderDescriptorRegistryImpl.createStandaloneInstance());
	}

	/* Constructor - Full parameters */
	public SemanticMatchEngineFactoryImpl(UseIdentifiers useIDs, WeightProvider.Descriptor.Registry weightProviderRegistry) {
		this.matchEngine = SemanticMatchEngine.create(useIDs, weightProviderRegistry); 
	}

}
