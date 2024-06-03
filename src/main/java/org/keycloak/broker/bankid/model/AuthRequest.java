package org.keycloak.broker.bankid.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Request to the /auth endpoint to initiate an authentication order. 
 * 
 * <p>Use the collect method to query the status of the order. If the request is successful the response includes:
 *
 * <ul>
 * <li>orderRef</li>
 * <li>autoStartToken</li>
 * <li>qrStartToken and</li>
 * <li>qrStartSecret</li>
 * </ul>
 * </p>
 */
@Getter
@Setter
public class AuthRequest {
    private String endUserIp;
    private Requirements requirement;
    private String userVisibleData;
    private String userNonVisibleData;
    private String userVisibleDataFormat;
}
