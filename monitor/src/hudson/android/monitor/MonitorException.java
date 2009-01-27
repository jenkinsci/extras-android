package hudson.android.monitor;

/**
 *
 * @author Xavier Le Vourch
 * 
 */

public class MonitorException extends Exception {

    private static final long serialVersionUID = 1L;

    public MonitorException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
