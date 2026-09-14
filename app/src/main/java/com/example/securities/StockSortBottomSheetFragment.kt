package com.example.securities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.securities.databinding.DialogStockSortBottomBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class StockSortBottomSheetFragment(
    private val onSortSelected: (SortOrder) -> Unit
) : BottomSheetDialogFragment() {

    enum class SortOrder { ASCENDING, DESCENDING }

    private var _binding: DialogStockSortBottomBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogStockSortBottomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 升序點擊事件
        binding.tvSortCodeAsc.setOnClickListener {
            onSortSelected(SortOrder.ASCENDING)
            dismiss()
        }

        // 降序點擊事件
        binding.tvSortCodeDesc.setOnClickListener {
            onSortSelected(SortOrder.DESCENDING)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}