package com.ject6.boost.domain.user.domain.repository;

import com.ject6.boost.domain.user.domain.entity.User;
import java.time.OffsetDateTime;

public interface BlogAnalysisResultRepository {

    int softDeleteByUser(User user, OffsetDateTime deletedAt);
}
