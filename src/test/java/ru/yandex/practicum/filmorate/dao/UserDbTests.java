package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dal.UserDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbTests {
    private final UserDbStorage userStorage;

    @Test
    void testCreateUser() {
        User user = User.builder()
                .email("test@test.ru")
                .login("testLogin")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User created = userStorage.create(user);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("test@test.ru");

        Optional<User> found = userStorage.findById(created.getId());
        assertThat(found).isPresent();
    }

    @Test
    void testFindById() {
        User user = User.builder()
                .email("test@test.ru")
                .login("testLogin")
                .name("test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User created = userStorage.create(user);
        Optional<User> found = userStorage.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getLogin()).isEqualTo("testLogin");
    }

    //Этот тест надо поправить: сделать @AfterEach с удалением всех фильмов из БД
    /*@Test
    void testFindAll() {
        userStorage.create(User.builder()
                .email("a@test.ru")
                .login("aLogin")
                .name("User A")
                .birthday(LocalDate.now())
                .build());
        userStorage.create(User.builder()
                .email("b@test.ru")
                .login("bLogin")
                .name("User B")
                .birthday(LocalDate.now())
                .build());

        Collection<User> users = userStorage.findAll();
        assertThat(users.size()).isEqualTo(2);
    }*/

    @Test
    void testUpdateUser() {
        User user = userStorage.create(User.builder()
                .email("old@test.ru")
                .login("oldLogin")
                .name("Old Name")
                .birthday(LocalDate.of(1999, 9, 9))
                .build());

        User updatedUser = User.builder()
                .id(user.getId())
                .email("new@test.ru")
                .login("newLogin")
                .name("New Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        userStorage.update(updatedUser);

        Optional<User> found = userStorage.findById(user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("new@test.ru");
        assertThat(found.get().getName()).isEqualTo("New Name");
    }

    @Test
    void testContainsUser() {
        User user = userStorage.create(User.builder()
                .email("contain@test.ru")
                .login("containLogin")
                .name("Contain User")
                .birthday(LocalDate.of(1980, 3, 3))
                .build());

        boolean exists = userStorage.containsUser(user.getId());
        boolean notExists = userStorage.containsUser(9999L);

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
