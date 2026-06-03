package com.ticket.event_service.service.impl;

import com.ticket.event_service.dto.SeatResponse;
import com.ticket.event_service.dto.ShowtimeRequest;
import com.ticket.event_service.dto.ShowtimeResponse;
import com.ticket.event_service.model.Movie;
import com.ticket.event_service.model.Seat;
import com.ticket.event_service.model.SeatStatus;
import com.ticket.event_service.model.Showtime;
import com.ticket.event_service.repository.MovieRepository;
import com.ticket.event_service.repository.SeatRepository;
import com.ticket.event_service.repository.ShowtimeRepository;
import com.ticket.event_service.service.ShowtimeService;
import com.ticket.common.exception.AppException;
import com.ticket.event_service.exception.errorcode.EventErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import java.util.stream.Collectors;

@Service
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final SeatRepository seatRepository;

    public ShowtimeServiceImpl(ShowtimeRepository showtimeRepository, MovieRepository movieRepository, SeatRepository seatRepository) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
        this.seatRepository = seatRepository;
    }

    @Override
    @Transactional
    @CacheEvict(value = "showtimes", allEntries = true)
    public ShowtimeResponse createShowtime(ShowtimeRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new AppException(EventErrorCode.MOVIE_NOT_FOUND, "Movie not found with id: " + request.getMovieId()));

        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setShowDate(request.getShowDate());
        showtime.setStartTime(request.getStartTime());
        showtime.setEndTime(request.getEndTime());
        showtime.setRoomName(request.getRoomName());
        showtime.setPrice(request.getPrice());

        Showtime savedShowtime = showtimeRepository.save(showtime);

        // Tự động sinh ghế ngồi cho suất chiếu (Ví dụ: Hàng A, B, C từ 1 -> 10)
        List<Seat> seats = new ArrayList<>();
        String[] rows = {"A", "B", "C", "D", "E"};
        for (String row : rows) {
            for (int i = 1; i <= 10; i++) {
                Seat seat = new Seat();
                seat.setShowtime(savedShowtime);
                seat.setSeatNumber(row + i);
                seat.setStatus(SeatStatus.AVAILABLE);
                seats.add(seat);
            }
        }
        seatRepository.saveAll(seats);

        return mapToResponse(savedShowtime);
    }

    @Override
    @Cacheable(value = "showtimes", key = "#movieId.toString() + ':' + #showDate.toString()")
    public List<ShowtimeResponse> getShowtimesByMovieAndDate(UUID movieId, LocalDate showDate) {
        return showtimeRepository.findByMovieIdAndShowDate(movieId, showDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SeatResponse> getSeatsByShowtime(UUID showtimeId) {
        return seatRepository.findByShowtimeId(showtimeId).stream()
                .map(seat -> new SeatResponse(
                        seat.getId(),
                        seat.getShowtime().getId(),
                        seat.getSeatNumber(),
                        seat.getStatus().name()
                ))
                .collect(Collectors.toList());
    }

    private ShowtimeResponse mapToResponse(Showtime showtime) {
        return new ShowtimeResponse(
                showtime.getId(),
                showtime.getMovie().getId(),
                showtime.getMovie().getTitle(),
                showtime.getShowDate(),
                showtime.getStartTime(),
                showtime.getEndTime(),
                showtime.getRoomName(),
                showtime.getPrice()
        );
    }
}
