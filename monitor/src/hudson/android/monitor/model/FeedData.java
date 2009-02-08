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
