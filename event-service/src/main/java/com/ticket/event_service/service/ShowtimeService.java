package com.ticket.event_service.service;

import com.ticket.event_service.dto.SeatResponse;
import com.ticket.event_service.dto.ShowtimeRequest;
import com.ticket.event_service.dto.ShowtimeResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ShowtimeService {
    ShowtimeResponse createShowtime(ShowtimeRequest request);
    List<ShowtimeResponse> getShowtimesByMovieAndDate(UUID movieId, LocalDate showDate);
    List<SeatResponse> getSeatsByShowtime(UUID showtimeId);
}
