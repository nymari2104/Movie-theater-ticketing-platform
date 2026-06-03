package com.ticket.event_service.init;

import com.ticket.event_service.model.Movie;
import com.ticket.event_service.repository.MovieRepository;
import com.ticket.event_service.service.ShowtimeService;
import com.ticket.event_service.dto.ShowtimeRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final MovieRepository movieRepository;
    private final ShowtimeService showtimeService;

    public DataSeeder(MovieRepository movieRepository, ShowtimeService showtimeService) {
        this.movieRepository = movieRepository;
        this.showtimeService = showtimeService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (movieRepository.count() == 0) {
            // Seed Movies
            Movie movie1 = new Movie();
            movie1.setTitle("Lat Mat 7: Mot Dieu Uoc");
            movie1.setDescription("Bo phim tam ly tinh cam gia dinh day xuc dong cua dao dien Ly Hai.");
            movie1.setDurationMinutes(138);
            movie1.setReleaseDate(LocalDate.of(2026, 4, 26));
            movie1.setEndDate(LocalDate.of(2026, 7, 30));
            Movie savedMovie1 = movieRepository.save(movie1);

            Movie movie2 = new Movie();
            movie2.setTitle("Doraemon: Ban Giao Huong Dia Cau");
            movie2.setDescription("Hanh trinh phieu luu am nhac ky thu cua Doraemon va nhung nguoi ban.");
            movie2.setDurationMinutes(115);
            movie2.setReleaseDate(LocalDate.of(2026, 5, 24));
            movie2.setEndDate(LocalDate.of(2026, 8, 15));
            Movie savedMovie2 = movieRepository.save(movie2);

            // Seed Showtimes (Và tu dong sinh ghe nho logic trong ShowtimeServiceImpl)
            LocalDate today = LocalDate.now();
            
            // Suat chieu 1 cho Lat Mat 7 (Hom nay, 18:00 - 20:18, Rap 1, gia 90,000 VND)
            ShowtimeRequest showtime1 = new ShowtimeRequest();
            showtime1.setMovieId(savedMovie1.getId());
            showtime1.setShowDate(today);
            showtime1.setStartTime(LocalDateTime.of(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), 18, 0));
            showtime1.setEndTime(LocalDateTime.of(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), 20, 18));
            showtime1.setRoomName("Rap 1");
            showtime1.setPrice(new BigDecimal("90000.00"));
            showtimeService.createShowtime(showtime1);

            // Suat chieu 2 cho Lat Mat 7 (Hom nay, 21:00 - 23:18, Rap 1, gia 95,000 VND)
            ShowtimeRequest showtime2 = new ShowtimeRequest();
            showtime2.setMovieId(savedMovie1.getId());
            showtime2.setShowDate(today);
            showtime2.setStartTime(LocalDateTime.of(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), 21, 0));
            showtime2.setEndTime(LocalDateTime.of(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), 23, 18));
            showtime2.setRoomName("Rap 1");
            showtime2.setPrice(new BigDecimal("95000.00"));
            showtimeService.createShowtime(showtime2);

            // Suat chieu 3 cho Doraemon (Hom nay, 15:00 - 16:55, Rap 2, gia 80,000 VND)
            ShowtimeRequest showtime3 = new ShowtimeRequest();
            showtime3.setMovieId(savedMovie2.getId());
            showtime3.setShowDate(today);
            showtime3.setStartTime(LocalDateTime.of(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), 15, 0));
            showtime3.setEndTime(LocalDateTime.of(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), 16, 55));
            showtime3.setRoomName("Rap 2");
            showtime3.setPrice(new BigDecimal("80000.00"));
            showtimeService.createShowtime(showtime3);

            System.out.println(">>> DATA SEEDING COMPLETED SUCCESSFULLY! <<<");
        }
    }
}
