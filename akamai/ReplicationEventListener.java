
package com..aem.cloud.core.corelisteners;


import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationActionType;
import com..aem.cloud.core.services.sitesreplication.SitesTargetReplicationService;
import com..aem.cloud.productcatalog.services.workflows.StartWorkflowService;

import lombok.extern.slf4j.Slf4j;

import static com..aem.cloud.productcatalog.utils.Constants.FRAGMENT_PATH_PATTERN;
import static com..aem.cloud.productcatalog.utils.Constants.FRAGMENT_TYPES;
import static com..aem.cloud.productcatalog.utils.Constants.SITES_PAGES_PATH;


@Component(
    immediate = true,
    service = EventHandler.class,
    property = {
        Constants.SERVICE_DESCRIPTION + "= This event handler listens replication events",
        EventConstants.EVENT_TOPIC + "=" + ReplicationAction.EVENT_TOPIC
    }
)
@Slf4j
public class ReplicationEventListener implements EventHandler {

    @Reference
    private StartWorkflowService startWorkflowService;

    @Reference
    private SitesTargetReplicationService sitesTargetReplicationService;

    @Override
    public void handleEvent (Event event) {
        String[] paths = (String[]) event.getProperty("paths");
        String payloadPath = paths[0];
        String userId = event.getProperty("userId").toString();

        if (StringUtils.isEmpty(payloadPath)) {
            log.debug("Payload path is empty");
            return;
        }

        try {
            String fragmentType = payloadPath.replaceFirst(FRAGMENT_PATH_PATTERN, "$1");
            ReplicationAction action = ReplicationAction.fromEvent(event);
            String visibility = action.getType().equals(ReplicationActionType.ACTIVATE) ? "true" : "false";

            if (FRAGMENT_TYPES.contains(fragmentType) && visibility.equals("true")) {
                startWorkflowService.startWorkflow(fragmentType, payloadPath, userId);
            } else if (payloadPath.startsWith(SITES_PAGES_PATH)) {
                sitesTargetReplicationService.sitesTargetReplication(payloadPath, visibility);
            }
        } catch (Exception ex) {
            log.error("\n Error while Activating/Deactivating - {} " , ex.getMessage());
        }
    }

}
