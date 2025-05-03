package domain.driven.entity;

import domain.driven.entity.enums.SocialType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SocialInfo {
    @Enumerated(EnumType.STRING)
    private SocialType socialType;
    private String socialId;
}
