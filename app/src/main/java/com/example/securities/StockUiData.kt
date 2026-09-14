package com.example.securities

import androidx.annotation.ColorRes

data class StockUiData(
// --- 基礎資訊 ---
    val code: String,             // 股票代碼 (例: "2330")
    val name: String,             // 股票名稱 (例: "台積電")

    // --- 價格與漲跌 (核心欄位) ---
    val closingPrice: String,     // 收盤價 (例: "1,050.00")
    val change: String,           // 漲跌價額 (例: "+25.00" 或 "-10.00")
//    val changePercent: String,    // 漲跌幅 (例: "+2.44%")

    // --- 當日四值 ---
    val openPrice: String,        // 開盤價 (例: "1,030.00")
    val highestPrice: String,     // 最高價 (例: "1,055.00")
    val lowestPrice: String,      // 最低價 (例: "1,025.00")

    // --- 成交量與金額 ---
    val volume: String,           // 成交股數/成交量 (例: "45,123,000")
    val transactionCount: String, // 成交筆數 (例: "32,150")
    val tradeValue: String,       // 成交金額 (例: "47,380,000,000")

    // --- 估值與基本面指標 ---
    val peRatio: String,          // 本益比 (例: "24.50")
    val pbRatio: String,          // 股價淨值比 (例: "5.12")
    val dividendYield: String,    // 殖利率 (%) (例: "3.25%")

    val monthlyAveragePrice: String,//月平均價

    // --- 視覺控制欄位 ---
    @ColorRes
    val changeColorRes: Int       // 漲跌文字與趨勢顏色資源 ID (漲紅/跌綠/平灰)
)