package com.yuktix.watsonchat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.ibm.watson.developer_cloud.conversation.v1.Conversation;

@ConfigurationProperties(prefix = "watson")
public class WatsonProperties {
	private String username;
	
	private String password;
	
	private String workspace;
	
	private String imUrl;
	
	private String imUserName;
	
	private String imPassword;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getWorkspace() {
		return workspace;
	}

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}
	
	public Conversation getConversationService()
	{
		Conversation service = new Conversation(Conversation.VERSION_DATE_2017_05_26);
		service.setEndPoint("https://watson-api-explorer.mybluemix.net/conversation/api");
		service.setUsernameAndPassword(getUsername(), getPassword());
		return service;
	}

	public String getImUrl() {
		return imUrl;
	}

	public void setImUrl(String imUrl) {
		this.imUrl = imUrl;
	}

	public String getImUserName() {
		return imUserName;
	}

	public void setImUserName(String imUserName) {
		this.imUserName = imUserName;
	}

	public String getImPassword() {
		return imPassword;
	}

	public void setImPassword(String imPassword) {
		this.imPassword = imPassword;
	}
	
}
