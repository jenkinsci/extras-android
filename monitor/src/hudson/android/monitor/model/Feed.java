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

    private final String userName;

    private final String password;

    public Feed(final int id, final String name, final String url, final String userName, final String password) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.userName = userName;
        this.password = password;
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

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public boolean isIgnored() {
        boolean isIgnored = true;
        if (this.url != null && this.url.length() > 0) {
            isIgnored = false;
        }
        return isIgnored;
    }

}
