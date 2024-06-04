package com.TakeNotes.Document;

import com.TakeNotes.Enum.TokenType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "token")
public class Token {
    @Id
    private String id;

    @Indexed(unique = true)
    private String token;

    private TokenType tokenType = TokenType.BEARER;
    private String userId;
    public boolean revoked;
    public boolean expired;
}
