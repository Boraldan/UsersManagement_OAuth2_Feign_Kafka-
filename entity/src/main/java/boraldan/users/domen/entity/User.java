package boraldan.users.domen.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private UUID userId;

    @NotBlank(message = "Фамилия не может быть пустой")
    @Size(max = 30, message = "username не может быть длиннее 30 символов")
    @Column(name = "username")
    private String username;

    @Size(max = 50, message = "Фамилия не может быть длиннее 50 символов")
    @Column(name = "last_name")
    private String lastName;

    @Size(max = 50, message = "Имя не может быть длиннее 50 символов")
    @Column(name = "first_name")
    private String firstName;

    @Size(max = 50, message = "Отчество не может быть длиннее 50 символов")
    @Column(name = "middle_name" )
    private String middleName;

    @Past(message = "Дата рождения должна быть в прошлом")
    @Column(name = "date_birth" )
    private LocalDate dateBirth;

    @Email(message = "Электронная почта должна быть корректной")
    @Column(name = "email")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]*$", message = "Номер телефона должен содержать только цифры и может начинаться с +")
    @Column(name = "phone_number" )
    private String phoneNumber;

    @Column(name = "photo_url" )
    private String photoUrl;
}