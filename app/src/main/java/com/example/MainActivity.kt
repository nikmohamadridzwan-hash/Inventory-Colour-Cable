package com.example

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppTab
import com.example.model.CableDefinition
import com.example.model.ThemePreference
import com.example.ui.screens.ColorChartScreen
import com.example.ui.screens.ColourToCoreScreen
import com.example.ui.screens.CoreToColourScreen
import com.example.ui.theme.CoreFinderTheme
import com.example.ui.theme.MonoNumericSmall
import com.example.viewmodel.FiberFinderViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: FiberFinderViewModel = viewModel()
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      val systemDark = isSystemInDarkTheme()
      val useDark =
        when (uiState.themePreference) {
          ThemePreference.SYSTEM -> systemDark
          ThemePreference.DARK -> true
          ThemePreference.LIGHT -> false
        }

      CoreFinderTheme(darkTheme = useDark) {
        FiberFinderApp(viewModel = viewModel, isDarkTheme = useDark)
      }
    }
  }
}

private data class NavDestinationItem(
  val tab: AppTab,
  val labelRes: Int,
  val testTag: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

private val destinations =
  listOf(
    NavDestinationItem(
      tab = AppTab.CORE_TO_COLOUR,
      labelRes = R.string.tab_core_to_colour,
      testTag = "tab_core_to_colour",
      selectedIcon = Icons.Rounded.Numbers,
      unselectedIcon = Icons.Outlined.Numbers,
    ),
    NavDestinationItem(
      tab = AppTab.COLOUR_TO_CORE,
      labelRes = R.string.tab_colour_to_core,
      testTag = "tab_colour_to_core",
      selectedIcon = Icons.Rounded.ColorLens,
      unselectedIcon = Icons.Outlined.ColorLens,
    ),
    NavDestinationItem(
      tab = AppTab.COLOR_CHART,
      labelRes = R.string.tab_color_chart,
      testTag = "tab_color_chart",
      selectedIcon = Icons.Rounded.ViewList,
      unselectedIcon = Icons.Outlined.ViewList,
    ),
  )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiberFinderApp(
  viewModel: FiberFinderViewModel,
  isDarkTheme: Boolean,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }
  var showCablePicker by remember { mutableStateOf(false) }

  val csvPickerLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
      if (uri != null) {
        val fileName = resolveFileName(context, uri)
        val content =
          runCatching {
              context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }
            .getOrNull()
        if (content != null) {
          viewModel.importCsv(fileName, content)
        } else {
          viewModel.importCsv(fileName, "")
        }
      }
    }

  LaunchedEffect(uiState.statusMessage) {
    val msg = uiState.statusMessage
    if (msg != null) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearStatusMessage()
    }
  }

  BackHandler(enabled = uiState.activeTab != AppTab.CORE_TO_COLOUR && !showCablePicker) {
    viewModel.setTab(AppTab.CORE_TO_COLOUR)
  }

  val backgroundBrush =
    Brush.verticalGradient(
      colors =
        listOf(
          MaterialTheme.colorScheme.background,
          MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
          MaterialTheme.colorScheme.background,
        )
    )

  BoxWithConstraints(
    modifier = Modifier.fillMaxSize().background(backgroundBrush),
  ) {
    val useNavigationRail = maxWidth >= 600.dp

    Scaffold(
      containerColor = androidx.compose.ui.graphics.Color.Transparent,
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      topBar = {
        TopAppBar(
          colors =
            TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            ),
          title = {
            Column {
              Text(
                text = stringResource(R.string.app_header_title),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = stringResource(R.string.app_header_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          },
          actions = {
            IconButton(
              onClick = { csvPickerLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain", "*/*")) },
              modifier = Modifier.testTag("import_csv_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.UploadFile,
                contentDescription = stringResource(R.string.action_import_csv),
              )
            }
            IconButton(
              onClick = { viewModel.cycleTheme() },
              modifier = Modifier.testTag("theme_toggle_button"),
            ) {
              Icon(
                imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                contentDescription = stringResource(R.string.action_toggle_theme),
              )
            }
          },
        )
      },
      bottomBar = {
        if (!useNavigationRail) {
          NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
          ) {
            destinations.forEach { item ->
              val selected = uiState.activeTab == item.tab
              NavigationBarItem(
                selected = selected,
                onClick = { viewModel.setTab(item.tab) },
                modifier = Modifier.testTag(item.testTag),
                icon = {
                  Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = null,
                  )
                },
                label = {
                  Text(
                    text = stringResource(item.labelRes),
                    maxLines = 1,
                  )
                },
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
      ) {
        if (useNavigationRail) {
          NavigationRail(
            modifier = Modifier.fillMaxHeight(),
            containerColor = MaterialTheme.colorScheme.surface,
          ) {
            Spacer(modifier = Modifier.height(12.dp))
            destinations.forEach { item ->
              val selected = uiState.activeTab == item.tab
              NavigationRailItem(
                selected = selected,
                onClick = { viewModel.setTab(item.tab) },
                modifier = Modifier.testTag(item.testTag),
                icon = {
                  Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = null,
                  )
                },
                label = {
                  Text(
                    text = stringResource(item.labelRes),
                    maxLines = 1,
                  )
                },
              )
            }
          }
        }

        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
          when (uiState.activeTab) {
            AppTab.CORE_TO_COLOUR ->
              CoreToColourScreen(
                uiState = uiState,
                onOpenCablePicker = { showCablePicker = true },
                onCoreQueryChange = viewModel::updateCoreQuery,
                onStepCore = viewModel::stepCore,
                onJumpToCore = { viewModel.jumpToCore(it, switchToLookupTab = false) },
                onFilterTubeInColourTab = viewModel::filterByTubeAndOpenColourTab,
              )
            AppTab.COLOUR_TO_CORE ->
              ColourToCoreScreen(
                uiState = uiState,
                onOpenCablePicker = { showCablePicker = true },
                onSelectTubeColour = viewModel::selectTubeColour,
                onSelectCoreColour = viewModel::selectCoreColour,
                onSelectSlot = viewModel::selectSlot,
                onResetFilters = viewModel::resetColourFilters,
                onTapCoreRow = { viewModel.jumpToCore(it, switchToLookupTab = true) },
              )
            AppTab.COLOR_CHART ->
              ColorChartScreen(
                uiState = uiState,
                onOpenCablePicker = { showCablePicker = true },
                onSelectTubeGroup = viewModel::filterByTubeAndOpenColourTab,
              )
          }
        }
      }
    }

    if (showCablePicker) {
      val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
      ModalBottomSheet(
        onDismissRequest = { showCablePicker = false },
        sheetState = sheetState,
      ) {
        CablePickerSheetContent(
          cables = uiState.cables,
          selectedCableId = uiState.selectedCable.id,
          onSelectCable = { cableId ->
            viewModel.selectCable(cableId)
            showCablePicker = false
          },
          onImportCsv = {
            showCablePicker = false
            csvPickerLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain", "*/*"))
          },
        )
      }
    }
  }
}

@Composable
private fun CablePickerSheetContent(
  cables: List<CableDefinition>,
  selectedCableId: String,
  onSelectCable: (String) -> Unit,
  onImportCsv: () -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column {
        Text(
          text = stringResource(R.string.section_select_cable),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = "Choose Cable Database",
          style = MaterialTheme.typography.titleLarge,
        )
      }
      Surface(
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onImportCsv),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.UploadFile,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(16.dp),
          )
          Text(
            text = "Import CSV",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
          )
        }
      }
    }

    LazyColumn(
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth().height(420.dp),
    ) {
      items(cables, key = { it.id }) { cable ->
        val selected = cable.id == selectedCableId
        Surface(
          modifier =
            Modifier
              .fillMaxWidth()
              .testTag("cable_option_${cable.id}")
              .clip(RoundedCornerShape(14.dp))
              .clickable { onSelectCable(cable.id) },
          shape = RoundedCornerShape(14.dp),
          color =
            if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          border =
            if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = cable.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "${cable.summary} • ${cable.standardNote}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surface,
            ) {
              Text(
                text = "${cable.totalCores}C",
                style = MonoNumericSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
              )
            }
            if (selected) {
              Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }
      }
    }
  }
}

private fun resolveFileName(context: android.content.Context, uri: Uri): String {
  context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    if (nameIndex >= 0 && cursor.moveToFirst()) {
      return cursor.getString(nameIndex) ?: "imported.csv"
    }
  }
  return uri.lastPathSegment ?: "imported.csv"
}
