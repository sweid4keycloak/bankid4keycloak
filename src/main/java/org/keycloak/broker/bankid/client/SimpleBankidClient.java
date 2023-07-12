package org.keycloak.broker.bankid.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.jboss.logging.Logger;
import org.keycloak.broker.bankid.model.AuthRequest;
import org.keycloak.broker.bankid.model.AuthResponse;
import org.keycloak.broker.bankid.model.BankidHintCodes;
import org.keycloak.broker.bankid.model.CollectResponse;
import org.keycloak.broker.bankid.model.Requirements;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.provider.util.SimpleHttp.Response;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;

import com.fasterxml.jackson.databind.JsonNode;

public class SimpleBankidClient {

	private static final Logger logger = Logger.getLogger(SimpleBankidClient.class);

	private HttpClient bankidHttpClient;
	private String baseUrl;

	public SimpleBankidClient(HttpClient bankidHttpClient, String baseUrl) {
		this.bankidHttpClient = bankidHttpClient;
		this.baseUrl = baseUrl;
	}

	public AuthResponse sendAuth(String personalNumber, String endUserIp) {

		AuthRequest request = new AuthRequest(endUserIp);
		
		if (personalNumber != null) {
			Requirements requirements = new Requirements();
			requirements.setPersonalNumber(personalNumber);
			request.setRequirement(requirements);
		}

		Response response = sendRequest("/rp/v6.0/auth", request);

		try {
			AuthResponse ar = response.asJson(AuthResponse.class);
			ar.setAuthTimestamp(System.currentTimeMillis() / 1000);
			return ar;
		} catch (IOException e) {
			logger.error("Failed to parse BankID response", e);
			throw new BankidClientException(BankidHintCodes.internal, e);
		}
	}

	public CollectResponse sendCollect(String orderrRef) {
		Map<String, String> requestData = new HashMap<>();
		requestData.put("orderRef", orderrRef);
		try {
			Response response = sendRequest("/rp/v6.0/collect", requestData);
			CollectResponse responseData = response.asJson(CollectResponse.class);
			// TODO: Handle when status is failed
			return responseData;
		} catch (IOException e) {
			logger.error("Failed to parse BankID response", e);
			throw new BankidClientException(BankidHintCodes.internal, e);
		}
	}

	public void sendCancel(String orderrRef) {
		Map<String, String> requestData = new HashMap<>();
		requestData.put("orderRef", orderrRef);
		try {
			sendRequest("/rp/v6.0/cancel", requestData);
			return;
		} catch (Exception e) {
			logger.warn("Failed cancel BankID auth request " + orderrRef, e);
		}
	}

	private Response sendRequest(String path, Object entity) {
		try {
			Response response = SimpleHttp.doPost(
					this.baseUrl + path,
					this.bankidHttpClient)
					.json(entity)
					.asResponse();
			switch (response.getStatus()) {
				case 200:
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
			throw new BankidClientException(BankidHintCodes.internal, e);
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
		throw new BankidClientException(BankidHintCodes.internal);
	}

	private Response handle503Response(String path, Response response) {
		try {
			logger.errorf("Request to %s failed with status code %d and payload %s",
					path,
					response.getStatus(),
					response.asString());
		} catch (IOException e) {
		}
		throw new BankidClientException(BankidHintCodes.Maintenance);
	}

	private Response handle400Response(String path, Response response) {
		try {
			JsonNode responseJson = response.asJson();
			logger.errorf("Request to %s failed with status code %d and payload %s",
					path,
					response.getStatus(),
					responseJson.toString());
			throw new BankidClientException(BankidHintCodes.valueOf(responseJson.get("errorCode").textValue()));
		} catch (IOException e) {
			throw new BankidClientException(BankidHintCodes.internal);
		}
	}
}
