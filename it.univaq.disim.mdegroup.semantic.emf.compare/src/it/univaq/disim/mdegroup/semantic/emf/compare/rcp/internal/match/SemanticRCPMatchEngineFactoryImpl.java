package it.univaq.disim.mdegroup.semantic.emf.compare.rcp.internal.match;

import it.univaq.disim.mdegroup.semantic.emf.compare.match.SemanticMatchEngine;

import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.rcp.EMFCompareRCPPlugin;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.utils.UseIdentifiers;

public class SemanticRCPMatchEngineFactoryImpl implements IMatchEngine.Factory {
	
	private int ranking; 
	
	/**
	 * {@inheritDoc}
	 */
	public IMatchEngine getMatchEngine() {
		return SemanticMatchEngine.create(UseIdentifiers.NEVER, EMFCompareRCPPlugin.getDefault().getWeightProviderRegistry());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getRanking() {
		return this.ranking;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRanking(int parseInt) {
		this.ranking = parseInt; 
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMatchEngineFactoryFor(IComparisonScope scope) {
		return true;
	}
	
}
