package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.FiberDatabase
import com.example.ui.components.CableSelectorCard
import com.example.ui.components.ColorFilterPill
import com.example.ui.components.CoreResultRowCard
import com.example.ui.theme.MonoNumericSmall
import com.example.viewmodel.FiberFinderUiState

@Composable
fun ColourToCoreScreen(
  uiState: FiberFinderUiState,
  onOpenCablePicker: () -> Unit,
  onSelectTubeColour: (String) -> Unit,
  onSelectCoreColour: (String) -> Unit,
  onSelectSlot: (String) -> Unit,
  onResetFilters: () -> Unit,
  onTapCoreRow: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val cable = uiState.selectedCable
  val hasActiveFilters =
    uiState.selectedTubeColour != FiberDatabase.FILTER_ANY ||
      uiState.selectedCoreColour != FiberDatabase.FILTER_ANY ||
      uiState.selectedSlot != FiberDatabase.FILTER_ANY
  val activeCoreNo = uiState.matchedRecord?.coreNo

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.widthIn(max = 680.dp).fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      item {
        CableSelectorCard(selectedCable = cable, onClick = onOpenCablePicker)
      }

      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          tonalElevation = 1.dp,
        ) {
          Column(
            modifier = Modifier.padding(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = stringResource(R.string.section_find_by_colour),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
              )

              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.testTag("matches_count_badge"),
              ) {
                Text(
                  text = "${uiState.filteredRecords.size} ${stringResource(R.string.label_matches)}",
                  style = MonoNumericSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
              }
            }

            if (uiState.availableTubeColours.isNotEmpty()) {
              FilterRowSection(
                title = stringResource(R.string.label_tube_colour),
                options = uiState.availableTubeColours,
                selected = uiState.selectedTubeColour,
                showSwatch = true,
                onSelect = onSelectTubeColour,
              )
            }

            if (uiState.availableSlots.isNotEmpty()) {
              FilterRowSection(
                title = stringResource(R.string.label_slot),
                options = uiState.availableSlots,
                selected = uiState.selectedSlot,
                showSwatch = false,
                onSelect = onSelectSlot,
              )
            }

            FilterRowSection(
              title = stringResource(R.string.label_core_colour),
              options = uiState.availableCoreColours,
              selected = uiState.selectedCoreColour,
              showSwatch = true,
              onSelect = onSelectCoreColour,
            )

            Row(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = stringResource(R.string.tip_any_full_map),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
              )
              if (hasActiveFilters) {
                TextButton(
                  onClick = onResetFilters,
                  modifier = Modifier.testTag("reset_filters_button"),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                  )
                  Spacer(modifier = Modifier.size(4.dp))
                  Text("Any")
                }
              }
            }
          }
        }
      }

      if (uiState.filteredRecords.isEmpty()) {
        item {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          ) {
            Column(
              modifier = Modifier.padding(28.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp),
              )
              Text(
                text = stringResource(R.string.empty_matches_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = stringResource(R.string.empty_matches_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
              )
              Button(onClick = onResetFilters) {
                Text(stringResource(R.string.action_reset_to_any))
              }
            }
          }
        }
      } else {
        item {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = "CORE NO  •  TUBE / SLOT",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = "CORE COLOUR",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        items(uiState.filteredRecords, key = { it.coreNo }) { record ->
          CoreResultRowCard(
            record = record,
            isHighlighted = record.coreNo == activeCoreNo,
            onClick = { onTapCoreRow(record.coreNo) },
          )
        }
      }

      item { Spacer(modifier = Modifier.height(12.dp)) }
    }
  }
}

@Composable
private fun FilterRowSection(
  title: String,
  options: List<String>,
  selected: String,
  showSwatch: Boolean,
  onSelect: (String) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text(
      text = title.uppercase(),
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      items(options, key = { it }) { option ->
        ColorFilterPill(
          label = option,
          selected = option.equals(selected, ignoreCase = true),
          showSwatch = showSwatch,
          onClick = { onSelect(option) },
        )
      }
    }
  }
}
