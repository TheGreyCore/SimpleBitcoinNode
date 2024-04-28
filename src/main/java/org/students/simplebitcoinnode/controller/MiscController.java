package org.students.simplebitcoinnode.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.logging.Logger;

@RestController
@RequestMapping("/blockchain")
public class MiscController {
    Logger logger = Logger.getLogger(TransactionsController.class.getName());

    /**
     * It returns the current time in UTC format.
     * @return ResponseEntity - OK status and the current time in UTC if successful,
     * INTERNAL_SERVER_ERROR status and error message if an exception occurs.
     */
    @GetMapping("/time")
    private ResponseEntity<?> getTime(){
        try {
            LocalDateTime time = LocalDateTime.now(ZoneId.of("UTC"));
            return ResponseEntity.ok().body(time.toString());
        } catch (Exception e) {
            logger.warning("An server error occurred when user tried get Time: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An server error occurred!");
        }
    }
}
