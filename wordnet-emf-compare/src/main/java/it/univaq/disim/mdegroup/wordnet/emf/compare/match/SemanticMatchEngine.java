package it.univaq.disim.mdegroup.wordnet.emf.compare.match; 

import static com.google.common.base.Preconditions.checkNotNull;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject.HybridEObjectMatcher;
import it.univaq.disim.mdegroup.wordnet.emf.compare.match.impl.SemanticMatchEngineFactoryRegistryImpl;

import java.util.Collections;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.Monitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.match.DefaultComparisonFactory;
import org.eclipse.emf.compare.match.DefaultEqualityHelperFactory;
import org.eclipse.emf.compare.match.IComparisonFactory;
import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.match.eobject.IEObjectMatcher;
import org.eclipse.emf.compare.match.eobject.WeightProvider;
import org.eclipse.emf.compare.match.eobject.WeightProviderDescriptorRegistryImpl;
import org.eclipse.emf.compare.match.resource.IResourceMatcher;
import org.eclipse.emf.compare.match.resource.StrategyResourceMatcher;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.utils.UseIdentifiers;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public class SemanticMatchEngine implements IMatchEngine {

	/** 
	 *	The delegate {@link IEObjectMatcher matcher} that will actually pair EObjects together 
	 **/
	private final IEObjectMatcher eObjectMatcher;
	/**
	 * The {@link IComparisonFactory factory} that will be used to instantiate {@link Comparison Comparison} as return by match() methods. 
	 **/
	private final IComparisonFactory comparisonFactory; 
	
	/** 
	 *	This semantic match engine delegates the pairing of EObjects to an {@link IEObjectMatcher eObjectMatcher}
	 *	@param eObjectMatcher 
	 *		The {@link IEObjectMatcher eObjectMatcher} that will be in charge of pairing {@link EObject eObject}s together for this comparison process.
	 *	@param comparisonFactory
	 *		The {@link IComparisonFactory comparisonFactory} that will be used to instantiate Comparison as return by match()
	 **/
	public SemanticMatchEngine(IEObjectMatcher eObjectMatcher, IComparisonFactory comparisonFactory){
		this.eObjectMatcher = checkNotNull(eObjectMatcher); 
		this.comparisonFactory = checkNotNull(comparisonFactory); 
	}
	
	/** 
	 *	{@inheritDoc}
	 *	@see org.eclipse.emf.compare.match.IMatchEngine#match(org.eclipse.emf.compare.scopre.IComparisonScope, org.eclipse.emf.common.util.Monitor)
	 **/
	public Comparison match(IComparisonScope comparisonScope, Monitor monitor){
		Comparison comparison = this.comparisonFactory.createComparison(); 
		final Notifier left = comparisonScope.getLeft(); 
		final Notifier right = comparisonScope.getRight(); 
		final Notifier origin = comparisonScope.getOrigin();
		comparison.setThreeWay(origin != null);
		this.match(comparison, comparisonScope, left, right, origin, monitor);
		return comparison; 
	}
	
	/**
	 *	This method will delegate to the proper "match(T, T, T)" implementation according to the types of {@code left}, {@code right} and {@code origin}
	 *	@param comparison
	 *		The comparison process to which the detected matches will be added
	 *	@parma comparisonScope
	 *		The comparison scope that has to be used by this engine to determinate the element to match
	 *	@param left
	 *		The left {@link Notifier notifier}
	 *	@param right
	 *		The right {@link Notifier notifier}
	 *	@param origin
	 *		The origin {@link Notifier notifier}, i.e. the common ancestor among {@code left} and {@code right}
	 *	@param monitor
	 *		The monitor to report progress or to check for cancellation
	 **/
	protected void match(Comparison comparison, IComparisonScope comparisonScope, final Notifier left, final Notifier right, final Notifier origin, Monitor monitor){
		System.out.println("1 : " + comparison.getMatches().size());
		if(comparison.isThreeWay()){
			if(left instanceof ResourceSet && right instanceof ResourceSet && origin instanceof ResourceSet){ 
				this.match(comparison, comparisonScope, (ResourceSet)left, (ResourceSet)right, (ResourceSet)origin, monitor); 
			} else if(left instanceof Resource && right instanceof Resource && origin instanceof Resource){
				this.match(comparison, comparisonScope, (Resource)left, (Resource)right, (Resource)origin, monitor);
			} else if(left instanceof EObject && right instanceof EObject && origin instanceof EObject){
				this.match(comparison, comparisonScope, (EObject)left, (EObject)right, (EObject)origin, monitor); 
			}
		} else {
			if(left instanceof ResourceSet && right instanceof ResourceSet){
				this.match(comparison, comparisonScope, (ResourceSet)left, (ResourceSet)right, (ResourceSet)origin, monitor);
			} else if(left instanceof Resource && right instanceof Resource){
				this.match(comparison, comparisonScope, (Resource)left, (Resource)right, (Resource)origin, monitor);
			} else if(left instanceof EObject && right instanceof EObject){
				this.match(comparison, comparisonScope, (EObject)left, (EObject)right, (EObject)origin, monitor);
			}
		}
	}
	
	/**
	 *	This method will be used to match the given {@link ResourceSet resourceSet}s. 
	 *	This implementation will query the comparison scope in order to obtain the {@link Resource resource} children of these {@link ResourceSet resourceSet}s.
	 *	Then, it will delegate the comparison to a {@link IResourceMatcher resourceMatcher} to determine the resource mappings.
	 *	@param comparison
	 *		The {@link Comparison comparison} to which the detected matches will be added
	 *	@param comparisonScope
	 *		The {@link IComparisonScope comparisonScope} that this engine has to use to determine the elements to match 
	 *	@param left
	 *		The left {@link ResourceSet resourceSet}
	 *	@param right
	 *		The left {@link ResourceSet resourceSet}
	 *	@param origin
	 *		The origin {@link ResourceSet resourceSet}, i.e. the common ancestor among {@code left} and {@code right}
	 *	@param monitor
	 *		The monitor to report progress or to check for cancellation.
	 **/
	protected void match(Comparison comparison, IComparisonScope comparisonScope, ResourceSet left, ResourceSet right, ResourceSet origin, Monitor monitor){
		System.out.println("2 : " + comparison.getMatches().size());
		if(!comparison.isThreeWay()){
			for(Resource leftResource : Lists.newArrayList(comparisonScope.getCoveredResources(left))){
				for(Resource rightResource : Lists.newArrayList(comparisonScope.getCoveredResources(right))){
					this.match(comparison, comparisonScope, leftResource, rightResource, null, monitor);
				}
			}
		} else {
			for(Resource leftResource : Lists.newArrayList(comparisonScope.getCoveredResources(left))){
				for(Resource rightResource : Lists.newArrayList(comparisonScope.getCoveredResources(right))){
					for(Resource originResource : Lists.newArrayList(comparisonScope.getCoveredResources(origin))){
						this.match(comparison, comparisonScope, leftResource, rightResource, originResource, monitor); 
					}
				}
			}
		}
	}
	
	/**
	 *	This method will be used to match the given {@link Resource resource}s. 
	 *	This implementation will query the comparison scope in order to obtain the {@link EObject eObject} children of these {@link Resource resource}s.
	 *	Then, it will delegate the comparison to a {@link IEObjectMatcher eObjectMatcher} to determine the EObject mappings.
	 *	@param comparison
	 *		The {@link Comparison comparison} to which the detected matches will be added
	 *	@param comparisonScope
	 *		The {@link IComparisonScope comparisonScope} that this engine has to use to determine the elements to match 
	 *	@param left
	 *		The left {@link Resource resource}
	 *	@param right
	 *		The left {@link Resource resource}
	 *	@param origin
	 *		The origin {@link Resource resource}, i.e. the common ancestor among {@code left} and {@code right}
	 *	@param monitor
	 *		The monitor to report progress or to check for cancellation.
	 **/
	protected void match(Comparison comparison, IComparisonScope comparisonScope, Resource left, Resource right, Resource origin, Monitor monitor){
		System.out.println("3 : " + comparison.getMatches().size());
		/* Resource Matching - Create Resource Matcher */
		IResourceMatcher resourceMatcher = this.createResourceMatcher();
		/* Resource Matching - Update Comparison */
		comparison.getMatchedResources().addAll(Lists.newArrayList(resourceMatcher
				.createMappings(Iterators.singletonIterator(left), Iterators.singletonIterator(right), 
						comparison.isThreeWay() ? Iterators.singletonIterator(origin) : Collections.emptyIterator())));
		
		/* EObjects Matching - Delegation */
		if(!comparison.isThreeWay()){
			for(EObject leftResourceEObject : left.getContents()){
				for(EObject rightResourceEObject : right.getContents()){
					this.match(comparison, comparisonScope, leftResourceEObject, rightResourceEObject, null, monitor);
				}
			}
		} else {
			for(EObject leftResourceEObject : left.getContents()){
				for(EObject rightResourceEObject : right.getContents()){
					for(EObject originResourceEObject : origin.getContents()){
						this.match(comparison, comparisonScope, leftResourceEObject, rightResourceEObject, originResourceEObject, monitor);
					}
				}
			}
		}
	}
	
	/**
	 *	This method will be used to match the given {@link EObject eObjects}s. 
	 *	This implementation will query the comparison scope in order to obtain the {@link EObject eObject} children of these {@link EObject eObject}s.
	 *	Then, it will delegate the comparison to a {@link IEObjectMatcher eObjectMatcher} to determine the eObject mappings.
	 *	@param comparison
	 *		The {@link Comparison comparison} to which the detected matches will be added
	 *	@param comparisonScope
	 *		The {@link IComparisonScope comparisonScope} that this engine has to use to determine the elements to match 
	 *	@param left
	 *		The left {@link EObject eObject}
	 *	@param right
	 *		The left {@link EObject eObject}
	 *	@param origin
	 *		The origin {@link EObject eObject}, i.e. the common ancestor among {@code left} and {@code right}
	 *	@param monitor
	 *		The monitor to report progress or to check for cancellation.
	 **/
	protected void match(Comparison comparison, IComparisonScope comparisonScope, EObject left, EObject right, EObject origin, Monitor monitor){
		System.out.println("4 : " + comparison.getMatches().size());
		IEObjectMatcher eObjectMatcher = this.createEObjectMatcher(); 
		eObjectMatcher.createMatches(comparison, Iterators.singletonIterator(left), Iterators.singletonIterator(right), 
				origin != null ? Iterators.singletonIterator(origin) : Collections.emptyIterator(), monitor);
	}
	
	/**
	 * 	This method is used to create the {@link IResourceMatcher resourceMatcher} that the {@link IMatchEngine matchEngine} will use to compare {@link Resource resource}s.
	 **/
	private IResourceMatcher createResourceMatcher() { 
		return new StrategyResourceMatcher();
	}
	
	/**
	 *	This method is used to create the {@link IEObjectMatcher eObjectMatcher} that the {@link IMatchEngine matchEngine} will use to compare {@link EObject eObject}s. 
	 **/
	private IEObjectMatcher createEObjectMatcher() { 
		return this.eObjectMatcher; 
	}
	
	/**
	 *	Helper creator method that instantiates a {@link NewSemanticMatchEngine semanticMatchEngine}.
	 *	@return new {@link NewSemanticMatchEngine semanticMatchEngine} instance. 
	 **/
	public static IMatchEngine create(){
		return create(UseIdentifiers.NEVER);
	}
	
	/**
	 *	Helper creator method that instantiates a {@link NewSemanticMatchEngine semanticMatchEngine}.
	 *	@param useIDs (currently ignored)
	 *	@return new {@link NewSemanticMatchEngine semanticMatchEngine} instance.
	 * */
	public static IMatchEngine create(UseIdentifiers useIDs){
		return create(useIDs, WeightProviderDescriptorRegistryImpl.createStandaloneInstance());
	}
	
	/**
	 *	Helper creator method that instantiates a {@link NewSemanticMatchEngine semanticMatchEngine}.
	 *	@param useIDs (currently ignored)
	 *	@param weightProviderRegistry (currently ignored)
	 *	@return new {@link NewSemanticMatchEngine semanticMatchEngine} instance 
	 **/
	public static IMatchEngine create(UseIdentifiers useIDs, WeightProvider.Descriptor.Registry weightProviderRegistry){
		final IComparisonFactory comparisonFactory = new DefaultComparisonFactory(new DefaultEqualityHelperFactory());
		final IEObjectMatcher eObjectMatcher = createDefaultEObjectMatcher(useIDs, weightProviderRegistry);
		final IMatchEngine matchEngine = new SemanticMatchEngine(eObjectMatcher, comparisonFactory);
		return matchEngine; 
	}
	
	/**
	 *	Helper creator method that instantiates a {@link IEObjectMatcher semanticEObjectMatcher}.
	 *	@return new {@link IEObjectMatcher semanticEObjectMatcher} instance 
	 **/
	public static IEObjectMatcher createDefaultEObjectMatcher(){
		return createDefaultEObjectMatcher(UseIdentifiers.NEVER);
	}
	
	/**
	 *	Helper creator method that instantiates a {@link IEObjectMatcher semanticEObjectMatcher}.
	 *	@param useIDs (currently ignored)
	 *	@return new {@link IEObjectMatcher semanticEObjectMatcher} instance
	 **/
	public static IEObjectMatcher createDefaultEObjectMatcher(UseIdentifiers useIDs){
		return createDefaultEObjectMatcher(useIDs, WeightProviderDescriptorRegistryImpl.createStandaloneInstance());
	}
	
	/**
	 *	Helper creator method that instantiates a {@link IEObjectMatcher semanticEObjectMatcher}.
	 *	@param useIDs (currently ignored)
	 *	@param weightProviderRegistry (currently ignored)
	 *	@return new {@link IEObjectMatcher semanticEObjectMatcher}
	 **/
	public static IEObjectMatcher createDefaultEObjectMatcher(UseIdentifiers useIDs, WeightProvider.Descriptor.Registry weightProviderRegistry){
		return new HybridEObjectMatcher();
	}
	
	/*** 
	 * MDE FORGE - JAR Access Point 
	 ***/
	public static Comparison match(String firstPath, String secondPath){
		/** Build Comparison Scope **/
		ResourceSet firstResourceSet = new ResourceSetImpl();
		firstResourceSet.getResource(URI.createFileURI(firstPath), true); 
		ResourceSet secondResourceSet = new ResourceSetImpl();
		secondResourceSet.getResource(URI.createFileURI(secondPath), true); 
		IComparisonScope comparisonScope = new DefaultComparisonScope(firstResourceSet, secondResourceSet, null); 
		/** Build Match Engine Factory Registry **/
		IMatchEngine.Factory.Registry matchEngineFactoryRegistry = SemanticMatchEngineFactoryRegistryImpl.createStandaloneInstance(); 
		return EMFCompare.builder().setMatchEngineFactoryRegistry(matchEngineFactoryRegistry).build().compare(comparisonScope); 
	}
	
}
