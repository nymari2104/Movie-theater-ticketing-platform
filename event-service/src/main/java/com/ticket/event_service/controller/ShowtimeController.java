package com.ticket.event_service.controller;

import com.ticket.event_service.dto.SeatResponse;
import com.ticket.event_service.dto.ShowtimeRequest;
import com.ticket.event_service.dto.ShowtimeResponse;
import com.ticket.event_service.service.ShowtimeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events/showtimes")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    public ShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    @PostMapping
    public ResponseEntity<ShowtimeResponse> createShowtime(@RequestBody ShowtimeRequest request) {
        return new ResponseEntity<>(showtimeService.createShowtime(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ShowtimeResponse>> getShowtimes(
            @RequestParam UUID movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate showDate) {
        return ResponseEntity.ok(showtimeService.getShowtimesByMovieAndDate(movieId, showDate));
    }

    @GetMapping("/{showtimeId}/seats")
    public ResponseEntity<List<SeatResponse>> getSeats(@PathVariable UUID showtimeId) {
        return ResponseEntity.ok(showtimeService.getSeatsByShowtime(showtimeId));
    }
}
