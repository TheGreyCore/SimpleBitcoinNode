package org.students.simplebitcoinwallet.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.students.simplebitcoinwallet.service.EndpointService;

import java.util.Map;

@RestController()
@RequestMapping("/blockchain")
public class EndpointController {
    private final EndpointService endpointService;

    public EndpointController(EndpointService endpointService) {
        this.endpointService = endpointService;
    }

    @GetMapping("/status")
    public Map<String, String> status(){
        return endpointService.getStatus();
    }
}
