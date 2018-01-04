package com.yuktix.watsonchat.config;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chat")
class ChatProperties {

	private int maxProfanityLevel;
	
	private Set<String> disallowedWords;
	
	private Destinations destinations;
	
	private String imUrl;
	
	private String imUserName;
	
	private String imPassword;
	
	
	public int getMaxProfanityLevel() {
		return maxProfanityLevel;
	}

	public void setMaxProfanityLevel(int maxProfanityLevel) {
		this.maxProfanityLevel = maxProfanityLevel;
	}
	
	public Set<String> getDisallowedWords() {
		return disallowedWords;
	}

	public void setDisallowedWords(Set<String> disallowedWords) {
		this.disallowedWords = disallowedWords;
	}

	public Destinations getDestinations() {
		return destinations;
	}

	public void setDestinations(Destinations destinations) {
		this.destinations = destinations;
	}


	static class Destinations {

		private String login;
		
		private String logout;

		public String getLogin() {
			return login;
		}

		public void setLogin(String login) {
			this.login = login;
		}

		public String getLogout() {
			return logout;
		}

		public void setLogout(String logout) {
			this.logout = logout;
		}
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
