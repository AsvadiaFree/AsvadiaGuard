package fr.asvadia.guard.util;

public enum Notification {
        DISABLE_PLUGIN(Level.ALERT),
        ILLEGAL_CREATIVE(Level.ALERT),
        PERMISSION(Level.ALERT),
        CREATIVE_NOT_ACCESS(Level.WARN),
        WRONG_TOKEN(Level.WARN),
        INCORRECT_CONNECTION(Level.WARN),
        COMMAND_NOT_ACCESS(Level.WARN),
        HIDE_COMMAND(Level.WARN);

        Level level;

        Notification(Level level) {
            this.level = level;
        }

    public Level getLevel() {
        return level;
    }

    public enum Level {
        ALERT("alert"),
        WARN("warn"),
        SUCCESS("success");

        String key;

        Level(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
}
