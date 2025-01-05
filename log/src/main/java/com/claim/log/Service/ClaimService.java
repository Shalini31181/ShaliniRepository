package com.claim.log.Service;

import java.io.BufferedWriter;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClaimService {

    private static final Logger logger = LoggerFactory.getLogger(ClaimService.class);
    private static final String FILE_PATH = "claims_log.txt";
    private static final String ZIP_FILE_PATH = "claims_log.zip";
    private static final long MAX_FILE_SIZE = 1024; // 1 KB for demonstration
    private static final long RUN_DURATION = 3; // Run for 3 seconds

    private volatile boolean stopProcessing = false; // Flag to stop processing

    public void runWithTimeout() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = this::processClaims;

        logger.info("Starting claims processing for 3 seconds...");

        // Start processing claims
        scheduler.execute(task);

        // Schedule the stop after RUN_DURATION
        scheduler.schedule(() -> {
            logger.info("Stopping claims processing after 3 seconds.");
            stopProcessing = true; // Signal to stop processing
            scheduler.shutdown(); // Gracefully stop the executor
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    logger.warn("Forcefully shutting down tasks.");
                    scheduler.shutdownNow(); // Force shutdown if tasks aren't stopped
                }
            } catch (InterruptedException e) {
                logger.error("Interrupted during shutdown: {}", e.getMessage());
                scheduler.shutdownNow();
                Thread.currentThread().interrupt(); // Restore interrupt status
            }
        }, RUN_DURATION, TimeUnit.SECONDS);
    }

    public void processClaims() {
        try {
            for (int i = 1; i <= 50; i++) { // Simulating a large number of claims
                if (stopProcessing) {
                    logger.warn("Processing stopped by timeout.");
                    break;
                }

                writeClaimToFile(i);

                // Check file size after writing each claim
                if (Files.exists(Paths.get(FILE_PATH)) && Files.size(Paths.get(FILE_PATH)) >= MAX_FILE_SIZE) {
                    logger.info("File size limit reached. Compressing file into ZIP.");
                    closeAndCompressFile();
                }

                Thread.sleep(200); // Simulate time delay for processing
            }
        } catch (IOException e) {
            logger.error("Error during claims processing: {}", e.getMessage(), e);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.warn("Claims processing interrupted.");
        }
    }

    private void writeClaimToFile(int claimNumber) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            String message = "Claim number " + claimNumber + " processed successfully.";
            writer.write(message + "\n");
            logger.info(message);
        }
    }

    private void closeAndCompressFile() {
        Path sourceFilePath = Paths.get(FILE_PATH);
        Path zipFilePath = Paths.get(ZIP_FILE_PATH);

        try {
            if (Files.exists(sourceFilePath)) {
                try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(ZIP_FILE_PATH))) {
                    ZipEntry zipEntry = new ZipEntry(sourceFilePath.getFileName().toString());
                    zos.putNextEntry(zipEntry);
                    zos.write(Files.readAllBytes(sourceFilePath));
                    zos.closeEntry();
                }

                Files.delete(sourceFilePath); // Safely delete file
                logger.info("Compressed and deleted file: {}", FILE_PATH);
            }
        } catch (IOException e) {
            logger.error("Error compressing or deleting file: {}", e.getMessage(), e);
        }
    }
}
