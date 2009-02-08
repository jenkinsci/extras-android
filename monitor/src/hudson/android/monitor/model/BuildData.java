/*
 * The MIT License
 * 
 * Copyright (c) 2009, Xavier Le Vourch
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
