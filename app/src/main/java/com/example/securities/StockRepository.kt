package com.example.securities

import kotlinx.coroutines.flow.Flow

//網路資料整合
// 由於 3 個 API 是獨立非同步進行的，建議在 Repository 層使用 Coroutines 的 async 進行平行發送，縮短載入時間：

interface StockRepository {
    /**
     * 取得合併後的 TWSE 股票清單 Flow
     */
    fun getStockList(): Flow<List<StockUiData>>
}