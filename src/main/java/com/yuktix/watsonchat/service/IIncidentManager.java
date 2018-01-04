package com.yuktix.watsonchat.service;

import java.util.Map;

import com.yuktix.watsonchat.domain.IncidentData;

public interface IIncidentManager {
	
	public String getLatestUpdateForIncident(String incidentId);
	
	public String createIncident(Map<String,String> incidentData);
	
	public String updateIncident(Map<String,String> incidentData);
	
	public String createIncident(IncidentData incidentData);

}
