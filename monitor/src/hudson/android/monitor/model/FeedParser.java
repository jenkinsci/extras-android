package hudson.android.monitor.model;

import hudson.android.monitor.MonitorException;

import java.io.IOException;
import java.net.URL;
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

    public static FeedData parseHistory(String feedUrl) throws MonitorException {
        try {
            URL url;
            URLConnection urlConn = null;

            url = new URL(feedUrl);
            urlConn = url.openConnection();

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
