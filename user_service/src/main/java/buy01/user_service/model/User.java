package buy01.user_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "users")
// add constraints for username, email 
public class User {
    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    @Indexed(unique = true)
    private String email;
    private String password;
    //@Enumerated(EnumType.STRING)
    private Role role;

    private String avatarUrl; 
    private String createdAt; // ISO 8601 format
}


