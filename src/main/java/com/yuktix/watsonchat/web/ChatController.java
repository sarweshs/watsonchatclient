package com.yuktix.watsonchat.web;

import java.nio.charset.Charset;
import java.security.Principal;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import com.ibm.watson.developer_cloud.conversation.v1.model.Context;
import com.ibm.watson.developer_cloud.conversation.v1.model.InputData;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageOptions;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.yuktix.watsonchat.config.WatsonProperties;
import com.yuktix.watsonchat.domain.ChatMessage;
import com.yuktix.watsonchat.domain.IncidentData;
import com.yuktix.watsonchat.domain.SessionProfanity;
import com.yuktix.watsonchat.event.LoginEvent;
import com.yuktix.watsonchat.event.ParticipantRepository;
import com.yuktix.watsonchat.exception.TooMuchProfanityException;
import com.yuktix.watsonchat.service.IIncidentManager;
import com.yuktix.watsonchat.util.ProfanityChecker;

/**
 * Controller that handles WebSocket chat messages
 * 
 * @author Sergi Almar
 */
@Controller
@EnableConfigurationProperties(WatsonProperties.class)
public class ChatController {
	
	/*static {
		System.setProperty("http.proxyHost", "seproxy.hm.com");
	    System.setProperty("http.proxyPort", "8080");
	    System.setProperty("https.proxyHost", "seproxy.hm.com");
	    System.setProperty("https.proxyPort", "8080");
	    System.setProperty("http.nonProxyHosts","*.hm.com");
	}*/

	@Autowired private ProfanityChecker profanityFilter;
	
	@Autowired private SessionProfanity profanity;
	
	@Autowired private WatsonProperties watsonProperties;
	
	@Autowired private ParticipantRepository participantRepository;
	
	@Autowired private SimpMessagingTemplate simpMessagingTemplate;
	
	@Autowired private IIncidentManager jiraManager;
	
	Map<String, Context> contextMap = new HashMap<>();
	
	@SubscribeMapping("/chat.participants")
	public Collection<LoginEvent> retrieveParticipants() {
		return participantRepository.getActiveSessions().values();
	}
	
	@MessageMapping("/chat.message")
	public ChatMessage filterMessage(@Payload ChatMessage message, Principal principal) {
		//getResponseAfterAction("ACTION:getItem");
		checkProfanityAndSanitize(message);
		
		message.setUsername(principal.getName());
		
		return message;
	}
	
	@MessageMapping("/chat.private.{username}")
	public void filterPrivateMessage(@Payload ChatMessage message, @DestinationVariable("username") String username, Principal principal) {
		checkProfanityAndSanitize(message);
		
		message.setUsername(principal.getName());
		if(username.equalsIgnoreCase("watson"))
		{
			//simpMessagingTemplate.convertAndSend("/user/" + username + "/exchange/amq.direct/chat.message", message);
			MessageResponse response = callWatsonConversationService(message);
			contextMap.put(username, response.getContext());
			List<String> list = response.getOutput().getText();
					
			String watsonResponse = list.stream()
	                .collect(Collectors.joining(""));
			if(watsonResponse.startsWith("ACTION:"))
			{
				watsonResponse = getResponseAfterAction(response,watsonResponse);
			}
			ChatMessage watsonMessage = new ChatMessage();
			watsonMessage.setMessage(watsonResponse);
			watsonMessage.setUsername(username);
			simpMessagingTemplate.convertAndSend("/user/" + principal.getName() + "/exchange/amq.direct/chat.message", watsonMessage);
		}
		else
		{
			simpMessagingTemplate.convertAndSend("/user/" + username + "/exchange/amq.direct/chat.message", message);
		}
	}
	
	@MessageExceptionHandler
	@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public String handleProfanity(TooMuchProfanityException e) {
		return e.getMessage();
	}
	
	private MessageResponse callWatsonConversationService(ChatMessage message)
	{
		String workspaceId = watsonProperties.getWorkspace();
		InputData input = new InputData.Builder(message.getMessage()).build();
		MessageOptions options = new MessageOptions.Builder(workspaceId).input(input).context(contextMap.get(message.getUsername())).build();
		MessageResponse response = watsonProperties.getConversationService().message(options).execute();
		/*List<String> list = response.getOutput().getText();
		return list.stream()
                .collect(Collectors.joining(""));*/
		return response;
	}
	
	HttpHeaders createHeaders(String username, String password){
		   return new HttpHeaders() {{
		         String auth = username + ":" + password;
		         byte[] encodedAuth = Base64.getEncoder().encode( 
		            auth.getBytes(Charset.forName("US-ASCII")) );
		         String authHeader = "Basic " + new String( encodedAuth );
		         set( "Authorization", authHeader );
		      }};
		}
	
	private String getResponseAfterAction(MessageResponse watsonResponse, String text) {
		// TODO Auto-generated method stub
		//restTemplate.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
		String responseStr = "Incident not found.";
		if(text.contains("getData"))
		{
			responseStr = jiraManager.getLatestUpdateForIncident((String)watsonResponse.getContext().get("incidentid"));
		}else if(text.contains("createIncident"))
		{
			String type = (String)watsonResponse.getContext().get("type");
			String userId = (String)watsonResponse.getContext().get("userid");
			String summary = null;
			String description = (String)watsonResponse.getContext().get("repeat");;
			if(type == null)
			{
				return "Incident could not be created. Missing type field. It should be either account or order.";
				
			}
			//String userId = (String)watsonResponse.getContext().get("userid");
			if(userId == null)
			{
				return "Incident could not be created. User ID is not provided.";
			}
			if(type.equals("order"))
			{
				summary = "Issue with order for userid " + userId + "." ;
			}
			else
			{
				summary = "Issue with account for userid " + userId + "." ;
			}
			description = " User says that " +  (String)watsonResponse.getContext().get("repeat");
			IncidentData incidentData = new IncidentData();
			incidentData.setSummary(summary);
			incidentData.setDescription(description);
			responseStr = jiraManager.createIncident(incidentData);
		}
		
		return responseStr;
	}

	private void checkProfanityAndSanitize(ChatMessage message) {
		long profanityLevel = profanityFilter.getMessageProfanity(message.getMessage());
		profanity.increment(profanityLevel);
		message.setMessage(profanityFilter.filter(message.getMessage()));
	}
	
}