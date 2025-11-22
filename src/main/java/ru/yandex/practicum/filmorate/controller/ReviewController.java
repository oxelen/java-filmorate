package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;

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
    public Review findById(@PathVariable Long id) {
        log.info("Starting GET METHOD find by id");
        PathVariableValidator.checkIds(id);

        return reviewService.findById(id);
    }

    @GetMapping
    public Collection<Review> findAll(@RequestParam Long filmId,
                                      @RequestParam(defaultValue = "10") Integer count) {
        log.info("Starting GET METHOD find all reviews");

        if (filmId != null) {
            log.debug("filmId = {}. Starting find reviews of this film. Count = {}", filmId, count);

            PathVariableValidator.checkIds(filmId);

            return reviewService.findAll(filmId, count);
        }
        log.debug("filmId is null. Starting find all reviews");

        return reviewService.findAll();
    }

    @PutMapping("/{id}/like/{userId}")
    public Review putLike(@PathVariable Long id,
                          @PathVariable Long userId) {
        log.info("Starting PUT METHOD put like");

        PathVariableValidator.checkIds(id, userId);

        return reviewService.putLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public Review putDuslike(@PathVariable Long id,
                             @PathVariable Long userId) {
        log.info("Starting PUT METHOD put dislike");

        PathVariableValidator.checkIds(id, userId);

        return reviewService.putDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public boolean deleteLike(@PathVariable Long id,
                              @PathVariable Long userId) {
        log.info("Starting DELETE METHOD delete like");

        PathVariableValidator.checkIds(id, userId);

        return reviewService.deleteLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public boolean deleteDislike(@PathVariable Long id,
                                 @PathVariable Long userId) {
        log.info("Starting DELETE METHOD delete dislike");

        PathVariableValidator.checkIds(id, userId);

        return reviewService.deleteDislike(id, userId);
    }
}
