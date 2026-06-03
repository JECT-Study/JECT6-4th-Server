package com.ject6.boost.domain.user.domain.entity;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@EqualsAndHashCode
public class UserRegionId implements Serializable {

    private Long user;
    private Long region;
}
