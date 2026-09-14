package com.example.securities

import com.google.gson.annotations.SerializedName

data class StockDayAllResponseData(
    @SerializedName("Code")
    val code: String?,          // 證券代號
    @SerializedName("Name")
    val name: String?,          // 證券名稱
    @SerializedName("TradeVolume")
    val tradeVolume: String?,   // 成交股數
    @SerializedName("TradeValue")
    val tradeValue: String?,    // 成交金額
    @SerializedName("OpeningPrice")
    val openingPrice: String?,  // 開盤價
    @SerializedName("HighestPrice")
    val highestPrice: String?,  // 最高價
    @SerializedName("LowestPrice")
    val lowestPrice: String?,   // 最低價
    @SerializedName("ClosingPrice")
    val closingPrice: String?,  // 收盤價
    @SerializedName("Change")
    val change: String?,        // 漲跌價差
    @SerializedName("Transaction")
    val transaction: String?    // 成交筆數
)
