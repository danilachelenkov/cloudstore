package ru.netology.diplomcloudstore.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.diplomcloudstore.entities.UserJwtToken;

import java.util.Optional;

@Repository
@EnableJpaRepositories
public interface UserJwtRepository extends CrudRepository<UserJwtToken, Long> {

    //Если для одного пользователя будет сгенерировано несоклько jwt, то брать для проверки будем последний по дате\времени
    @Query(value = "select bl.*" +
            "  from diplom_netology_db.users_jwt_blacklist bl" +
            " inner join diplom_netology_db.users u" +
            "    on u.id = bl.user_id" +
            " where u.email = ?1 " +
            "   and bl.actual = (select max(sbl.actual) " +
            "                      from diplom_netology_db.users_jwt_blacklist sbl " +
            "                     where sbl.user_id = bl.user_id )",
            nativeQuery = true
    )
    @Transactional
    Optional<UserJwtToken> findUserJwtTokenByUserAndActual(String username);


    //Добавляем запись сгенерированого токена для конкретного пользователя на каждую попытку генерации
    @Modifying
    @Query(value = "insert into diplom_netology_db.users_jwt_blacklist (actual, jwt, user_id) " +
                   "select CURRENT_TIMESTAMP, :jwt, u.id " +
                   "  from diplom_netology_db.users u\n" +
                   " where u.email = :username",
            nativeQuery = true)
    @Transactional
    void insertJwt(@Param("username") String username, @Param("jwt") String jwt);

    //logout удаление всех записей о токене для конгкретного пользователя
    @Modifying
    @Query(value = " delete from diplom_netology_db.users_jwt_blacklist bl " +
                   "  where bl.user_id in (select u.id " +
                   "  from diplom_netology_db.users u " +
                   "  where u.email =:username)",
            nativeQuery = true)
    @Transactional
    void deleteJwt(@Param("username") String username);
}
