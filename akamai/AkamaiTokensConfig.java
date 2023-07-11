
package com..aem.cloud.core.config;


import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;


@ObjectClassDefinition(
    name="Akamai API Configuration",
    description = "Configuration Needed for the Akamai API call"
)
public @interface AkamaiTokensConfig {

    @AttributeDefinition(name = "Akamai Host", description = "Enter Akamai Host Url", type = AttributeType.STRING)
    String akamaiHost();

    @AttributeDefinition(name = "Akamai Access Token", description = "Enter Akamai Access Token.", type = AttributeType.STRING)
    String akamaiAccessToken();

    @AttributeDefinition(name = "Akamai Client Token", description = "Enter Akamai Client Token.", type = AttributeType.STRING)
    String akamaiClientToken();

    @AttributeDefinition(name = "Akamai Client Secret", description = "Enter Akamai Client Secret.", type = AttributeType.STRING)
    String akamaiClientSecret();

    @AttributeDefinition(name = "Akamai Timeout", description = "Timeout for Akamai client call", type = AttributeType.INTEGER)
    int timeout();

}
