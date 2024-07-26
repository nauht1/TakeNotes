package com.TakeNotes.Components;

import com.TakeNotes.Repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TrashCleanupScheduler {
    @Autowired
    private NoteRepository noteRepository;

    // run every day at night to check time,
    // rm note if it was 7days from deletedAt field
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteOldNotes() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        noteRepository.deleteByActiveIsFalseAndDeletedAtBefore(sevenDaysAgo);
    }
}
