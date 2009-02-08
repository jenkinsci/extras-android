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

import hudson.android.monitor.MonitorException;
import hudson.android.monitor.Util;

import java.io.IOException;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.jcip.annotations.ThreadSafe;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Xavier Le Vourch
 *
 */

@ThreadSafe
public final class FeedParser extends DefaultHandler {

    private final StringBuilder buffer = new StringBuilder();

    private List<BuildData> builds;

    private String title;
    private String link;
    private String published;
    private String updated;

    private FeedParser() {
    }

    public static FeedData parseHistory(Feed feed) throws MonitorException {
        try {
            String feedUrl = feed.getUrl();
            String userName = feed.getUserName();
            String password = feed.getPassword();

            URLConnection urlConn = Util.getURLConnection(feedUrl, userName, password);

            SAXParserFactory f = SAXParserFactory.newInstance();
            SAXParser parser = f.newSAXParser();

            FeedParser handler = new FeedParser();
            parser.parse(urlConn.getInputStream(), handler);

            return new FeedData(handler.updated, handler.builds);
        } catch (IOException e) {
            throw new MonitorException("Feed Data Parsing", e);
        } catch (ParserConfigurationException e) {
            throw new MonitorException("Feed Data Parsing", e);
        } catch (SAXException e) {
            throw new MonitorException("Feed Data Parsing", e);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        builds = new LinkedList<BuildData>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        buffer.setLength(0);
        if ("link".equals(localName)) {
            link = attributes.getValue("href");
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("entry".equals(localName)) {
            BuildData build = new BuildData(title, link, published);
            builds.add(build);
        } else if ("title".equals(localName)) {
            title = buffer.toString();
        } else if ("published".equals(localName)) {
            published = buffer.toString();
        } else if ("updated".equals(localName)) {
            updated = buffer.toString();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        buffer.append(ch, start, length);
    }

}
