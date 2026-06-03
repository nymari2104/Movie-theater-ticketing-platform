package com.ticket.event_service.service;

import com.ticket.event_service.dto.MovieRequest;
import com.ticket.event_service.dto.MovieResponse;
import java.util.List;
import java.util.UUID;

public interface MovieService {
    MovieResponse createMovie(MovieRequest request);
    List<MovieResponse> getAllMovies();
    MovieResponse getMovieById(UUID id);
}
