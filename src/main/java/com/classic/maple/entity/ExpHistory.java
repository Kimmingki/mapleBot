package com.classic.maple.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "exp_history")
public class ExpHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String characterName;

    @Column(nullable = false)
    private String worldName;

    @Column(nullable = false)
    private LocalDate recordDate; // 기록된 날짜

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false)
    private Long exp; // 누적 경험치

    @Builder
    public ExpHistory(String characterName, String worldName, LocalDate recordDate, Integer level, Long exp) {
        this.characterName = characterName;
        this.worldName = worldName;
        this.recordDate = recordDate;
        this.level = level;
        this.exp = exp;
    }
}
