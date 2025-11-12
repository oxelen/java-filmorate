package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Review {
    private Long id;
    private String content;
    private Long userId;
    private Long filmId;
    private int useful;

    public boolean isPositive() {
        return useful > 0;
    }
}
