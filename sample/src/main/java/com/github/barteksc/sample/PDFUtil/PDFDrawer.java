package com.github.barteksc.sample.PDFUtil;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;

import com.github.barteksc.sample.OCGHelper.OCGRemover;
import com.github.barteksc.sample.R;
import com.lowagie.text.Annotation;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PRTokeniser;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfAnnotation;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfBorderArray;
import com.lowagie.text.pdf.PdfBorderDictionary;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfContentParser;
import com.lowagie.text.pdf.PdfDestination;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfFormField;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfImage;
import com.lowagie.text.pdf.PdfIndirectObject;
import com.lowagie.text.pdf.PdfLayer;
import com.lowagie.text.pdf.PdfLayerMembership;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.TextField;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class PDFDrawer {

    private static final String TAG = "PDFDrawer";

    public static void addSampleLines(String filePath) throws FileNotFoundException {

        // get file and FileOutputStream
        if (filePath == null || filePath.isEmpty())
            throw new FileNotFoundException();

        File file = new File(filePath);

        if (!file.exists())
            throw new FileNotFoundException();

        FileOutputStream fileOutputStream = new FileOutputStream(file, true);

        System.out.println("Text at absolute positions");

        // step 1: creation of a document-object
        Document document = new Document();

        try {

            // step 2: creation of the writer
            PdfWriter writer = PdfWriter.getInstance(document, fileOutputStream);

            // step 3: we open the document
            document.open();

            // step 4: we grab the ContentByte and do some stuff with it
            PdfContentByte cb = writer.getDirectContent();

            // first we draw some lines to be able to visualize the text alignment functions
            cb.setLineWidth(0f);
            cb.moveTo(250, 500);
            cb.lineTo(250, 800);
            cb.moveTo(50, 700);
            cb.lineTo(400, 700);
            cb.moveTo(50, 650);
            cb.lineTo(400, 650);
            cb.moveTo(50, 600);
            cb.lineTo(400, 600);
            cb.stroke();

            // we tell the ContentByte we're ready to draw text
            cb.beginText();

            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            cb.setFontAndSize(bf, 12);
            String text = "Sample text for alignment";
            // we show some text starting on some absolute position with a given alignment
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, text + " Center", 250, 700, 0);
            cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, text + " Right", 250, 650, 0);
            cb.showTextAligned(PdfContentByte.ALIGN_LEFT, text + " Left", 250, 600, 0);

            // we draw some text on a certain position
            cb.setTextMatrix(100, 400);
            cb.showText("Text at position 100,400.");

            // we draw some text on a certain position
            cb.setFontAndSize(bf, 35);
            cb.setTextMatrix(50, 100);
            cb.showText("Random Num: " + PublicFunction.getRandomNumber());

            // we draw some rotated text on a certain position
            cb.setFontAndSize(bf, 12);
            cb.setTextMatrix(0, 1, -1, 0, 100, 300);
            cb.showText("Text at position 100,300, rotated 90 degrees.");

            // we draw some mirrored, rotated text on a certain position
            cb.setTextMatrix(0, 1, 1, 0, 200, 200);
            cb.showText("Text at position 200,200, mirrored and rotated 90 degrees.");

            // we tell the contentByte, we've finished drawing text
            cb.endText();

            cb.sanityCheck();
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }

    public static void addRandomNumber(String filePath) throws FileNotFoundException {

        // get file and FileOutputStream
        if (filePath == null || filePath.isEmpty())
            throw new FileNotFoundException();

        File file = new File(filePath);

        if (!file.exists())
            throw new FileNotFoundException();

        try {

            // inout stream from file
            InputStream inputStream = new FileInputStream(file);

            // we create a reader for a certain document
            PdfReader reader = new PdfReader(inputStream);

            // get page file number count
            int pageNumbers = reader.getNumberOfPages();

            // we create a stamper that will copy the document to a new file
            PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(file));

            // adding content to each page
            int i = 0;
            PdfContentByte over;

            // create base font for text
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);

            while (i < pageNumbers) {
                i++;
                // text over the existing page
                over = stamp.getOverContent(i);
                over.beginText();
                over.setFontAndSize(bf, 20);
                over.setTextMatrix(130, 80);
                over.showText("Random Number: " + PublicFunction.getRandomNumber());
                over.endText();
            }

            // closing PdfStamper will generate the new PDF file
            stamp.close();

        } catch (Exception de) {
            de.printStackTrace();
        }
    }

    public static void addWatermark(Context context, String filePath) throws FileNotFoundException, IOException {

        // get file and FileOutputStream
        if (filePath == null || filePath.isEmpty())
            throw new FileNotFoundException();

        File file = new File(filePath);

        if (!file.exists())
            throw new FileNotFoundException();

        try {

            // inout stream from file
            InputStream inputStream = new FileInputStream(file);

            // we create a reader for a certain document
            PdfReader reader = new PdfReader(inputStream);

            // get page file number count
            int pageNumbers = reader.getNumberOfPages();

            // we create a stamper that will copy the document to a new file
            PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(file));

            // adding content to each page
            int i = 0;
            PdfContentByte under;

            // get watermark icon
            Image img = Image.getInstance(PublicFunction.getByteFromDrawable(context, R.drawable.ic_chat));
            img.setAbsolutePosition(230, 190);
            img.scaleAbsolute(50, 50);

            while (i < pageNumbers) {
                i++;
                // watermark under the existing page
                under = stamp.getUnderContent(i);
                under.addImage(img);
            }

            // closing PdfStamper will generate the new PDF file
            stamp.close();

        } catch (Exception de) {
            de.printStackTrace();
        }

    }

    public static void addText(String filePath) throws FileNotFoundException, IOException {

        // get file and FileOutputStream
        if (filePath == null || filePath.isEmpty())
            throw new FileNotFoundException();

        File file = new File(filePath);

        if (!file.exists())
            throw new FileNotFoundException();

        try {

            // inout stream from file
            InputStream inputStream = new FileInputStream(file);

            // we create a reader for a certain document
            PdfReader reader = new PdfReader(inputStream);

            // get page file number count
            int pageNumbers = reader.getNumberOfPages();

            // we create a stamper that will copy the document to a new file
            PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(file));

            // adding content to each page
            int i = 0;
            PdfContentByte over;

            // create base font for text
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);

            while (i < pageNumbers) {
                i++;
                // text over the existing page
                over = stamp.getOverContent(i);
                over.beginText();
                over.setFontAndSize(bf, 18);
                over.setTextMatrix(30, 30);
                over.showText("page " + i);
                over.setFontAndSize(bf, 32);
                over.showTextAligned(Element.ALIGN_LEFT, "DUPLICATE", 230, 250, 45);
                over.endText();
            }

            // closing PdfStamper will generate the new PDF file
            stamp.close();

        } catch (Exception de) {
            de.printStackTrace();
        }
    }

    public static void addIconWithLocation(Context context, String filePath, float userX, float userY) throws FileNotFoundException, IOException {

        // get file and FileOutputStream
        if (filePath == null || filePath.isEmpty())
            throw new FileNotFoundException();

        File file = new File(filePath);

        if (!file.exists())
            throw new FileNotFoundException();

        try {

            // inout stream from file
            InputStream inputStream = new FileInputStream(file);

            // we create a reader for a certain document
            PdfReader reader = new PdfReader(inputStream);

            // get page file number count
            int pageNumbers = reader.getNumberOfPages();

            // we create a stamper that will copy the document to a new file
            PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(file));

            // adding content to each page
            int i = 0;
            PdfContentByte under;

            // get watermark icon
            Image img = Image.getInstance(PublicFunction.getByteFromDrawable(context, R.drawable.ic_chat));
            img.setAbsolutePosition(userX, userY);
            img.scaleAbsolute(50, 50);

            while (i < pageNumbers) {
                i++;
                // watermark under the existing page
                under = stamp.getUnderContent(i);
                under.addImage(img);
            }

            // closing PdfStamper will generate the new PDF file
            stamp.close();

        } catch (Exception de) {
            de.printStackTrace();
        }
    }

    public static void addAnnotatedImage(Context context, String filePath) throws FileNotFoundException, IOException {

        // get file and FileOutputStream
        if (filePath == null || filePath.isEmpty())
            throw new FileNotFoundException();

        File file = new File(filePath);

        if (!file.exists())
            throw new FileNotFoundException();

        try {

            // inout stream from file
            InputStream inputStream = new FileInputStream(file);

            // we create a reader for a certain document
            PdfReader reader = new PdfReader(inputStream);

            // get page file number count
            int pageNumbers = reader.getNumberOfPages();

            // we create a stamper that will copy the document to a new file
            PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(file));

            // adding content to each page
            int i = 0;
            PdfContentByte under;

            // get watermark icon
            Image img = Image.getInstance(PublicFunction.getByteFromDrawable(context, R.drawable.ic_chat));
            // img.setAnnotation(new Annotation("picture", "This is my dog", 0, 0, 0, 0));
            img.setAnnotation(new Annotation(0, 0, 0, 0, "http://www.lowagie.com/iText"));
            img.setAbsolutePosition(100f, 250f);
            img.scaleAbsolute(50, 50);

            while (i < pageNumbers) {
                i++;
                // watermark under the existing page
                under = stamp.getUnderContent(i);
                under.addImage(img);
            }

            // closing PdfStamper will generate the new PDF file
            stamp.close();

        } catch (Exception de) {
            de.printStackTrace();
        }
    }

    public static void addOptionalLayers(Context context, String filePath) throws FileNotFoundException, IOException {

        // get file and FileOutputStream
        if (filePath == null || filePath.isEmpty())
            throw new FileNotFoundException();

        File file = new File(filePath);

        if (!file.exists())
            throw new FileNotFoundException();

        FileOutputStream fileOutputStream = new FileOutputStream(file, true);

        System.out.println("Text at absolute positions");

        // step 1: creation of a document-object
        Document document = new Document();

        try {

            // step 2: creation of the writer
            PdfWriter writer = PdfWriter.getInstance(document, fileOutputStream);

            writer.setPdfVersion(PdfWriter.VERSION_1_5);
            writer.setViewerPreferences(PdfWriter.PageModeUseOC);

            // step 3: we open the document
            document.open();

            // step 4: content
            PdfContentByte cb = writer.getDirectContent();
            Phrase explanation = new Phrase("Automatic layers, form fields, images, templates and actions", new Font(Font.HELVETICA, 18, Font.BOLD, Color.red));
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, explanation, 50, 650, 0);
            PdfLayer l1 = new PdfLayer("Layer 1", writer);
            PdfLayer l2 = new PdfLayer("Layer 2", writer);
            PdfLayer l3 = new PdfLayer("Layer 3", writer);
            PdfLayer l4 = new PdfLayer("Form and XObject Layer", writer);
            PdfLayerMembership m1 = new PdfLayerMembership(writer);
            m1.addMember(l2);
            m1.addMember(l3);
            Phrase p1 = new Phrase("Text in layer 1");
            Phrase p2 = new Phrase("Text in layer 2 or layer 3");
            Phrase p3 = new Phrase("Text in layer 3");
            cb.beginLayer(l1);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, p1, 50, 600, 0f);
            cb.endLayer();
            cb.beginLayer(m1);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, p2, 50, 550, 0);
            cb.endLayer();
            cb.beginLayer(l3);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, p3, 50, 500, 0);
            cb.endLayer();
            TextField ff = new TextField(writer, new Rectangle(200, 600, 300, 620), "field1");
            ff.setBorderColor(Color.blue);
            ff.setBorderStyle(PdfBorderDictionary.STYLE_SOLID);
            ff.setBorderWidth(TextField.BORDER_WIDTH_THIN);
            ff.setText("I'm a form field");
            PdfFormField form = ff.getTextField();
            form.setLayer(l4);
            writer.addAnnotation(form);

            // get watermark icon
            // Image img = Image.getInstance("pngnow.png");
            Image img = Image.getInstance(PublicFunction.getByteFromDrawable(context, R.drawable.ic_chat));
            img.setLayer(l4);
            img.setAbsolutePosition(200, 550);
            cb.addImage(img);
            PdfTemplate tp = cb.createTemplate(100, 20);
            Phrase pt = new Phrase("I'm a template", new Font(Font.HELVETICA, 12, Font.NORMAL, Color.magenta));
            ColumnText.showTextAligned(tp, Element.ALIGN_LEFT, pt, 0, 0, 0);
            tp.setLayer(l4);
            tp.setBoundingBox(new Rectangle(0, -10, 100, 20));
            cb.addTemplate(tp, 200, 500);
            List<Object> state = new ArrayList<>();
            state.add("toggle");
            state.add(l1);
            state.add(l2);
            state.add(l3);
            state.add(l4);
            PdfAction action = PdfAction.setOCGstate(state, true);
            Chunk ck = new Chunk("Click here to toggle the layers", new Font(Font.HELVETICA, 18, Font.NORMAL, Color.yellow)).setBackground(Color.blue).setAction(action);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(ck), 250, 400, 0);
            cb.sanityCheck();

            // step 5: closing the document
            document.close();
        } catch (Exception de) {
            de.printStackTrace();
        }
    }

    public static void addAnnotatedImageWithLocation(Context context, String filePath, PointF pointF, int pageIndex) throws PDFException, IOException {

        // hint : pageIndex starts from --> 1
        pageIndex++;

        // get file and FileOutputStream
        if (filePath == null || filePath.isEmpty())
            throw new FileNotFoundException();

        File file = new File(filePath);

        if (!file.exists())
            throw new FileNotFoundException();

        try {

            // inout stream from file
            InputStream inputStream = new FileInputStream(file);

            // we create a reader for a certain document
            PdfReader reader = new PdfReader(inputStream);

            // we create a stamper that will copy the document to a new file
            PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(file));

            // adding content to each page
            PdfContentByte over;

            // generate random id
            String randomHashRef = "watermark:" + PublicFunction.getRandomNumber();

            // get watermark icon
            Image img = Image.getInstance(PublicFunction.getByteFromDrawable(context, R.drawable.ic_chat_lawone_new));
            img.setAnnotation(new Annotation(0, 0, 0, 0, randomHashRef));
            img.scaleAbsolute(50, 50);
            img.setAbsolutePosition(pointF.x, pointF.y);
            PdfImage stream = new PdfImage(img, randomHashRef, null);
            stream.put(new PdfName(PublicValues.KEY_SPECIAL_ID), new PdfName(randomHashRef));
            PdfIndirectObject ref = stamp.getWriter().addToBody(stream);
            img.setDirectReference(ref.getIndirectReference());

            // add as layer
            PdfLayer wmLayer = new PdfLayer(randomHashRef, stamp.getWriter());

            // set layer parameters
            // wmLayer.setOnPanel(true);
            // wmLayer.setPrint("print", true);
            // wmLayer.setOn(true);
            // wmLayer.setView(true);

            // Prepare transparency
            PdfGState transparent = new PdfGState();
            transparent.setAlphaIsShape(false);
//            transparent.setFillOpacity(0.9f);
//            transparent.setStrokeOpacity(0.3f);
//            transparent.setBlendMode(PdfGState.BM_DARKEN);

            // get page file number count
            if (reader.getNumberOfPages() < pageIndex) {
                // closing PdfStamper will generate the new PDF file
                stamp.close();
                throw new PDFException("page index is out of pdf file page numbers", new Throwable());
            }

            // annotation added into target page
            over = stamp.getOverContent(pageIndex);
            if (over == null) {
                stamp.close();
                throw new PDFException("getUnderContent is null", new Throwable());
            }

            // add as layer
            over.beginLayer(wmLayer);
            over.setGState(transparent); // set block transparency properties
            over.addImage(img);
            over.endLayer();

            // closing PdfStamper will generate the new PDF file
            stamp.close();

            // close reader
            reader.close();

        } catch (Exception de) {
            de.printStackTrace();
        }
    }

    public static void addAnnotatedBoxImageWithLocation(Context context, String filePath, PointF pointF, int pageIndex) throws PDFException, IOException {

        // hint : pageIndex starts from --> 1
        pageIndex++;

        // get file and FileOutputStream
        if (filePath == null || filePath.isEmpty())
            throw new FileNotFoundException();

        File file = new File(filePath);

        if (!file.exists())
            throw new FileNotFoundException();

        try {

            // inout stream from file
            InputStream inputStream = new FileInputStream(file);

            // we create a reader for a certain document
            PdfReader reader = new PdfReader(inputStream);

            // we create a stamper that will copy the document to a new file
            PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(file));

            // adding content to each page
            PdfContentByte over;

            // generate random id
            String randomHashRef = "watermark:" + PublicFunction.getRandomNumber();

            // get watermark icon
            Image img = Image.getInstance(PublicFunction.generateCommentBox1(context));
            img.setAnnotation(new Annotation(0, 0, 0, 0, randomHashRef));
            img.scaleAbsolute(80, 25);
            img.setAbsolutePosition(pointF.x, pointF.y);
            PdfImage stream = new PdfImage(img, randomHashRef, null);
            stream.put(new PdfName(PublicValues.KEY_SPECIAL_ID), new PdfName(randomHashRef));
            PdfIndirectObject ref = stamp.getWriter().addToBody(stream);
            img.setDirectReference(ref.getIndirectReference());

            // add as layer
            PdfLayer wmLayer = new PdfLayer(randomHashRef, stamp.getWriter());

            // set layer parameters
            // wmLayer.setOnPanel(true);
            // wmLayer.setPrint("print", true);
            // wmLayer.setOn(true);
            // wmLayer.setView(true);

            // Prepare transparency
            PdfGState transparent = new PdfGState();
            transparent.setAlphaIsShape(false);
//            transparent.setFillOpacity(0.9f);
//            transparent.setStrokeOpacity(0.3f);
//            transparent.setBlendMode(PdfGState.BM_DARKEN);

            // get page file number count
            if (reader.getNumberOfPages() < pageIndex) {
                // closing PdfStamper will generate the new PDF file
                stamp.close();
                throw new PDFException("page index is out of pdf file page numbers", new Throwable());
            }

            // annotation added into target page
            over = stamp.getOverContent(pageIndex);
            if (over == null) {
                stamp.close();
                throw new PDFException("getUnderContent is null", new Throwable());
            }

            // add as layer
            over.beginLayer(wmLayer);
            over.setGState(transparent); // set block transparency properties
            over.addImage(img);
            over.endLayer();

            // closing PdfStamper will generate the new PDF file
            stamp.close();

            // close reader
            reader.close();

        } catch (Exception de) {
            de.printStackTrace();
        }
    }

    public static void addAnnotatedWithLayer(Context context, String filePath, PointF pointF, int pageIndex) throws PDFException, IOException {

        String watermarkText = "This is a test";

        // hint : pageIndex starts from --> 1
        pageIndex++;

        // get file and FileOutputStream
        if (filePath == null || filePath.isEmpty())
            throw new FileNotFoundException();

        File file = new File(filePath);

        if (!file.exists())
            throw new FileNotFoundException();

        try {

            // inout stream from file
            InputStream inputStream = new FileInputStream(file);

            // we create a reader for a certain document
            PdfReader reader = new PdfReader(inputStream);

            // we create a stamper that will copy the document to a new file
            PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(file));

            // adding content to each page
            PdfContentByte over;

            // create new watermark
            PdfLayer layer = new PdfLayer("WatermarkLayer", stamp.getWriter());

            Rectangle rect = reader.getPageSize(pageIndex);

            // get watermark icon
            Image img = Image.getInstance(PublicFunction.getByteFromDrawable(context, R.drawable.ic_chat_lawone_new));
            img.setAnnotation(new Annotation(0, 0, 0, 0, "http://www.lowagie.com/iText"));
            img.setAbsolutePosition(pointF.x, pointF.y);
            img.scaleAbsolute(50, 50);

            // get page file number count
            int pageNumbers = reader.getNumberOfPages();

            if (pageNumbers < pageIndex) {
                // closing PdfStamper will generate the new PDF file
                stamp.close();
                throw new PDFException("page index is out of pdf file page numbers", new Throwable());
            }

            // annotation added into target page
            over = stamp.getOverContent(pageIndex);
            if (over == null) {
                stamp.close();
                throw new PDFException("getUnderContent is null", new Throwable());
            }

            over.addImage(img);

            // closing PdfStamper will generate the new PDF file
            stamp.close();

            // close reader
            reader.close();

        } catch (Exception de) {
            de.printStackTrace();
        }
    }

    public static void addAnnotation(String filePath, int currPage) throws FileNotFoundException {

        currPage++; // page index start from 1

        // get file and FileOutputStream
        if (filePath == null || filePath.isEmpty())
            throw new FileNotFoundException();

        File file = new File(filePath);

        if (!file.exists())
            throw new FileNotFoundException();

        try {

            // inout stream from file
            InputStream inputStream = new FileInputStream(file);

            // we create a reader for a certain document
            PdfReader pdfReader = new PdfReader(inputStream);

            // get page file number count
            int pageNumbers = pdfReader.getNumberOfPages();

            // we create a stamper that will copy the document to a new file
            PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(file));

            // add annotations
            Rectangle linkLocation = new Rectangle(120, 120, 220, 230);
            PdfDestination destination = new PdfDestination(PdfDestination.FIT);
            PdfAnnotation link = PdfAnnotation.createLink(pdfStamper.getWriter(),
                    linkLocation, PdfAnnotation.HIGHLIGHT_INVERT, currPage, destination);
            link.setBorder(new PdfBorderArray(0, 0, 0));
            pdfStamper.addAnnotation(link, currPage);

            // closing PdfStamper will generate the new PDF file
            pdfStamper.close();

            // close reader
            pdfReader.close();

        } catch (Exception de) {
            de.printStackTrace();
        }
    }

    public static void removeAllAnnotation(String filePath, int currPage) throws FileNotFoundException {

        currPage++;

        // get file and FileOutputStream
        if (filePath == null || filePath.isEmpty())
            throw new FileNotFoundException();

        File file = new File(filePath);

        if (!file.exists())
            throw new FileNotFoundException();

        try {

            // inout stream from file
            InputStream inputStream = new FileInputStream(file);

            // we create a reader for a certain document
            PdfReader pdfReader = new PdfReader(inputStream);

            // get page file number count
            int pageNumbers = pdfReader.getNumberOfPages();

            // we create a stamper that will copy the document to a new file
            PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(file));

            // get page
            PdfDictionary page = pdfReader.getPageN(currPage);
            PdfArray contents = page.getAsArray(PdfName.CONTENTS);
            // PdfDictionary resources = page.getAsDict(PdfName.RESOURCES);
            // PdfArray annots = page.getAsArray(PdfName.ANNOTS);
            // PdfDictionary xobjects = resources.getAsDict(PdfName.XOBJECT);

            // test
            OCGRemover ocgRemover = new OCGRemover();
            // ocgRemover.removeLayers(pdfReader, "watermark");
            // test

            /*
            // this is remover for layers and is working
            if (contents != null) {
                for (int i = 0; i < contents.size(); i++) {
                    PRStream pdfStream = (PRStream) contents.getAsStream(i);

                    String content = new String(PdfReader.getStreamBytes(pdfStream));
                    if (content.indexOf("/OC") > 0) {
                        pdfStream.put(PdfName.LENGTH, new PdfNumber(0));
                        pdfStream.setData(new byte[0]);
                    }
                    PdfDictionary resources = page.getAsDict(PdfName.RESOURCES);
                    PdfDictionary xobjects = resources.getAsDict(PdfName.XOBJECT);

                    for (PdfName name : xobjects.getKeys()) {
                        pdfStream = (PRStream) xobjects.getAsStream(name);
                        if (pdfStream.get(PdfName.OC) == null)
                            continue;
                        pdfStream.put(PdfName.LENGTH, new PdfNumber(0));
                        pdfStream.setData(new byte[0]);
                    }
                }
            }
            */

            // PdfContentParser

            // pdfStamper.getPdfLayers();

            // work on it

            Log.i(TAG, "removeAnnotation: ");

            /*
            PdfName imgRef = xobjects.getKeys().iterator().next();
            PRStream stream = (PRStream) xobjects.getAsStream(imgRef);
            */

            /*try {
                do {
                    try {
                        for (PdfName key : xobjects.getKeys()) {

                            PdfObject pdfObjectToKill = xobjects.get(key);
                            pdfReader.killIndirect(pdfObjectToKill);
                            xobjects.remove(key);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (xobjects.size() > 0);

            } catch (Exception e) {
                e.printStackTrace();
            }*/

            /*try {

             *//*for (PdfObject element : contents.getElements()) {
                    contents.remove(0);
                }*//*

                for (int i = 0; i < contents.size(); i++) {

                    PRStream pdfStreamForRemove = (PRStream) contents.getAsStream(i);

                    byte[] streamBytes = PdfReader.getStreamBytes(pdfStreamForRemove);

                    // byte[] to string
                    String content = new String(streamBytes, StandardCharsets.US_ASCII);

                    // split string
                    String[] tokens = content.split("\n");

                    for (int j = 0; j < tokens.length; j++) {
                        Log.i(TAG, "removeAnnotation: " + tokens[j]);
                        if (tokens[j].contains("/QuickPDF")) {
                            tokens[j] = "";
                        }
                    }

                    pdfStreamForRemove.setData(new byte[0]);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }*/

            /*try {
                for (PdfObject element : annots.getElements()) {
                    annots.remove(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }*/

            // closing PdfStamper will generate the new PDF file
            pdfStamper.close();

            // close reader
            pdfReader.close();

        } catch (Exception de) {
            de.printStackTrace();
        } finally {

        }
    }

    public static void removeAndUpdateAnnotationWithCode(Context context, String filePath, int currPage, String hashID, PointF pointF) throws FileNotFoundException {

        currPage++;

        // get file and FileOutputStream
        if (filePath == null || filePath.isEmpty())
            throw new FileNotFoundException();

        File file = new File(filePath);

        if (!file.exists())
            throw new FileNotFoundException();

        try {

            // inout stream from file
            // InputStream inputStream = new FileInputStream(file);

            // we create a reader for a certain document
            // PdfReader pdfReader = new PdfReader(inputStream);

            // we create a stamper that will copy the document to a new file
            // PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(file));

            // remove last item
            removeAnnotationWithCode(filePath, currPage, hashID);

            // add new item with new res
            updateAnnotatedImageWithLocation(context, filePath, pointF, currPage, hashID);

            // closing PdfStamper will generate the new PDF file
            // pdfStamper.close();

            // close reader
            // pdfReader.close();

        } catch (Exception de) {
            de.printStackTrace();
        } finally {

        }
    }

    public static void removeAnnotationWithCode(String filePath, int currPage, String hashID) throws FileNotFoundException {

        // currPage++;

        // get file and FileOutputStream
        if (filePath == null || filePath.isEmpty())
            throw new FileNotFoundException();

        File file = new File(filePath);

        if (!file.exists())
            throw new FileNotFoundException();

        try {

            // inout stream from file
            InputStream inputStream = new FileInputStream(file);

            // we create a reader for a certain document
            PdfReader pdfReader = new PdfReader(inputStream);

            // get page file number count
            int pageNumbers = pdfReader.getNumberOfPages();

            // we create a stamper that will copy the document to a new file
            PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(file));

            // remove target object
            OCGRemover ocgRemover = new OCGRemover();
            ocgRemover.removeLayers(pdfReader, hashID);

            // closing PdfStamper will generate the new PDF file
            pdfStamper.close();

            // close reader
            pdfReader.close();

        } catch (Exception de) {
            de.printStackTrace();
        }
    }

    public static void updateAnnotatedImageWithLocation(Context context, String filePath, PointF pointF, int pageIndex, String randomHashRef) throws PDFException, IOException {

        // hint : pageIndex starts from --> 1
        // pageIndex++;

        // get file and FileOutputStream
        if (filePath == null || filePath.isEmpty())
            throw new FileNotFoundException();

        File file = new File(filePath);

        if (!file.exists())
            throw new FileNotFoundException();

        try {

            // inout stream from file
            InputStream inputStream = new FileInputStream(file);

            // we create a reader for a certain document
            PdfReader reader = new PdfReader(inputStream);

            // we create a stamper that will copy the document to a new file
            PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(file));

            // adding content to each page
            PdfContentByte over;

            // generate random id
            // String randomHashRef = "watermark:" + PublicFunction.getRandomNumber();

            // get watermark icon
            Image img = Image.getInstance(PublicFunction.getByteFromDrawable(context, R.drawable.ic_chat_lawone_red));
            img.setAnnotation(new Annotation(0, 0, 0, 0, randomHashRef));
            img.setAbsolutePosition(pointF.x, pointF.y);
            img.scaleAbsolute(50, 50);
            PdfImage stream = new PdfImage(img, randomHashRef, null);
            stream.put(new PdfName(PublicValues.KEY_SPECIAL_ID), new PdfName(randomHashRef));
            PdfIndirectObject ref = stamp.getWriter().addToBody(stream);
            img.setDirectReference(ref.getIndirectReference());

            // add as layer
            PdfLayer wmLayer = new PdfLayer(randomHashRef, stamp.getWriter());

            // set layer parameters
            // wmLayer.setOnPanel(true);
            // wmLayer.setPrint("print", true);
            // wmLayer.setOn(true);
            // wmLayer.setView(true);

            // Prepare transparency
            PdfGState transparent = new PdfGState();
            // transparent.setStrokeOpacity(0.9f);
            transparent.setFillOpacity(0.9f);

            // get page file number count
            if (reader.getNumberOfPages() < pageIndex) {
                // closing PdfStamper will generate the new PDF file
                stamp.close();
                throw new PDFException("page index is out of pdf file page numbers", new Throwable());
            }

            // annotation added into target page
            over = stamp.getOverContent(pageIndex);
            if (over == null) {
                stamp.close();
                throw new PDFException("getUnderContent is null", new Throwable());
            }

            // add as layer
            over.beginLayer(wmLayer);
            over.setGState(transparent); // set block trasparency properties
            over.addImage(img);
            over.endLayer();

            // closing PdfStamper will generate the new PDF file
            stamp.close();

            // close reader
            reader.close();

        } catch (Exception de) {
            de.printStackTrace();
        }
    }

    public static Image makeBlackAndWhitePng(String image) throws IOException, DocumentException {
        BufferedImage bi = ImageIO.read(new File(image));
        BufferedImage newBi = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_USHORT_GRAY);
        newBi.getGraphics().drawImage(bi, 0, 0, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(newBi, "png", baos);
        return Image.getInstance(baos.toByteArray());
    }

    public static void removeAnnotationWithDirectObject(String filePath, int currPage) throws
            FileNotFoundException {

        currPage++;

        // get file and FileOutputStream
        if (filePath == null || filePath.isEmpty())
            throw new FileNotFoundException();

        File file = new File(filePath);

        if (!file.exists())
            throw new FileNotFoundException();

        try {

            // inout stream from file
            InputStream inputStream = new FileInputStream(file);

            // we create a reader for a certain document
            PdfReader pdfReader = new PdfReader(inputStream);

            // get page file number count
            int pageNumbers = pdfReader.getNumberOfPages();

            // we create a stamper that will copy the document to a new file
            PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(file));

            // get page
            PdfDictionary page = pdfReader.getPageN(currPage);

            // get direct objects
            PdfObject object = page.getDirectObject(PdfName.CONTENTS);

            if (object instanceof PRStream) {
                PRStream stream = (PRStream) object;
                byte[] data = PdfReader.getStreamBytes(stream);
                stream.setData(new byte[0]);
            }

            /*PdfDictionary resources = page.getAsDict(PdfName.RESOURCES);
            PdfArray contents = resources.getAsArray(PdfName.CONTENTS);
            PdfArray annots = resources.getAsArray(PdfName.ANNOTS);
            PdfDictionary xobjects = resources.getAsDict(PdfName.XOBJECT);
            Log.i(TAG, "removeAnnotation: ");*/

            /*
            PdfName imgRef = xobjects.getKeys().iterator().next();
            PRStream stream = (PRStream) xobjects.getAsStream(imgRef);
            */

            /*try {

                do {
                    try {
                        for (PdfName key : xobjects.getKeys()) {
                            xobjects.remove(key);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (xobjects.size() > 0);

            } catch (Exception e) {
                e.printStackTrace();
            }*/

            /*try {

                for (PdfObject element : contents.getElements()) {
                    contents.remove(0);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }*/

            /*try {
                for (PdfObject element : annots.getElements()) {
                    annots.remove(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }*/

            // closing PdfStamper will generate the new PDF file
            pdfStamper.close();

            // close reader
            pdfReader.close();

        } catch (Exception de) {
            de.printStackTrace();
        }
    }

    public static void reviewPageContent(String filePath, int currPage) throws
            FileNotFoundException {

        currPage++;

        // get file and FileOutputStream
        if (filePath == null || filePath.isEmpty())
            throw new FileNotFoundException();

        File file = new File(filePath);

        if (!file.exists())
            throw new FileNotFoundException();

        try {

            // inout stream from file
            InputStream inputStream = new FileInputStream(file);

            // we create a reader for a certain document
            PdfReader pdfReader = new PdfReader(inputStream);

            // get page file number count
            int pageNumbers = pdfReader.getNumberOfPages();

            // we create a stamper that will copy the document to a new file
            PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(file));

            // get page
            PdfDictionary page = pdfReader.getPageN(currPage);
            PdfDictionary resources = page.getAsDict(PdfName.RESOURCES);

            PdfArray contents = page.getAsArray(PdfName.CONTENTS);
            PdfArray annots = page.getAsArray(PdfName.ANNOTS);
            PdfDictionary xobjects = resources.getAsDict(PdfName.XOBJECT);

            Map pdfLayers = pdfStamper.getPdfLayers();

            // PdfContentParser

            PdfContentParser pdfContentParser = new PdfContentParser(new PRTokeniser(filePath));

            // closing PdfStamper will generate the new PDF file
            pdfStamper.close();

            // close reader
            pdfReader.close();

        } catch (Exception de) {
            de.printStackTrace();
        }
    }

}