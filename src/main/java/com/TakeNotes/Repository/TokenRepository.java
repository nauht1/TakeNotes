package com.TakeNotes.Repository;

import com.TakeNotes.Document.Token;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends MongoRepository<Token, String> {
    @Query(value = """
        {
            $and: [
                { userId: ?0 },
                { $or: [{expired: false}, {revoked: false}] }
            ]
        }
    """)
    List<Token> findAllValidTokenByUser(String id);

    Optional<Token> findByToken(String token);
}
