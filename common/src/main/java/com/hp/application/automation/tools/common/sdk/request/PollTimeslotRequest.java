package com.hp.application.automation.tools.common.sdk.request;

import com.hp.application.automation.tools.common.sdk.Client;

/***
 * 
 * @author Effi Bar-She'an
 * @author Dani Schreiber
 * 
 */

public class PollTimeslotRequest extends GetRequest {
    
    private final String _timeslotId;
    
    public PollTimeslotRequest(Client client, String timeslotId) {
        
        super(client, timeslotId);
        _timeslotId = timeslotId;
    }
    
    @Override
    protected String getSuffix() {
        
        return String.format("reservations/%s", _timeslotId);
    }
}
