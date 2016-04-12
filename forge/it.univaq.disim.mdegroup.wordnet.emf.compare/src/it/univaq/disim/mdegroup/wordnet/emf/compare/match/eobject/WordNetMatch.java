package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.compare.impl.MatchImpl;
import org.openrdf.model.URI;

public class WordNetMatch extends MatchImpl {
	
	/* Container Semantic Distance Weight */
	private Double containerSemanticDistanceWeight; 
	/* Content Semantic Distance Weight */
	private Double contentSemanticDistanceWeight; 
	/* Name Semantic Distance Weight */
	private Double nameSemanticDistanceWeight;
	
	/* Container Semantic Distance Score */
	private Double containerSemanticDistanceScore;
	/* Content Semantic Distance Score */
	private Double contentSemanticDistanceScore; 
	/* Name Semantic Distance Score */
	private Double nameSemanticDistanceScore; 
	
	/* Overall Semantic Distance Threshold */
	private Double overallSemanticDistanceThreshold; 
	
	/* Left WordNet Token */
	private WordNetMatchElement leftElement; 
	/* Right WordNet Token */
	private WordNetMatchElement rightElement; 
	
	/* Container WordNet matches */
	private List<WordNetMatch> containerMatches; 
	/* Content WordNet matches */
	private List<WordNetMatch> contentMatches; 
	
	public List<WordNetMatch> getContainerMatches(){
		return this.containerMatches; 
	}
	
	public void addContainerMatch(WordNetMatch match){
		this.containerMatches.add(match);
	}
	
	public List<WordNetMatch> getContentMatches(){
		return this.contentMatches; 
	}
	
	public void addContentMatch(WordNetMatch match){
		this.contentMatches.add(match);
	}
	
	public Double getContainerSemanticDistanceWeight() {
		return containerSemanticDistanceWeight;
	}

	public void setContainerSemanticDistanceWeight(
			Double containerSemanticDistanceWeight) {
		this.containerSemanticDistanceWeight = containerSemanticDistanceWeight;
	}

	public Double getContentSemanticDistanceWeight() {
		return contentSemanticDistanceWeight;
	}

	public void setContentSemanticDistanceWeight(
			Double contentSemanticDistanceWeight) {
		this.contentSemanticDistanceWeight = contentSemanticDistanceWeight;
	}

	public Double getNameSemanticDistanceWeight() {
		return nameSemanticDistanceWeight;
	}

	public void setNameSemanticDistanceWeight(Double nameSemanticDistanceWeight) {
		this.nameSemanticDistanceWeight = nameSemanticDistanceWeight;
	}

	public Double getContainerSemanticDistanceScore() {
		return containerSemanticDistanceScore;
	}

	public void setContainerSemanticDistanceScore(
			Double containerSemanticDistanceScore) {
		this.containerSemanticDistanceScore = containerSemanticDistanceScore;
	}

	public Double getContentSemanticDistanceScore() {
		return contentSemanticDistanceScore;
	}

	public void setContentSemanticDistanceScore(Double contentSemanticDistanceScore) {
		this.contentSemanticDistanceScore = contentSemanticDistanceScore;
	}

	public Double getNameSemanticDistanceScore() {
		return nameSemanticDistanceScore;
	}

	public void setNameSemanticDistanceScore(Double nameSemanticDistanceScore) {
		this.nameSemanticDistanceScore = nameSemanticDistanceScore;
	}

	public Double getOverallSemanticDistanceThreshold() {
		return overallSemanticDistanceThreshold;
	}

	public void setOverallSemanticDistanceThreshold(
			Double overallSemanticDistanceThreshold) {
		this.overallSemanticDistanceThreshold = overallSemanticDistanceThreshold;
	}

	public WordNetMatchElement getLeftElement() {
		return leftElement;
	}

	public void setLeftElement(WordNetMatchElement leftElement) {
		this.leftElement = leftElement;
	}

	public WordNetMatchElement getRightElement() {
		return rightElement;
	}

	public void setRightElement(WordNetMatchElement rightElement) {
		this.rightElement = rightElement;
	}
	
	public static class WordNetMatchElement {
		
		private String elementName; 
		private Set<List<Map<String, Map<String, List<URI>>>>> elementTokenListSet; 
		
		public String getName(){
			return this.elementName; 
		}
		
		public void setName(String elementName){
			this.elementName = elementName; 
		}
		
		public Set<List<Map<String, Map<String, List<URI>>>>> getTokenListSet(){
			return this.elementTokenListSet; 
		}
		
		public void setTokenListSet(Set<List<Map<String, Map<String, List<URI>>>>> elementTokenListSet){
			this.elementTokenListSet = elementTokenListSet; 
		}
		
	}
	
}
