package com.ticket.event_service.init;

import com.ticket.event_service.model.Movie;
import com.ticket.event_service.repository.MovieRepository;
import com.ticket.event_service.repository.SeatRepository;
import com.ticket.event_service.repository.ShowtimeRepository;
import com.ticket.event_service.service.ShowtimeService;
import com.ticket.event_service.dto.ShowtimeRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Properties;

@Component
public class DataSeeder implements CommandLineRunner {

    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeService showtimeService;
    private final CacheManager cacheManager;

    public DataSeeder(MovieRepository movieRepository,
                      ShowtimeRepository showtimeRepository,
                      SeatRepository seatRepository,
                      ShowtimeService showtimeService,
                      CacheManager cacheManager) {
        this.movieRepository = movieRepository;
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.showtimeService = showtimeService;
        this.cacheManager = cacheManager;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> STARTING DATA SEEDING CLEANUP <<<");
        
        // Xóa toàn bộ Redis Cache để tránh dữ liệu bị cache cũ
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(name -> {
                var cache = cacheManager.getCache(name);
                if (cache != null) cache.clear();
            });
            System.out.println(">>> REDIS CACHES CLEARED SUCCESSFULLY <<<");
        }

        seatRepository.deleteAll();
        showtimeRepository.deleteAll();
        movieRepository.deleteAll();
        System.out.println(">>> CLEANUP SUCCESSFUL. STARTING SEEDING MOCK DATA <<<");

        // Load poster images từ file properties (Base64 đã được pre-encoded)
        Properties posterProps = loadPosterProperties();

        // 1. Seed Movies
        Movie movie1 = new Movie();
        movie1.setTitle("Lật Mặt 7: Một Điều Ước");
        movie1.setDescription("Bộ phim tâm lý tình cảm gia đình đầy cảm động và ý nghĩa của đạo diễn Lý Hải.");
        movie1.setDurationMinutes(138);
        movie1.setReleaseDate(LocalDate.now().minusDays(10));
        movie1.setEndDate(LocalDate.now().plusDays(30));
        movie1.setPosterData(decodeBase64Poster(posterProps, "lat_mat_7"));
        Movie m1 = movieRepository.save(movie1);

        Movie movie2 = new Movie();
        movie2.setTitle("Doraemon: Bản Giao Hưởng Địa Cầu");
        movie2.setDescription("Hành trình phiêu lưu âm nhạc kỳ thú của Doraemon, Nobita và những người bạn để giải cứu Trái Đất.");
        movie2.setDurationMinutes(115);
        movie2.setReleaseDate(LocalDate.now().minusDays(5));
        movie2.setEndDate(LocalDate.now().plusDays(20));
        movie2.setPosterData(decodeBase64Poster(posterProps, "doraemon"));
        Movie m2 = movieRepository.save(movie2);

        Movie movie3 = new Movie();
        movie3.setTitle("Deadpool & Wolverine");
        movie3.setDescription("Deadpool bắt tay cùng Wolverine trong một nhiệm vụ giải cứu đa vũ trụ đầy hài hước, kịch tính và hành động mãn nhãn.");
        movie3.setDurationMinutes(127);
        movie3.setReleaseDate(LocalDate.now().minusDays(2));
        movie3.setEndDate(LocalDate.now().plusDays(45));
        movie3.setPosterData(decodeBase64Poster(posterProps, "action"));
        Movie m3 = movieRepository.save(movie3);

        Movie movie4 = new Movie();
        movie4.setTitle("Inside Out 2");
        movie4.setDescription("Riley bước vào tuổi dậy thì với sự xuất hiện của những cảm xúc mới tinh như Lo Âu, Ghen Tị, Sĩ Diện.");
        movie4.setDurationMinutes(96);
        movie4.setReleaseDate(LocalDate.now().minusDays(1));
        movie4.setEndDate(LocalDate.now().plusDays(25));
        movie4.setPosterData(decodeBase64Poster(posterProps, "animated_joy"));
        Movie m4 = movieRepository.save(movie4);

        Movie movie5 = new Movie();
        movie5.setTitle("Dune: Hành Tinh Cát - Phần 2");
        movie5.setDescription("Paul Atreides đồng hành cùng Chani và người Fremen để trả thù những kẻ đã hủy hoại gia đình mình.");
        movie5.setDurationMinutes(166);
        movie5.setReleaseDate(LocalDate.now().minusDays(20));
        movie5.setEndDate(LocalDate.now().plusDays(15));
        movie5.setPosterData(decodeBase64Poster(posterProps, "sci_fi"));
        Movie m5 = movieRepository.save(movie5);

        // 2. Seed Showtimes cho 3 ngày (Hôm nay, Ngày mai, Ngày kia)
        LocalDate[] dates = {
            LocalDate.now(),
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2)
        };

        for (LocalDate date : dates) {
            // --- Lật Mặt 7 ---
            createShowtime(m1.getId(), date, 10, 0, 12, 18, "Rạp 1 (Standard)", "80000.00");
            createShowtime(m1.getId(), date, 14, 0, 16, 18, "Rạp 1 (Standard)", "90000.00");
            createShowtime(m1.getId(), date, 18, 0, 20, 18, "Rạp 1 (Standard)", "100000.00");
            createShowtime(m1.getId(), date, 21, 0, 23, 18, "Rạp 3 (IMAX 3D)", "150000.00");

            // --- Doraemon ---
            createShowtime(m2.getId(), date, 9, 0, 10, 55, "Rạp 2 (Standard)", "80000.00");
            createShowtime(m2.getId(), date, 13, 0, 14, 55, "Rạp 2 (Standard)", "80000.00");
            createShowtime(m2.getId(), date, 16, 0, 17, 55, "Rạp 2 (Standard)", "90000.00");

            // --- Deadpool & Wolverine ---
            createShowtime(m3.getId(), date, 15, 0, 17, 7, "Rạp 3 (IMAX 3D)", "140000.00");
            createShowtime(m3.getId(), date, 19, 0, 21, 7, "Rạp 3 (IMAX 3D)", "160000.00");
            createShowtime(m3.getId(), date, 21, 30, 23, 37, "Rạp 4 (VIP Gold Class)", "200000.00");

            // --- Inside Out 2 ---
            createShowtime(m4.getId(), date, 11, 0, 12, 36, "Rạp 2 (Standard)", "80000.00");
            createShowtime(m4.getId(), date, 17, 30, 19, 6, "Rạp 4 (VIP Gold Class)", "180000.00");

            // --- Dune: Part Two ---
            createShowtime(m5.getId(), date, 13, 30, 16, 16, "Rạp 4 (VIP Gold Class)", "180000.00");
            createShowtime(m5.getId(), date, 20, 0, 22, 46, "Rạp 2 (Standard)", "100000.00");
        }

        System.out.println(">>> MOCK DATA SEEDING COMPLETED SUCCESSFULLY! <<<");
    }

    /**
     * Tải file posters.properties từ classpath chứa dữ liệu ảnh dạng Base64.
     */
    private Properties loadPosterProperties() {
        Properties props = new Properties();
        try {
            ClassPathResource resource = new ClassPathResource("posters.properties");
            props.load(resource.getInputStream());
            System.out.println(">>> Poster properties loaded successfully. <<<");
        } catch (IOException e) {
            System.err.println("Warning: Could not load posters.properties: " + e.getMessage());
        }
        return props;
    }

    /**
     * Giải mã chuỗi Base64 từ properties thành mảng byte[] để lưu vào DB.
     */
    private byte[] decodeBase64Poster(Properties props, String key) {
        String b64 = props.getProperty(key);
        if (b64 == null || b64.isBlank()) {
            System.err.println("Warning: No poster data found for key: " + key);
            return null;
        }
        try {
            return Base64.getDecoder().decode(b64.trim());
        } catch (IllegalArgumentException e) {
            System.err.println("Warning: Failed to decode poster for key: " + key + " - " + e.getMessage());
            return null;
        }
    }

    private void createShowtime(java.util.UUID movieId, LocalDate date, int startH, int startM, int endH, int endM, String room, String price) {
        ShowtimeRequest request = new ShowtimeRequest();
        request.setMovieId(movieId);
        request.setShowDate(date);
        request.setStartTime(LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), startH, startM));
        request.setEndTime(LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), endH, endM));
        request.setRoomName(room);
        request.setPrice(new BigDecimal(price));
        showtimeService.createShowtime(request);
    }
}
