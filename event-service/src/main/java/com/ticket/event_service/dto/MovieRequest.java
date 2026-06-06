package com.ticket.event_service.dto;

import java.time.LocalDate;

public class MovieRequest {
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private LocalDate endDate;

    private String posterBase64;

    public MovieRequest() {
    }

    public MovieRequest(String title, String description, Integer durationMinutes, LocalDate releaseDate, LocalDate endDate, String posterBase64) {
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.releaseDate = releaseDate;
        this.endDate = endDate;
        this.posterBase64 = posterBase64;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getPosterBase64() {
        return posterBase64;
    }

    public void setPosterBase64(String posterBase64) {
        this.posterBase64 = posterBase64;
    }
}
