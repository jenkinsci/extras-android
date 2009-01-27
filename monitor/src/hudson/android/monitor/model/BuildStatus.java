package hudson.android.monitor.model;

import net.jcip.annotations.Immutable;

/**
 *
 * @author Xavier Le Vourch
 *
 */

@Immutable
public enum BuildStatus {

    FAILURE("failure"),
    SUCCESS("success"),
    WARNING("warning"),
    UNKNOWN("unknown");

    private final String name;

    private BuildStatus(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
