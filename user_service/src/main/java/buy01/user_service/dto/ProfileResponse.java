package buy01.user_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import buy01.user_service.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileResponse {
    private String id;
    private String name;
    private String email;
    private Role role;
    private String avatarUrl;
    private String createdAt;

}