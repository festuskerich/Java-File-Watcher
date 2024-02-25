package com.fkerich.filewatcher.service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class FileWatcherService {
    Logger log = LoggerFactory.getLogger(FileWatcherService.class);
    Path dir = Path.of("/Users/festus.kerich/Desktop/filewatcher/");
    @Bean
    public void watch() throws IOException, InterruptedException {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        WatchKey key = dir.register(watcher,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);

        while (true) {
            watcher.take();
            for (WatchEvent<?> event : key.pollEvents()) {
                processEvent(event);
            }
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }

    private void processEvent(WatchEvent<?> event) {
        WatchEvent.Kind<?> kind = event.kind();
        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            handleFileCreationEvent(event);
        } else if (kind == StandardWatchEventKinds.OVERFLOW) {
            // Overflow event, do nothing
        }
    }

    private void handleFileCreationEvent(WatchEvent<?> event) {
        WatchEvent<Path> ev = (WatchEvent<Path>) event;
        Path filename = ev.context();
        try {
            Path child = dir.resolve(filename);
            if (Files.probeContentType(child).equals("text/csv")) {
                log.info("New file {} is is a csv file", filename);
                // todo: Make a call to a method the processes the CSV file.
            } else {
                log.error("New file {} is not a csv file", filename);
            }
        } catch (IOException x) {
            log.error("Erorr occured {}", filename, x);
        }
    }

}
