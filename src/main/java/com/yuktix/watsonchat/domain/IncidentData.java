package com.yuktix.watsonchat.domain;

import com.yuktix.watsonchat.util.ProjectDataConverter;

public class IncidentData {
	
	private String project = "\"project\":{\"key\": \"HMCOM\"}";
	
	private String summary;
	
	private String description;
	
	private String issueType = "\"issuetype\": {\"name\": \"Defect\"}";
	
	//This string maps to Responsible Team
	private String customfield_11071 = "\"customfield_11071\": {\"self\": \"https://jira.hm.com/rest/api/2/customFieldOption/22044\","
										+ "\"value\": \"Platform Functional\","
										+ "\"id\": \"22044\"}";

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProject() {
		return project;
	}

	public String getIssueType() {
		return issueType;
	}

	public String getCustomfield_11071() {
		return customfield_11071;
	}
	
	public static void main(String[] args) {
		IncidentData projectData = new IncidentData();
		projectData.setSummary("Defect created by ChatBot example");
		projectData.setDescription("Chat bot creaed this defect");
		System.out.println(ProjectDataConverter.convertIncidentDataToJiraJson(projectData));
	}

}
