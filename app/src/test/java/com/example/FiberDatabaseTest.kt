package com.example

import com.example.data.FiberDatabase
import com.example.viewmodel.FiberFinderViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FiberDatabaseTest {
  @Test
  fun looseTube192C_core180_matchesExcelCsvExactly() {
    val viewModel = FiberFinderViewModel()
    val state = viewModel.uiState.value

    assertEquals("Loose Tube 192C", state.selectedCable.name)
    assertEquals(192, state.selectedCable.totalCores)

    val matched = state.matchedRecord
    assertNotNull(matched)
    assertEquals(180, matched!!.coreNo)
    assertEquals(11, matched.tubeNo)
    assertEquals("Green (Stripe)", matched.tubeColour)
    assertEquals("Aqua", matched.coreColour)
    assertEquals("-", matched.slot)
  }

  @Test
  fun looseTube192C_violetTubeFilter_matchesCores49Through60() {
    val viewModel = FiberFinderViewModel()
    val state = viewModel.uiState.value

    assertEquals("Violet", state.selectedTubeColour)
    assertEquals(FiberDatabase.FILTER_ANY, state.selectedCoreColour)
    assertEquals(12, state.filteredRecords.size)
    assertEquals((49..60).toList(), state.filteredRecords.map { it.coreNo })
    assertEquals(
      listOf(
        "Blue",
        "Yellow",
        "Green",
        "Red",
        "Violet",
        "Brown",
        "Pink",
        "Grey",
        "Black",
        "Orange",
        "White",
        "Aqua",
      ),
      state.filteredRecords.map { it.coreColour },
    )
    assertTrue(state.filteredRecords.all { it.tubeNo == 5 && it.tubeColour == "Violet" })
  }

  @Test
  fun bidirectionalJump_switchesToCoreAndUpdatesResult() {
    val viewModel = FiberFinderViewModel()
    viewModel.selectCoreColour("Red")
    val redCores = viewModel.uiState.value.filteredRecords
    assertTrue(redCores.any { it.coreNo == 52 })

    viewModel.jumpToCore(52, switchToLookupTab = true)
    val updated = viewModel.uiState.value
    assertEquals(52, updated.matchedRecord?.coreNo)
    assertEquals("Violet", updated.matchedRecord?.tubeColour)
    assertEquals("Red", updated.matchedRecord?.coreColour)
  }

  @Test
  fun csvParser_importsCustomCableTable() {
    val sampleCsv =
      """
      Cable Type,Custom 24C
      Core No,Tube No,Tube Colour,Core Colour,Slot
      1,1,Blue,Blue,-
      2,1,Blue,Yellow,-
      13,2,Yellow,Blue,-
      """.trimIndent()

    val parsed = FiberDatabase.parseCsvToCables("custom.csv", sampleCsv)
    assertTrue(parsed.isSuccess)
    val cables = parsed.getOrThrow()
    assertEquals(1, cables.size)
    assertEquals("Custom 24C", cables.first().name)
    assertEquals(3, cables.first().records.size)
    assertEquals("Yellow", cables.first().records.last().tubeColour)
  }
}
