package com.example.ui.screens.directory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.BusinessEntity
import com.example.data.remote.GeminiService
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch

@Composable
fun AddEditBusinessDialog(
    existingBusiness: BusinessEntity?,
    onDismiss: () -> Unit,
    onSave: (BusinessEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(existingBusiness?.name.orEmpty()) }
    var industry by remember { mutableStateOf(existingBusiness?.industry.orEmpty()) }
    var countryCity by remember { mutableStateOf(existingBusiness?.countryCity.orEmpty()) }
    var phone by remember { mutableStateOf(existingBusiness?.phone.orEmpty()) }
    var email by remember { mutableStateOf(existingBusiness?.email.orEmpty()) }
    var website by remember { mutableStateOf(existingBusiness?.website.orEmpty()) }
    var whyNeed by remember { mutableStateOf(existingBusiness?.whyNeedVoiceBot.orEmpty()) }
    var missedRevStr by remember { mutableStateOf(existingBusiness?.estMonthlyMissedRev?.toInt()?.toString() ?: "5000") }
    var hours by remember { mutableStateOf(existingBusiness?.customHours ?: "Mon-Fri: 8:00 AM - 6:00 PM, Sat: 9:00 AM - 3:00 PM") }
    var services by remember { mutableStateOf(existingBusiness?.servicesList.orEmpty()) }
    var pricing by remember { mutableStateOf(existingBusiness?.pricingGuide.orEmpty()) }
    var personaName by remember { mutableStateOf(existingBusiness?.botPersonaName ?: "Alex (AI Receptionist)") }
    var systemPrompt by remember { mutableStateOf(existingBusiness?.systemPrompt.orEmpty()) }
    var voiceSpeed by remember { mutableFloatStateOf(existingBusiness?.botVoiceSpeed ?: 1.0f) }
    var voicePitch by remember { mutableFloatStateOf(existingBusiness?.botVoicePitch ?: 1.0f) }

    var isGeneratingAi by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val geminiService = remember { GeminiService() }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingBusiness != null) "Edit Business & Voice Bot" else "Add New Business Listing",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (existingBusiness != null && onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_business_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Business",
                            tint = ErrorRose
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Business Profile
                Text(
                    text = "BUSINESS INFORMATION",
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryTeal,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Business Name *") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_biz_name"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = industry,
                        onValueChange = { industry = it },
                        label = { Text("Industry * (e.g. Dental, HVAC)") },
                        modifier = Modifier.weight(1f).testTag("input_biz_industry"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = countryCity,
                        onValueChange = { countryCity = it },
                        label = { Text("City / Country *") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier.weight(1f).testTag("input_biz_location"),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.weight(1f).testTag("input_biz_phone"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.weight(1f).testTag("input_biz_email"),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("Website (e.g. www.smiledental.example)") },
                    leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_biz_website"),
                    singleLine = true
                )

                // Section 2: Opportunity & Missed Revenue
                Text(
                    text = "PAIN POINTS & REVENUE ANALYSIS",
                    style = MaterialTheme.typography.labelMedium,
                    color = WarningAmber,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )

                OutlinedTextField(
                    value = whyNeed,
                    onValueChange = { whyNeed = it },
                    label = { Text("Why They Need Voice AI (e.g. Misses calls after hours)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_biz_why_need"),
                    minLines = 2
                )

                OutlinedTextField(
                    value = missedRevStr,
                    onValueChange = { missedRevStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Est. Monthly Missed Revenue ($)") },
                    leadingIcon = { Icon(Icons.Default.MonetizationOn, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_biz_missed_rev"),
                    singleLine = true
                )

                // Section 3: Services, Pricing & Hours
                Text(
                    text = "OPERATING INFO & KNOWLEDGE BASE",
                    style = MaterialTheme.typography.labelMedium,
                    color = SecondaryCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )

                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Operating Hours") },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = services,
                    onValueChange = { services = it },
                    label = { Text("Services Offered (e.g. Emergency Repairs, Cleanings, Consultation)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = pricing,
                    onValueChange = { pricing = it },
                    label = { Text("Pricing Guide (e.g. Consultation: Free, Standard Service: $120)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Section 4: Voice Bot Persona & AI Config
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AI VOICE AGENT CONFIGURATION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )

                            // AI Auto-Generate Button
                            Button(
                                onClick = {
                                    if (name.isNotBlank() && industry.isNotBlank()) {
                                        isGeneratingAi = true
                                        coroutineScope.launch {
                                            val (genPersona, genPrompt) = geminiService.generateCustomAgentPersona(
                                                name, industry, services, pricing, countryCity
                                            )
                                            personaName = genPersona
                                            systemPrompt = genPrompt
                                            isGeneratingAi = false
                                        }
                                    }
                                },
                                enabled = !isGeneratingAi && name.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("ai_generate_prompt_button")
                            ) {
                                if (isGeneratingAi) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI Auto-Tune", fontSize = 12.sp)
                            }
                        }

                        OutlinedTextField(
                            value = personaName,
                            onValueChange = { personaName = it },
                            label = { Text("Bot Persona Name") },
                            leadingIcon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = systemPrompt,
                            onValueChange = { systemPrompt = it },
                            label = { Text("System Prompt & Behavior Instructions") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        // Voice pitch & speed sliders
                        Text(
                            text = "Voice Pitch: %.1f".format(voicePitch),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Slider(
                            value = voicePitch,
                            onValueChange = { voicePitch = it },
                            valueRange = 0.7f..1.4f
                        )

                        Text(
                            text = "Speech Speed: %.1f".format(voiceSpeed),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Slider(
                            value = voiceSpeed,
                            onValueChange = { voiceSpeed = it },
                            valueRange = 0.7f..1.4f
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && industry.isNotBlank()) {
                        val parsedRev = missedRevStr.toDoubleOrNull() ?: 5000.0
                        val finalPrompt = if (systemPrompt.isNotBlank()) systemPrompt else {
                            "You are $personaName, the AI receptionist for $name ($industry in $countryCity). Speak concisely (1-2 sentences), warmly, and help with services ($services), pricing ($pricing), and appointment scheduling."
                        }
                        val businessToSave = existingBusiness?.copy(
                            name = name.trim(),
                            industry = industry.trim(),
                            countryCity = countryCity.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            website = website.trim(),
                            whyNeedVoiceBot = whyNeed.trim(),
                            estMonthlyMissedRev = parsedRev,
                            customHours = hours.trim(),
                            servicesList = services.trim(),
                            pricingGuide = pricing.trim(),
                            botPersonaName = personaName.trim(),
                            botVoicePitch = voicePitch,
                            botVoiceSpeed = voiceSpeed,
                            systemPrompt = finalPrompt
                        ) ?: BusinessEntity(
                            name = name.trim(),
                            industry = industry.trim(),
                            countryCity = countryCity.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            website = website.trim(),
                            whyNeedVoiceBot = whyNeed.trim(),
                            estMonthlyMissedRev = parsedRev,
                            customHours = hours.trim(),
                            servicesList = services.trim(),
                            pricingGuide = pricing.trim(),
                            botPersonaName = personaName.trim(),
                            botVoicePitch = voicePitch,
                            botVoiceSpeed = voiceSpeed,
                            systemPrompt = finalPrompt,
                            demoCode = "DEMO-%04d".format((1000..9999).random())
                        )
                        onSave(businessToSave)
                    }
                },
                enabled = name.isNotBlank() && industry.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                modifier = Modifier.testTag("save_business_button")
            ) {
                Text("Save Business", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
