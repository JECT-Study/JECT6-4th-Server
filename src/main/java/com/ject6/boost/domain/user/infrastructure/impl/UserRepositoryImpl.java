package com.ject6.boost.domain.user.infrastructure.impl;

import com.ject6.boost.domain.user.domain.entity.User;
import com.ject6.boost.domain.user.infrastructure.repository.UserJpaRepository;
import com.ject6.boost.domain.user.infrastructure.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findActiveById(Long id) {
        return userJpaRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public boolean existsByNicknameAndDeletedAtIsNull(String nickname) {
        return userJpaRepository.existsByNicknameAndDeletedAtIsNull(nickname);
    }
}
