package com.wu.my_todolist

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

class ToDoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = ToDoDatabase.getDatabase(context)
        val dao = database.toDoDao()
        val flow = dao.getAllToDos().map { list -> list.filter { !it.isCompleted } }

        provideContent {
            val todos by flow.collectAsState(initial = emptyList())

            GlanceTheme {
                ToDoWidgetContent(todos)
            }
        }
    }

    @Composable
    private fun ToDoWidgetContent(todos: List<ToDo>) {
        val context = LocalContext.current
        val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(Date()).uppercase()
        val dayNumber = SimpleDateFormat("dd", Locale.getDefault()).format(Date())
        val monthYear = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date()).uppercase()

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(2.dp)
                .appWidgetBackground()
                .background(GlanceTheme.colors.surface)
                .cornerRadius(12.dp)
                .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = dayName,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = GlanceTheme.colors.primary
                        )
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = dayNumber,
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlanceTheme.colors.onSurface
                            )
                        )
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        Text(
                            text = monthYear,
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = GlanceTheme.colors.onSurfaceVariant
                            ),
                            modifier = GlanceModifier.padding(bottom = 2.dp)
                        )
                    }
                }
                
                if (todos.isNotEmpty()) {
                    Box(
                        modifier = GlanceModifier
                            .background(GlanceTheme.colors.primaryContainer)
                            .cornerRadius(6.dp)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${todos.size} LEFT",
                            style = TextStyle(
                                color = GlanceTheme.colors.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }

            // Task Grid (2 Columns)
            if (todos.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks ✨",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                }
            } else {
                val chunks = todos.chunked(2)
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(chunks) { chunk ->
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Column 1
                            Box(modifier = GlanceModifier.defaultWeight()) {
                                ToDoWidgetItem(chunk[0])
                            }
                            
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            
                            // Column 2
                            if (chunk.size > 1) {
                                Box(modifier = GlanceModifier.defaultWeight()) {
                                    ToDoWidgetItem(chunk[1])
                                }
                            } else {
                                // Transparent spacer for balance
                                Spacer(modifier = GlanceModifier.defaultWeight())
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ToDoWidgetItem(todo: ToDo) {
        val deadlineInfo = getDeadlineInfo(todo.deadline)

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .width(2.5.dp)
                    .height(24.dp)
                    .background(
                        if (deadlineInfo?.isUrgent == true) GlanceTheme.colors.error 
                        else GlanceTheme.colors.primary
                    )
                    .cornerRadius(1.dp)
            ) {}
            
            Spacer(modifier = GlanceModifier.width(8.dp))
            
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = todo.title,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    maxLines = 1
                )
                if (deadlineInfo != null) {
                    Text(
                        text = deadlineInfo.text,
                        style = TextStyle(
                            color = if (deadlineInfo.isUrgent) GlanceTheme.colors.error else GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 10.sp,
                            fontWeight = if (deadlineInfo.isUrgent) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}

class ToDoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ToDoWidget()
}
