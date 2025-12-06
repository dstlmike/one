package com.example.pdfgenerateanddownload

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.itextpdf.io.image.ImageData
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import java.io.*
import java.lang.Exception
import java.net.URL
import java.net.URLConnection

class DownloadImageMangerPDF(private val mContext: Context, workerParameters: WorkerParameters) :
    Worker(mContext, workerParameters) {
    val Tags = DownloadImageMangerPDF::class.java.simpleName
    val directoryPath = Environment.DIRECTORY_DOWNLOADS +
            "/PDF/" + "temp/"
    private val re = Regex("[^A-Za-z0-9 ]")
    override fun doWork(): Result {
        try {

            val data: Data = inputData
            val pdfString: String = data.getString("data").toString()
            Log.i("visit","pdf $pdfString")

            if (pdfString != null) {
                val pdfResult = pdfString.let { deserializeFromJsonNote(it) }

                var cnt = pdfResult.image?.let { it1 -> downloadFileInDevice1(it1) }

                Log.i("visit",cnt.toString())
                val imagePath: ArrayList<String> by lazy { ArrayList() }

                if (pdfResult.image != null) {
                    downloadFileInDevice1(pdfResult.image)
                    val path = Environment.getExternalStoragePublicDirectory(
                        directoryPath + getFileName(pdfResult.image)
                    )
                    imagePath.add(path.toString())
                }
                if (cnt == imagePath.size) {
                    val resultVisit = createAndDownLoadPdfForVisit(
                        pdfResult,
                        imagePath
                    )
                }

            }


            return Result.success()

        } catch (e: Exception) {

            return Result.failure()
        }
    }

    fun createAndDownLoadPdfForVisit(
        pdfResult: PdfResultModel,
        signpath: List<String>
    ): String {
        try {
            val path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS + "/PDF/"
            )
            val tsLong = System.currentTimeMillis() / 1000
            val ts = tsLong.toString()
            if (!path.exists()) path.mkdirs()

            val file = File(path, pdfResult.txtNoteName?.replace(re, "_") + "_" + ts + ".pdf")
            val fOut = FileOutputStream(file)
            val pdfWriter = PdfWriter(fOut)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)
            val font = PdfFontFactory.createFont("res/font/red_hat_display_medium.ttf")
            val mValueFontSize = 12.0f
            val mHeadingFontSize = 16.0f
            val fixed = 2f
            val multiplied = 0f

            //note name
            val colon = " :"
            val text1 = Text("Note name" + colon).setFont(font)
                .setFontSize(mHeadingFontSize)
            val mTitleParagraph = Paragraph()
            mTitleParagraph.setFixedLeading(fixed)
            mTitleParagraph.add(text1)
            document.add(mTitleParagraph)


            val text = Text(pdfResult.txtNoteName).setFont(font).setFontSize(mValueFontSize)
            val mNoteNamedParagraph1 = Paragraph()
            mNoteNamedParagraph1.add(text)
            document.add(mNoteNamedParagraph1)


            //Notes
            val text17 = Text("notes" + colon).setFont(font)
                .setFontSize(mHeadingFontSize)

            val mNotesParagraph = Paragraph()
            mNotesParagraph.add(text17)
            mNotesParagraph.setFixedLeading(fixed)
            document.add(mNotesParagraph)

            val text18 = Text(pdfResult.txtNoteData).setFont(font).setFontSize(mValueFontSize)
            val mNotesValueParagraph = Paragraph()
            mNotesValueParagraph.add(text18)
            document.add(mNotesValueParagraph)
            document.add(Paragraph())

            //comment
            val text23 = Text("Comments" + colon).setFont(font)
                .setFontSize(mHeadingFontSize)
            val mCommentParagraph = Paragraph()
            mCommentParagraph.setFixedLeading(fixed)
            mCommentParagraph.add(text23)
            document.add(mCommentParagraph)
            document.add(Paragraph())

            val text24 = Text(pdfResult.txtCommentData).setFont(font)
                .setFontSize(mValueFontSize)
            val mCommentValueParagraph = Paragraph()
            mCommentValueParagraph.add(text24)
            document.add(mCommentValueParagraph)


            for (element in signpath) {
                try {
                    document.add(Paragraph())
                    document.add(Paragraph())
                    val data1: ImageData = ImageDataFactory.create(element)
                    val img1 = Image(data1)
                    img1.scaleToFit(200f, 200f)
                    document.add(img1)

                } catch (ex: Exception) {
                    ex.printStackTrace()

                }
            }

            document.close()
            return "pdf"

        } catch (e: IOException) {
            return ("error_download")

        }
    }

    fun downloadFileInDevice1(
        stringUrl: String,
    ): Int {
        var count: Int
        val urgiDocPath = Environment.getExternalStoragePublicDirectory(
            directoryPath
        )
        if (!urgiDocPath.exists()) urgiDocPath.mkdirs()

        if (stringUrl.contains("https")) {
            val path = Environment.getExternalStoragePublicDirectory(
                directoryPath + getFileName(stringUrl)
            )
            try {

                val url = URL(stringUrl)
                val conection: URLConnection = url.openConnection()
                conection.connect()
                // download the file
                val input: InputStream = BufferedInputStream(url.openStream(), 8192)

                // Output stream
                val output: OutputStream = FileOutputStream(path)
                val data = ByteArray(1024)
                var total: Long = 0
                while (input.read(data).also { count = it } !== -1) {
                    total += count
                    output.write(data, 0, count)
                }
                output.flush()

                output.close()
                input.close()
                return 1

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return 0
    }


    // Deserialize to single object.
    fun deserializeFromJsonNote(str: String): PdfResultModel {
        val gson = Gson();
        return gson.fromJson(str, PdfResultModel::class.java)
    }

    fun getFileName(url: String?): String {
        val splitArray = url?.split("/")
        return splitArray?.get(splitArray.size - 1) ?: ""
    }
}
