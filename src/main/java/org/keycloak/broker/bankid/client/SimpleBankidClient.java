package org.keycloak.broker.bankid.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import org.apache.http.client.HttpClient;
import org.keycloak.broker.bankid.model.AuthRequest;
import org.keycloak.broker.bankid.model.AuthResponse;
import org.keycloak.broker.bankid.model.BankidHintCodes;
import org.keycloak.broker.bankid.model.CollectResponse;
import org.keycloak.broker.bankid.model.Requirements;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.provider.util.SimpleHttp.Response;

import com.fasterxml.jackson.databind.JsonNode;

@JBossLog
@RequiredArgsConstructor
public class SimpleBankidClient {
	private final HttpClient bankidHttpClient;
	private final String baseUrl;

	public AuthResponse sendAuth(String personalNumber, String endUserIp) {

		AuthRequest request = new AuthRequest();
		request.setEndUserIp(endUserIp);
		
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
			log.error("Failed to parse BankID response", e);
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
			log.error("Failed to parse BankID response", e);
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
			log.warn("Failed cancel BankID auth request " + orderrRef, e);
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
			log.error("Failed to send request to BankID", e);
			throw new BankidClientException(BankidHintCodes.internal, e);
		}
	}

	private Response handleOtherHttpErrors(String path, Response response) {
		try {
			log.errorf("Request to %s failed with status code %d and payload %s",
					path,
					response.getStatus(),
					response.asString());
		} catch (IOException e) {
		}
		throw new BankidClientException(BankidHintCodes.internal);
	}

	private Response handle503Response(String path, Response response) {
		try {
			log.errorf("Request to %s failed with status code %d and payload %s",
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
			log.errorf("Request to %s failed with status code %d and payload %s",
					path,
					response.getStatus(),
					responseJson.toString());
			throw new BankidClientException(BankidHintCodes.valueOf(responseJson.get("errorCode").textValue()));
		} catch (IOException e) {
			throw new BankidClientException(BankidHintCodes.internal);
		}
	}
}
