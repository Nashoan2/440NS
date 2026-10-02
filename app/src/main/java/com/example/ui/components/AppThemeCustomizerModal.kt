package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AppThemePreset
import com.example.data.HomeScreenStyle
import com.example.ui.theme.parseHexColor
import com.example.ui.viewmodel.InvoiceViewModel
import java.io.ByteArrayOutputStream

private val PALETTE_COLORS = listOf(
  "#5E258D" to "بنفسجي ملكي",
  "#0F172A" to "كحلي داكن",
  "#065F46" to "زمردي مالي",
  "#18181B" to "فخامة سوداء",
  "#881337" to "ياقوت إمبراطوري",
  "#334155" to "تيتانيوم تقني",
  "#C2410C" to "غروب ذهبي",
  "#6B21A8" to "لافندر هادئ",
  "#0369A1" to "محيط عميق",
  "#78350F" to "برونز صحراوي",
  "#14532D" to "غابة ونعناع",
  "#854D0E" to "ذهب ملكي",
  "#991B1B" to "لهب قرمزي",
  "#312E81" to "نيلي غامض",
  "#0E7490" to "فيروزي كاريبي",
  "#9D174D" to "روز وشمبانيا",
  "#0070BA" to "أزرق تجاري",
  "#10B981" to "نعناع ساطع",
  "#F59E0B" to "عنبري لامع",
  "#F0F4F8" to "رمادي فاتح",
  "#FFFFFF" to "أبيض ناصع"
)

private fun processImageUri(context: Context, uri: Uri): String? {
  return try {
    context.contentResolver.openInputStream(uri)?.use { stream ->
      val original = BitmapFactory.decodeStream(stream) ?: return null
      val maxDim = 800
      val scaled = if (original.width > maxDim || original.height > maxDim) {
        val ratio = minOf(maxDim.toFloat() / original.width, maxDim.toFloat() / original.height)
        Bitmap.createScaledBitmap(original, (original.width * ratio).toInt(), (original.height * ratio).toInt(), true)
      } else {
        original
      }
      val baos = ByteArrayOutputStream()
      scaled.compress(Bitmap.CompressFormat.JPEG, 85, baos)
      val bytes = baos.toByteArray()
      Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
  } catch (_: Exception) {
    null
  }
}

@Composable
fun AppThemeCustomizerModal(
  viewModel: InvoiceViewModel,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  val config = uiState.uiCustomizationConfig
  var selectedTab by remember { mutableStateOf(0) }

  // Custom Colors state
  var customPrimary by remember(config.customPrimaryColorHex) { mutableStateOf(config.customPrimaryColorHex) }
  var customSecondary by remember(config.customSecondaryColorHex) { mutableStateOf(config.customSecondaryColorHex) }
  var customBg by remember(config.customBgColorHex) { mutableStateOf(config.customBgColorHex) }
  var customCard by remember(config.customCardColorHex) { mutableStateOf(config.customCardColorHex) }

  // JSON import input
  var jsonImportInput by remember { mutableStateOf("") }
  var activeHexPickerKey by remember { mutableStateOf<String?>(null) }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      val base64 = processImageUri(context, uri)
      if (!base64.isNullOrBlank()) {
        viewModel.setCustomBackgroundImage(base64)
      } else {
        viewModel.showToast("❌ تعذر تحميل صورة الخلفية.")
      }
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
      Card(
        modifier = Modifier
          .fillMaxWidth(0.96f)
          .fillMaxHeight(0.90f),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          // Top Header Bar
          val activePrimaryColor = parseHexColor(config.customPrimaryColorHex.ifBlank { config.currentTheme().primaryHex })
          val activeSecondaryColor = parseHexColor(config.customSecondaryColorHex.ifBlank { config.currentTheme().secondaryHex })

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                Brush.horizontalGradient(
                  colors = listOf(activePrimaryColor, activeSecondaryColor)
                )
              )
              .padding(horizontal = 16.dp, vertical = 14.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Palette,
                  contentDescription = null,
                  tint = Color.White
                )
                Column {
                  Text(
                    text = "🎨 تخصيص ثيم ومظهر وألوان التطبيق",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                  )
                  Text(
                    text = "ثيمات ملكية، ألوان مخصصة، خلفيات ونمط الواجهة الرئيسية",
                    fontSize = 11.5.sp,
                    color = Color.White.copy(alpha = 0.85f)
                  )
                }
              }
              IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
              }
            }
          }

          // Tabs
          TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = activePrimaryColor,
            indicator = { tabPositions ->
              if (selectedTab < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                  Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                  color = activePrimaryColor,
                  height = 3.dp
                )
              }
            }
          ) {
            Tab(
              selected = selectedTab == 0,
              onClick = { selectedTab = 0 },
              text = { Text("👑 الثيمات الجاهزة", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
              selected = selectedTab == 1,
              onClick = { selectedTab = 1 },
              text = { Text("🎨 ألوان مخصصة", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
              selected = selectedTab == 2,
              onClick = { selectedTab = 2 },
              text = { Text("🖼️ الخلفية والنمط", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
              selected = selectedTab == 3,
              onClick = { selectedTab = 3 },
              text = { Text("📋 كود JSON", fontSize = 12.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) }
            )
          }

          // Tab Content
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
              .padding(14.dp)
          ) {
            when (selectedTab) {
              0 -> PresetsTab(
                viewModel = viewModel,
                currentPresetId = config.themePresetId
              )
              1 -> CustomColorsTab(
                primaryHex = customPrimary,
                secondaryHex = customSecondary,
                bgHex = customBg,
                cardHex = customCard,
                onPrimaryChange = { customPrimary = it },
                onSecondaryChange = { customSecondary = it },
                onBgChange = { customBg = it },
                onCardChange = { customCard = it },
                onRequestHexDialog = { activeHexPickerKey = it },
                onApply = {
                  viewModel.updateCustomThemeColors(customPrimary, customSecondary, customBg, customCard)
                }
              )
              2 -> BackgroundAndStyleTab(
                config = config,
                viewModel = viewModel,
                onPickImage = {
                  photoPickerLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                  )
                }
              )
              3 -> JsonImportExportTab(
                viewModel = viewModel,
                jsonInput = jsonImportInput,
                onJsonInputChange = { jsonImportInput = it },
                context = context
              )
            }
          }

          // Bottom Bar
          Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedButton(
                onClick = { viewModel.resetAppThemeToDefault() },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("استعادة الافتراضي", fontSize = 12.sp)
              }

              Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = activePrimaryColor),
                modifier = Modifier.weight(1f)
              ) {
                Text("حفظ وإغلاق ✓", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }
            }
          }
        }
      }
    }
  }

  // Custom HEX Dialog
  activeHexPickerKey?.let { key ->
    val (title, curHex) = when (key) {
      "primary" -> "اللون الأساسي" to customPrimary
      "secondary" -> "اللون الثانوي" to customSecondary
      "bg" -> "لون الخلفية" to customBg
      "card" -> "لون البطاقات" to customCard
      else -> "تخصيص اللون" to "#000000"
    }
    CustomHexColorDialog(
      title = "إدخال كود HEX لـ: $title",
      initialHex = curHex,
      onDismiss = { activeHexPickerKey = null },
      onSave = { newHex ->
        when (key) {
          "primary" -> customPrimary = newHex
          "secondary" -> customSecondary = newHex
          "bg" -> customBg = newHex
          "card" -> customCard = newHex
        }
        activeHexPickerKey = null
      }
    )
  }
}

@Composable
private fun PresetsTab(
  viewModel: InvoiceViewModel,
  currentPresetId: String
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Text(
        "اختر أحد الثيمات المصممة باحترافية لتطبيق ألوانها فوراً:",
        fontSize = 12.sp,
        color = Color(0xFF64748B),
        modifier = Modifier.padding(bottom = 4.dp)
      )
    }

    items(AppThemePreset.entries) { preset ->
      val isSelected = currentPresetId == preset.id
      Card(
        onClick = { viewModel.setAppThemePreset(preset) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isSelected) Color(0xFFF3E8FF) else Color.White
        ),
        border = BorderStroke(
          width = if (isSelected) 2.dp else 1.dp,
          color = if (isSelected) Color(0xFF7C3AED) else Color(0xFFE2E8F0)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text(text = preset.emoji, fontSize = 24.sp)
            Column {
              Text(
                text = preset.titleAr,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                color = Color(0xFF0F172A)
              )
              Text(
                text = preset.descAr,
                fontSize = 11.5.sp,
                color = Color(0xFF64748B),
                maxLines = 1
              )
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(parseHexColor(preset.primaryHex))
                .border(1.dp, Color(0xFFCBD5E1), CircleShape)
            )
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(parseHexColor(preset.secondaryHex))
                .border(1.dp, Color(0xFFCBD5E1), CircleShape)
            )
            if (isSelected) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "محدد",
                tint = Color(0xFF7C3AED),
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CustomColorsTab(
  primaryHex: String,
  secondaryHex: String,
  bgHex: String,
  cardHex: String,
  onPrimaryChange: (String) -> Unit,
  onSecondaryChange: (String) -> Unit,
  onBgChange: (String) -> Unit,
  onCardChange: (String) -> Unit,
  onRequestHexDialog: (String) -> Unit,
  onApply: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Text(
        "قم باختيار وتنسيق ألوان الثيم الخاصة بك بكل حرية:",
        fontSize = 12.sp,
        color = Color(0xFF64748B)
      )
    }

    // Mini Live Preview
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = parseHexColor(bgHex, Color(0xFFF1F5F9))),
        border = BorderStroke(1.5.dp, parseHexColor(primaryHex))
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            "معاينة الألوان المخصصة الحالية:",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = parseHexColor(primaryHex)
          )
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = parseHexColor(cardHex, Color.White),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("المملكة للإلكترونيات", fontWeight = FontWeight.Black, fontSize = 13.sp, color = parseHexColor(primaryHex))
                Text("نموذج البطاقة والواجهة", fontSize = 11.sp, color = parseHexColor(secondaryHex))
              }
              Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = parseHexColor(secondaryHex)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(6.dp)
              ) {
                Text("زر تجريبي", fontSize = 11.sp, color = Color.White)
              }
            }
          }
        }
      }
    }

    // 1. Primary Color
    item {
      ColorSelectorSection(
        title = "🟣 اللون الأساسي (Primary Color)",
        currentHex = primaryHex,
        onColorSelect = onPrimaryChange,
        onRequestCustom = { onRequestHexDialog("primary") }
      )
    }

    // 2. Secondary Color
    item {
      ColorSelectorSection(
        title = "🔵 اللون الثانوي (Secondary Color)",
        currentHex = secondaryHex,
        onColorSelect = onSecondaryChange,
        onRequestCustom = { onRequestHexDialog("secondary") }
      )
    }

    // 3. Background Color
    item {
      ColorSelectorSection(
        title = "⚪ لون خلفية الواجهة (Background)",
        currentHex = bgHex,
        onColorSelect = onBgChange,
        onRequestCustom = { onRequestHexDialog("bg") }
      )
    }

    // 4. Card Color
    item {
      ColorSelectorSection(
        title = "🗂️ لون البطاقات والأسطح (Card Surface)",
        currentHex = cardHex,
        onColorSelect = onCardChange,
        onRequestCustom = { onRequestHexDialog("card") }
      )
    }

    item {
      Button(
        onClick = onApply,
        colors = ButtonDefaults.buttonColors(containerColor = parseHexColor(primaryHex)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(46.dp)
      ) {
        Text("💾 حفظ وتطبيق الألوان المخصصة", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
      }
    }
  }
}

@Composable
private fun ColorSelectorSection(
  title: String,
  currentHex: String,
  onColorSelect: (String) -> Unit,
  onRequestCustom: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
  ) {
    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.clickable { onRequestCustom() }
        ) {
          Box(
            modifier = Modifier
              .size(20.dp)
              .clip(CircleShape)
              .background(parseHexColor(currentHex))
              .border(1.dp, Color.Gray, CircleShape)
          )
          Text(text = currentHex, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
          Text(text = "✏️ تعديل", fontSize = 11.sp, color = Color(0xFF2563EB))
        }
      }

      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(PALETTE_COLORS) { (hex, _) ->
          val isSelected = currentHex.equals(hex, ignoreCase = true)
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(parseHexColor(hex))
              .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) Color(0xFF7C3AED) else Color(0xFFCBD5E1),
                shape = CircleShape
              )
              .clickable { onColorSelect(hex) },
            contentAlignment = Alignment.Center
          ) {
            if (isSelected) {
              Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }
  }
}

@Composable
private fun BackgroundAndStyleTab(
  config: com.example.data.UiCustomizationConfig,
  viewModel: InvoiceViewModel,
  onPickImage: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Home Screen Style Selection
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("📱 نمط وشكل الواجهة الرئيسية:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
          Text("اختر المظهر المعماري الذي يناسب شاشة متجرك وطبيعة استخدامك:", fontSize = 11.5.sp, color = Color.Gray)

          HomeScreenStyle.entries.forEach { style ->
            val isSelected = config.homeScreenStyleId == style.id
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
              border = BorderStroke(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.setHomeScreenStyle(style) }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Text(text = style.emoji, fontSize = 20.sp)
                  Column {
                    Text(text = style.titleAr, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color(0xFF0F172A))
                    Text(text = style.descAr, fontSize = 11.sp, color = Color(0xFF64748B))
                  }
                }
                if (isSelected) {
                  Icon(Icons.Default.Check, contentDescription = "محدد", tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                }
              }
            }
          }
        }
      }
    }

    // 2. Custom Background Image
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("🖼️ صورة خلفية مخصصة للواجهة:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
          Text("يمكنك تعيين صورة خلفية خاصة بمتجرك تظهر خلف أزرار وعناصر الواجهة الرئيسية:", fontSize = 11.5.sp, color = Color.Gray)

          val hasImage = config.customBackgroundImageBase64.isNotBlank()
          if (hasImage) {
            val bitmap = remember(config.customBackgroundImageBase64) {
              try {
                val bytes = Base64.decode(config.customBackgroundImageBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
              } catch (_: Exception) {
                null
              }
            }
            if (bitmap != null) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(130.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
              ) {
                androidx.compose.foundation.Image(
                  bitmap = bitmap,
                  contentDescription = "خلفية الواجهة",
                  modifier = Modifier.fillMaxSize(),
                  contentScale = ContentScale.Crop
                )
              }
            }

            // Opacity slider
            Column {
              Text("درجة وضوح الخلفية: ${(config.bgImageAlpha * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              Slider(
                value = config.bgImageAlpha,
                onValueChange = { viewModel.updateBgImageAlpha(it) },
                valueRange = 0.05f..1.0f
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = onPickImage,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text("تغيير الصورة 🖼️", fontSize = 12.sp)
              }
              OutlinedButton(
                onClick = { viewModel.removeCustomBackgroundImage() },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text("حذف الخلفية 🗑️", fontSize = 12.sp, color = Color(0xFFDC2626))
              }
            }
          } else {
            Button(
              onClick = onPickImage,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("اختيار صورة خلفية من الجهاز", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun JsonImportExportTab(
  viewModel: InvoiceViewModel,
  jsonInput: String,
  onJsonInputChange: (String) -> Unit,
  context: Context
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Text(
        "يمكنك مشاركة وتصدير كود الثيم الحالي أو استيراد كود ثيم مسبق عبر JSON:",
        fontSize = 12.sp,
        color = Color(0xFF64748B)
      )
    }

    // Export Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("📤 تصدير الثيم الحالي:", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color(0xFF0F172A))
          Button(
            onClick = {
              val json = viewModel.exportCurrentThemeJson()
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val clip = ClipData.newPlainText("AppThemeJson", json)
              clipboard.setPrimaryClip(clip)
              viewModel.showToast("📋 تم نسخ كود الثيم JSON إلى الحافظة بنجاح!")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("نسخ كود الثيم الحالي (Copy JSON)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Import Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("📥 استيراد وتركيب ثيم من كود JSON:", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color(0xFF0F172A))
          OutlinedTextField(
            value = jsonInput,
            onValueChange = onJsonInputChange,
            placeholder = { Text("الصق كود JSON الخاص بالثيم هنا...") },
            modifier = Modifier
              .fillMaxWidth()
              .height(110.dp),
            shape = RoundedCornerShape(8.dp)
          )
          Button(
            onClick = {
              if (jsonInput.isNotBlank()) {
                viewModel.uploadCustomThemeJson(jsonInput)
              } else {
                viewModel.showToast("⚠️ يرجى لصق كود JSON أولاً.")
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("تطبيق الثيم المستورد الآن ⚡", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
