package com.TakeNotes.Repository;

import com.TakeNotes.Document.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends MongoRepository<Note, String> {
    List<Note> findAllByUserIdAndActiveIsTrueAndImportantIsFalse(String userId);
    List<Note> findAllByUserIdAndActiveIsFalse(String userId);
    List<Note> findAllByUserIdAndImportantIsTrueAndActiveIsTrue(String userId);
    @Query("{ 'userId': ?0, 'active': true, " +
            "$or: [{'title': {$regex: ?1, $options: 'i'}}, {'content':  {$regex:  ?1, $options: 'i'}}] }")
    List<Note> findByUserIdAndTitleOrContent(String userId, String searchText);
}
