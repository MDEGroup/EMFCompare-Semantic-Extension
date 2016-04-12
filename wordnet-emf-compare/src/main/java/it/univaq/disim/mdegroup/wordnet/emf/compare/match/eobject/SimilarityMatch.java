package it.univaq.disim.mdegroup.wordnet.emf.compare.match.eobject;

import java.util.Comparator;
import java.util.List;

import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.impl.MatchImpl;
import org.eclipse.emf.ecore.ENamedElement;

import com.google.common.collect.Lists;

import edu.stanford.nlp.ling.TaggedWord;

public class SimilarityMatch extends MatchImpl {

	/* Left Tagged Stemmed Token List */
	private List<TaggedWord> leftTaggedStemmedTokenList = Lists.newArrayList(); 
	
	/* Right Tagged Stemmed Token List */
	private List<TaggedWord> rightTaggedStemmedTokenList = Lists.newArrayList(); 
	
	/* Container Distance Weight */
	private Double containerDistanceWeight = 0.0d;
	/* Incoming Similarity Match Edges */
	private List<SimilarityMatch> containerSimilarityMatches = Lists.newArrayList();
	/* Incoming Similarity Matches Distance Score */
	private Double containerSimilarityMatchesDistanceScore = 1.0d; 
	
	/* Content Distance Weight */
	private Double contentDistanceWeight = 0.0d;
	/* Outgoing Similarity Match Edges */
	private List<SimilarityMatch> contentSimilarityMatches = Lists.newArrayList();
	/* Outgoing Similarity Matches Distance Score */
	private Double contentSimilarityMatchesDistanceScore = 1.0d; 
	
	/* Semantic Distance Weight */
	private Double semanticDistanceWeight = 0.0d;
	/* Semantic Distance Score */
	private Double semanticDistanceScore = 1.0d; 

	/* Wrapped Match Object */
	private Match match = null;  
	
	/* Distance Threshold */
	private Double distanceThreshold = 1.0d;
	
	/* Retrieve Left Tagged Stemmed Token List */
	public List<TaggedWord> getLeftTaggedStemmedTokenList(){
		return this.leftTaggedStemmedTokenList; 
	}
	
	/* Set Left Tagged Stemmed Token List */
	public void setLeftTaggedStemmedTokenList(List<TaggedWord> leftTaggedStemmedTokenList){
		this.leftTaggedStemmedTokenList = leftTaggedStemmedTokenList; 
	}
	
	/* Retrieve Right Tagged Stemmed Token List */
	public List<TaggedWord> getRightTaggedStemmedTokenList(){
		return this.rightTaggedStemmedTokenList; 
	}
	
	/* Set Right Tagged Stemmed Token List */
	public void setRightTaggedStemmedTokenList(List<TaggedWord> rightTaggedStemmedTokenList){
		this.rightTaggedStemmedTokenList = rightTaggedStemmedTokenList; 
	}
	
	/* Retrieve Distance Threshold */
	public Double getDistanceThreshold(){
		return this.distanceThreshold; 
	}
	
	/* Set Distance Threshold */
	public void setDistanceThreshold(Double distanceThreshold){
		this.distanceThreshold = distanceThreshold; 
	}
	
	/* Retrieve Content Distance Weight */
	public Double getContentDistanceWeight(){
		return this.contentDistanceWeight; 
	}
	
	/* Set Content Distance Weight */
	public void setContentDistanceWeight(Double contentDistanceWeight){
		this.contentDistanceWeight = contentDistanceWeight;
	}
	
	/* Retrieve Container Distance Weight */
	public Double getContainerDistanceWeight(){
		return this.containerDistanceWeight; 
	}
	
	/* Set Container Distance Weight */
	public void setContainerDistanceWeight(Double containerDistanceWeight){
		this.containerDistanceWeight = containerDistanceWeight;
	}
	
	/* Retrieve Semantic Distance Weight */
	public Double getSemantictDistanceWeight(){
		return this.semanticDistanceWeight; 
	}
	
	/* Set Semantic Distance Weight */
	public void setSemanticDistanceWeight(Double semanticDistanceWeight){
		this.semanticDistanceWeight = semanticDistanceWeight;
	}
	
	/* Retrieve Match */
	public Match getMatch(){
		return this.match;
	}
	
	/* Set Match */
	public void setMatch(Match match){
		this.match = match; 
	}
	
	/* Retrieve Incoming Similarity Matches */
	public List<SimilarityMatch> getContainerSimilarityMatches(){
		return this.containerSimilarityMatches; 
	}
	
	/* Add Incoming Similarity Match */
	public boolean addContainerSimilarityMatch(SimilarityMatch containerSimilarityMatch){
		if(containerSimilarityMatch != null && !this.containerSimilarityMatches.contains(containerSimilarityMatch)){
			this.containerSimilarityMatches.add(containerSimilarityMatch);
			return true;
		} else {
			return false;
		}
	}
	
	/* Retrieve Outgoing Similarity Matches */
	public List<SimilarityMatch> getContentSimilarityMatches(){
		return this.contentSimilarityMatches; 
	}
	
	/* Add Incoming Similarity Match */
	public boolean addContentSimilarityMatch(SimilarityMatch contentSimilarityMatch){
		if(contentSimilarityMatch != null && !this.contentSimilarityMatches.contains(contentSimilarityMatch)){
			this.contentSimilarityMatches.add(contentSimilarityMatch);
			return true;
		} else {
			return false; 
		}
	}
	
	/* Retrieve Semantic Distance Score */
	public Double getSemanticDistanceScore(){
		return this.semanticDistanceScore;
	}
	
	/* Set Semantic Distance Score */
	public boolean setSemanticDistanceScore(Double semanticDistanceScore){
		if(semanticDistanceScore != null){
			this.semanticDistanceScore = semanticDistanceScore;
			return true;
		} else {
			return false;
		}
	}
	
	/* Retrieve Incoming Similarity Matches Distance Score */
	public Double getContainerSimilarityMatchesDistanceScore(){
		return this.containerSimilarityMatchesDistanceScore; 
	}
	
	/* Set Incoming Similarity Matches Distance Score */
	public boolean setContainerSimilarityMatchesDistanceScore(Double containerSimilarityMatchesDistanceScore){
		if(containerSimilarityMatchesDistanceScore != null){
			this.containerSimilarityMatchesDistanceScore = containerSimilarityMatchesDistanceScore; 
			return true;
		} else {
			return false; 
		}
	}
	
	/* Retrieve Outgoing Similarity Matches Distance Score */
	public Double getContentSimilarityMatchesDistanceScore(){
		return this.contentSimilarityMatchesDistanceScore;
	}
	
	/* Set Outgoing Similarity Matches Distance Score */
	public boolean setContentSimilarityMatchesDistanceScore(Double contentSimilarityMatchesDistanceScore){
		if(contentSimilarityMatchesDistanceScore != null){
			this.contentSimilarityMatchesDistanceScore = contentSimilarityMatchesDistanceScore; 
			return true;
		} else {
			return false; 
		}
	}
	
	/* Compare Similarity Matches */
	@Override 
	public boolean equals(Object object){
		if(object instanceof SimilarityMatch){
			SimilarityMatch el = (SimilarityMatch)object; 
			if(el.getMatch() != null && this.getMatch() != null &&
					el.getMatch().getLeft() != null && this.getMatch().getLeft() != null &&
							el.getMatch().getRight() != null && this.getMatch().getRight() != null &&
			   ((ENamedElement)el.getMatch().getLeft()).getName().equals(((ENamedElement)this.getMatch().getLeft()).getName()) &&
			   ((ENamedElement)el.getMatch().getRight()).getName().equals(((ENamedElement)this.getMatch().getRight()).getName()) &&
			   (el.getMatch().getLeft().eContainer() != null ? this.getMatch().getLeft().eContainer() != null ? 
					   ((ENamedElement)el.getMatch().getLeft().eContainer()).getName().equals(((ENamedElement)this.getMatch().getLeft().eContainer()).getName()) : false : this.getMatch().getLeft().eContainer() == null ? true : false) &&
			   (el.getMatch().getRight().eContainer() != null ? this.getMatch().getRight().eContainer() != null ? 
					   ((ENamedElement)el.getMatch().getRight().eContainer()).getName().equals(((ENamedElement)this.getMatch().getRight().eContainer()).getName()) : false : this.getMatch().getRight().eContainer() == null ? true : false)){
				return true; 
			} else {
				return false; 
			}
		} else {
			return false; 
		}

	}
	

	public static class SimilarityMatchComparator implements Comparator<SimilarityMatch> {
		
		public int compare(SimilarityMatch firstSimilarityMatch, SimilarityMatch secondSimilarityMatch){
			return firstSimilarityMatch.getSemanticDistanceScore().compareTo(secondSimilarityMatch.getSemanticDistanceScore());
		}
		
	}
	
	
} 