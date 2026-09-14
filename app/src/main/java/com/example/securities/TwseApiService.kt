package com.example.securities

import retrofit2.http.GET

interface TwseApiService {

    // 1. 上市個股日本益比、殖利率及股價淨值比
    @GET("v1/exchangeReport/BWIBBU_ALL")
    suspend fun getBwibbuAll(): List<BwiResponseData>

    // 2. 上市個股日收盤價及月平均價
    @GET("v1/exchangeReport/STOCK_DAY_AVG_ALL")
    suspend fun getStockDayAvgAll(): List<StockDayAvgResponseData>

    // 3. 上市個股日成交資訊
    @GET("v1/exchangeReport/STOCK_DAY_ALL")
    suspend fun getStockDayAll(): List<StockDayAllResponseData>
}