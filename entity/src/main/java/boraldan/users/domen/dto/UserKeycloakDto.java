package boraldan.users.domen.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserKeycloakDto {

    private String username;
    private String password;
}