package com.ticket.event_service.repository;

import com.ticket.event_service.model.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, UUID> {
    List<Showtime> findByMovieIdAndShowDate(UUID movieId, LocalDate showDate);
}
