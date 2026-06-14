package ru.slie.luna.plugins.gravity.metrics.dto;

public class MetricPointDto {
    private final String bucket;
    private final Long totalCount;
    private final Double avgExecutionTimeMs;
    private final Long maxExecutionTimeMs;
    private final Double avgCpuTimeMs;
    private final Long maxCpuTimeMs;

    public MetricPointDto(Object bucket, Long totalCount, Double avgExecutionTimeMs, Long maxExecutionTimeMs, Double avgCpuTimeMs, Long maxCpuTimeMs) {
        this.bucket = bucket != null ? bucket.toString() : null;
        this.totalCount = totalCount;
        this.avgExecutionTimeMs = avgExecutionTimeMs;
        this.maxExecutionTimeMs = maxExecutionTimeMs;
        this.avgCpuTimeMs = avgCpuTimeMs;
        this.maxCpuTimeMs = maxCpuTimeMs;
    }

    public MetricPointDto(Object bucket, Long totalCount, Long avgExecutionTimeMs, Long maxExecutionTimeMs, Long avgCpuTimeMs, Long maxCpuTimeMs) {
        this.bucket = bucket != null ? bucket.toString() : null;
        this.totalCount = totalCount;
        this.avgExecutionTimeMs = avgExecutionTimeMs.doubleValue();
        this.maxExecutionTimeMs = maxExecutionTimeMs;
        this.avgCpuTimeMs = avgCpuTimeMs.doubleValue();
        this.maxCpuTimeMs = maxCpuTimeMs;
    }

    public String getBucket() {
        return bucket;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public Double getAvgExecutionTimeMs() {
        return avgExecutionTimeMs;
    }

    public Long getMaxExecutionTimeMs() {
        return maxExecutionTimeMs;
    }

    public Double getAvgCpuTimeMs() {
        return avgCpuTimeMs;
    }

    public Long getMaxCpuTimeMs() {
        return maxCpuTimeMs;
    }
}
