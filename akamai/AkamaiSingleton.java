
package com..aem.cloud.core.akamai;


import java.util.ArrayList;
import java.util.List;


public class AkamaiSingleton {
    private final List<String> urlList = new ArrayList<>();
    private static AkamaiSingleton instance;
    private AkamaiSingleton() {}

    public static synchronized AkamaiSingleton getInstance() {
        if (instance == null) {
            instance = new AkamaiSingleton();
        }
        return instance;
    }

    public List<String> getUrlList () {
        return urlList;
    }

    public void setUrlList (List<String> urlList) {
        this.urlList.addAll(urlList);
    }

    public void removePurgedList (List<String> newUrls){
        for (String url : newUrls) {
            urlList.remove(url);
        }
    }

}
