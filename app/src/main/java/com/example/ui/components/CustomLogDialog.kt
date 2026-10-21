package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

data class BeverageOption(val name: String, val icon: ImageVector)

val beverageOptions = listOf(
    BeverageOption("Water", Icons.Default.WaterDrop),
    BeverageOption("Tea", Icons.Default.EmojiFoodBeverage),
    BeverageOption("Coffee", Icons.Default.Coffee),
    BeverageOption("Juice", Icons.Default.LocalDrink),
    BeverageOption("Electrolyte", Icons.Default.WaterDrop),
    BeverageOption("Smoothie", Icons.Default.LocalDrink)
)

val containerOptions = listOf("Cup (150ml)", "Glass (250ml)", "Mug (350ml)", "Bottle (500ml)", "Flask (750ml)")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomLogDialog(
    onDismiss: () -> Unit,
    onConfirm: (amountMl: Long, beverageType: String, container: String) -> Unit
) {
    var selectedBeverage by remember { mutableStateOf("Water") }
    var selectedContainer by remember { mutableStateOf("Glass") }
    var amountSlider by remember { mutableFloatStateOf(250f) }
    var amountText by remember { mutableStateOf("250") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Drink Intake",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Beverage Type",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    beverageOptions.forEach { beverage ->
                        val selected = beverage.name == selectedBeverage
                        FilterChip(
                            selected = selected,
                            onClick = { selectedBeverage = beverage.name },
                            label = { Text(beverage.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = beverage.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Volume Amount",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() }
                            amountText = clean
                            val num = clean.toFloatOrNull() ?: 0f
                            amountSlider = num.coerceIn(50f, 2000f)
                        },
                        label = { Text("Volume (ml)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Slider(
                    value = amountSlider,
                    onValueChange = {
                        amountSlider = it
                        amountText = it.toInt().toString()
                    },
                    valueRange = 50f..1500f,
                    steps = 28,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Container Preset",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    containerOptions.forEach { container ->
                        val nameOnly = container.substringBefore(" (")
                        val selected = selectedContainer == nameOnly
                        FilterChip(
                            selected = selected,
                            onClick = {
                                selectedContainer = nameOnly
                                when (nameOnly) {
                                    "Cup" -> { amountSlider = 150f; amountText = "150" }
                                    "Glass" -> { amountSlider = 250f; amountText = "250" }
                                    "Mug" -> { amountSlider = 350f; amountText = "350" }
                                    "Bottle" -> { amountSlider = 500f; amountText = "500" }
                                    "Flask" -> { amountSlider = 750f; amountText = "750" }
                                }
                            },
                            label = { Text(container) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalAmount = amountText.toLongOrNull() ?: amountSlider.toLong()
                    if (finalAmount > 0) {
                        onConfirm(finalAmount, selectedBeverage, selectedContainer)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("confirm_custom_log_button")
            ) {
                Text("Log Intake")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
