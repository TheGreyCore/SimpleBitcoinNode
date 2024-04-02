package org.students.simplebitcoinwallet.endpoint;


import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EndpointService {
    public EndpointService() {
    }

    /**
     * Method to check status of application.
     * @return Math with status of application
     */
    public Map<String, String> getStatus() {
        Map<String, String> response = new HashMap<>();
        response.put("API status", "UP");
        response.put("Database status", "Unknown"); // TODO implement database check

        return response;
    }
}

