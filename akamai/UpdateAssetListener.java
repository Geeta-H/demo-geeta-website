
package com..aem.cloud.core.corelisteners;


import com.day.cq.dam.api.DamEvent;
import com..aem.cloud.productcatalog.services.subservice.SubserviceService;
import com..aem.cloud.productcatalog.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import javax.jcr.Node;
import javax.jcr.RepositoryException;


@Component(
    immediate = true,
    service = EventHandler.class,
    property = {
        Constants.SERVICE_DESCRIPTION + "= This event handler listens replication events",
        EventConstants.EVENT_TOPIC + "=" + DamEvent.EVENT_TOPIC
    }
)
@Slf4j
public class UpdateAssetListener implements EventHandler {

    @Reference
    private SubserviceService subserviceService;

    @Override
    public void handleEvent (Event event) {
        if (event.getProperty("type") == DamEvent.Type.DAM_UPDATE_ASSET_WORKFLOW_COMPLETED) {
            String path = DamEvent.fromEvent(event).getAssetPath();
            
            if (StringUtils.startsWith(path, "/content/dam/hn-one")) {
                try (ResourceResolver resourceResolver = subserviceService.getSubserviceResourceResolver(Constants.SUBSERVICE_NAME_REGIONAL_WRITER)) {
                    Resource resource = resourceResolver.getResource(path);
                    Node node = resource.adaptTo(Node.class);

                    if (node.hasNode("jcr:content")) {
                        Node jcrContent = node.getNode("jcr:content");

                        if (jcrContent.hasProperty("onTime")) {
                            jcrContent.getProperty("onTime").remove();
                            log.debug("Removed 'onTime' property from asset: {}", path);
                        }

                        if (jcrContent.hasProperty("offTime")) {
                            jcrContent.getProperty("offTime").remove();
                            log.debug("Removed 'offTime' property from asset: {}", path);
                        }

                        if (jcrContent.hasNode("metadata")) {
                            Node metadataNode = jcrContent.getNode("metadata");

                            if (metadataNode.hasProperty("cq:tags")) {
                                metadataNode.getProperty("cq:tags").remove();
                                log.debug("Removed 'cq:tags' property from asset: {}", path);
                            }

                            if (metadataNode.hasProperty("prism:expirationDate")) {
                                metadataNode.getProperty("prism:expirationDate").remove();
                                log.debug("Removed 'prism:expirationDate' property from asset: {}", path);
                            }
                        }
                    }

                    resourceResolver.commit();

                } catch (PersistenceException | LoginException | RepositoryException e) {
                    log.error("Exception catched on Asset Handler event", e);
                }
            }
        }
    }

}
