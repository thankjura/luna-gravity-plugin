package ru.slie.luna.plugins.gravity.metrics.dto;

import java.util.List;

public class MetricPointCollection {
    private final List<MetricPointDto> points;
    private final String bucket;

    public MetricPointCollection(List<MetricPointDto> points, String bucket) {
        this.points = points;
        this.bucket = bucket;
    }

    public List<MetricPointDto> getPoints() {
        return points;
    }

    public String getBucket() {
        return bucket;
    }
}
