package org.keycloak.lf.userprofileauthenticator.client;

import java.io.IOException; 

import org.apache.http.client.HttpClient;
import org.jboss.logging.Logger; 
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.provider.util.SimpleHttp.Response;
import com.fasterxml.jackson.databind.JsonNode;
import org.keycloak.lf.userprofileauthenticator.model.UserRequest;
import org.keycloak.lf.userprofileauthenticator.model.UserResponse;

import org.keycloak.lf.userprofileauthenticator.model.UserServiceHindCodes;
public class UserServiceClient {

	private static final Logger logger = Logger.getLogger(UserServiceClient.class);

	private HttpClient userServiceHttpClient;
	private String baseUrl;
	private String apikey;

	 

	public UserServiceClient(HttpClient userServiceHttpClient, String baseUrl, String apiKey) {
		this.userServiceHttpClient = userServiceHttpClient;
		this.baseUrl = baseUrl;
		this.apikey = apiKey;
	} 

	public UserResponse sendAuth(UserRequest request) { 
		 
		Response response = sendRequest("/internal/user", request);

		try {
			UserResponse ar = response.asJson(UserResponse.class); 
			return ar;
		} catch (IOException e) {
			logger.error("Failed to parse User service response", e);
			throw new UserServiceClientException(UserServiceHindCodes.internal, e);
		}
	}

	
	private Response sendRequest(String path, Object entity) {
		try {
			Response response = SimpleHttp.doPost(
					this.baseUrl + path,
					this.userServiceHttpClient)
					.param("Alf-Internal-API-Key", this.apikey)
					.json(entity)
					.asResponse();
			switch (response.getStatus()) {
				case 200:
				case 201:
					return response;
				case 400:
					return handle400Response(path, response);
				case 503:
					return handle503Response(path, response);
				default:
					return handleOtherHttpErrors(path, response);
			}
		} catch (IOException e) {
			logger.error("Failed to send request to BankID", e);
			throw new UserServiceClientException(UserServiceHindCodes.internal, e);
		}
	}

	private Response handleOtherHttpErrors(String path, Response response) {
		try {
			logger.errorf("Request to %s failed with status code %d and payload %s",
					path,
					response.getStatus(),
					response.asString());
		} catch (IOException e) {
		}
		throw new UserServiceClientException(UserServiceHindCodes.internal);
	}

	private Response handle503Response(String path, Response response) {
		try {
			logger.errorf("Request to %s failed with status code %d and payload %s",
					path,
					response.getStatus(),
					response.asString());
		} catch (IOException e) {
		}
		throw new UserServiceClientException(UserServiceHindCodes.userservice500);
	}

	private Response handle400Response(String path, Response response) {
		try {
			JsonNode responseJson = response.asJson();
			logger.errorf("Request to %s failed with status code %d and payload %s",
					path,
					response.getStatus(),
					responseJson.toString());
			throw new UserServiceClientException(UserServiceHindCodes.valueOf(responseJson.get("errorCode").textValue()));
		} catch (IOException e) {
			throw new UserServiceClientException(UserServiceHindCodes.userservice400);
		}
	}
}