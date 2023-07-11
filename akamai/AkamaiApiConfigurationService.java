
package com..aem.cloud.core.services.akamai;

public interface AkamaiApiConfigurationService {
    String getAkamaiHost();
    String getAkamaiAccessToken();
    String getAkamaiClientToken();
    String getAkamaiClientSecret();
    int getTimeout();
}
