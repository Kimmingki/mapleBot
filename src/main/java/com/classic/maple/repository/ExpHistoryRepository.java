package com.classic.maple.repository;

import com.classic.maple.entity.ExpHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpHistoryRepository extends JpaRepository<ExpHistory, Long> {

    // 1. 오늘 날짜로 이미 저장된 기록이 있는지 확인하는 용도
    Optional<ExpHistory> findByCharacterNameAndWorldNameAndRecordDate(String name, String world, LocalDate date);

    // 2. 특정 캐릭터의 최근 기록을 날짜 내림차순(최신순)으로 최대 6개 가져오는 용도
    List<ExpHistory> findTop6ByCharacterNameAndWorldNameOrderByRecordDateDesc(String name, String world);
}
