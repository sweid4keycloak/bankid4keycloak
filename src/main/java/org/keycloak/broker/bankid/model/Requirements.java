package org.keycloak.broker.bankid.model;

import java.util.List;

public class Requirements {

    private Boolean pinCode;
    private Boolean mrtd;
    private String cardReader;
    private List<String> certificatePolicies;
    private String personalNumber;

    public Boolean getPinCode() {
        return pinCode;
    }
    public void setPinCode(Boolean pinCode) {
        this.pinCode = pinCode;
    }
    public Boolean getMrtd() {
        return mrtd;
    }
    public void setMrtd(Boolean mrtd) {
        this.mrtd = mrtd;
    }
    public String getCardReader() {
        return cardReader;
    }
    public void setCardReader(String cardReader) {
        this.cardReader = cardReader;
    }
    public List<String> getCertificatePolicies() {
        return certificatePolicies;
    }
    public void setCertificatePolicies(List<String> certificatePolicies) {
        this.certificatePolicies = certificatePolicies;
    }
    public String getPersonalNumber() {
        return personalNumber;
    }
    public void setPersonalNumber(String personalNumber) {
        this.personalNumber = personalNumber;
    }
}
