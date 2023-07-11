

package com..aem.cloud.core.schedulers;


import com..aem.cloud.core.akamai.AkamaiSingleton;
import com..aem.cloud.core.services.akamai.AkamaiFastPurgeService;

import lombok.extern.slf4j.Slf4j;

import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.util.ArrayList;
import java.util.List;


@Designate(ocd = AkamaiPurgeScheduler.Config.class)
@Component(service = Runnable.class)
@Slf4j
public class AkamaiPurgeScheduler implements Runnable {

    @Reference
    AkamaiFastPurgeService akamaiFastPurge;

    @Reference
    private Scheduler scheduler;

    @Override
    public void run() {
        AkamaiSingleton singleton = AkamaiSingleton.getInstance();
        List<String> urlList = new ArrayList<>();
        urlList.addAll(singleton.getUrlList());

        Gson gson = new Gson();
        JsonArray urlsTopurge = JsonParser.parseString(gson.toJson(urlList)).getAsJsonArray();

        log.debug("List of Urls to be sent to Akamai for cache purge:{}", urlsTopurge);
        if (!urlsTopurge.isEmpty()) {
            int statusCode = akamaiFastPurge.sendRequestToAkamai(urlsTopurge);
            
            if (statusCode == 200 || statusCode == 201) {
                singleton.removePurgedList(urlList);
                log.debug("---------Akamai Purge Request is successful--------");
            }
        }
    }

    @Activate
    protected void activate (final Config config) {
        scheduleTask(config.scheduler_expression());
    }

    private void scheduleTask(String expression) {
        ScheduleOptions options = scheduler.EXPR(expression);
        scheduler.schedule(this, options);
    }

    @ObjectClassDefinition(
        name = "Akamai cache Purge scheduled task",
        description = "Akamai Cache Purge Schedular Job"
    )
    public @interface Config {
        @AttributeDefinition(name = "Cron-job expression")
        String scheduler_expression() default "0 0/5 * 1/1 * ? *";
    }

}
