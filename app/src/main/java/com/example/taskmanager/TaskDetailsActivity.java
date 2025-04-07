package com.example.taskmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.taskmanager.database.TaskRepository;
import com.example.taskmanager.model.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskDetailsActivity extends AppCompatActivity {
    private static final int REQUEST_EDIT_TASK = 1;

    private TextView textViewTitle;
    private TextView textViewDescription;
    private TextView textViewDueDate;
    private CheckBox checkBoxCompleted;

    private TaskRepository taskRepository;
    private Task task;
    private long taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        textViewTitle = findViewById(R.id.text_view_title);
        textViewDescription = findViewById(R.id.text_view_description);
        textViewDueDate = findViewById(R.id.text_view_due_date);
        checkBoxCompleted = findViewById(R.id.checkbox_completed);

        taskRepository = new TaskRepository(this);

        taskId = getIntent().getLongExtra("TASK_ID", -1);
        if (taskId == -1) {
            Toast.makeText(this, "Error: Task not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load and display task
        loadTask();

        checkBoxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (task != null) {
                task.setCompleted(isChecked);
                taskRepository.updateTask(task);

                String message = isChecked ? "Task marked as completed" : "Task marked as incomplete";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTask() {
        task = taskRepository.getTaskById(taskId);
        if (task != null) {
            textViewTitle.setText(task.getTitle());
            textViewDescription.setText(task.getDescription());

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(new Date(task.getDueDate()));
            textViewDueDate.setText(formattedDate);

            checkBoxCompleted.setChecked(task.isCompleted());
        } else {
            Toast.makeText(this, "Error: Task not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_TASK && resultCode == RESULT_OK) {
            loadTask();
            Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_edit) {
            Intent intent = new Intent(this, AddEditTaskActivity.class);
            intent.putExtra("TASK_ID", taskId);
            startActivityForResult(intent, REQUEST_EDIT_TASK);
            return true;
        } else if (id == R.id.action_delete) {
            showDeleteConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Task");
        builder.setMessage("Are you sure you want to delete this task?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            deleteTask();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteTask() {
        int deletedRows = taskRepository.deleteTask(taskId);
        if (deletedRows > 0) {
            Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Error deleting task", Toast.LENGTH_SHORT).show();
        }
    }
}