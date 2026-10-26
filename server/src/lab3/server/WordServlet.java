package lab3.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.util.zip.*;

/**
 * Вариант 4: сервлет возвращает документ Word (.docx) с заголовками для
 * автоматического открытия на стороне клиента.
 *
 * .docx — это ZIP-архив с XML-файлами. Создаётся в памяти без внешних библиотек.
 */
public class WordServlet implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        byte[] docx = buildDocx();

        exchange.getResponseHeaders().set("Content-Type",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        exchange.getResponseHeaders().set("Content-Disposition",
                "attachment; filename=\"document.docx\"");
        exchange.sendResponseHeaders(200, docx.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(docx);
        }

        System.out.println("Запрос обработан: документ Word отправлен клиенту.");
    }

    /** Строит минимальный валидный .docx-файл в памяти. */
    private byte[] buildDocx() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {

            // 1. [Content_Types].xml
            zip.putNextEntry(new ZipEntry("[Content_Types].xml"));
            zip.write(contentTypes().getBytes("UTF-8"));
            zip.closeEntry();

            // 2. _rels/.rels
            zip.putNextEntry(new ZipEntry("_rels/.rels"));
            zip.write(rootRels().getBytes("UTF-8"));
            zip.closeEntry();

            // 3. word/_rels/document.xml.rels
            zip.putNextEntry(new ZipEntry("word/_rels/document.xml.rels"));
            zip.write(documentRels().getBytes("UTF-8"));
            zip.closeEntry();

            // 4. word/document.xml — тело документа
            zip.putNextEntry(new ZipEntry("word/document.xml"));
            zip.write(documentXml().getBytes("UTF-8"));
            zip.closeEntry();
        }
        return baos.toByteArray();
    }

    private String contentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
               "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n" +
               "  <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n" +
               "  <Default Extension=\"xml\"  ContentType=\"application/xml\"/>\n" +
               "  <Override PartName=\"/word/document.xml\"\n" +
               "    ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>\n" +
               "</Types>";
    }

    private String rootRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
               "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
               "  <Relationship Id=\"rId1\"\n" +
               "    Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\"\n" +
               "    Target=\"word/document.xml\"/>\n" +
               "</Relationships>";
    }

    private String documentRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
               "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
               "</Relationships>";
    }

    private String documentXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
               "<w:document xmlns:wpc=\"http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas\"\n" +
               "            xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">\n" +
               "  <w:body>\n" +
               "    <w:p>\n" +
               "      <w:pPr><w:jc w:val=\"center\"/></w:pPr>\n" +
               "      <w:r><w:rPr><w:b/><w:sz w:val=\"32\"/></w:rPr>\n" +
               "        <w:t>Лабораторная работа 3 — Вариант 4</w:t>\n" +
               "      </w:r>\n" +
               "    </w:p>\n" +
               "    <w:p><w:r><w:t></w:t></w:r></w:p>\n" +
               "    <w:p>\n" +
               "      <w:r><w:rPr><w:b/></w:rPr>\n" +
               "        <w:t>Тема: </w:t>\n" +
               "      </w:r>\n" +
               "      <w:r><w:t>Передача документа Word через сервлет</w:t></w:r>\n" +
               "    </w:p>\n" +
               "    <w:p><w:r><w:t></w:t></w:r></w:p>\n" +
               "    <w:p>\n" +
               "      <w:r><w:t xml:space=\"preserve\">Данный документ сформирован на стороне сервера " +
               "и автоматически открыт на стороне клиента. Передача выполнена по протоколу HTTP " +
               "с заголовком Content-Disposition: attachment.</w:t></w:r>\n" +
               "    </w:p>\n" +
               "    <w:p><w:r><w:t></w:t></w:r></w:p>\n" +
               "    <w:p>\n" +
               "      <w:r><w:rPr><w:b/></w:rPr><w:t xml:space=\"preserve\">Сервер: </w:t></w:r>\n" +
               "      <w:r><w:t>Java HttpServer (com.sun.net.httpserver)</w:t></w:r>\n" +
               "    </w:p>\n" +
               "    <w:p>\n" +
               "      <w:r><w:rPr><w:b/></w:rPr><w:t xml:space=\"preserve\">Клиент: </w:t></w:r>\n" +
               "      <w:r><w:t>Java HttpURLConnection + Desktop.open()</w:t></w:r>\n" +
               "    </w:p>\n" +
               "    <w:sectPr/>\n" +
               "  </w:body>\n" +
               "</w:document>";
    }
}
