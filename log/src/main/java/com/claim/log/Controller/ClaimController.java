package com.claim.log.Controller;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.claim.log.Service.ClaimService;
@RequestMapping("/api")
	@RestController
	public class ClaimController {
  
	    private static final Logger logger = LoggerFactory.getLogger(ClaimController.class);

	    @Autowired
	    private ClaimService claimsService;

	    @GetMapping("/process-claims")
	    public String processClaims() {
	        logger.info("Received request to process claims.");
	        try {
	            claimsService.processClaims();
	            logger.info("Successfully processed claims.");
	            return "Claims processed successfully!";
	        } catch (Exception e) {
	            logger.error("Failed to process claims", e);
	            return "Error processing claims.";
	        }
	    }
	}
