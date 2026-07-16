package com.steamtracker.domain.guide;

import com.steamtracker.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guides")
@Getter
@Setter
@NoArgsConstructor
public class Guide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "app_id", nullable = false)
    private Long appId;

    @Column(name = "game_name", nullable = false)
    private String gameName;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "guide", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<GuideStep> steps = new ArrayList<>();

    public void replaceSteps(List<GuideStep> newSteps) {
        steps.clear();
        newSteps.forEach(this::addStep);
    }

    public void addStep(GuideStep step) {
        step.setGuide(this);
        steps.add(step);
    }
}
