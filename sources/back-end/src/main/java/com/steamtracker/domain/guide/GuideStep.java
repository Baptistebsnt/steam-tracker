package com.steamtracker.domain.guide;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guide_steps")
@Getter
@Setter
@NoArgsConstructor
public class GuideStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id", nullable = false)
    private Guide guide;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false)
    private String title;

    @Column
    private String content;

    @OneToMany(mappedBy = "step", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GuideStepAchievement> achievements = new ArrayList<>();

    public void addAchievement(GuideStepAchievement achievement) {
        achievement.setStep(this);
        achievements.add(achievement);
    }
}
