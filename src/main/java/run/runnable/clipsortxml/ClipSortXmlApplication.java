package run.runnable.clipsortxml;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ClipSortXmlApplication {

    public static void main(String[] args) throws Exception {
        String content = getFromClip();

        boolean xml = isXml(content);
        if (!xml){
            System.out.println("不是xml格式");
            throw new RuntimeException();
        }

        // 加载XML文件
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new ByteArrayInputStream(content.getBytes()));
        doc.getDocumentElement().normalize();

        // 对根节点进行排序
        Element root = doc.getDocumentElement();
        sortChildren(root);

        // 输出排序后的XML文本
        System.out.println(xmlToString(doc));
        //放入剪切板
        putIntoClip(xmlToString(doc));
    }

    public static boolean isXml(String input) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource source = new InputSource(new StringReader(input));
            Document doc = builder.parse(source);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String getFromClip() throws UnsupportedFlavorException, IOException {
        Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable clipTf = sysClip.getContents(null);
        return clipTf.getTransferData(DataFlavor.stringFlavor).toString();
    }

    private static void putIntoClip(String finalSoredContent) {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable tText = new StringSelection(finalSoredContent);
        clip.setContents(tText, null);
    }

    /**
     * 对一个节点的子节点按照标签名进行排序
     */
    public static void sortChildren(Node node) {
        NodeList children = node.getChildNodes();
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                elements.add((Element) child);
            }
        }
        Collections.sort(elements, Comparator.comparing(Element::getTagName));
        for (Element element : elements) {
            node.removeChild(element);
        }
        for (Element element : elements) {
            node.appendChild(element);
            sortChildren(element);
        }
    }

    /**
     * 将一个Document对象转化为格式化后的XML字符串
     */
    public static String xmlToString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        // 新建一个StreamResult，用于输出格式化后的XML字符串
        StringWriter stringWriter = new StringWriter();
        StreamResult result = new StreamResult(stringWriter);

        // 将XML字符串输出到StreamResult中
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);

        // 将输出的XML字符串格式化
        OutputFormat format = new OutputFormat(doc);
        format.setIndenting(true);
        format.setIndent(4);
        format.setLineWidth(100);
        format.setLineSeparator("\n");
        XMLSerializer serializer = new XMLSerializer();
        serializer.setOutputFormat(format);
        StringWriter writer = new StringWriter();
        serializer.setOutputCharStream(writer);
        serializer.serialize(doc);

        return writer.toString();
    }


}
