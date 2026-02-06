package com.example.config_service.core.service.impl;

import com.example.config_service.core.service.RuleWatcher;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Component
public class RuleWatcherImpl implements RuleWatcher {

    private final ScheduledExecutorService debounceExecutor = Executors.newSingleThreadScheduledExecutor();
    private final Map<Path, Long> lastModifiedTimes = new ConcurrentHashMap<>();

    @Override
    @Async
    public void watch(Path ruleFolder, long debounceTimeMs, RuleChangeCallback onRuleChanged) throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        ruleFolder.register(
                watchService,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE
        );

        log.info("Watching folder {} for DRL changes", ruleFolder.toAbsolutePath());

        while (true) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                Path fileName = (Path) event.context();
                log.info("event context: " + fileName);
                if (fileName.toString().endsWith(".drl")) {
                    Path changedFile = ruleFolder.resolve(fileName);

                    // Log file thay đổi và loại sự kiện (ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
                    WatchEvent.Kind<?> kind = event.kind();
                    log.info("Detected change: " + kind.name() + " on file: " + changedFile);

                    lastModifiedTimes.put(changedFile, System.currentTimeMillis());

                    debounceExecutor.schedule(() -> {
                        long lastModifiedTime = lastModifiedTimes.getOrDefault(changedFile, 0L);
                        if (System.currentTimeMillis() - lastModifiedTime >= debounceTimeMs) {
                            log.info("Triggering onChanged function for file: " + changedFile);
                            onRuleChanged.onChanged(changedFile);
                        }
                    }, debounceTimeMs, TimeUnit.MILLISECONDS);
                }
            }
            key.reset();
        }
    }

    @PreDestroy
    public void shutdown() {
        debounceExecutor.shutdownNow();
    }
}