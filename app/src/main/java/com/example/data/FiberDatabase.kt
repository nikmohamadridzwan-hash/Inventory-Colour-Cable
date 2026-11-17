package com.example.data

import com.example.model.CableDefinition
import com.example.model.FiberColorLegendItem
import com.example.model.FiberRecord

object FiberDatabase {
  const val FILTER_ANY = "Any"
  const val NOT_APPLICABLE = "-"

  val base12ColorLegend: List<FiberColorLegendItem> =
    listOf(
      FiberColorLegendItem(1, "Blue", "BL"),
      FiberColorLegendItem(2, "Yellow", "YL"),
      FiberColorLegendItem(3, "Green", "GR"),
      FiberColorLegendItem(4, "Red", "RD"),
      FiberColorLegendItem(5, "Violet", "VL"),
      FiberColorLegendItem(6, "Brown", "BR"),
      FiberColorLegendItem(7, "Pink", "PK"),
      FiberColorLegendItem(8, "Grey", "GY"),
      FiberColorLegendItem(9, "Black", "BK"),
      FiberColorLegendItem(10, "Orange", "OR"),
      FiberColorLegendItem(11, "White", "WH"),
      FiberColorLegendItem(12, "Aqua", "AQ"),
    )

  private val base12ColorNames: List<String> = base12ColorLegend.map { it.name }

  private fun tubeColour8Ring(tubeNo: Int): String {
    val index = (tubeNo - 1) % 8
    val ring = (tubeNo - 1) / 8
    val base = base12ColorNames[index]
    return if (ring == 0) base else "$base (Stripe)"
  }

  private fun tubeColour12Ring(tubeNo: Int): String {
    val index = (tubeNo - 1) % 12
    val ring = (tubeNo - 1) / 12
    val base = base12ColorNames[index]
    return if (ring == 0) base else "$base (Stripe)"
  }

  private fun coreColour12Ring(positionInGroup: Int): String {
    val index = (positionInGroup - 1) % 12
    val ring = (positionInGroup - 1) / 12
    val base = base12ColorNames[index]
    return if (ring == 0) base else "$base (Stripe)"
  }

  /**
   * Builds the exact Loose Tube 192C cable matching the user's Excel database:
   * - Tubes 1..6: 12 cores each (Cores 1..72) -> Tube 5 (Violet) = Cores 49..60 (Blue..Aqua)
   * - Tubes 7..11: 24 cores each (Cores 73..192) -> Tube 11 (Green (Stripe)) = Cores 169..192,
   *   so Core 180 = Tube No. 11, Tube Colour = Green (Stripe), Core Colour = Aqua, Slot = -
   */
  private fun buildLooseTube192CsvMatch(): CableDefinition {
    val tubeSizes = listOf(12, 12, 12, 12, 12, 12, 24, 24, 24, 24, 24)
    val records = ArrayList<FiberRecord>(192)
    var coreNo = 1
    tubeSizes.forEachIndexed { idx, count ->
      val tubeNo = idx + 1
      val tubeColour = tubeColour8Ring(tubeNo)
      for (pos in 1..count) {
        records.add(
          FiberRecord(
            coreNo = coreNo,
            tubeNo = tubeNo,
            tubeColour = tubeColour,
            coreColour = coreColour12Ring(pos),
            slot = NOT_APPLICABLE,
          )
        )
        coreNo++
      }
    }
    return CableDefinition(
      id = "loose_tube_192c",
      name = "Loose Tube 192C",
      totalCores = 192,
      summary = "11 Tubes (6×12C + 5×24C)",
      standardNote = "8-colour tube sequence with stripe • 12-colour core sequence",
      records = records,
    )
  }

  private fun buildUniformLooseTube(
    id: String,
    name: String,
    tubeCount: Int,
    coresPerTube: Int,
    use12TubeColors: Boolean = true,
  ): CableDefinition {
    val total = tubeCount * coresPerTube
    val records = ArrayList<FiberRecord>(total)
    var coreNo = 1
    for (tubeNo in 1..tubeCount) {
      val tubeColour =
        if (use12TubeColors) tubeColour12Ring(tubeNo) else tubeColour8Ring(tubeNo)
      for (pos in 1..coresPerTube) {
        records.add(
          FiberRecord(
            coreNo = coreNo,
            tubeNo = tubeNo,
            tubeColour = tubeColour,
            coreColour = coreColour12Ring(pos),
            slot = NOT_APPLICABLE,
          )
        )
        coreNo++
      }
    }
    return CableDefinition(
      id = id,
      name = name,
      totalCores = total,
      summary = "$tubeCount Tubes × ${coresPerTube}C",
      standardNote = "12-colour optical sequence (Blue → Aqua)",
      records = records,
    )
  }

  private fun buildSlottedCore(
    id: String,
    name: String,
    slotCount: Int,
    coresPerSlot: Int,
  ): CableDefinition {
    val total = slotCount * coresPerSlot
    val records = ArrayList<FiberRecord>(total)
    var coreNo = 1
    for (slotNo in 1..slotCount) {
      for (pos in 1..coresPerSlot) {
        records.add(
          FiberRecord(
            coreNo = coreNo,
            tubeNo = null,
            tubeColour = NOT_APPLICABLE,
            coreColour = coreColour12Ring(pos),
            slot = "Slot $slotNo",
          )
        )
        coreNo++
      }
    }
    return CableDefinition(
      id = id,
      name = name,
      totalCores = total,
      summary = "$slotCount Slots × ${coresPerSlot}C",
      standardNote = "Slotted core arrangement • 12-colour core sequence",
      records = records,
    )
  }

  val defaultCables: List<CableDefinition> =
    listOf(
      buildLooseTube192CsvMatch(),
      buildUniformLooseTube("loose_tube_12c", "Loose Tube 12C", tubeCount = 1, coresPerTube = 12),
      buildUniformLooseTube("loose_tube_24c", "Loose Tube 24C", tubeCount = 2, coresPerTube = 12),
      buildUniformLooseTube("loose_tube_48c", "Loose Tube 48C", tubeCount = 4, coresPerTube = 12),
      buildUniformLooseTube("loose_tube_96c", "Loose Tube 96C", tubeCount = 8, coresPerTube = 12),
      buildUniformLooseTube("loose_tube_144c", "Loose Tube 144C", tubeCount = 12, coresPerTube = 12),
      buildUniformLooseTube(
        "loose_tube_192c_16x12",
        "Loose Tube 192C (16×12)",
        tubeCount = 16,
        coresPerTube = 12,
      ),
      buildUniformLooseTube("loose_tube_288c", "Loose Tube 288C", tubeCount = 12, coresPerTube = 24),
      buildSlottedCore("slotted_core_96c", "Slotted Core 96C", slotCount = 8, coresPerSlot = 12),
      buildSlottedCore("slotted_core_192c", "Slotted Core 192C", slotCount = 16, coresPerSlot = 12),
    )

  /**
   * Parses an exported Excel CSV database file.
   * Supports flat table headers:
   * - Core No, Tube No, Tube Colour, Core Colour, Slot
   * - Cable Type, Core No, Tube No, Tube Colour, Core Colour, Slot
   */
  fun parseCsvToCables(fileName: String, csvText: String): Result<List<CableDefinition>> {
    val lines =
      csvText
        .lineSequence()
        .map { parseCsvRow(it) }
        .filter { row -> row.any { it.isNotBlank() } }
        .toList()

    if (lines.isEmpty()) {
      return Result.failure(IllegalArgumentException("The selected CSV file is empty."))
    }

    // Try finding the table header row anywhere in the sheet
    var headerRowIndex = -1
    var coreCol = -1
    var tubeNoCol = -1
    var tubeColourCol = -1
    var coreColourCol = -1
    var slotCol = -1
    var cableTypeCol = -1

    for (i in lines.indices) {
      val row = lines[i].map { it.trim().lowercase() }
      val cIdx = row.indexOfFirst { it == "core no" || it == "core no." || it == "core" }
      val ccIdx = row.indexOfFirst { it == "core colour" || it == "core color" }
      if (cIdx >= 0 && ccIdx >= 0) {
        headerRowIndex = i
        coreCol = cIdx
        coreColourCol = ccIdx
        tubeNoCol = row.indexOfFirst { it == "tube no" || it == "tube no." || it == "tube" }
        tubeColourCol = row.indexOfFirst { it == "tube colour" || it == "tube color" }
        slotCol = row.indexOfFirst { it == "slot" || it == "slot no" }
        cableTypeCol = row.indexOfFirst { it == "cable type" || it == "cable" }
        break
      }
    }

    if (headerRowIndex < 0) {
      return Result.failure(
        IllegalArgumentException(
          "Could not find columns 'Core No' and 'Core Colour' in this CSV."
        )
      )
    }

    // Check if Cable Type was declared in metadata cells above header (like the FIBER CORE COLOUR FINDER sheet)
    var sheetCableName = fileName.substringBeforeLast('.').ifBlank { "Imported Cable" }
    for (i in 0 until headerRowIndex) {
      val row = lines[i]
      val labelIdx = row.indexOfFirst { it.trim().equals("Cable Type", ignoreCase = true) }
      if (labelIdx >= 0 && labelIdx + 1 < row.size && row[labelIdx + 1].isNotBlank()) {
        sheetCableName = row[labelIdx + 1].trim()
        break
      }
    }

    val grouped = linkedMapOf<String, MutableList<FiberRecord>>()
    for (i in (headerRowIndex + 1) until lines.size) {
      val row = lines[i]
      val coreStr = row.getOrNull(coreCol)?.trim().orEmpty()
      val coreNo = coreStr.toIntOrNull() ?: continue
      val coreColour = row.getOrNull(coreColourCol)?.trim().orEmpty()
      if (coreColour.isBlank()) continue

      val tubeNo = row.getOrNull(tubeNoCol)?.trim()?.toIntOrNull()
      val tubeColour =
        row.getOrNull(tubeColourCol)?.trim()?.takeIf { it.isNotBlank() } ?: NOT_APPLICABLE
      val slot = row.getOrNull(slotCol)?.trim()?.takeIf { it.isNotBlank() } ?: NOT_APPLICABLE
      val cableName =
        if (cableTypeCol >= 0) {
          row.getOrNull(cableTypeCol)?.trim()?.takeIf { it.isNotBlank() } ?: sheetCableName
        } else {
          sheetCableName
        }

      grouped
        .getOrPut(cableName) { mutableListOf() }
        .add(
          FiberRecord(
            coreNo = coreNo,
            tubeNo = tubeNo,
            tubeColour = tubeColour,
            coreColour = coreColour,
            slot = slot,
          )
        )
    }

    if (grouped.isEmpty()) {
      return Result.failure(
        IllegalArgumentException("No valid numeric core rows were found in the CSV.")
      )
    }

    val cables =
      grouped.entries.mapIndexed { idx, (name, rawRecords) ->
        val sorted = rawRecords.sortedBy { it.coreNo }
        val total = sorted.maxOf { it.coreNo }.coerceAtLeast(sorted.size)
        val tubeCount = sorted.mapNotNull { it.tubeNo }.distinct().size
        val summary =
          if (tubeCount > 0) "$tubeCount Tubes • ${sorted.size} Rows"
          else "${sorted.size} Cores (Imported)"
        CableDefinition(
          id = "imported_${System.currentTimeMillis()}_$idx",
          name = name,
          totalCores = total,
          summary = summary,
          standardNote = "Imported from $fileName",
          records = sorted,
          isCustomImported = true,
        )
      }

    return Result.success(cables)
  }

  private fun parseCsvRow(line: String): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
      val ch = line[i]
      when {
        ch == '"' -> {
          if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
            current.append('"')
            i++
          } else {
            inQuotes = !inQuotes
          }
        }
        ch == ',' && !inQuotes -> {
          result.add(current.toString())
          current.clear()
        }
        else -> current.append(ch)
      }
      i++
    }
    result.add(current.toString())
    return result
  }
}
