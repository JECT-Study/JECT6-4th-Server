package com.ject6.boost.application.onboarding.service;

import com.ject6.boost.domain.onboarding.entity.OnboardingResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OnboardingProfileTextBuilder {

    public String build(OnboardingResponse response) {
        StringBuilder sb = new StringBuilder("온보딩 프로필:\n");
        appendLine(sb, "관심 카테고리", response.getStep1Answer());
        appendLine(sb, "활동 목적/스타일", response.getStep2Answer());
        appendLine(sb, "선호 캠페인 유형", response.getStep3Answer());
        appendLine(sb, "활동 수준", response.getStep4Answer());
        appendLine(sb, "선호 지역 ID", formatList(response.getRegionIds()));
        appendLine(sb, "활동 유형", formatList(response.getActivityTypes()));
        sb.append("\n이 사용자는 위 조건에 맞는 체험단 공고에 관심이 있다.");
        return sb.toString();
    }

    private void appendLine(StringBuilder sb, String label, String value) {
        sb.append("- ").append(label).append(": ")
          .append(value != null ? value : "미입력").append("\n");
    }

    private <T> String formatList(List<T> items) {
        if (items == null || items.isEmpty()) return "미입력";
        return items.stream().map(Object::toString).reduce((a, b) -> a + ", " + b).orElse("미입력");
    }
}
