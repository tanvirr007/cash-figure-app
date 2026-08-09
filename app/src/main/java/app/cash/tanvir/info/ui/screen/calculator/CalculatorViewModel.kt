package app.cash.tanvir.info.ui.screen.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.tanvir.info.data.local.preferences.AppLanguage
import app.cash.tanvir.info.domain.model.Denomination
import app.cash.tanvir.info.domain.model.DenominationRow
import app.cash.tanvir.info.domain.model.Sheet
import app.cash.tanvir.info.domain.repository.SettingsRepository
import app.cash.tanvir.info.domain.repository.SheetRepository
import app.cash.tanvir.info.util.CurrencyFormatter
import app.cash.tanvir.info.util.NumberToWordsConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the calculator screen.
 */
data class CalculatorUiState(
    val rows: List<DenominationRow> = Denomination.ALL.map { DenominationRow(it) },
    val grandTotal: Long = 0L,
    val grandTotalFormatted: String = "BDT 0",
    val totalPieces: Long = 0L,
    val activeDenominations: Int = 0,
    val amountInWordsEn: String = "Zero Taka",
    val amountInWordsBn: String = "শূন্য টাকা",
    val isBreakdownExpanded: Boolean = false,
    val quantities: Map<Int, String> = Denomination.ALL.associate { it.value to "" },
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    val disabledDenominations: Set<Int> = emptySet()
)

/**
 * ViewModel for the calculator screen.
 * Manages denomination quantities, computes derived values instantly,
 * and auto-saves the current sheet to Room DB in the background.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val sheetRepository: SheetRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    // Upper bound: 999 Crore (9,99,99,99,999)
    private val maxGrandTotal = 9_99_99_99_999L

    init {
        // Observe language settings
        viewModelScope.launch {
            settingsRepository.getLanguage().collect { lang ->
                _uiState.update { state ->
                    recalculate(state.copy(currentLanguage = lang))
                }
            }
        }

        // Observe disabled denominations settings
        viewModelScope.launch {
            settingsRepository.getDisabledDenominations().collect { disabled ->
                _uiState.update { state ->
                    recalculate(state.copy(disabledDenominations = disabled))
                }
            }
        }

        // Restore active working sheet on startup
        viewModelScope.launch {
            sheetRepository.getCurrentSheet().collect { sheet ->
                if (sheet != null) {
                    val restoredQuantities = sheet.rows.associate {
                        it.denomination.value to if (it.quantity > 0) it.quantity.toString() else ""
                    }
                    _uiState.update { state ->
                        recalculate(state.copy(quantities = restoredQuantities))
                    }
                }
            }
        }

        // Debounced auto-save to Room DB (waits 500ms after last edit)
        _uiState
            .distinctUntilChanged { old, new -> old.quantities == new.quantities }
            .debounce(500L)
            .onEach { state ->
                sheetRepository.saveCurrentSheet(
                    quantities = state.quantities,
                    grandTotal = state.grandTotal,
                    totalPieces = state.totalPieces,
                    activeDenominations = state.activeDenominations
                )
            }
            .launchIn(viewModelScope)
    }

    /**
     * Update the quantity for a specific denomination.
     * Recalculates all totals instantly.
     */
    fun updateQuantity(denominationValue: Int, input: String) {
        _uiState.update { state ->
            val newQuantities = state.quantities.toMutableMap()
            newQuantities[denominationValue] = input
            recalculate(state.copy(quantities = newQuantities))
        }
    }

    /**
     * Clear a single denomination row.
     */
    fun clearRow(denominationValue: Int) {
        updateQuantity(denominationValue, "")
    }

    /**
     * Clear all denomination quantities.
     */
    fun clearAll() {
        _uiState.update { state ->
            val emptyQuantities = state.quantities.mapValues { "" }
            recalculate(state.copy(quantities = emptyQuantities))
        }
    }

    /**
     * Save current sheet to History explicitly.
     * Returns false if total amount is 0.
     */
    fun saveToHistory(name: String = ""): Boolean {
        val state = _uiState.value
        if (state.grandTotal <= 0L) {
            return false
        }
        viewModelScope.launch {
            val rows = Denomination.ALL.map { denom ->
                val qtyStr = state.quantities[denom.value] ?: ""
                val qty = qtyStr.toLongOrNull() ?: 0L
                DenominationRow(
                    denomination = denom,
                    quantity = qty,
                    total = denom.value.toLong() * qty
                )
            }
            val newSheet = Sheet(
                name = name,
                rows = rows,
                grandTotal = state.grandTotal,
                totalPieces = state.totalPieces,
                activeDenominations = state.activeDenominations,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            sheetRepository.saveSheet(newSheet)
        }
        clearAll()
        return true
    }

    /**
     * Toggle the breakdown section open/closed.
     */
    fun toggleBreakdown() {
        _uiState.update { it.copy(isBreakdownExpanded = !it.isBreakdownExpanded) }
    }

    /**
     * Recalculate all derived values from current quantities.
     */
    private fun recalculate(state: CalculatorUiState): CalculatorUiState {
        val visibleDenominations = Denomination.ALL.filter { it.value !in state.disabledDenominations }
        val rows = visibleDenominations.map { denom ->
            val qtyStr = state.quantities[denom.value] ?: ""
            val qty = qtyStr.toLongOrNull() ?: 0L
            DenominationRow(
                denomination = denom,
                quantity = qty,
                total = denom.value.toLong() * qty
            )
        }

        val grandTotal = rows.sumOf { it.total }.coerceAtMost(maxGrandTotal)
        val totalPieces = rows.sumOf { it.quantity }
        val activeDenominations = rows.count { it.quantity > 0 }
        val useBengali = state.currentLanguage == AppLanguage.BANGLA

        return state.copy(
            rows = rows,
            grandTotal = grandTotal,
            grandTotalFormatted = CurrencyFormatter.format(grandTotal, useBengaliDigits = useBengali),
            totalPieces = totalPieces,
            activeDenominations = activeDenominations,
            amountInWordsEn = NumberToWordsConverter.toEnglish(grandTotal),
            amountInWordsBn = NumberToWordsConverter.toBangla(grandTotal)
        )
    }
}
