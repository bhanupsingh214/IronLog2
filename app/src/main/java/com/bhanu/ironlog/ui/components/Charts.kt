package com.bhanu.ironlog.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bhanu.ironlog.data.local.pojo.ExerciseStrengthHistory
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StrengthProgressionChart(
    history: List<ExerciseStrengthHistory>,
    isE1RM: Boolean,
    modifier: Modifier = Modifier
) {
    ProvideChartStyle(m3ChartStyle()) {
        if (history.size < 2) {
            Box(modifier.height(200.dp), contentAlignment = Alignment.Center) {
                Text("Train more to unlock your progress graph", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        } else {
            val entries = history.mapIndexed { index, item ->
                val value = if (isE1RM) item.maxE1RM else item.maxWeight
                entryOf(index.toFloat(), value.toFloat())
            }
            
            val model = entryModelOf(entries)
            
            Chart(
                chart = lineChart(),
                model = model,
                startAxis = rememberStartAxis(
                    valueFormatter = { value, _ -> String.format(Locale.getDefault(), "%,.0f", value) }
                ),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = { value, _ ->
                        val index = value.toInt()
                        if (index in history.indices) {
                            SimpleDateFormat("dd/MM", Locale("en", "IN")).format(Date(history[index].date))
                        } else ""
                    }
                ),
                chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = true),
                isZoomEnabled = true,
                modifier = modifier.height(200.dp).fillMaxWidth()
            )
        }
    }
}
