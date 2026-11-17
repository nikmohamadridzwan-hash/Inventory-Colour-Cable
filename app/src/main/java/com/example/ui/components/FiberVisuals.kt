package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cable
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.FiberDatabase
import com.example.model.CableDefinition
import com.example.model.FiberRecord
import com.example.ui.theme.FiberPalette
import com.example.ui.theme.MonoNumericMedium
import com.example.ui.theme.MonoNumericSmall

/**
 * Draws a physical optical color swatch (solid or with diagonal/longitudinal tracer stripe)
 * so striped tubes/fibers like "Green (Stripe)" are unmistakable in the field.
 */
@Composable
fun FiberColorSwatch(
  colourName: String,
  modifier: Modifier = Modifier,
  size: Dp = 20.dp,
  isAny: Boolean = colourName.equals(FiberDatabase.FILTER_ANY, ignoreCase = true),
) {
  if (isAny || colourName == FiberDatabase.NOT_APPLICABLE) {
    val borderColor = MaterialTheme.colorScheme.outline
    Box(
      modifier =
        modifier
          .size(size)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .border(1.5.dp, borderColor, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      if (isAny) {
        Canvas(modifier = Modifier.size(size * 0.55f)) {
          val r = this.size.minDimension / 2f
          drawArc(FiberPalette.Blue, 0f, 90f, useCenter = true)
          drawArc(FiberPalette.Yellow, 90f, 90f, useCenter = true)
          drawArc(FiberPalette.Green, 180f, 90f, useCenter = true)
          drawArc(FiberPalette.Red, 270f, 90f, useCenter = true)
          drawCircle(color = Color.White.copy(alpha = 0.25f), radius = r * 0.35f)
        }
      }
    }
    return
  }

  val style = FiberPalette.styleFor(colourName)
  Canvas(
    modifier =
      modifier
        .size(size)
        .clip(CircleShape)
        .border(1.25.dp, style.borderColor, CircleShape)
  ) {
    drawCircle(color = style.fillColor)
    if (style.isStriped) {
      val circlePath = Path().apply { addOval(androidx.compose.ui.geometry.Rect(0f, 0f, this@Canvas.size.width, this@Canvas.size.height)) }
      clipPath(circlePath) {
        drawLine(
          color = style.stripeColor,
          start = Offset(0f, this.size.height * 0.85f),
          end = Offset(this.size.width * 0.85f, 0f),
          strokeWidth = this.size.minDimension * 0.24f,
          cap = StrokeCap.Butt,
        )
      }
    }
  }
}

/**
 * Custom Canvas cross-section showing the active Buffer Tube (outer ring, striped if applicable)
 * and the selected Core strand glowing at the center.
 */
@Composable
fun FiberTubeStrandGraphic(
  tubeColour: String,
  coreColour: String,
  modifier: Modifier = Modifier,
) {
  val hasTube = tubeColour != FiberDatabase.NOT_APPLICABLE
  val tubeStyle = FiberPalette.styleFor(tubeColour)
  val coreStyle = FiberPalette.styleFor(coreColour)
  val neutralRing = MaterialTheme.colorScheme.outline

  Canvas(modifier = modifier.size(84.dp)) {
    val w = size.width
    val h = size.height
    val center = Offset(w / 2f, h / 2f)
    val outerRadius = size.minDimension * 0.45f
    val innerTubeRadius = size.minDimension * 0.31f
    val coreRadius = size.minDimension * 0.18f

    if (hasTube) {
      // Buffer tube outer ring
      drawCircle(color = tubeStyle.fillColor, radius = outerRadius, center = center)
      if (tubeStyle.isStriped) {
        val outerPath =
          Path().apply {
            addOval(
              androidx.compose.ui.geometry.Rect(
                center.x - outerRadius,
                center.y - outerRadius,
                center.x + outerRadius,
                center.y + outerRadius,
              )
            )
          }
        clipPath(outerPath) {
          drawLine(
            color = tubeStyle.stripeColor,
            start = Offset(center.x - outerRadius, center.y + outerRadius * 0.45f),
            end = Offset(center.x + outerRadius * 0.45f, center.y - outerRadius),
            strokeWidth = outerRadius * 0.34f,
          )
        }
      }
      drawCircle(
        color = tubeStyle.borderColor,
        radius = outerRadius,
        center = center,
        style = Stroke(width = 2.5f),
      )
      // Gel / hollow cavity inside tube
      drawCircle(color = Color(0xFF0F172A).copy(alpha = 0.85f), radius = innerTubeRadius, center = center)
    } else {
      // Slotted / ribbon guide ring
      drawCircle(
        color = neutralRing.copy(alpha = 0.3f),
        radius = outerRadius,
        center = center,
        style = Stroke(width = 6f),
      )
    }

    // Optical fiber core strand at center
    drawCircle(color = coreStyle.fillColor, radius = coreRadius, center = center)
    if (coreStyle.isStriped) {
      val corePath =
        Path().apply {
          addOval(
            androidx.compose.ui.geometry.Rect(
              center.x - coreRadius,
              center.y - coreRadius,
              center.x + coreRadius,
              center.y + coreRadius,
            )
          )
        }
      clipPath(corePath) {
        drawLine(
          color = coreStyle.stripeColor,
          start = Offset(center.x - coreRadius, center.y + coreRadius * 0.5f),
          end = Offset(center.x + coreRadius * 0.5f, center.y - coreRadius),
          strokeWidth = coreRadius * 0.42f,
        )
      }
    }
    drawCircle(
      color = coreStyle.borderColor,
      radius = coreRadius,
      center = center,
      style = Stroke(width = 2f),
    )
    // Specular optical cladding glint
    drawCircle(
      color = Color.White.copy(alpha = 0.5f),
      radius = coreRadius * 0.28f,
      center = Offset(center.x - coreRadius * 0.25f, center.y - coreRadius * 0.25f),
    )
  }
}

@Composable
fun CableSelectorCard(
  selectedCable: CableDefinition,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier =
      modifier
        .fillMaxWidth()
        .testTag("cable_selector_button")
        .clip(RoundedCornerShape(18.dp))
        .clickable(onClick = onClick)
        .semantics(mergeDescendants = true) {},
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    tonalElevation = 1.dp,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Box(
        modifier =
          Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Rounded.Cable,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.size(22.dp),
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(R.string.section_select_cable),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = selectedCable.name,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = selectedCable.summary,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = "${selectedCable.totalCores}C",
            style = MonoNumericSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
          )
          Icon(
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = stringResource(R.string.action_select_cable),
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(18.dp),
          )
        }
      }
    }
  }
}

@Composable
fun ColorFilterPill(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  showSwatch: Boolean = true,
) {
  val isAny = label.equals(FiberDatabase.FILTER_ANY, ignoreCase = true)
  val containerColor =
    if (selected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surface
  val contentColor =
    if (selected) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface
  val borderColor =
    if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outlineVariant

  Surface(
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .clip(RoundedCornerShape(14.dp))
        .clickable(onClick = onClick),
    shape = RoundedCornerShape(14.dp),
    color = containerColor,
    border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      if (showSwatch) {
        FiberColorSwatch(colourName = label, size = 18.dp, isAny = isAny)
      }
      Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = contentColor,
        maxLines = 1,
      )
    }
  }
}

@Composable
fun CoreResultRowCard(
  record: FiberRecord,
  isHighlighted: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier =
      modifier
        .fillMaxWidth()
        .testTag("core_row_${record.coreNo}")
        .clip(RoundedCornerShape(14.dp))
        .clickable(onClick = onClick),
    shape = RoundedCornerShape(14.dp),
    color =
      if (isHighlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
      else MaterialTheme.colorScheme.surface,
    border =
      BorderStroke(
        width = if (isHighlighted) 1.5.dp else 1.dp,
        color =
          if (isHighlighted) MaterialTheme.colorScheme.primary
          else MaterialTheme.colorScheme.outlineVariant,
      ),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      // Core number badge
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.width(58.dp),
      ) {
        Box(
          modifier = Modifier.padding(vertical = 8.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "#${record.coreNo}",
            style = MonoNumericMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }

      // Tube info
      Column(modifier = Modifier.weight(1f)) {
        val tubeLabel =
          if (record.tubeNo != null) "Tube ${record.tubeNo}"
          else if (record.slot != FiberDatabase.NOT_APPLICABLE) record.slot
          else "No Tube"
        Text(
          text = tubeLabel,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          if (record.tubeColour != FiberDatabase.NOT_APPLICABLE) {
            FiberColorSwatch(colourName = record.tubeColour, size = 16.dp)
            Text(
              text = record.tubeColour,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          } else {
            Text(
              text = record.slot,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }
      }

      // Core colour pill
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
          FiberColorSwatch(colourName = record.coreColour, size = 18.dp)
          Text(
            text = record.coreColour,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
          )
        }
      }
    }
  }
}
