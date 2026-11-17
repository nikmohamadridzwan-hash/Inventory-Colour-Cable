package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Precision Telecom Instrument Light Palette
val LightPrimary = Color(0xFF0052CC)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFD9E6FF)
val LightOnPrimaryContainer = Color(0xFF001A43)

val LightSecondary = Color(0xFF0284C7)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFE0F2FE)
val LightOnSecondaryContainer = Color(0xFF082F49)

val LightTertiary = Color(0xFF0F766E)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFCCFBF1)
val LightOnTertiaryContainer = Color(0xFF042F2E)

val LightBackground = Color(0xFFF3F6FA)
val LightOnBackground = Color(0xFF0F172A)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF0F172A)
val LightSurfaceVariant = Color(0xFFE6ECF4)
val LightOnSurfaceVariant = Color(0xFF475569)
val LightOutline = Color(0xFFCBD5E1)
val LightOutlineVariant = Color(0xFFE2E8F0)

// Precision Telecom Instrument Dark Palette
val DarkPrimary = Color(0xFF38BDF8)
val DarkOnPrimary = Color(0xFF06253A)
val DarkPrimaryContainer = Color(0xFF0C3A5E)
val DarkOnPrimaryContainer = Color(0xFFD0EFFF)

val DarkSecondary = Color(0xFF60A5FA)
val DarkOnSecondary = Color(0xFF082246)
val DarkSecondaryContainer = Color(0xFF17355E)
val DarkOnSecondaryContainer = Color(0xFFDBEAFE)

val DarkTertiary = Color(0xFF2DD4BF)
val DarkOnTertiary = Color(0xFF042F2E)
val DarkTertiaryContainer = Color(0xFF115E59)
val DarkOnTertiaryContainer = Color(0xFFCCFBF1)

val DarkBackground = Color(0xFF090E1A)
val DarkOnBackground = Color(0xFFF1F5F9)
val DarkSurface = Color(0xFF111827)
val DarkOnSurface = Color(0xFFF1F5F9)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkOutline = Color(0xFF334155)
val DarkOutlineVariant = Color(0xFF1E293B)

// Physical Fiber Strand & Buffer Tube Visual Mapping
data class FiberVisualStyle(
  val fillColor: Color,
  val isStriped: Boolean,
  val stripeColor: Color,
  val contentColor: Color,
  val borderColor: Color,
)

object FiberPalette {
  val Blue = Color(0xFF1D6FE8)
  val Yellow = Color(0xFFFACC15)
  val Green = Color(0xFF16A34A)
  val Red = Color(0xFFDC2626)
  val Violet = Color(0xFF8B5CF6)
  val Brown = Color(0xFF8B4513)
  val Pink = Color(0xFFEC4899)
  val Grey = Color(0xFF64748B)
  val Black = Color(0xFF18181B)
  val Orange = Color(0xFFF97316)
  val White = Color(0xFFF8FAFC)
  val Aqua = Color(0xFF06B6D4)

  fun styleFor(colourName: String): FiberVisualStyle {
    val trimmed = colourName.trim()
    val isStriped = trimmed.contains("Stripe", ignoreCase = true)
    val base =
      trimmed
        .replace("(Stripe)", "", ignoreCase = true)
        .replace("Stripe", "", ignoreCase = true)
        .trim()
        .lowercase()

    return when (base) {
      "blue" ->
        FiberVisualStyle(
          fillColor = Blue,
          isStriped = isStriped,
          stripeColor = Color(0xFF0F172A),
          contentColor = Color.White,
          borderColor = Color(0xFF1E40AF),
        )
      "yellow" ->
        FiberVisualStyle(
          fillColor = Yellow,
          isStriped = isStriped,
          stripeColor = Color(0xFF18181B),
          contentColor = Color(0xFF1E293B),
          borderColor = Color(0xFFCA8A04),
        )
      "green" ->
        FiberVisualStyle(
          fillColor = Green,
          isStriped = isStriped,
          stripeColor = Color(0xFF0F172A),
          contentColor = Color.White,
          borderColor = Color(0xFF15803D),
        )
      "red" ->
        FiberVisualStyle(
          fillColor = Red,
          isStriped = isStriped,
          stripeColor = Color(0xFF0F172A),
          contentColor = Color.White,
          borderColor = Color(0xFF991B1B),
        )
      "violet",
      "purple" ->
        FiberVisualStyle(
          fillColor = Violet,
          isStriped = isStriped,
          stripeColor = Color(0xFF0F172A),
          contentColor = Color.White,
          borderColor = Color(0xFF6D28D9),
        )
      "brown" ->
        FiberVisualStyle(
          fillColor = Brown,
          isStriped = isStriped,
          stripeColor = Color(0xFF0F172A),
          contentColor = Color.White,
          borderColor = Color(0xFF5C2D0C),
        )
      "pink",
      "rose" ->
        FiberVisualStyle(
          fillColor = Pink,
          isStriped = isStriped,
          stripeColor = Color(0xFF0F172A),
          contentColor = Color.White,
          borderColor = Color(0xFFBE185D),
        )
      "grey",
      "gray",
      "slate" ->
        FiberVisualStyle(
          fillColor = Grey,
          isStriped = isStriped,
          stripeColor = Color(0xFF0F172A),
          contentColor = Color.White,
          borderColor = Color(0xFF475569),
        )
      "black" ->
        FiberVisualStyle(
          fillColor = Black,
          isStriped = isStriped,
          stripeColor = Color(0xFFF8FAFC),
          contentColor = Color.White,
          borderColor = Color(0xFF52525B),
        )
      "orange" ->
        FiberVisualStyle(
          fillColor = Orange,
          isStriped = isStriped,
          stripeColor = Color(0xFF0F172A),
          contentColor = Color(0xFF1E293B),
          borderColor = Color(0xFFC2410C),
        )
      "white" ->
        FiberVisualStyle(
          fillColor = White,
          isStriped = isStriped,
          stripeColor = Color(0xFF0F172A),
          contentColor = Color(0xFF0F172A),
          borderColor = Color(0xFF94A3B8),
        )
      "aqua",
      "turquoise",
      "cyan" ->
        FiberVisualStyle(
          fillColor = Aqua,
          isStriped = isStriped,
          stripeColor = Color(0xFF0F172A),
          contentColor = Color(0xFF082F49),
          borderColor = Color(0xFF0E7490),
        )
      else ->
        FiberVisualStyle(
          fillColor = Color(0xFF94A3B8),
          isStriped = false,
          stripeColor = Color(0xFF0F172A),
          contentColor = Color.White,
          borderColor = Color(0xFF64748B),
        )
    }
  }
}
