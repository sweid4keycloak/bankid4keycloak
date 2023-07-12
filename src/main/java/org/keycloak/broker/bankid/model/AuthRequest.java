package org.keycloak.broker.bankid.model;

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
public class AuthRequest {

    private String endUserIp;
    private Requirements requirement;
    private String userVisibleData;
    private String userNonVisibleData;
    private String userVisibleDataFormat;

    public AuthRequest(String endUserIp) {
        this.endUserIp = endUserIp;
    }

    /** 
     * Gets the end user ip for this {@link AuthRequest}.
     * 
     * @see AuthRequest#setEndUserIp(String)
     */
    public String getEndUserIp() {
        return endUserIp;
    }

    /**
     * Sets the end user ip for this request.
     * 
     * <p>The user IP address as seen by RP. String. IPv4 and IPv6 is allowed.</p>
     * 
     * <p>Correct IP address must be the IP address representing the user agent (the
     * end user device) as seen by the RP. In case of inbound proxy, special
     * considerations may need to be taken into account to get the correct address.</p>
     * 
     * <p>In some use cases the IP address is not available, for instance in
     * voice-based services. In these cases, the internal representation of those
     * systems’ IP address may be used.</p>

     * @param endUserIp the end user ip to use in this {@link AuthRequest}
     */
    public void setEndUserIp(String endUserIp) {
        this.endUserIp = endUserIp;
    }

    public Requirements getRequirement() {
        return requirement;
    }

    public void setRequirement(Requirements requirement) {
        this.requirement = requirement;
    }

    public String getUserVisibleData() {
        return userVisibleData;
    }

    public void setUserVisibleData(String userVisibleData) {
        this.userVisibleData = userVisibleData;
    }

    public String getUserNonVisibleData() {
        return userNonVisibleData;
    }

    public void setUserNonVisibleData(String userNonVisibleData) {
        this.userNonVisibleData = userNonVisibleData;
    }

    public String getUserVisibleDataFormat() {
        return userVisibleDataFormat;
    }

    public void setUserVisibleDataFormat(String userVisibleDataFormat) {
        this.userVisibleDataFormat = userVisibleDataFormat;
    }

}
