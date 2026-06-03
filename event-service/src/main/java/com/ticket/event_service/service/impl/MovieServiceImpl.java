package com.ticket.event_service.service.impl;

import com.ticket.event_service.dto.MovieRequest;
import com.ticket.event_service.dto.MovieResponse;
import com.ticket.event_service.model.Movie;
import com.ticket.event_service.repository.MovieRepository;
import com.ticket.event_service.service.MovieService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    public MovieServiceImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    public MovieResponse createMovie(MovieRequest request) {
        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDurationMinutes(request.getDurationMinutes());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setEndDate(request.getEndDate());

        Movie savedMovie = movieRepository.save(movie);
        return mapToResponse(savedMovie);
    }

    @Override
    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MovieResponse getMovieById(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
        return mapToResponse(movie);
    }

    private MovieResponse mapToResponse(Movie movie) {
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getDurationMinutes(),
                movie.getReleaseDate(),
                movie.getEndDate()
        );
    }
}
