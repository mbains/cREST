package com.jonnyi.wadl.model;

import java.util.List;

public class Service {

	private List<String> aliases,searchTags;
	private String name, version, description, platform, portfolioStatus, esbConsumerId, 
		composition, visibility, dataEffect, starTeamProject, documentationUrl,executableName;
	
	public List<String> getAliases() {
		return aliases;
	}
	public void setAliases(List<String> aliases) {
		this.aliases = aliases;
	}
	public List<String> getSearchTags() {
		return searchTags;
	}
	public void setSearchTags(List<String> searchTags) {
		this.searchTags = searchTags;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public String getPortfolioStatus() {
		return portfolioStatus;
	}
	public void setPortfolioStatus(String portfolioStatus) {
		this.portfolioStatus = portfolioStatus;
	}
	public String getEsbConsumerId() {
		return esbConsumerId;
	}
	public void setEsbConsumerId(String esbConsumerId) {
		this.esbConsumerId = esbConsumerId;
	}
	public String getComposition() {
		return composition;
	}
	public void setComposition(String composition) {
		this.composition = composition;
	}
	public String getVisibility() {
		return visibility;
	}
	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}
	public String getDataEffect() {
		return dataEffect;
	}
	public void setDataEffect(String dataEffect) {
		this.dataEffect = dataEffect;
	}
	public String getStarTeamProject() {
		return starTeamProject;
	}
	public void setStarTeamProject(String starTeamProject) {
		this.starTeamProject = starTeamProject;
	}
	public String getDocumentationUrl() {
		return documentationUrl;
	}
	public void setDocumentationUrl(String documentationUrl) {
		this.documentationUrl = documentationUrl;
	}
	public String getExecutableName() {
		return executableName;
	}
	public void setExecutableName(String executableName) {
		this.executableName = executableName;
	}
}
