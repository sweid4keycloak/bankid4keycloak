package org.keycloak.broker.bankid.model;

public enum BankidLoginFlow {
    api,
    webview;

    public static BankidLoginFlow valueOfOrDefault(String s, BankidLoginFlow defaultValue) {
        try {
            return BankidLoginFlow.valueOf(s);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
}
