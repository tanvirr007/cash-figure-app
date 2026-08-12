package app.cash.tanvir.info.ui.screen.calculator

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    val quantities: Map<Int, String> = Denomination.ALL.associate { it.value to "" },
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    val disabledDenominations: Set<Int> = emptySet()
)

/**
 * ViewModel for the calculator screen.
 * Manages denomination quantities and computes derived values instantly.
 * The draft (current working sheet) is only persisted when leaving the app
 * (back-exit dialog, direct close via ON_STOP) or when explicitly discarded.
 */
@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val sheetRepository: SheetRepository,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    // Upper bound: 999 Crore (9,99,99,99,999)
    private val maxGrandTotal = 9_99_99_99_999L

    // One-shot: draft id to load into the calculator (from Report "Load into Calculator")
    private val loadDraftId: Long = savedStateHandle.get<Long>("loadDraftId") ?: -1L

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

        // Restore active working sheet on startup or database updates (including reset)
        viewModelScope.launch {
            sheetRepository.getCurrentSheet().collect { sheet ->
                val restoredQuantities = if (sheet != null) {
                    sheet.rows.associate {
                        it.denomination.value to if (it.quantity > 0) it.quantity.toString() else ""
                    }
                } else {
                    Denomination.ALL.associate { it.value to "" }
                }
                _uiState.update { state ->
                    recalculate(
                        state.copy(
                            quantities = restoredQuantities
                        )
                    )
                }
            }
        }

        // Load a saved draft into the calculator (navigated from Report with loadDraftId arg)
        if (loadDraftId > 0L) {
            viewModelScope.launch {
                val draft = sheetRepository.getDraftById(loadDraftId)
                if (draft != null) {
                    val draftQuantities = draft.rows.associate {
                        it.denomination.value to if (it.quantity > 0) it.quantity.toString() else ""
                    }
                    _uiState.update { state ->
                        recalculate(state.copy(quantities = draftQuantities))
                    }
                    // Persist as the working sheet so a later restart resumes it, then drop the draft
                    flushDraft()
                    sheetRepository.deleteDraft(draft.id)
                }
            }
        }

        // Direct close (recents-swipe, home, screen-off): auto-save the draft silently.
        // drop(1) ignores the initial lifecycle snapshot so a cold start never triggers a flush.
        viewModelScope.launch {
            ProcessLifecycleOwner.get().lifecycle.currentStateFlow
                .drop(1)
                .collect { state ->
                    if (state == Lifecycle.State.CREATED) {
                        flushDraft()
                    }
                }
        }
    }

    /**
     * Persist the current state to the draft row immediately (synchronous).
     * Used only on exit paths (back-exit dialog, ON_STOP) and discard.
     * Blocking is intentional: the write must complete before the app process dies.
     */
    fun flushDraft() {
        val state = _uiState.value
        runBlocking {
            sheetRepository.saveCurrentSheet(
                quantities = state.quantities,
                grandTotal = state.grandTotal,
                totalPieces = state.totalPieces,
                activeDenominations = state.activeDenominations
            )
        }
    }

    /**
     * Clear the current count and persist the empty draft immediately,
     * so a later force-kill can never resurrect the discarded draft.
     */
    fun discardDraft() {
        clearAll()
        flushDraft()
    }

    /**
     * Save the current count as a new draft entry (back-exit "Save to Draft").
     * The working sheet (id = -1) is left untouched so the count resumes on restart.
     * Blocking is intentional: the write must complete before the app process dies.
     */
    fun saveAsDraft() {
        val state = _uiState.value
        if (state.grandTotal <= 0L) return
        runBlocking {
            sheetRepository.saveDraft(
                quantities = state.quantities,
                grandTotal = state.grandTotal,
                totalPieces = state.totalPieces,
                activeDenominations = state.activeDenominations
            )
        }
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
    fun saveToHistory(
        name: String = "",
        remark: String = "",
        onSuccess: (Long, String) -> Unit
    ) {
        val state = _uiState.value
        if (state.grandTotal <= 0L) {
            return
        }
        val savedAmountFormatted = state.grandTotalFormatted
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
                updatedAt = System.currentTimeMillis(),
                remark = remark
            )
            val savedId = sheetRepository.saveSheetAndResetCurrent(newSheet)
            onSuccess(savedId, savedAmountFormatted)
        }
        clearAll()
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
