package com.ticket.event_service.controller;

import com.ticket.common.dto.ApiResponse;
import com.ticket.event_service.dto.MovieRequest;
import com.ticket.event_service.dto.MovieResponse;
import com.ticket.event_service.service.MovieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MovieResponse>> createMovie(@RequestBody MovieRequest request) {
        return new ResponseEntity<>(ApiResponse.success("Movie created successfully", movieService.createMovie(request)), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getAllMovies() {
        return ResponseEntity.ok(ApiResponse.success("Get all movies successfully", movieService.getAllMovies()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieResponse>> getMovieById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Get movie details successfully", movieService.getMovieById(id)));
    }
}
