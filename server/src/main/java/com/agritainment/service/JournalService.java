package com.agritainment.service;

import com.agritainment.annotation.BusinessLog;
import com.agritainment.common.AppException;
import com.agritainment.entity.Journal;
import com.agritainment.mapper.JournalMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JournalService {

    private final JournalMapper journalMapper;

    public List<Journal> getJournals(Long userId) {
        return journalMapper.selectList(new LambdaQueryWrapper<Journal>()
                .eq(Journal::getUserId, userId).orderByDesc(Journal::getCreatedAt));
    }

    public Journal getJournal(Long userId, Long id) {
        Journal journal = journalMapper.selectOne(new LambdaQueryWrapper<Journal>()
                .eq(Journal::getId, id).eq(Journal::getUserId, userId));
        if (journal == null) throw new AppException(40405, "日记不存在");
        return journal;
    }

    public List<Journal> getSharedJournals() {
        return journalMapper.selectList(new LambdaQueryWrapper<Journal>()
                .eq(Journal::getIsShared, true).orderByDesc(Journal::getCreatedAt));
    }

    public Journal createJournal(Long userId, String title, String content, String images) {
        Journal journal = new Journal();
        journal.setUserId(userId);
        journal.setTitle(title);
        journal.setContent(content);
        journal.setImages(images);
        journal.setIsShared(false);
        journalMapper.insert(journal);
        return journal;
    }

    public Journal updateJournal(Long userId, Long id, String title, String content, String images) {
        Journal journal = journalMapper.selectById(id);
        if (journal == null || !journal.getUserId().equals(userId))
            throw new AppException(40501, "日记不存在");
        journal.setTitle(title);
        journal.setContent(content);
        journal.setImages(images);
        journalMapper.updateById(journal);
        return journal;
    }

    @BusinessLog("分享日志")
    public void shareJournal(Long userId, Long id) {
        Journal journal = journalMapper.selectById(id);
        if (journal == null || !journal.getUserId().equals(userId))
            throw new AppException(40501, "日记不存在");
        journalMapper.update(null, new LambdaUpdateWrapper<Journal>()
                .eq(Journal::getId, id).set(Journal::getIsShared, true));
    }

    @BusinessLog("取消分享日志")
    public void unshareJournal(Long userId, Long id) {
        Journal journal = journalMapper.selectById(id);
        if (journal == null || !journal.getUserId().equals(userId))
            throw new AppException(40501, "日记不存在");
        journalMapper.update(null, new LambdaUpdateWrapper<Journal>()
                .eq(Journal::getId, id).set(Journal::getIsShared, false));
    }
}
