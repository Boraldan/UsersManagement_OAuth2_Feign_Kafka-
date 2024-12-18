package boraldan.users.domen.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class CreatUserDto {

    @NotBlank(message = "Фамилия не может быть пустой")
    @Size(max = 30, message = "username не может быть длиннее 30 символов")
    private String username;

    @NotBlank(message = "Пароль не может быть пустой")
    @Size(max = 50, message = "password не может быть длиннее 50 символов")
    private String password;

    @Size(max = 50, message = "Фамилия не может быть длиннее 50 символов")
    private String lastName;

    @Size(max = 50, message = "Имя не может быть длиннее 50 символов")
    private String firstName;

    @Size(max = 50, message = "Отчество не может быть длиннее 50 символов")
    private String middleName;

    @Past(message = "Дата рождения должна быть в прошлом")
    private LocalDate dateBirth;

    @Email(message = "Электронная почта должна быть корректной")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]*$", message = "Номер телефона должен содержать только цифры и может начинаться с +")
    private String phoneNumber;

    @Column(name = "photo_url" )
    private String photoUrl;
}
