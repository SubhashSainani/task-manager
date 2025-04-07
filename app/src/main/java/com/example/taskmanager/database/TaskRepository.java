package com.example.taskmanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.taskmanager.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    private TaskDbHelper dbHelper;

    public TaskRepository(Context context) {
        dbHelper = new TaskDbHelper(context);
    }

    // Insert a new task into the database
    public long insertTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_TITLE, task.getTitle());
        values.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, task.getDescription());
        values.put(TaskContract.TaskEntry.COLUMN_DUE_DATE, task.getDueDate());
        values.put(TaskContract.TaskEntry.COLUMN_COMPLETED, task.isCompleted() ? 1 : 0);

        long newRowId = db.insert(TaskContract.TaskEntry.TABLE_NAME, null, values);
        db.close();
        return newRowId;
    }

    // Get all tasks from the database, sorted by due date
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                TaskContract.TaskEntry._ID,
                TaskContract.TaskEntry.COLUMN_TITLE,
                TaskContract.TaskEntry.COLUMN_DESCRIPTION,
                TaskContract.TaskEntry.COLUMN_DUE_DATE,
                TaskContract.TaskEntry.COLUMN_COMPLETED
        };

        String sortOrder = TaskContract.TaskEntry.COLUMN_DUE_DATE + " ASC";

        Cursor cursor = db.query(
                TaskContract.TaskEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry._ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_TITLE));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_DESCRIPTION));
            long dueDate = cursor.getLong(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_DUE_DATE));
            boolean completed = cursor.getInt(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_COMPLETED)) == 1;

            Task task = new Task(id, title, description, dueDate, completed);
            tasks.add(task);
        }
        cursor.close();
        db.close();
        return tasks;
    }

    // Get a specific task by ID
    public Task getTaskById(long taskId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                TaskContract.TaskEntry._ID,
                TaskContract.TaskEntry.COLUMN_TITLE,
                TaskContract.TaskEntry.COLUMN_DESCRIPTION,
                TaskContract.TaskEntry.COLUMN_DUE_DATE,
                TaskContract.TaskEntry.COLUMN_COMPLETED
        };

        String selection = TaskContract.TaskEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(taskId) };

        Cursor cursor = db.query(
                TaskContract.TaskEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        Task task = null;
        if (cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_TITLE));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_DESCRIPTION));
            long dueDate = cursor.getLong(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_DUE_DATE));
            boolean completed = cursor.getInt(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_COMPLETED)) == 1;

            task = new Task(taskId, title, description, dueDate, completed);
        }
        cursor.close();
        db.close();
        return task;
    }

    // Update an existing task
    public int updateTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_TITLE, task.getTitle());
        values.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, task.getDescription());
        values.put(TaskContract.TaskEntry.COLUMN_DUE_DATE, task.getDueDate());
        values.put(TaskContract.TaskEntry.COLUMN_COMPLETED, task.isCompleted() ? 1 : 0);

        String selection = TaskContract.TaskEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(task.getId()) };

        int count = db.update(
                TaskContract.TaskEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
        db.close();
        return count;
    }

    // Delete a task by ID
    public int deleteTask(long taskId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = TaskContract.TaskEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(taskId) };

        int deletedRows = db.delete(
                TaskContract.TaskEntry.TABLE_NAME,
                selection,
                selectionArgs
        );
        db.close();
        return deletedRows;
    }

    public int deleteAllTasks() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Delete all rows and return the number of rows deleted
        int deletedRows = db.delete(
                TaskContract.TaskEntry.TABLE_NAME,
                null,
                null
        );

        db.close();
        return deletedRows;
    }
}