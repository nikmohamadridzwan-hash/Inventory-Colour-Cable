package com.example.model

enum class AppTab {
  CORE_TO_COLOUR,
  COLOUR_TO_CORE,
  COLOR_CHART,
}

enum class ThemePreference {
  SYSTEM,
  LIGHT,
  DARK,
}

data class FiberColorLegendItem(
  val index: Int,
  val name: String,
  val shortCode: String,
)

data class FiberRecord(
  val coreNo: Int,
  val tubeNo: Int?,
  val tubeColour: String,
  val coreColour: String,
  val slot: String,
)

data class CableDefinition(
  val id: String,
  val name: String,
  val totalCores: Int,
  val summary: String,
  val standardNote: String,
  val records: List<FiberRecord>,
  val isCustomImported: Boolean = false,
)
