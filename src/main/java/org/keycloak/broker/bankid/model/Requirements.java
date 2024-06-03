package org.keycloak.broker.bankid.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Requirements {
    private boolean pinCode;
    private boolean mrtd;
    private String cardReader;
    private List<String> certificatePolicies;
    private String personalNumber;
}
