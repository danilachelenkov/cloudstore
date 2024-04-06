package ru.netology.diplomcloudstore.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.netology.diplomcloudstore.entities.FileEntity;

import java.util.List;
import java.util.Optional;


@Repository
@EnableJpaRepositories
public interface FileEntityRepository extends JpaRepository<FileEntity, Long> {
    @Query(value = """
                        select  f.*
                          from diplom_netology_db.users u
                         inner join  diplom_netology_db.files_cloudstore f 
                            on u.id = f.user_id
                         where u.email = :username 
                           and f.deleted = :deleted
                           limit :limit         
            """
            , nativeQuery = true
    )
    List<FileEntity> findAllByUserAndNotDeletedWithLimit(@Param("username") String username,
                                                         @Param("deleted") boolean deleted,
                                                         @Param("limit") int limit);

    @Query(value = """
                        select  f.*
                          from diplom_netology_db.users u
                         inner join  diplom_netology_db.files_cloudstore f 
                            on u.id = f.user_id
                         where u.email = :username 
                           and f.deleted = :deleted
                           and f.file_Name = :fileName
                         limit 1      
            """
            , nativeQuery = true)
    Optional<FileEntity> findFirstByFileNameAndUserAndDeletedNot(@Param("username") String username,
                                                                 @Param("fileName") String fileName,
                                                                 @Param("deleted") boolean deleted


    );
}
