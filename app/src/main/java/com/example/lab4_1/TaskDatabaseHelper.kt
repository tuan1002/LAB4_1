package com.example.lab4_1

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.Cursor
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class TaskDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "tasks.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_TASKS = "tasks"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Tạo bảng khi cơ sở dữ liệu được tạo lần đầu
        val createTableQuery = """
            CREATE TABLE $TABLE_TASKS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                description TEXT
            )
        """
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Xóa bảng cũ nếu nó tồn tại và tạo lại bảng mới
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        onCreate(db)
    }

    // Thêm một công việc mới
    fun addTask(task: Task) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", task.name)
            put("description", task.description)
        }
        db.insert(TABLE_TASKS, null, values)
        db.close()
    }

    // Lấy tất cả công việc
    fun getAllTasks(): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = readableDatabase
        val cursor: Cursor = db.query(TABLE_TASKS, null, null, null, null, null, null)

        if (cursor.moveToFirst()) {
            do {
                val idIndex = cursor.getColumnIndex("id")
                val nameIndex = cursor.getColumnIndex("name")
                val descriptionIndex = cursor.getColumnIndex("description")

                // Kiểm tra nếu các chỉ số không âm
                if (idIndex != -1 && nameIndex != -1 && descriptionIndex != -1) {
                    val id = cursor.getInt(idIndex)
                    val name = cursor.getString(nameIndex)
                    val description = cursor.getString(descriptionIndex)
                    tasks.add(Task(id, name, description))
                } else {
                    // Xử lý trường hợp cột không tồn tại
                    println("Một hoặc nhiều cột không tồn tại trong cursor.")
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return tasks
    }


    // Cập nhật một công việc
    fun updateTask(task: Task) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", task.name)
            put("description", task.description)
        }
        db.update(TABLE_TASKS, values, "id = ?", arrayOf(task.id.toString()))
        db.close()
    }

    // Xóa một công việc
    fun deleteTask(taskId: Int) {
        val db = writableDatabase
        db.delete(TABLE_TASKS, "id = ?", arrayOf(taskId.toString()))
        db.close()
    }

    // Hàm sao chép cơ sở dữ liệu từ assets
    fun copyDatabase() {
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        if (!dbFile.exists()) {
            val input: InputStream = context.assets.open(DATABASE_NAME)
            val output: FileOutputStream = FileOutputStream(dbFile)
            val buffer = ByteArray(1024)
            var length: Int
            try {
                while (input.read(buffer).also { length = it } > 0) {
                    output.write(buffer, 0, length)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                output.flush()
                output.close()
                input.close()
            }
        }
    }
}
