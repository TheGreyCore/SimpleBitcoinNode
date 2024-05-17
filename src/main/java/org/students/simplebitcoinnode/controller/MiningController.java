package org.students.simplebitcoinnode.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.students.simplebitcoinnode.dto.DistributedMiningRequestDTO;

@RestController
@RequestMapping("/blockchain/")
public class MiningController {



    @PostMapping("/mine")
    public ResponseEntity<?> mine (@RequestBody DistributedMiningRequestDTO miningRequestDTO, HttpServletRequest httpServletRequest){
        return null;
    }
}
