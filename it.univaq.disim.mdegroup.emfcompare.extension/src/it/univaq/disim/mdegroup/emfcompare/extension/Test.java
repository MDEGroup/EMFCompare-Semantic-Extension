package it.univaq.disim.mdegroup.emfcompare.extension;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

}

	public double calculateSimilarity2() {
		try {
			URI uri1 = URI
					.createFileURI("PATH FILE DA COMPARARE 1");
			URI uri2 = URI
					.createFileURI("PATH FILE DA COMPARARE 2");

			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
					"ecore", new XMIResourceFactoryImpl());

			ResourceSet resourceSet1 = new ResourceSetImpl();
			ResourceSet resourceSet2 = new ResourceSetImpl();

			resourceSet1.getResource(uri1, true);
			resourceSet2.getResource(uri2, true);

			IComparisonScope scope = new DefaultComparisonScope(resourceSet1,
					resourceSet2, null);
			Comparison comparison = EMFCompare.builder().build().compare(scope);

			List<Match> matches = comparison.getMatches();
			int total = matches.size();
			int counter = 0;
			for (Match match : matches) {
				/* Trasformare Iterator in Lista */
				List<Match> lm = null; //Lists.newArrayList(match.getAllSubmatches());
				total += lm.size();
				for (Match match2 : lm)
					if (match2.getLeft() != null && match2.getRight() != null)
						counter++;
				if (match.getLeft() != null && match.getRight() != null)
					counter++;
			}

			// List<Diff> differences = comparison.getDifferences();
			// // Let's merge every single diff
			// // IMerger.Registry mergerRegistry = new IMerger.RegistryImpl();
			// IMerger.Registry mergerRegistry = IMerger.RegistryImpl
			// .createStandaloneInstance();
			// IBatchMerger merger = new BatchMerger(mergerRegistry);
			// merger.copyAllLeftToRight(differences, new BasicMonitor());

			double resultValue = (counter * 1.0) / total;

			// Used to save Diff model
			Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
			Map<String, Object> m = reg.getExtensionToFactoryMap();
			m.put("xmi", new XMIResourceFactoryImpl());

			ResourceSet resSet = new ResourceSetImpl();
			// create a resource
			Resource resource = resSet.createResource(URI.createURI("PATH FILE RESULTS"
					+ "/compare.xmi"));
			resource.getContents().add(comparison);
			try {
				resource.save(Collections.EMPTY_MAP);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException();
			}
			/*SimilarityRelation sr = new SimilarityRelation();
			sr.setFromArtifact(art1);
			sr.setToArtifact(art2);
			sr.setValue(resultValue);
			relationRepository.save(sr);*/
			return resultValue;
		} catch (Exception e) {
			/*System.out.println("ERROR from" + art1.getName() + "_"
					+ art1.getId() + " to " + art2.getName() + "_"
					+ art2.getId());*/
			return 0;
		}
	}

}
