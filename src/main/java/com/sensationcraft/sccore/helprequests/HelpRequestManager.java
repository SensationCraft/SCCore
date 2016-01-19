package com.sensationcraft.sccore.helprequests;

import com.sensationcraft.sccore.SCCore;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Anml on 1/18/16.
 */

@Getter
public class HelpRequestManager {

    private SCCore instance;
    private Map<UUID, HelpRequest> requests = new HashMap<>();

    public HelpRequestManager(SCCore instance) {
        this.instance = instance;
    }

    public boolean addRequest(HelpRequest helpRequest) {
        if (requests.containsKey(helpRequest.getCreator()))
            return false;

        requests.put(helpRequest.getCreator(), helpRequest);
        return true;
    }

    public boolean removeRequest(UUID uuid) {
        if (!requests.containsKey(uuid))
            return false;

        requests.remove(uuid);
        return true;
    }

    public HelpRequest getRequest(UUID uuid) {
        if (!requests.containsKey(uuid))
            return null;

        return requests.get(uuid);
    }
}
