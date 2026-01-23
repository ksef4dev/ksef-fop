package io.alapierre.ksef.fop.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import io.alapierre.ksef.fop.qr.exceptions.BarcodeGenerationException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;


@Slf4j
public class QrCodeGenerator {

    private QrCodeGenerator() {
    }

    /**
     * Generates a QR Code as a byte array for a given barcode text, width, and height.
     *
     * @param barcodeText The text that will be encoded in the barcode.
     * @param width The width of the barcode in pixels.
     * @param height The height of the barcode in pixels.
     * @return The generated barcode as a byte array.
     * @throws BarcodeGenerationException if a problem with barcode generation occurs
     */
    public static byte[] generateBarcode(@NonNull String barcodeText, int width, int height)  {
        try {
            val buf = new ByteArrayOutputStream();
            writeBarcode(barcodeText, width, height, buf);
            return buf.toByteArray();
        } catch (IOException ex) {
            log.debug(ex.getLocalizedMessage(), ex);
            throw new BarcodeGenerationException(ex);
        }
    }

    /**
     * Generates a QR Code barcode image with the given barcode text, width, and height, and writes it to the specified output stream as a PNG.
     *
     * @param barcodeText The text to be encoded in the barcode.
     * @param width The width of the barcode image in pixels.
     * @param height The height of the barcode image in pixels.
     * @param out The output stream to write the barcode image to. The OutputStream should be closed by the calling code.
     * @throws IOException if an I/O error occurs while writing the image to the output stream.
     * @throws BarcodeGenerationException if a problem with barcode generation occurs
     */
    public static void writeBarcode(@NonNull String barcodeText, int width, int height, OutputStream out) throws IOException {

        if (width <= 0 || height <= 0) {
            throw new BarcodeGenerationException("width and height must be positive", null);
        }

        try {
            QRCodeWriter barcodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, width, height);
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", out);
        } catch (WriterException ex) {
            log.debug(ex.getLocalizedMessage(), ex);
            throw new BarcodeGenerationException("Failed to generate barcode: " + ex.getLocalizedMessage());
        }
    }

}
