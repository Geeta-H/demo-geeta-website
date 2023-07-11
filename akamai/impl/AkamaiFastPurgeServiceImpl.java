package com..aem.cloud.core.services.akamai.impl;

import com.akamai.edgegrid.signer.ClientCredential;
import com.akamai.edgegrid.signer.apachehttpclient.ApacheHttpClientEdgeGridInterceptor;
import com.akamai.edgegrid.signer.apachehttpclient.ApacheHttpClientEdgeGridRoutePlanner;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com..aem.cloud.core.services.akamai.AkamaiApiConfigurationService;
import com..aem.cloud.core.services.akamai.AkamaiFastPurgeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.util.Arrays;


@Component(immediate = true, service = AkamaiFastPurgeService.class)
@Slf4j
public class AkamaiFastPurgeServiceImpl implements AkamaiFastPurgeService {

    @Reference
    AkamaiApiConfigurationService akamaiApiConfigurationService;

    @Override
    public int sendRequestToAkamai (JsonArray urlsToPurge) {
        int statusCode = 200;

        JsonObject urlsInJsonToPurge = new JsonObject();
        urlsInJsonToPurge.add("objects", urlsToPurge);

        HttpPost postRequest = new HttpPost(akamaiApiConfigurationService.getAkamaiHost());
        postRequest.setEntity(new StringEntity(urlsInJsonToPurge.toString(), ContentType.APPLICATION_JSON));
        postRequest.addHeader("Content-Type", "application/json");

        ClientCredential credential = ClientCredential.builder()
            .accessToken(akamaiApiConfigurationService.getAkamaiAccessToken())
            .clientToken(akamaiApiConfigurationService.getAkamaiClientToken())
            .clientSecret(akamaiApiConfigurationService.getAkamaiClientSecret())
            .host(postRequest.getURI().getHost())
            .build();

        int timeout = akamaiApiConfigurationService.getTimeout();
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(timeout * 1000)
            .setSocketTimeout(timeout * 1000).build();

        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.addInterceptorFirst(new ApacheHttpClientEdgeGridInterceptor(credential));
        builder.setRoutePlanner(new ApacheHttpClientEdgeGridRoutePlanner(credential));
        builder.setDefaultRequestConfig(requestConfig);

        CloseableHttpClient client = builder.build();

        try {
            log.debug("---------------Akamai API request---------");
            log.debug("URL : {}", postRequest.getURI());
            log.debug("Headers : {}", Arrays.toString(postRequest.getAllHeaders()));
            log.debug("Method : {}", postRequest.getMethod());

            HttpResponse httpResponse = client.execute(postRequest);
            statusCode = httpResponse.getStatusLine().getStatusCode();

            log.error("Request Executed. Status code: {}, HTTP Response : {}", statusCode, httpResponse);

        } catch (IOException e) {
            log.error("Something went wrong trying to purge Akamai cache: ", e);
        }
        
        return statusCode;
    }

}
