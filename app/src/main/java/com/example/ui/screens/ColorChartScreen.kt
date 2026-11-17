package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.FiberDatabase
import com.example.ui.components.CableSelectorCard
import com.example.ui.components.FiberColorSwatch
import com.example.ui.theme.MonoNumericMedium
import com.example.ui.theme.MonoNumericSmall
import com.example.viewmodel.FiberFinderUiState

private data class TubeSummaryGroup(
  val title: String,
  val colour: String,
  val startCore: Int,
  val endCore: Int,
  val count: Int,
)

@Composable
fun ColorChartScreen(
  uiState: FiberFinderUiState,
  onOpenCablePicker: () -> Unit,
  onSelectTubeGroup: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val cable = uiState.selectedCable
  val groups =
    remember(cable) {
      val grouped =
        if (cable.records.any { it.tubeNo != null }) {
          cable.records.groupBy { it.tubeNo }
        } else {
          cable.records.groupBy { it.slot }
        }
      grouped.entries.map { (key, rows) ->
        val first = rows.first()
        val last = rows.last()
        val title =
          if (first.tubeNo != null) "Tube ${first.tubeNo}"
          else first.slot
        TubeSummaryGroup(
          title = title,
          colour = first.tubeColour,
          startCore = first.coreNo,
          endCore = last.coreNo,
          count = rows.size,
        )
      }
    }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.widthIn(max = 680.dp).fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        CableSelectorCard(selectedCable = cable, onClick = onOpenCablePicker)
      }

      // 12-Color Optical Sequence Reference Card
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          tonalElevation = 1.dp,
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Column {
              Text(
                text = "12-COLOUR CORE SEQUENCE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
              )
              Text(
                text = "Standard 1–12 optical strand order from your database",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            val rows = FiberDatabase.base12ColorLegend.chunked(2)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              rows.forEach { pair ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  pair.forEach { item ->
                    Surface(
                      modifier = Modifier.weight(1f),
                      shape = RoundedCornerShape(12.dp),
                      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    ) {
                      Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                      ) {
                        Text(
                          text = item.index.toString().padStart(2, '0'),
                          style = MonoNumericSmall.copy(fontWeight = FontWeight.Bold),
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FiberColorSwatch(colourName = item.name, size = 20.dp)
                        Text(
                          text = item.name,
                          style = MaterialTheme.typography.labelLarge,
                          color = MaterialTheme.colorScheme.onSurface,
                          modifier = Modifier.weight(1f),
                        )
                        Text(
                          text = item.shortCode,
                          style = MonoNumericSmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      // Active Cable Tube / Slot Map Overview
      item {
        Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
          Text(
            text = "${cable.name.uppercase()} BREAKDOWN",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = cable.standardNote,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      items(groups, key = { "${it.title}_${it.startCore}" }) { group ->
        val clickable = group.colour != FiberDatabase.NOT_APPLICABLE
        Surface(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .let { mod ->
                if (clickable) mod.clickable { onSelectTubeGroup(group.colour) } else mod
              },
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            if (group.colour != FiberDatabase.NOT_APPLICABLE) {
              FiberColorSwatch(colourName = group.colour, size = 26.dp)
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text =
                  if (group.colour != FiberDatabase.NOT_APPLICABLE) "${group.title} • ${group.colour}"
                  else group.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "Cores ${group.startCore}–${group.endCore} (${group.count}C)",
                style = MonoNumericSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            if (clickable) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Text(
                    text = "View",
                    style = MonoNumericSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                  )
                  Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "View cores in ${group.title}",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.width(14.dp),
                  )
                }
              }
            } else {
              Text(
                text = "${group.count}C",
                style = MonoNumericMedium,
                color = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }
      }

      item { Spacer(modifier = Modifier.height(12.dp)) }
    }
  }
}
