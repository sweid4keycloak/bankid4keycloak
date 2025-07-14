package org.keycloak.broker.bankid.model;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.ProtoSchema;

@ProtoSchema(
        schemaPackageName = "org.keycloak.broker.bankid.model",
        includeClasses = {
                AuthResponse.class,
                CompletionData.class,
                BankidUser.class,
                BankidDevice.class,
                StepUp.class,
        }
)
public interface BankidSerializationContextInitializer extends SerializationContextInitializer {
}
