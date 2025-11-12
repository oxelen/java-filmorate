package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/reviews")
@Slf4j
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public Review create(@RequestBody Review review) {
        log.info("Starting POST METHOD create review");

        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@RequestBody Review newReview) {
        log.info("Starting PUT METHOD update review");

        return reviewService.update(newReview);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        log.info("Starting DELETE METHOD delete review");
        PathVariableValidator.checkIds(id);

        return reviewService.delete(id);
    }

    @GetMapping("/{id}")
    public Review findById(Long id) {
        log.info("Starting GET METHOD find by id");
        PathVariableValidator.checkIds(id);

        return reviewService.findById(id);
    }

    @GetMapping
    public Collection<Review> findAll(@RequestParam Optional<Long> optionalFilmId,
                                      @RequestParam Optional<Integer> optionalCount) {
        log.info("Starting GET METHOD find all reviews");
        final Integer DEFAULT_COUNT = 10;
        log.debug("default count = {}", DEFAULT_COUNT);

        if (optionalFilmId.isPresent()) {
            Long filmId = optionalFilmId.get();
            PathVariableValidator.checkIds(filmId);

            return reviewService.findAll(filmId, optionalCount.orElse(DEFAULT_COUNT));
        }

        return reviewService.findAll();
    }

    @PutMapping("/{id}/like/{userId}")
    public Review putLike(@PathVariable Long id,
                          @PathVariable Long userId) {
        log.info("Starting PUT METHOD put like");

        PathVariableValidator.checkIds(id, userId);

        return reviewService.putLike(id, userId);
    }
}
