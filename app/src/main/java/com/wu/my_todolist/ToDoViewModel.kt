package com.wu.my_todolist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ToDoViewModel(application: Application, private val toDoDao: ToDoDao) : AndroidViewModel(application) {

    // StateFlow holding the list of all tasks, updated automatically from the database
    val allToDos: StateFlow<List<ToDo>> = toDoDao.getAllToDos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun updateWidget() {
        viewModelScope.launch {
            ToDoWidget().updateAll(getApplication())
        }
    }

    fun addToDo(title: String, deadline: Long? = null) {
        viewModelScope.launch {
            val newToDo = ToDo(title = title, deadline = deadline)
            toDoDao.insert(newToDo)
            updateWidget()
        }
    }

    fun updateToDo(todo: ToDo) {
        viewModelScope.launch {
            toDoDao.update(todo)
            updateWidget()
        }
    }

    fun toggleToDoCompletion(todo: ToDo) {
        viewModelScope.launch {
            val updatedToDo = todo.copy(isCompleted = !todo.isCompleted)
            toDoDao.update(updatedToDo)
            updateWidget()
        }
    }

    fun deleteToDo(todo: ToDo) {
        viewModelScope.launch {
            toDoDao.delete(todo)
            updateWidget()
        }
    }
}
