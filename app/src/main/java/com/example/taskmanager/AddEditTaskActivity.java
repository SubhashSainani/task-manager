package com.example.taskmanager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.taskmanager.database.TaskRepository;
import com.example.taskmanager.model.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditTaskActivity extends AppCompatActivity {
    private EditText editTextTitle;
    private EditText editTextDescription;
    private TextView textViewDueDate;
    private Button buttonSelectDate;
    private Button buttonSaveTask;

    private TaskRepository taskRepository;
    private Task existingTask = null;
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        textViewDueDate = findViewById(R.id.text_view_due_date);
        buttonSelectDate = findViewById(R.id.button_select_date);
        buttonSaveTask = findViewById(R.id.button_save_task);


        taskRepository = new TaskRepository(this);


        long taskId = getIntent().getLongExtra("TASK_ID", -1);
        if (taskId != -1) {
            setTitle("Edit Task");
            existingTask = taskRepository.getTaskById(taskId);
            if (existingTask != null) {
                populateFields(existingTask);
            }
        } else {
            setTitle("Add New Task");
            updateDateDisplay(); // Display current date as default
        }


        buttonSelectDate.setOnClickListener(v -> showDatePickerDialog());


        buttonSaveTask.setOnClickListener(v -> saveTask());
    }

    private void populateFields(Task task) {
        editTextTitle.setText(task.getTitle());
        editTextDescription.setText(task.getDescription());


        selectedDate.setTimeInMillis(task.getDueDate());
        updateDateDisplay();
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateDisplay();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        textViewDueDate.setText(dateFormat.format(selectedDate.getTime()));
    }

    private void saveTask() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        long dueDate = selectedDate.getTimeInMillis();

        // Validate input
        if (title.isEmpty()) {
            editTextTitle.setError("Title is required");
            editTextTitle.requestFocus();
            return;
        }

        if (existingTask != null) {
            // Update existing task
            existingTask.setTitle(title);
            existingTask.setDescription(description);
            existingTask.setDueDate(dueDate);
            taskRepository.updateTask(existingTask);
        } else {
            // Create new task
            Task newTask = new Task(title, description, dueDate);
            long newTaskId = taskRepository.insertTask(newTask);
            if (newTaskId == -1) {
                Toast.makeText(this, "Error saving task", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Return to main activity
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}