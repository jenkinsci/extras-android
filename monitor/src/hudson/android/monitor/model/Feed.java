package hudson.android.monitor.model;

import net.jcip.annotations.Immutable;

/**
 *
 * @author Xavier Le Vourch
 *
 */

@Immutable
public class Feed {

    private final int id;

    private final String name;

    private final String url;

    public Feed(final int id, final String name, final String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public boolean isIgnored() {
        boolean isIgnored = true;
        if (this.url != null && this.url.length() > 0) {
            isIgnored = false;
        }
        return isIgnored;
    }

}
