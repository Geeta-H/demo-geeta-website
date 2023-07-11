package com..aem.cloud.core.services.akamai.impl;


import com..aem.cloud.core.config.AkamaiTokensConfig;
import com..aem.cloud.core.services.akamai.AkamaiApiConfigurationService;
import lombok.Getter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;


@Component(service = AkamaiApiConfigurationService.class, immediate = true)
@Designate(ocd = AkamaiTokensConfig.class)
public class AkamaiApiConfigurationServiceImpl implements AkamaiApiConfigurationService {

    @Getter
    String akamaiHost;

    @Getter
    String akamaiAccessToken;

    @Getter
    String akamaiClientToken;

    @Getter
    String akamaiClientSecret;

    @Getter
    int timeout;

    @Activate
    public void activate (AkamaiTokensConfig config) {
        akamaiHost = config.akamaiHost();
        akamaiAccessToken = config.akamaiAccessToken();
        akamaiClientToken = config.akamaiClientToken();
        akamaiClientSecret = config.akamaiClientSecret();
        timeout = config.timeout();
    }

}
