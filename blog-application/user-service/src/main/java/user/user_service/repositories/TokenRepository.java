package user.user_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import user.user_service.entities.Token;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {
    @Query("SELECT t FROM Token t " +
            "inner join User u on t.user.id = u.id " +
            "WHERE u.id = :userId " +
            "and (t.expired =false or t.revoked = false)")
    List<Token> findAllValidTokensByUser(Long userId);


    Optional<Token> findByToken(String token);
}