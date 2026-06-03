package com.ject6.boost.domain.auth.infrastructure.oauth;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "oauth")
public class OAuthClientProperties {

    private Duration sessionTtl = Duration.ofMinutes(30);
    private Duration refreshSessionTtl = Duration.ofDays(7);
}
