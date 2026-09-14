package com.example.securities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch


sealed interface StockUiState {
    object Loading : StockUiState
    data class Success(val stocks: List<StockUiData>) : StockUiState
    data class Error(val message: String) : StockUiState
}

@HiltViewModel
class SecuritiesViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StockUiState>(StockUiState.Loading)
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    // 暫存最新從 API 抓取的原始資料，避免切換排序時重新發起 API 請求
    private var rawStockList: List<StockUiData> = emptyList()
    private var isAscendingData = false

    init {
        fetchStockList()
    }

    /**
     * 發起 API 請求抓取股票清單
     */
    fun fetchStockList() {
        viewModelScope.launch {
            repository.getStockList()
                .onStart {
                    // 發起請求前，切換為 Loading 狀態
                    _uiState.value = StockUiState.Loading
                }
                .catch {
                    // 由 Repository coroutineScope 拋出的例外
                    val errorMessage =  "網路連線異常，請稍後再試"
                    _uiState.value = StockUiState.Error(errorMessage)
                }
                .collect { stockList ->
                    if (stockList.isEmpty()) {
                        _uiState.value = StockUiState.Error("查無股票資料")
                    } else {
                        // 1. 暫存 API 回傳的原始清單
                        rawStockList = stockList
                        // 2. 根據當前排序狀態重新排序並發射 Success，預設遞減
                        sortStockListByCode(isAscendingData)
                    }
                }
        }
    }

    /**
     * 當使用者點擊選單切換排序時呼叫
     */

    fun sortStockListByCode(isAscending: Boolean) {
        isAscendingData =isAscending
        val sortedList = if (isAscending) {
            rawStockList.sortedBy { it.code }
        } else {
            rawStockList.sortedByDescending { it.code }
        }

        // 更新 StateFlow
        _uiState.value = StockUiState.Success(stocks = sortedList)
    }
}
