package org.students.simplebitcoinnode.service;

import org.springframework.stereotype.Service;
import org.students.simplebitcoinnode.util.DTOMapperWrapper;

@Service
public class NodeRegistrationService {
    private final DTOMapperWrapper dtoMapperWrapper;

    public NodeRegistrationService(DTOMapperWrapper dtoMapperWrapper) {
        this.dtoMapperWrapper = dtoMapperWrapper;
    }
}
