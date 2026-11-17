package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.FiberDatabase
import com.example.model.FiberRecord
import com.example.ui.components.CableSelectorCard
import com.example.ui.components.FiberColorSwatch
import com.example.ui.components.FiberTubeStrandGraphic
import com.example.ui.theme.MonoNumericHero
import com.example.ui.theme.MonoNumericMedium
import com.example.ui.theme.MonoNumericSmall
import com.example.viewmodel.FiberFinderUiState
import kotlin.math.roundToInt

@Composable
fun CoreToColourScreen(
  uiState: FiberFinderUiState,
  onOpenCablePicker: () -> Unit,
  onCoreQueryChange: (String) -> Unit,
  onStepCore: (Int) -> Unit,
  onJumpToCore: (Int) -> Unit,
  onFilterTubeInColourTab: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val focusManager = LocalFocusManager.current
  val cable = uiState.selectedCable
  val scrollState = rememberScrollState()

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    Column(
      modifier =
        Modifier
          .widthIn(max = 680.dp)
          .fillMaxSize()
          .verticalScroll(scrollState)
          .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      CableSelectorCard(selectedCable = cable, onClick = onOpenCablePicker)

      // Section 2: Core No. Input & Quick Scrubber
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
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = stringResource(R.string.section_find_by_core),
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = "Range 1–${cable.totalCores}",
              style = MonoNumericSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            FilledTonalIconButton(
              onClick = {
                focusManager.clearFocus()
                onStepCore(-1)
              },
              modifier = Modifier.size(52.dp).testTag("core_decrement_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.Remove,
                contentDescription = stringResource(R.string.action_previous_core),
              )
            }

            OutlinedTextField(
              value = uiState.coreQueryInput,
              onValueChange = onCoreQueryChange,
              modifier = Modifier.weight(1f).testTag("core_number_input"),
              singleLine = true,
              label = { Text(stringResource(R.string.label_core_no)) },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Rounded.Numbers,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                )
              },
              textStyle =
                MonoNumericMedium.copy(
                  textAlign = TextAlign.Start,
                  color = MaterialTheme.colorScheme.onSurface,
                ),
              keyboardOptions =
                KeyboardOptions(
                  keyboardType = KeyboardType.Number,
                  imeAction = ImeAction.Done,
                ),
              keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
              isError = uiState.coreLookupError != null && uiState.coreQueryInput.isNotBlank(),
              shape = RoundedCornerShape(14.dp),
            )

            FilledTonalIconButton(
              onClick = {
                focusManager.clearFocus()
                onStepCore(1)
              },
              modifier = Modifier.size(52.dp).testTag("core_increment_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.action_next_core),
              )
            }
          }

          val sliderMax = cable.totalCores.coerceAtLeast(2).toFloat()
          val currentVal =
            (uiState.coreQueryInput.toIntOrNull() ?: 1).coerceIn(1, cable.totalCores).toFloat()

          Slider(
            value = currentVal,
            onValueChange = { onJumpToCore(it.roundToInt()) },
            valueRange = 1f..sliderMax,
            modifier = Modifier.fillMaxWidth().testTag("core_range_slider"),
          )

          // Quick jump milestones
          val quickCores =
            listOf(1, (cable.totalCores / 4).coerceAtLeast(1), (cable.totalCores / 2).coerceAtLeast(1), cable.totalCores)
              .distinct()
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            quickCores.forEach { preset ->
              val isSelected = uiState.matchedRecord?.coreNo == preset
              Surface(
                modifier =
                  Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                      focusManager.clearFocus()
                      onJumpToCore(preset)
                    },
                shape = RoundedCornerShape(10.dp),
                color =
                  if (isSelected) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              ) {
                Box(
                  modifier = Modifier.padding(vertical = 8.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = "#$preset",
                    style = MonoNumericSmall.copy(fontWeight = FontWeight.SemiBold),
                    color =
                      if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                      else MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }
        }
      }

      // Result Card
      AnimatedContent(
        targetState = uiState.matchedRecord,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "core_lookup_result",
      ) { record ->
        if (record != null) {
          Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            MatchedCoreHeroCard(
              record = record,
              cableName = cable.name,
              onInspectTube = { onFilterTubeInColourTab(record.tubeColour) },
            )

            if (uiState.siblingCoresInGroup.isNotEmpty()) {
              SiblingCoresCard(
                siblings = uiState.siblingCoresInGroup,
                activeCoreNo = record.coreNo,
                onSelectCore = {
                  focusManager.clearFocus()
                  onJumpToCore(it)
                },
              )
            }
          }
        } else {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          ) {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp),
              )
              Text(
                text = uiState.coreLookupError ?: "Enter a core number",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
              )
              TextButton(onClick = { onJumpToCore(1) }) {
                Text("Jump to Core #1")
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}

@Composable
private fun MatchedCoreHeroCard(
  record: FiberRecord,
  cableName: String,
  onInspectTube: () -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth().testTag("matched_core_card"),
    shape = RoundedCornerShape(22.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    tonalElevation = 2.dp,
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Hero Header with Fiber Cross-Section Canvas
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = cableName.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = "CORE #${record.coreNo}",
            style = MonoNumericHero,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text =
              if (record.tubeColour != FiberDatabase.NOT_APPLICABLE) {
                "${record.tubeColour} Tube  •  ${record.coreColour} Core"
              } else {
                "${record.slot}  •  ${record.coreColour} Core"
              },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        FiberTubeStrandGraphic(
          tubeColour = record.tubeColour,
          coreColour = record.coreColour,
        )
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

      // 2x2 Breakdown matching exact Excel fields: Tube No., Slot, Tube Colour, Core Colour
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        DetailMetricBox(
          label = stringResource(R.string.label_tube_no),
          value = record.tubeNo?.toString() ?: FiberDatabase.NOT_APPLICABLE,
          modifier = Modifier.weight(1f),
        )
        DetailMetricBox(
          label = stringResource(R.string.label_slot),
          value = record.slot,
          modifier = Modifier.weight(1f),
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        DetailColorBox(
          label = stringResource(R.string.label_tube_colour),
          colourName = record.tubeColour,
          modifier = Modifier.weight(1f),
          onClick =
            if (record.tubeColour != FiberDatabase.NOT_APPLICABLE) onInspectTube else null,
        )
        DetailColorBox(
          label = stringResource(R.string.label_core_colour),
          colourName = record.coreColour,
          modifier = Modifier.weight(1f),
          onClick = null,
        )
      }
    }
  }
}

@Composable
private fun DetailMetricBox(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = value,
        style = MonoNumericMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun DetailColorBox(
  label: String,
  colourName: String,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
) {
  val baseModifier =
    if (onClick != null) {
      modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick)
    } else {
      modifier
    }

  Surface(
    modifier = baseModifier,
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (onClick != null) {
          Icon(
            imageVector = Icons.Rounded.FilterList,
            contentDescription = "Filter all cores in this tube",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp),
          )
        }
      }
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        if (colourName != FiberDatabase.NOT_APPLICABLE) {
          FiberColorSwatch(colourName = colourName, size = 22.dp)
        }
        Text(
          text = colourName,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun SiblingCoresCard(
  siblings: List<FiberRecord>,
  activeCoreNo: Int,
  onSelectCore: (Int) -> Unit,
) {
  val first = siblings.first()
  val groupTitle =
    if (first.tubeNo != null) {
      "Tube ${first.tubeNo} (${first.tubeColour}) • ${siblings.size} Cores"
    } else {
      "${first.slot} • ${siblings.size} Cores"
    }

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  ) {
    Column(
      modifier = Modifier.padding(vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
          text = stringResource(R.string.sibling_cores_title).uppercase(),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = groupTitle,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }

      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(siblings, key = { it.coreNo }) { item ->
          val isCurrent = item.coreNo == activeCoreNo
          Surface(
            modifier =
              Modifier
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onSelectCore(item.coreNo) },
            shape = RoundedCornerShape(12.dp),
            color =
              if (isCurrent) MaterialTheme.colorScheme.primaryContainer
              else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border =
              if (isCurrent) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
              else null,
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              FiberColorSwatch(colourName = item.coreColour, size = 16.dp)
              Column {
                Text(
                  text = "#${item.coreNo}",
                  style = MonoNumericSmall.copy(fontWeight = FontWeight.Bold),
                  color =
                    if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = item.coreColour,
                  style = MaterialTheme.typography.bodySmall,
                  color =
                    if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }
      }
    }
  }
}
