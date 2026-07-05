package com.noom.interview.fullstack.sleep.web.dto;

/** Small helper to render a minute count as a human-friendly "Xh Ym" string for the UI. */
final class DurationFormat {

    private DurationFormat() {
    }

    static String humanize(Integer totalMinutes) {
        if (totalMinutes == null) {
            return null;
        }
        return "%dh %02dm".formatted(totalMinutes / 60, totalMinutes % 60);
    }
}
