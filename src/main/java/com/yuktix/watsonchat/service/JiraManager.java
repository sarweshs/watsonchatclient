package com.yuktix.watsonchat.service;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yuktix.watsonchat.config.WatsonProperties;
import com.yuktix.watsonchat.domain.IncidentData;
import com.yuktix.watsonchat.util.ProjectDataConverter;

@Service(value = "jiraManager")
@EnableConfigurationProperties(WatsonProperties.class)
public class JiraManager implements IIncidentManager {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private WatsonProperties watsonProperties;

	@Override
	public String getLatestUpdateForIncident(String incidentId) {
		// TODO Auto-generated method stub
		JsonNode map = null;
		String responseStr = "We dont have any new update for this incident.";
		ResponseEntity<JsonNode> response = restTemplate.exchange(watsonProperties.getImUrl() + "issue/" + incidentId,
				HttpMethod.GET,
				new HttpEntity(createHeaders(watsonProperties.getImUserName(), watsonProperties.getImPassword())),
				JsonNode.class);
		map = response.getBody();
		if (map != null) {
			Iterator<JsonNode> iterator = map.get("fields").get("comment").get("comments").iterator();
			JsonNode node = null;
			while (iterator.hasNext()) {
				node = iterator.next();
			}
			responseStr = node.get("body").asText();
		}
		return responseStr;
	}

	@Override
	public String createIncident(Map<String, String> incidentData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String updateIncident(Map<String, String> incidentData) {
		// TODO Auto-generated method stub
		return null;
	}

	private static HttpHeaders createHeaders(String username, String password) {
		return new HttpHeaders() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				String auth = username + ":" + password;
				byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(Charset.forName("US-ASCII")));
				String authHeader = "Basic " + new String(encodedAuth);
				set("Authorization", authHeader);
				//set("content-type", "application/json");
			}
		};
	}
	
	private static MultiValueMap<String,String> createMultiHeaders(String username, String password) {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		String auth = username + ":" + password;
		byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(Charset.forName("US-ASCII")));
		headers.add("Authorization", "Basic " + new String(encodedAuth));
		headers.add("Content-Type", "application/json");
		return headers;
	}

	@Override
	public String createIncident(IncidentData incidentData) {
		String responseStr = "Failed to create incident.";
		try {
			ResponseEntity<JsonNode> response = restTemplate.exchange(watsonProperties.getImUrl() + "issue/",
					HttpMethod.POST,
					new HttpEntity(ProjectDataConverter.convertIncidentDataToJiraJson(incidentData), createMultiHeaders(watsonProperties.getImUserName(), watsonProperties.getImPassword())),
					JsonNode.class);
			JsonNode map = response.getBody();
			if (map != null) {
				JsonNode node = map.get("key");
				responseStr = node.asText();
			}
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return "An incident has been created for you. The incident id is " + responseStr + ". Going forward, You can mention this to know the status.";
	}
	
	public static void main(String[] args) {
		System.setProperty("http.nonProxyHosts","*.hm.com");
		JiraManager jm = new JiraManager();
		String responseStr = "Failed to create incident.";
		RestTemplate restTemplate = new RestTemplate();
		IncidentData incidentData = new IncidentData();
		incidentData.setSummary("Defect created by ChatBot");
		incidentData.setSummary("Creation of Jira defect using APIs");
		try {
			/*ResponseEntity<JsonNode> response = restTemplate.exchange( "https://jira.hm.com//rest/api/2/issue/",
					HttpMethod.POST,
					new HttpEntity<String>(ProjectDataConverter.convertIncidentDataToJiraJson(incidentData), createMultiHeaders("hmcomplatform", "t44Cxheck10")),
					JsonNode.class);
			JsonNode map = response.getBody();*/
			JsonNode map = restTemplate.postForObject( "https://jira.hm.com/rest/api/2/issue/",
					new HttpEntity<String>(ProjectDataConverter.convertIncidentDataToJiraJson(incidentData), createMultiHeaders("hmcomplatform", "t44Cxheck10")),
					JsonNode.class);
			if (map != null) {
				JsonNode node = map.get("key");
				responseStr = node.get("body").asText();
			}
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

}
