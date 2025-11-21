package ru.yandex.practicum.filmorate.service;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dal.*;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
class UserServiceIntegrationTest {

    private final UserService userService;
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final LikesRepository likesRepository;
    private final FriendsRepository friendsRepository;
    private final FilmService filmService;
    private final ReviewService reviewService;
    private final ReviewsDbStorage reviewsDbStorage;

    @Test
    @DisplayName("Удаление пользователя должно очищать друзей, отзывы и лайки, а также удалить его из базы")
    void deleteUserById_shouldRemoveUserHisFriendsReviewsAndLikes() {
        User user = userStorage.create(User.builder()
                .email("test@example.com")
                .login("testUser")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        MPA mpa = MPA.builder()
                .id(1L)
                .name("PG-13")
                .build();

        Film film = filmStorage.create(Film.builder()
                .name("Test Film")
                .description("Some film for test")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .mpa(mpa)
                .build());

        User friend = userStorage.create(User.builder()
                .email("testFriend@example.com")
                .login("testFriend")
                .name("testFriend")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        Review review = Review.builder()
                .content("Test Review")
                .isPositive(true)
                .userId(user.getId())
                .filmId(film.getId())
                .useful(0)
                .build();

        userService.addFriend(user.getId(), friend.getId());
        userService.addFriend(friend.getId(), user.getId());
        filmService.likeFilm(film.getId(), user.getId());
        reviewService.create(review);

        assertThat(reviewsDbStorage.findById(review.getReviewId())).contains(review);
        assertThat(friendsRepository.findAllFriends(user.getId())).contains(friend.getId());
        assertThat(friendsRepository.findAllFriends(friend.getId())).contains(user.getId());
        assertThat(likesRepository.findAllLikes(film.getId())).contains(user.getId());

        userService.deleteUserById(user.getId());

        assertThat(friendsRepository.findAllFriends(friend.getId())).doesNotContain(user.getId());
        assertThat(friendsRepository.findAllFriends(user.getId())).isEmpty();
        assertThat(likesRepository.findAllLikes(film.getId())).doesNotContain(user.getId());
        assertThat(reviewsDbStorage.findAll()).doesNotContain(review);
    }
}
