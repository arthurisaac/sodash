package com.arthurisaac.application.model

import com.google.gson.annotations.SerializedName

class SMSModel (
    date : String,
    number: String,
    text: String,
    type: Int
        ) {
    @SerializedName("date")
    var date : String? = date

    @SerializedName("number")
    var number : String = number

    @SerializedName("text")
    var text : String = text

    @SerializedName("type")
    var type : Int = type
}