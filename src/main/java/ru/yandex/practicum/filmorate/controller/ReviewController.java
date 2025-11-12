package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

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
}
