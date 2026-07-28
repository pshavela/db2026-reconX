package com.dbtraining.reconx.repository.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "recon_jobs")
public class ReconJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false, length = 36)
    private String jobId;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(nullable = false, length = 20)
    private String status = "QUEUED";

    @Column(name = "started_at")
    private Instant startAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "trades_processed")
    private Integer tradesProcessed = 0;

    @Column(name = "breaks_detected")
    private Integer breaksDetected = 0;

    public ReconJob() { }


    public Long getId()                 { return id; }
    public String getJobId()            { return jobId; }
    public LocalDate getFromDate()      { return fromDate; }
    public LocalDate getToDate()        { return toDate; }
    public String getStatus()           { return status; }
    public Instant getStartAt()         { return startAt; }
    public Instant getFinishedAt()      { return finishedAt; }
    public Integer getTradesProcessed() { return tradesProcessed; }
    public Integer getBreaksDetected()  { return breaksDetected; }

    public void setJobId(String jobId)                      { this.jobId = jobId; }
    public void setFromDate(LocalDate fromDate)             {this.fromDate = fromDate; }
    public void setToDate(LocalDate toDate)                 { this.toDate = toDate; }
    public void setStatus(String status)                    { this.status = status; }
    public void setStartAt(Instant startAt)                 { this.startAt = startAt; }
    public void setFinishedAt(Instant finishedAt)           { this.finishedAt = finishedAt; }
    public void setTradesProcessed(Integer tradesProcessed) { this.tradesProcessed = tradesProcessed; }
    public void setBreaksDetected(Integer breaksDetected)   { this.breaksDetected = breaksDetected; }

    public static class Builder {
        private String jobId;
        private LocalDate fromDate;
        private LocalDate toDate;
        private String status = "QUEUED";
        private Instant startAt;
        private Instant finishedAt;
        private Integer tradesProcessed = 0;
        private Integer breaksDetected = 0;

        public Builder jobId(String jobId)                               { this.jobId = jobId; return this; }
        public Builder fromDate(LocalDate fromDate)                      { this.fromDate = fromDate; return this; }
        public Builder toDate(LocalDate toDate)                          { this.toDate = toDate; return this; }
        public Builder status(String status)                             { this.status = status; return this; }
        public Builder startAt(Instant startAt)                          { this.startAt = startAt; return this; }
        public Builder finishedAt(Instant finishedAt)                    { this.finishedAt = finishedAt; return this; }
        public Builder tradesProcessed(Integer tradesProcessed)          { this.tradesProcessed = tradesProcessed; return this; }
        public Builder breaksDetected(Integer breaksDetected)            { this.breaksDetected = breaksDetected; return this; }

        public ReconJob build() {
            ReconJob job = new ReconJob();
            job.setJobId(jobId);
            job.setFromDate(fromDate);
            job.setToDate(toDate);
            job.setStatus(status);
            job.setStartAt(startAt);
            job.setFinishedAt(finishedAt);
            job.setTradesProcessed(tradesProcessed);
            job.setBreaksDetected(breaksDetected);
            return job;
        }
    }
}
