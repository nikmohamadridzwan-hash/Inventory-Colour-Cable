package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.FiberDatabase
import com.example.model.AppTab
import com.example.model.CableDefinition
import com.example.model.FiberRecord
import com.example.model.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FiberFinderUiState(
  val cables: List<CableDefinition> = FiberDatabase.defaultCables,
  val selectedCable: CableDefinition = FiberDatabase.defaultCables.first(),
  val activeTab: AppTab = AppTab.CORE_TO_COLOUR,
  val themePreference: ThemePreference = ThemePreference.SYSTEM,
  // Mode 2: Core No -> Colour
  val coreQueryInput: String = "180",
  val matchedRecord: FiberRecord? = null,
  val siblingCoresInGroup: List<FiberRecord> = emptyList(),
  val coreLookupError: String? = null,
  // Mode 3: Colour -> Core No
  val selectedTubeColour: String = "Violet",
  val selectedCoreColour: String = FiberDatabase.FILTER_ANY,
  val selectedSlot: String = FiberDatabase.FILTER_ANY,
  val availableTubeColours: List<String> = emptyList(),
  val availableCoreColours: List<String> = emptyList(),
  val availableSlots: List<String> = emptyList(),
  val filteredRecords: List<FiberRecord> = emptyList(),
  val statusMessage: String? = null,
)

class FiberFinderViewModel : ViewModel() {
  private val _uiState = MutableStateFlow(buildInitialState())
  val uiState: StateFlow<FiberFinderUiState> = _uiState.asStateFlow()

  private fun buildInitialState(): FiberFinderUiState {
    val defaultCable = FiberDatabase.defaultCables.first()
    return recalculateState(
      FiberFinderUiState(
        cables = FiberDatabase.defaultCables,
        selectedCable = defaultCable,
        coreQueryInput = "180",
        selectedTubeColour = "Violet",
        selectedCoreColour = FiberDatabase.FILTER_ANY,
        selectedSlot = FiberDatabase.FILTER_ANY,
      )
    )
  }

  fun setTab(tab: AppTab) {
    _uiState.update { it.copy(activeTab = tab) }
  }

  fun cycleTheme() {
    _uiState.update { state ->
      val next =
        when (state.themePreference) {
          ThemePreference.SYSTEM -> ThemePreference.DARK
          ThemePreference.DARK -> ThemePreference.LIGHT
          ThemePreference.LIGHT -> ThemePreference.SYSTEM
        }
      state.copy(themePreference = next)
    }
  }

  fun selectCable(cableId: String) {
    _uiState.update { state ->
      val cable = state.cables.find { it.id == cableId } ?: return@update state
      val currentCore = state.coreQueryInput.toIntOrNull()
      val clampedCore =
        when {
          currentCore == null -> cable.records.firstOrNull()?.coreNo?.toString() ?: "1"
          currentCore > cable.totalCores -> cable.totalCores.toString()
          currentCore < 1 -> "1"
          else -> currentCore.toString()
        }

      val tubeOptions = extractTubeColours(cable)
      val coreOptions = extractCoreColours(cable)
      val slotOptions = extractSlots(cable)

      val nextTube =
        if (state.selectedTubeColour in tubeOptions) state.selectedTubeColour
        else FiberDatabase.FILTER_ANY
      val nextCore =
        if (state.selectedCoreColour in coreOptions) state.selectedCoreColour
        else FiberDatabase.FILTER_ANY
      val nextSlot =
        if (state.selectedSlot in slotOptions) state.selectedSlot
        else FiberDatabase.FILTER_ANY

      recalculateState(
        state.copy(
          selectedCable = cable,
          coreQueryInput = clampedCore,
          selectedTubeColour = nextTube,
          selectedCoreColour = nextCore,
          selectedSlot = nextSlot,
        )
      )
    }
  }

  fun updateCoreQuery(input: String) {
    val digitsOnly = input.filter { it.isDigit() }.take(4)
    _uiState.update { state -> recalculateState(state.copy(coreQueryInput = digitsOnly)) }
  }

  fun stepCore(delta: Int) {
    _uiState.update { state ->
      val records = state.selectedCable.records
      if (records.isEmpty()) return@update state
      val minCore = records.first().coreNo
      val maxCore = records.last().coreNo
      val current = state.coreQueryInput.toIntOrNull() ?: minCore
      val next = (current + delta).coerceIn(minCore, maxCore)
      recalculateState(state.copy(coreQueryInput = next.toString()))
    }
  }

  fun jumpToCore(coreNo: Int, switchToLookupTab: Boolean = false) {
    _uiState.update { state ->
      recalculateState(
        state.copy(
          coreQueryInput = coreNo.toString(),
          activeTab = if (switchToLookupTab) AppTab.CORE_TO_COLOUR else state.activeTab,
        )
      )
    }
  }

  fun selectTubeColour(tubeColour: String) {
    _uiState.update { state -> recalculateState(state.copy(selectedTubeColour = tubeColour)) }
  }

  fun selectCoreColour(coreColour: String) {
    _uiState.update { state -> recalculateState(state.copy(selectedCoreColour = coreColour)) }
  }

  fun selectSlot(slot: String) {
    _uiState.update { state -> recalculateState(state.copy(selectedSlot = slot)) }
  }

  fun resetColourFilters() {
    _uiState.update { state ->
      recalculateState(
        state.copy(
          selectedTubeColour = FiberDatabase.FILTER_ANY,
          selectedCoreColour = FiberDatabase.FILTER_ANY,
          selectedSlot = FiberDatabase.FILTER_ANY,
        )
      )
    }
  }

  fun filterByTubeAndOpenColourTab(tubeColour: String) {
    _uiState.update { state ->
      recalculateState(
        state.copy(
          selectedTubeColour =
            if (tubeColour == FiberDatabase.NOT_APPLICABLE) FiberDatabase.FILTER_ANY
            else tubeColour,
          selectedCoreColour = FiberDatabase.FILTER_ANY,
          selectedSlot = FiberDatabase.FILTER_ANY,
          activeTab = AppTab.COLOUR_TO_CORE,
        )
      )
    }
  }

  fun importCsv(fileName: String, content: String) {
    val result = FiberDatabase.parseCsvToCables(fileName, content)
    result
      .onSuccess { imported ->
        _uiState.update { state ->
          val updatedCables = imported + state.cables
          val firstImported = imported.first()
          val firstCore = firstImported.records.firstOrNull()?.coreNo?.toString() ?: "1"
          recalculateState(
            state.copy(
              cables = updatedCables,
              selectedCable = firstImported,
              coreQueryInput = firstCore,
              selectedTubeColour = FiberDatabase.FILTER_ANY,
              selectedCoreColour = FiberDatabase.FILTER_ANY,
              selectedSlot = FiberDatabase.FILTER_ANY,
              statusMessage =
                "Loaded ${imported.size} cable table(s) from $fileName (${firstImported.records.size} cores)",
            )
          )
        }
      }
      .onFailure { err ->
        _uiState.update {
          it.copy(statusMessage = err.message ?: "Unable to read CSV file.")
        }
      }
  }

  fun clearStatusMessage() {
    _uiState.update { it.copy(statusMessage = null) }
  }

  private fun recalculateState(state: FiberFinderUiState): FiberFinderUiState {
    val cable = state.selectedCable
    val parsedCore = state.coreQueryInput.toIntOrNull()
    val matched =
      if (parsedCore != null) {
        cable.records.find { it.coreNo == parsedCore }
      } else {
        null
      }

    val lookupError =
      when {
        state.coreQueryInput.isBlank() -> "Enter a core number between 1 and ${cable.totalCores}"
        parsedCore == null -> "Enter a valid numeric core number"
        matched == null -> "Core #$parsedCore is out of range (1–${cable.totalCores}) for ${cable.name}"
        else -> null
      }

    val siblings =
      if (matched != null) {
        when {
          matched.tubeNo != null -> cable.records.filter { it.tubeNo == matched.tubeNo }
          matched.slot != FiberDatabase.NOT_APPLICABLE ->
            cable.records.filter { it.slot == matched.slot }
          else -> emptyList()
        }
      } else {
        emptyList()
      }

    val tubeColours = extractTubeColours(cable)
    val coreColours = extractCoreColours(cable)
    val slots = extractSlots(cable)

    val filtered =
      cable.records.filter { record ->
        val matchesTube =
          state.selectedTubeColour == FiberDatabase.FILTER_ANY ||
            record.tubeColour.equals(state.selectedTubeColour, ignoreCase = true)
        val matchesCore =
          state.selectedCoreColour == FiberDatabase.FILTER_ANY ||
            record.coreColour.equals(state.selectedCoreColour, ignoreCase = true)
        val matchesSlot =
          state.selectedSlot == FiberDatabase.FILTER_ANY ||
            record.slot.equals(state.selectedSlot, ignoreCase = true)
        matchesTube && matchesCore && matchesSlot
      }

    return state.copy(
      matchedRecord = matched,
      siblingCoresInGroup = siblings,
      coreLookupError = lookupError,
      availableTubeColours = tubeColours,
      availableCoreColours = coreColours,
      availableSlots = slots,
      filteredRecords = filtered,
    )
  }

  private fun extractTubeColours(cable: CableDefinition): List<String> {
    val values =
      cable.records
        .map { it.tubeColour }
        .filter { it != FiberDatabase.NOT_APPLICABLE }
        .distinct()
    return if (values.isEmpty()) emptyList() else listOf(FiberDatabase.FILTER_ANY) + values
  }

  private fun extractCoreColours(cable: CableDefinition): List<String> {
    val values = cable.records.map { it.coreColour }.distinct()
    return listOf(FiberDatabase.FILTER_ANY) + values
  }

  private fun extractSlots(cable: CableDefinition): List<String> {
    val values =
      cable.records
        .map { it.slot }
        .filter { it != FiberDatabase.NOT_APPLICABLE }
        .distinct()
    return if (values.isEmpty()) emptyList() else listOf(FiberDatabase.FILTER_ANY) + values
  }
}
