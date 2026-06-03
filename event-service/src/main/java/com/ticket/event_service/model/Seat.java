package com.ticket.event_service.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "event_seats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"showtime_id", "seat_number"})
})
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    public Seat() {
    }

    public Seat(UUID id, Showtime showtime, String seatNumber, SeatStatus status) {
        this.id = id;
        this.showtime = showtime;
        this.seatNumber = seatNumber;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Showtime getShowtime() {
        return showtime;
    }

    public void setShowtime(Showtime showtime) {
        this.showtime = showtime;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }
}
