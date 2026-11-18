package com.univibe.model;

/**
 * Standard College Event Categories.
 * Demonstrates: Enums, Static Helper Methods, Arrays.
 */
public enum EventCategory {
    TECHNICAL_EVENTS("Technical Events"),
    CULTURAL_EVENTS("Cultural Events"),
    SPORTS_EVENTS("Sports Events"),
    WORKSHOPS("Workshops"),
    SEMINARS("Seminars"),
    HACKATHONS("Hackathons"),
    MUSIC_DANCE("Music and Dance Competitions"),
    COLLEGE_FESTIVALS("College Festivals");

    private final String displayName;

    EventCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static String[] getAllDisplayNames() {
        EventCategory[] categories = values();
        String[] names = new String[categories.length];
        for (int i = 0; i < categories.length; i++) {
            names[i] = categories[i].getDisplayName();
        }
        return names;
    }

    public static EventCategory fromDisplayName(String name) {
        if (name == null) return TECHNICAL_EVENTS;
        for (EventCategory cat : values()) {
            if (cat.displayName.equalsIgnoreCase(name.trim())) {
                return cat;
            }
        }
        return TECHNICAL_EVENTS;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
