package com.yuktix.watsonchat.util;

import com.yuktix.watsonchat.domain.IncidentData;

public class ProjectDataConverter {
	
	public static String convertIncidentDataToJiraJson(IncidentData data)
	{
		String str = "{\"fields\":{" + data.getProject() + "," + "\"summary\":\"" + data.getSummary()
		+ "\"," + "\"description\":\"" + data.getDescription() + "\"," + data.getIssueType()
		+ ",\"labels\": [\"chat_bot\",\"Maintenance\"]," + data.getCustomfield_11071()
		+ "}}";
		
		return str;
	}

}
