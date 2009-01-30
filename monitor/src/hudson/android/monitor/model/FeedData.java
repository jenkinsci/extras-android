package hudson.android.monitor.model;

import hudson.android.monitor.Util;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.jcip.annotations.Immutable;

/**
 *
 * @author Xavier Le Vourch
 *
 */

@Immutable
public class FeedData implements Serializable {

    private static final long serialVersionUID = 6309664434872685024L;

    private final String date;
    private final List<BuildData> buildData;

    public FeedData(final String date, final List<BuildData> buildData) {
        this.date = date;
        Collections.sort(buildData, new BuildData.ReverseDateComparator());

        this.buildData = Collections.unmodifiableList(buildData);
    }

    public List<BuildData> getBuildData() {
        return buildData;
    }

    public Date getDate() {
        return Util.parseHudsonDate(date);
    }
}
