package ru.netology.diplomcloudstore.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.netology.diplomcloudstore.entities.UserJwtTokenEntity;

import java.util.List;
import java.util.Optional;

@Repository
@EnableJpaRepositories
public interface UserJwtRepository extends CrudRepository<UserJwtTokenEntity, Long> {
    @Query("""
             select t 
               from UserJwtTokenEntity t 
              inner join UserEntity u 
                 on t.user.id = u.id
              where u.username = :username 
                and (t.expired = false or t.revoke = false)
            """)
    List<UserJwtTokenEntity> findAllValidUserJwtTokenByUser(@Param("username") String username);

    Optional<UserJwtTokenEntity> findByJwt(String jwt);
}
