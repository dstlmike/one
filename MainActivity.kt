package com.example.pdfgenerateanddownload

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.work.*
import com.bumptech.glide.Glide
import com.example.pdfgenerateanddownload.databinding.ActivityMainBinding
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    lateinit var binder: ActivityMainBinding
    var pdfResultModel = PdfResultModel()
    val RECORD_REQUEST_CODE = 1
    val STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initialization()
    }

    private fun initialization() {
        pdfResultModel = PdfResultModel(
            "PDF generate",
            "check the pdf download",
            "Generate pdf with image and save in folder",
            "https://developer.android.com/images/activity_lifecycle.png"
        )
        binder.pdfResult = pdfResultModel


        Glide.with(this)
            .load(pdfResultModel.image.toString())
            .into(binder.imgSignature)


        binder.download.setOnClickListener {
            setupPermissions()
        }

    }


    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            STORAGE[0]
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        } else {
            callVisitDownload()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            STORAGE,
            RECORD_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i("Pdf", "Permission has been denied by user")
                } else {
                    Log.i("pdf", "Permission has been granted by user")
                    //   showToast(resources.getString(androidx.work.R.string.downloading_file))
                    callVisitDownload()

                }
            }
        }
    }


    // Serialize a single object.
    fun serializeToJson(any: Any): String {
        val gson = Gson();
        return gson.toJson(any);
    }


    fun callVisitDownload() {
        val pdfString: String = serializeToJson(pdfResultModel)
        val data = Data.Builder()
            .putString("data", pdfString)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)

        val oneTimeRequest =
            OneTimeWorkRequest.Builder(DownloadImageMangerPDF::class.java)
                .setInputData(data)
                .setConstraints(constraints.build())
                .addTag("visit")
                .build()

        WorkManager.getInstance(this).enqueue(oneTimeRequest)

    }

}
