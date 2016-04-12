package it.univaq.disim.mdegroup.wordnet.emf.compare.match.impl;

import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryImpl;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryRegistryImpl;

public class SemanticMatchEngineFactoryRegistryImpl extends MatchEngineFactoryRegistryImpl {
	
	/**
	 * Constructs the registry.
	 */
	public SemanticMatchEngineFactoryRegistryImpl() {
		super();
		final MatchEngineFactoryImpl matchEngineFactory = new MatchEngineFactoryImpl();
		matchEngineFactory.setRanking(10);
		this.add(matchEngineFactory);
	}

	/**
	 * Returns a registry filled with the default match engine factory provided
	 * by EMF Compare {@link MatchEngineFactoryImpl} and the semantic match
	 * engine {@link CustomMatchEngineFactoryImpl}.
	 * 
	 * @return A registry filled with the default match engine factory provided
	 *         by EMF Compare and the semantic match engine.
	 */
	public static IMatchEngine.Factory.Registry createStandaloneInstance() {
		final IMatchEngine.Factory.Registry registry = new SemanticMatchEngineFactoryRegistryImpl();
		final SemanticMatchEngineFactoryImpl semanticMatchEngineFactory = new SemanticMatchEngineFactoryImpl();
		semanticMatchEngineFactory.setRanking(20);
		registry.add(semanticMatchEngineFactory);
		return registry;
	}
	
	

}