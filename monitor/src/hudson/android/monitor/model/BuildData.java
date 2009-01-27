package hudson.android.monitor.model;

import hudson.android.monitor.Util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

import net.jcip.annotations.Immutable;

/**
 *
 * @author Xavier Le Vourch
 *
 */

@Immutable
public class BuildData implements Serializable {

    private static final long serialVersionUID = -308868090438898904L;

    private final String text;
    private final String link;
    private final String dateString;

    public BuildData(final String text, final String link, final String date) {
        this.text = text;
        this.link = link;
        this.dateString = date;
    }

    public String getText() {
        return text;
    }

    public String getText(final boolean removeStatus) {
        String t = text;
        if (removeStatus) {
            t = t.replaceAll(" \\(.*\\)$", "");
        }
        return t;
    }

    public String getLink() {
        return link;
    }

    public String getDateString() {
        return dateString;
    }

    public Date getDate() {
        return Util.parseHudsonDate(dateString);
    }

    public String getDateString(final boolean useLocalTime) {
        final String s = dateString;
        if (useLocalTime) {
            final Date d = getDate();
            return d.toLocaleString();
        }
        return s;
    }

    public String getDetails() {
        return this.text + " - " + this.dateString;
    }

    public BuildStatus getStatus() {
        BuildStatus status;
        if (this.text.endsWith("(SUCCESS)")) {
            status = BuildStatus.SUCCESS;
        } else if (this.text.endsWith("(FAILURE)")) {
            status = BuildStatus.FAILURE;
        } else if (this.text.endsWith("(UNSTABLE)")) {
            status = BuildStatus.WARNING;
        } else {
            // this is for:
            // ABORTED
            // NOT_BUILT
            // null -> when building
            status = BuildStatus.UNKNOWN;
        }
        return status;

    }

    public boolean isFailure() {
        return getStatus() == BuildStatus.FAILURE;
    }

    public boolean isWarning() {
        return getStatus() == BuildStatus.WARNING;
    }

    public boolean isSuccess() {
        return getStatus() == BuildStatus.SUCCESS;
    }

    public boolean isUnknown() {
        return getStatus() == BuildStatus.UNKNOWN;
    }

    public static class ReverseDateComparator implements Comparator<BuildData>, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public int compare(final BuildData object1, final BuildData object2) {
            return object2.getDateString().compareTo(object1.getDateString());
        }

    }
}
