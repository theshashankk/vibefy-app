package com.vibefy.musicwtf.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vibefy.musicwtf.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    viewModel: AboutViewModel = hiltViewModel(),
) {
    val config by viewModel.config.collectAsState()
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "About",
                        fontFamily = YatraOne,
                        fontSize = 34.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            // ── Dynamic Remote Announcement Banner (OTA updateable) ──
            if (!config.announcementMessage.isNullOrBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Amber.copy(alpha = 0.18f)),
                    shape = RoundedCornerShape(14.dp),
                    onClick = {
                        config.announcementLink?.let { uriHandler.openUri(it) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = Amber)
                        Text(
                            text = config.announcementMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            // App Branding Card (Remote Dynamic Text)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    config.aboutTitle,
                    fontFamily = YatraOne,
                    fontSize = 36.sp,
                    color = Lamp,
                )
                Text(
                    config.aboutVersion,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    config.aboutDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Features list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("App Philosophy", fontFamily = YatraOne, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Radio, contentDescription = null, tint = Lamp, modifier = Modifier.size(20.dp))
                    Column {
                        Text("68 Curated Vibe-Sites", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("Explore retro bus dashboards, barbershop radios & monsoon stages.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = PowerGreen, modifier = Modifier.size(20.dp))
                    Column {
                        Text("Zero Dependencies / Mirror Archiver", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("Original sites are preserved with mirror archiving so dead links never stop the music.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }

            // Dynamic Repository Link Button (OTA updateable)
            OutlinedButton(
                onClick = { uriHandler.openUri(config.githubUrl) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Code, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Open Source on GitHub", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}
