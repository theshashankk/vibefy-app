package com.vibefy.musicwtf.ui.submit

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibefy.musicwtf.ui.theme.*

private val CATEGORIES = listOf(
    "Safar",
    "Rozmarra",
    "Bachpan",
    "Dukaan",
    "Kshetriya",
    "Shaadi",
    "Tyohar",
    "Bhakti",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitScreen(onDismiss: () -> Unit) {
    val context = LocalContext.current

    var siteName by remember { mutableStateOf("") }
    var siteUrl by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Dukaan") }
    var categoryExpanded by remember { mutableStateOf(false) }

    fun sendEmailSubmission() {
        val subject = Uri.encode("[Gaane Playlist Submission] ${siteName.ifBlank { "New Playlist" }}")
        val bodyText = """
            Hi Shashank,

            I would like to add a new retro playlist website to Gaane:

            📻 Website / Playlist Name: $siteName
            🔗 Website URL: $siteUrl
            👤 Creator / Handle: $ownerName
            🏷️ Category: $category

            Thanks!
        """.trimIndent()

        val body = Uri.encode(bodyText)
        val mailUri = Uri.parse("mailto:shashannxd@gmail.com?subject=$subject&body=$body")

        val intent = Intent(Intent.ACTION_SENDTO, mailUri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback for devices without a default mail client
            val genericIntent = Intent(Intent.ACTION_VIEW, mailUri)
            context.startActivity(genericIntent)
        }

        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(enabled = false) {} // Prevent click-through
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "Add New Playlist 📻",
                        fontFamily = YatraOne,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Submit your custom retro web playlist to Gaane.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // 1. Website / Playlist Name
            OutlinedTextField(
                value = siteName,
                onValueChange = { siteName = it },
                label = { Text("1. Website / Playlist Name *") },
                placeholder = { Text("e.g. Cutting Shop") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // 2. Website URL
            OutlinedTextField(
                value = siteUrl,
                onValueChange = { siteUrl = it },
                label = { Text("2. Website URL *") },
                placeholder = { Text("e.g. https://cuttingshop.lol") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // 3. Owner Name / Handle
            OutlinedTextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = { Text("3. Owner Name / Handle *") },
                placeholder = { Text("e.g. @chakra5027") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // 4. Category Dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("4. Playlist Category *") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                ) {
                    CATEGORIES.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Submit Button
            val canSubmit = siteName.isNotBlank() && siteUrl.isNotBlank() && ownerName.isNotBlank()

            Button(
                onClick = { sendEmailSubmission() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = Lamp),
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Send to shashannxd@gmail.com",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
            }
        }
    }
}
