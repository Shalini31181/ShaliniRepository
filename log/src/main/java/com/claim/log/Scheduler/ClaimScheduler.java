package com.claim.log.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.claim.log.Service.ClaimService;

@Component
public class ClaimScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ClaimScheduler.class);

    @Autowired
    private ClaimService claimService;

    @Scheduled(cron = "0 0/1 * * * ?") // Every 1 minute
    public void scheduledClaimsProcessing() {
        logger.info("Scheduled claims processing started.");
        try {
            claimService.processClaims();
            logger.info("Scheduled claims processing completed.");
        } catch (Exception e) {
            logger.error("Scheduled claims processing failed.", e);
        }
    }
}
