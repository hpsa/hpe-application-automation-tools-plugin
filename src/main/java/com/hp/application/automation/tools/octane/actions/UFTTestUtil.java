package com.hp.application.automation.tools.octane.actions;

import hudson.FilePath;
import org.apache.poi.poifs.filesystem.*;
import org.apache.poi.util.StringUtil;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

/**
 * Created by berkovir on 19/04/2017.
 */
public class UFTTestUtil {

    public static String getTestDescription(FilePath dirPath) {
        String desc = null;
        try {
            FilePath tspTestFile = new FilePath(dirPath, "Test.tsp");
            InputStream is = new FileInputStream(tspTestFile.getRemote());
            String xmlContent = decodeXmlContent(is);

            SAXBuilder saxBuilder = new SAXBuilder(XMLReaders.NONVALIDATING, null, null);
            Document document = saxBuilder.build(new StringReader(xmlContent));
            Element rootElement = document.getRootElement();
            Element descElement = rootElement.getChild("Description");
            desc = descElement.getValue();
        } catch (Exception e) {
            return null;
        }

        return desc;
    }

    public static String decodeXmlContent(InputStream stream) throws IOException {
        POIFSFileSystem poiFS = new POIFSFileSystem(stream);
        DirectoryNode root = poiFS.getRoot();
        String xmlData = "";

        for (Entry entry : root) {
            String name = entry.getName();
            if ("ComponentInfo".equals(name)) {
                if (entry instanceof DirectoryEntry) {
                    System.out.println(entry);
                } else if (entry instanceof DocumentEntry) {
                    byte[] content = new byte[((DocumentEntry) entry).getSize()];
                    poiFS.createDocumentInputStream("ComponentInfo").read(content);
                    String fromUnicodeLE = StringUtil.getFromUnicodeLE(content);
                    xmlData = fromUnicodeLE.substring(fromUnicodeLE.indexOf('<')).replaceAll("\u0000", "");
                }
            }
        }
        return xmlData;
    }
}
