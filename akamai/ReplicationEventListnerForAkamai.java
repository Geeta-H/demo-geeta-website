
package com..aem.cloud.core.akamai;


import com.day.cq.commons.Externalizer;
import com.day.cq.replication.ReplicationAction;
import com..aem.cloud.productcatalog.services.subservice.SubserviceService;
import lombok.extern.slf4j.Slf4j;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

import static com..aem.cloud.productcatalog.utils.Constants.SUBSERVICE_NAME_REGIONAL_WRITER;


@Component(
    immediate = true,
    service = EventHandler.class,
    property = {
        Constants.SERVICE_DESCRIPTION + "= This event handler listens the events on page activation",
        EventConstants.EVENT_TOPIC + "=" + ReplicationAction.EVENT_TOPIC
    }
)
@Slf4j
public class ReplicationEventListnerForAkamai implements EventHandler {

    @Reference
    Externalizer externalizer;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    SubserviceService subserviceService;

    private AkamaiSingleton singleton = AkamaiSingleton.getInstance();

    @Override
    public void handleEvent (Event event) {
        String[] paths = (String[]) event.getProperty("paths");
        String payloadPath = paths[0];

        try (ResourceResolver resourceResolver = subserviceService.getSubserviceResourceResolver(SUBSERVICE_NAME_REGIONAL_WRITER)) {
            Resource resource = resourceResolver.resolve(payloadPath);
            if (resource != null && resource.getResourceType() != null) {
                getPublishLink(resourceResolver, payloadPath, resource.getResourceType(), externalizer);
            }
        } catch (LoginException e) {
            log.error("Error while getting the resource Resolver");
        }
    }

    public void getPublishLink(ResourceResolver resourceResolver, String path, String type, Externalizer externalizer) {
        List<String> urls = new ArrayList<>();

        if (type.equalsIgnoreCase("cq:Page")) {
            if (path.startsWith("/content/experience-fragments")) {
                String hostwithHtml = externalizer.publishLink(resourceResolver, path + ".html");
                String hostWithoutHtml = externalizer.publishLink(resourceResolver, path);
                urls.add(hostwithHtml);
                urls.add(hostWithoutHtml);
            } else {
                path = path.substring(15);
                String hostwithHtml = externalizer.publishLink(resourceResolver, path + ".html");
                String hostWithoutHtml = externalizer.publishLink(resourceResolver, path);
                urls.add(hostwithHtml);
                urls.add(hostWithoutHtml);
            }
        } else if (type.equalsIgnoreCase("dam:Asset")) {
            if (path.startsWith("/content/hn-one/sitemaps")) {
                path = path.substring(8);
            } else {
                path = path.substring(12);
            }
            String hostwithAssets = externalizer.publishLink(resourceResolver, "/assets" + path);
            String hostWithDmAssets = externalizer.publishLink(resourceResolver, "/dmassets" + path);
            urls.add(hostwithAssets);
            urls.add(hostWithDmAssets);
        } else {
            path = path.substring(5);
            String hostUrl = externalizer.publishLink(resourceResolver, "/etc.clientlibs" + path);
            urls.add(hostUrl);
        }

        singleton.setUrlList(urls);
    }

}
