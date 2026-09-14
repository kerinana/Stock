package com.example.securities


import android.annotation.SuppressLint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * 實作
 */
class StockRepositoryImpl @Inject constructor(
    private val apiService: TwseApiService
) : StockRepository {

    override fun getStockList(): Flow<List<StockUiData>> = flow {
        // 使用 coroutineScope 同時進行併發請求 (Parallel Async Requests)
        val combinedList = coroutineScope {//父
            // 1. 呼叫 TwseApiService 實際定義的三支 API 方法名稱
            // 讓 async 正常拋出例外，由外部 catch 捕獲
            val stockDayDeferred = async { apiService.getStockDayAll() }//子
            val stockDayAvgDeferred = async { apiService.getStockDayAvgAll() }//子
            val bwibbuDeferred = async { apiService.getBwibbuAll() }//子

            val stockDayList = stockDayDeferred.await()
            val stockDayAvgList = stockDayAvgDeferred.await()
            val bwibbuList = bwibbuDeferred.await()


            // 2. 建立 Map 以利快速比對 (以股票代碼 Code 為 Key)
            //用 Key 快速查資料的 Map。
            val stockDayAvgMap = stockDayAvgList.associateBy { cleanCode(it.code) }
            val bwibbuMap = bwibbuList.associateBy { cleanCode(it.code) }

            // 3. 合併三支 API 資料並轉換為 StockUiData
            stockDayList
                .map { stockDay ->
                    val code = cleanCode(stockDay.code)
                    val stockDayAvg = stockDayAvgMap[code]
                    val bwibbu = bwibbuMap[code]

                    mapToUiModel(stockDay, stockDayAvg, bwibbu)
                }
        }

        emit(combinedList)
    }.catch { e ->
        // 在 ViewModel 使用 catch 運算子統一處理 Error 狀態
        throw e
    }.flowOn(Dispatchers.IO) // 切換至 IO 線程執行網路請求與資料轉換

    /**
     * 資料對接與格式化轉換 (String Raw Data -> StockUiData)
     */

    @SuppressLint("DefaultLocale")
    private fun mapToUiModel(
        stockDay: StockDayAllResponseData,
        stockDayAvg: StockDayAvgResponseData?,
        bwibbu: BwiResponseData?
    ): StockUiData {
        // 1. 數值解析與清洗 (Change 本身已包含正負號)
        val closingPrice = parseDouble(stockDay.closingPrice)
        val signedChangeVal = parseDouble(stockDay.change)

        val openPrice = parseDouble(stockDay.openingPrice)
        val highestPrice = parseDouble(stockDay.highestPrice)
        val lowestPrice = parseDouble(stockDay.lowestPrice)

        // 計算漲跌幅 (%)：只要收盤價或漲跌價額有一方為 null，結果就直接為 null
//        val changeRate: Double? = if (closingPrice != null && signedChangeVal != null) {
//            val previousClose = closingPrice - signedChangeVal
//            // 確保分母 (前日收盤價) 大於 0 才進行除法，防止 Divide by Zero 得到 Infinity 或 NaN
//            if (previousClose > 0) {
//                (signedChangeVal / previousClose) * 100
//            } else {
//                null
//            }
//        } else {
//            null
//        }

        // 本益比 / 殖利率 / 股價淨值比 取自 getBwibbuAll 回傳資料
        val peRatio = parseDouble(bwibbu?.peRatio)
        val pbRatio = parseDouble(bwibbu?.pbRatio)
        val dividendYield = parseDouble(bwibbu?.dividendYield)

        //月均價
        val monthlyAvg = parseDouble(stockDayAvg?.monthlyAveragePrice)

        // 2. 顏色與趨勢判斷 (台股傳統：漲紅 / 跌綠 / 平灰)
        val colorRes = when {
            signedChangeVal != null && signedChangeVal > 0 -> R.color.stock_red
            signedChangeVal != null && signedChangeVal < 0 -> R.color.stock_green
            else -> R.color.stock_gray
        }

        val prefix = if (signedChangeVal != null && signedChangeVal > 0) "+" else ""

        // 3. 組合為 StockUiData
        return StockUiData(
            code = cleanCode(stockDay.code),
            name = stockDay.name?.trim().orEmpty(),
            closingPrice = formatNumber(closingPrice, "%,.2f"),
            change = "$prefix${formatNumber(signedChangeVal, "%.2f")}",
//            changePercent = "$prefix${formatNumber(changeRate, "%.2f")}%",
            openPrice = formatNumber(openPrice, "%,.2f"),
            highestPrice = formatNumber(highestPrice, "%,.2f"),
            lowestPrice = formatNumber(lowestPrice, "%,.2f"),
            volume = formatNumber(parseLong(stockDay.tradeVolume), "%,d"),
            transactionCount = formatNumber(parseLong(stockDay.transaction), "%,d"),
            tradeValue = formatNumber(parseLong(stockDay.tradeValue), "%,d"),
            peRatio = if (peRatio != null && peRatio > 0) String.format("%.2f", peRatio) else "--",
            pbRatio = if (pbRatio != null && pbRatio > 0) String.format("%.2f", pbRatio) else "--",
            dividendYield = if (dividendYield != null && dividendYield > 0) String.format(
                "%.2f%%",
                dividendYield
            ) else "--",
            changeColorRes = colorRes,
            monthlyAveragePrice = formatNumber(monthlyAvg, "%,.2f")
        )
    }
}

// --- 資料清洗 Helpers ---
//避免雙引號問題 (replace("\"", ""))和空白字元問題 (trim())
private fun cleanCode(code: String?): String = code?.trim()?.replace("\"", "").orEmpty()

//解析 Double，若為 null、空白、"--" 或非法字串，統一回傳 null
private fun parseDouble(value: String?): Double? {
    if (value.isNullOrBlank()) return null
    return value.replace(",", "").trim().toDoubleOrNull()
}


//解析 Long，若為 null、空白、"--" 或非法字串，統一回傳 null

private fun parseLong(value: String?): Long? {
    if (value.isNullOrBlank()) return null
    return value.replace(",", "").trim().toLongOrNull()
}


//格式化數字：有值才格式化，若為 null 則顯示預設的 "--"

private fun formatNumber(
    value: Number?,
    pattern: String,
    defaultText: String = "--"
): String {
    return if (value != null) {
        String.format(pattern, value)
    } else {
        defaultText
    }
}
