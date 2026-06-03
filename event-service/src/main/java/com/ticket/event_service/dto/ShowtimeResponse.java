package com.ticket.event_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ShowtimeResponse {
    private UUID id;
    private UUID movieId;
    private String movieTitle;
    private LocalDate showDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String roomName;
    private BigDecimal price;

    public ShowtimeResponse() {
    }

    public ShowtimeResponse(UUID id, UUID movieId, String movieTitle, LocalDate showDate, LocalDateTime startTime, LocalDateTime endTime, String roomName, BigDecimal price) {
        this.id = id;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.showDate = showDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.roomName = roomName;
        this.price = price;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMovieId() {
        return movieId;
    }

    public void setMovieId(UUID movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public LocalDate getShowDate() {
        return showDate;
    }

    public void setShowDate(LocalDate showDate) {
        this.showDate = showDate;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
