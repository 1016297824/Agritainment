package com.agritainment.controller;

import com.agritainment.annotation.RequireRole;
import com.agritainment.common.Result;
import com.agritainment.dto.CreateJournalRequest;
import com.agritainment.dto.UpdateJournalRequest;
import com.agritainment.entity.Journal;
import com.agritainment.service.JournalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/journals")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;

    @GetMapping
    @RequireRole({"customer", "staff", "admin"})
    public Result<List<Journal>> getJournals(@RequestAttribute("userId") Long userId) {
        return Result.ok(journalService.getJournals(userId));
    }

    @GetMapping("/shared")
    public Result<List<Journal>> getSharedJournals() {
        return Result.ok(journalService.getSharedJournals());
    }

    @PostMapping
    @RequireRole({"customer", "staff", "admin"})
    public Result<Journal> createJournal(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateJournalRequest request) {
        return Result.ok(journalService.createJournal(userId, request.getTitle(), request.getContent(), request.getImages()));
    }

    @PutMapping("/{id}")
    @RequireRole({"customer", "staff", "admin"})
    public Result<Journal> updateJournal(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateJournalRequest request) {
        return Result.ok(journalService.updateJournal(userId, id, request.getTitle(), request.getContent(), request.getImages()));
    }

    @PostMapping("/{id}/share")
    @RequireRole({"customer", "staff", "admin"})
    public Result<Void> shareJournal(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        journalService.shareJournal(userId, id);
        return Result.ok(null);
    }

    @DeleteMapping("/{id}/share")
    @RequireRole({"customer", "staff", "admin"})
    public Result<Void> unshareJournal(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        journalService.unshareJournal(userId, id);
        return Result.ok(null);
    }
}
