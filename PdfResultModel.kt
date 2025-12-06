package com.example.pdfgenerateanddownload

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class PdfResultModel(
    val txtNoteName: String? = null,
    val txtNoteData: String? = null,
    val txtCommentData: String? = null,
    val image: String? = null

):Parcelable
