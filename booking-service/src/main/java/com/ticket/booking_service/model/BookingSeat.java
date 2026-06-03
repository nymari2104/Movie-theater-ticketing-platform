package com.ticket.booking_service.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "booking_seats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"booking_id", "seat_id"})
})
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "seat_id", nullable = false)
    private UUID seatId;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    public BookingSeat() {
    }

    public BookingSeat(UUID id, Booking booking, UUID seatId, String seatNumber, LocalDate bookingDate) {
        this.id = id;
        this.booking = booking;
        this.seatId = seatId;
        this.seatNumber = seatNumber;
        this.bookingDate = bookingDate;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public UUID getSeatId() {
        return seatId;
    }

    public void setSeatId(UUID seatId) {
        this.seatId = seatId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }
}
