package org.students.simplebitcoinnode.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.students.simplebitcoinnode.dto.AdjacentNodeDTO;

@RestController
@RequestMapping("/blockchain/node/")
public class NodeRegistrationController {

    @PostMapping("/register")
    private ResponseEntity<?> registerNode(@RequestBody AdjacentNodeDTO adjacentNodeDTO){
        return null;
    }
}
