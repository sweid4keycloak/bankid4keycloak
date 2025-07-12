package org.keycloak.broker.bankid.model;

import org.infinispan.protostream.annotations.ProtoField;

public class StepUp {

    @ProtoField(number = 1)
    Boolean mrtd;

    public Boolean getMrtd() {
        return mrtd;
    }

    public void setMrtd(Boolean mrtd) {
        this.mrtd = mrtd;
    }
    
}
