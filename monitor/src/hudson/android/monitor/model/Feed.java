package hudson.android.monitor.model;

import hudson.android.monitor.MonitorException;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.jcip.annotations.Immutable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

    public FeedData parseHistory() throws MonitorException {
        try {
            URL url;
            URLConnection urlConn = null;

            url = new URL(this.getUrl());
            urlConn = url.openConnection();

            Document xml = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            xml = db.parse(urlConn.getInputStream());

            String feedDate = xml.getElementsByTagName("updated").item(0).getChildNodes().item(0).getNodeValue();
            NodeList entries = xml.getElementsByTagName("entry");
            BuildData[] builds;
            if (entries.getLength() > 0) {
                int size = entries.getLength();
                builds = new BuildData[size];
                for (int i = 0; i < size; i++) {
                    Element e = (Element) entries.item(i);
                    String text = e.getElementsByTagName("title").item(0).getChildNodes().item(0).getNodeValue();
                    String link = e.getElementsByTagName("link").item(0).getAttributes().getNamedItem("href").getNodeValue();
                    String date = e.getElementsByTagName("published").item(0).getChildNodes().item(0).getNodeValue();
                    builds[i] = new BuildData(text, link, date);
                }
            } else {
                builds = new BuildData[0];
            }
            return new FeedData(feedDate, builds);
        } catch (IOException e) {
            throw new MonitorException("Feed Data Parsing", e);
        } catch (ParserConfigurationException e) {
            throw new MonitorException("Feed Data Parsing", e);
        } catch (SAXException e) {
            throw new MonitorException("Feed Data Parsing", e);
        }
    }

}
