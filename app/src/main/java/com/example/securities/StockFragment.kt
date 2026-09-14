package com.example.securities

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.securities.databinding.FragmentStockBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StockFragment : Fragment() {

    private var _binding: FragmentStockBinding? = null
    private val binding get() = _binding!!

    // 使用 Hilt 自動注入 ViewModel
    private val viewModel: SecuritiesViewModel by viewModels()

    private lateinit var stockAdapter: StockAdapter

    // 記錄當前顯示的 AlertDialog，避免重複彈出多個對話框
    private var errorDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeUiState()
    }

    private fun setupRecyclerView() {
        stockAdapter = StockAdapter { stock ->
            showStockDetailDialog(stock)
        }

        stockAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                //項目位置移動
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                // 當資料順序移動時，將視角重置回最頂端
                binding.recyclerView.post {
                    (binding.recyclerView.layoutManager as? LinearLayoutManager)
                        ?.scrollToPositionWithOffset(0, 0)
                }
            }
        })

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = stockAdapter
        }
    }
    private fun showStockDetailDialog(stock: StockUiData) {
        val message = """
        本益比：${stock.peRatio.ifEmpty { "N/A" }}
        殖利率(%)：${stock.dividendYield.ifEmpty { "N/A" }}
        股價淨值比：${stock.pbRatio.ifEmpty { "N/A" }}
    """.trimIndent()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("${stock.code} ${stock.name}")
            .setMessage(message)
            .setPositiveButton("確定") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    /**
     * 彈出排序選擇選單，點擊後通知 ViewModel 的 onSortOrderChanged
     */
    private fun setupListeners() {
        // 下拉刷新觸發重新抓取資料
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchStockList()
        }

        // 點擊右上角排序選單按鈕
        binding.btnSortMenu.setOnClickListener {
            val bottomSheet = StockSortBottomSheetFragment { sortOrder ->
                when (sortOrder) {
                    StockSortBottomSheetFragment.SortOrder.ASCENDING -> {
                        viewModel.sortStockListByCode(isAscending = true)
                    }

                    StockSortBottomSheetFragment.SortOrder.DESCENDING -> {
                        viewModel.sortStockListByCode(isAscending = false)
                    }
                }
            }
            bottomSheet.show(childFragmentManager, "StockSortBottomSheet")
        }
    }


    /**
     * 安全地 Collect ViewModel 的 uiState StateFlow
     */
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderUiState(state)
                }
            }
        }
    }

    /**
     * 處理 UI 狀態繪製
     */
    private fun renderUiState(state: StockUiState) {
        binding.swipeRefreshLayout.isRefreshing = (state is StockUiState.Loading)

        when (state) {
            is StockUiState.Loading -> {
                binding.progressBar.isVisible = true
                binding.recyclerView.isVisible = false
                dismissErrorDialog()
            }

            is StockUiState.Success -> {
                binding.progressBar.isVisible = false
                binding.recyclerView.isVisible = true
                dismissErrorDialog()
                stockAdapter.submitList(state.stocks)
            }

            is StockUiState.Error -> {
                binding.progressBar.isVisible = false
                binding.recyclerView.isVisible = false
                showErrorDialog(state.message)
            }
        }
    }

    /**
     * 顯示錯誤提示 AlertDialog
     */
    private fun showErrorDialog(message: String) {
        // 如果對話框已經在顯示中，就不重複建立
        if (errorDialog?.isShowing == true) return

        errorDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("發生錯誤")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("重試") { dialog, _ ->
                dialog.dismiss()
                viewModel.fetchStockList() // 點擊重試重新發起 API 請求
            }
            .setNegativeButton("關閉") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        errorDialog?.show()
    }

    private fun dismissErrorDialog() {
        errorDialog?.dismiss()
        errorDialog = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}