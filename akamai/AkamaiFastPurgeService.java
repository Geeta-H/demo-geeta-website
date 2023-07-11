
package com..aem.cloud.core.services.akamai;


import com.google.gson.JsonArray;


public interface AkamaiFastPurgeService {
    public int sendRequestToAkamai(JsonArray urlsToPurge);
}
