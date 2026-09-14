package com.example.securities

import com.google.gson.annotations.SerializedName

data class StockDayAvgResponseData(
    @SerializedName("Code")
    val code: String?,                // 股票代號
    @SerializedName("Name")
    val name: String?,                // 股票名稱
    @SerializedName("ClosingPrice")
    val closingPrice: String?,        // 收盤價
    @SerializedName("MonthlyAveragePrice")
    val monthlyAveragePrice: String?  // 月平均價
)