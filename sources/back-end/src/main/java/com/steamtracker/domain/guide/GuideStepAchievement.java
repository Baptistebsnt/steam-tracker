package com.steamtracker.domain.guide;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "guide_step_achievements")
@Getter
@Setter
@NoArgsConstructor
public class GuideStepAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_step_id", nullable = false)
    private GuideStep step;

    @Column(name = "api_name", nullable = false)
    private String apiName;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "icon_url")
    private String iconUrl;
}
