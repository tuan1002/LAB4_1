package com.example.lab4_1

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: TaskDatabaseHelper
    private lateinit var taskListView: ListView
    private lateinit var taskAdapter: ArrayAdapter<Task>
    private lateinit var addTaskButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = TaskDatabaseHelper(this)
        dbHelper.copyDatabase() // Sao chép cơ sở dữ liệu từ assets

        taskListView = findViewById(R.id.taskListView)
        addTaskButton = findViewById(R.id.addTaskButton)

        updateTaskList()

        addTaskButton.setOnClickListener {
            showTaskDialog(null)
        }

        taskListView.setOnItemClickListener { _, _, position, _ ->
            val selectedTask = taskAdapter.getItem(position)
            showTaskDialog(selectedTask)
        }
    }

    private fun updateTaskList() {
        val tasks = dbHelper.getAllTasks() // Lấy danh sách công việc
        taskAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tasks)
        taskListView.adapter = taskAdapter
    }

    private fun showTaskDialog(task: Task?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_task, null)
        val taskNameEditText = dialogView.findViewById<EditText>(R.id.taskNameEditText)
        val taskDescriptionEditText = dialogView.findViewById<EditText>(R.id.taskDescriptionEditText)
        val saveTaskButton = dialogView.findViewById<Button>(R.id.saveTaskButton)
        val deleteTaskButton = dialogView.findViewById<Button>(R.id.deleteTaskButton)

        // Tạo đối tượng AlertDialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle(if (task == null) "Thêm Công Việc" else "Chỉnh Sửa Công Việc")
            .create()

        if (task != null) {
            taskNameEditText.setText(task.name)
            taskDescriptionEditText.setText(task.description)

            deleteTaskButton.visibility = View.VISIBLE
            deleteTaskButton.setOnClickListener {
                dbHelper.deleteTask(task.id)
                dialog.dismiss() // Gọi dismiss ở đây
                updateTaskList()
            }
        } else {
            deleteTaskButton.visibility = View.GONE
        }

        saveTaskButton.setOnClickListener {
            val name = taskNameEditText.text.toString()
            val description = taskDescriptionEditText.text.toString()

            if (task == null) {
                // Thêm công việc mới
                dbHelper.addTask(Task(0, name, description))
            } else {
                // Cập nhật công việc hiện có
                dbHelper.updateTask(Task(task.id, name, description))
            }

            dialog.dismiss() // Gọi dismiss trên đối tượng dialog
            updateTaskList()
        }

        dialog.show() // Hiển thị dialog
    }
}
