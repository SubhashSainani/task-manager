package com.example.taskmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.adapter.TaskAdapter;
import com.example.taskmanager.database.TaskRepository;
import com.example.taskmanager.model.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {
    private static final int REQUEST_ADD_TASK = 1;
    private static final int REQUEST_EDIT_TASK = 2;
    private static final int REQUEST_VIEW_TASK = 3;

    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private TaskRepository taskRepository;
    private DrawerLayout drawerLayout;

    // Track the current filter mode
    private static final int FILTER_ALL = 0;
    private static final int FILTER_COMPLETED = 1;
    private static final int FILTER_PENDING = 2;
    private int currentFilter = FILTER_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        setupNavigationView(navigationView);


        taskRepository = new TaskRepository(this);


        recyclerViewTasks = findViewById(R.id.recycler_view_tasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));


        loadTasks();


        FloatingActionButton fab = findViewById(R.id.fab_add_task);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddEditTaskActivity.class);
            startActivityForResult(intent, REQUEST_ADD_TASK);
        });
    }

    private void setupNavigationView(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(item -> {
            // Handle navigation view item clicks here
            int id = item.getItemId();
            if (id == R.id.nav_all_tasks) {
                currentFilter = FILTER_ALL;
                setTitle("All Tasks");
                loadTasks();
                Toast.makeText(this, "All Tasks", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_completed_tasks) {
                currentFilter = FILTER_COMPLETED;
                setTitle("Completed Tasks");
                loadTasks();
                Toast.makeText(this, "Completed Tasks", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_pending_tasks) {
                currentFilter = FILTER_PENDING;
                setTitle("Pending Tasks");
                loadTasks();
                Toast.makeText(this, "Pending Tasks", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_settings) {

                showDeleteAllTasksDialog();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void showDeleteAllTasksDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete All Tasks");
        builder.setMessage("Are you sure you want to delete ALL tasks? This action cannot be undone.");

        // Delete button
        builder.setPositiveButton("Delete All", (dialog, which) -> {
            // Delete all tasks from database
            int deletedCount = taskRepository.deleteAllTasks();

            // Show feedback
            if (deletedCount > 0) {
                Toast.makeText(this,
                        deletedCount + " tasks deleted successfully",
                        Toast.LENGTH_SHORT).show();

                // Refresh the list
                loadTasks();
            } else {
                Toast.makeText(this,
                        "No tasks to delete",
                        Toast.LENGTH_SHORT).show();
            }
        });


        builder.setNegativeButton("Go Back", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }

    private void loadTasks() {
        List<Task> allTasks = taskRepository.getAllTasks();
        List<Task> filteredTasks;

        switch (currentFilter) {
            case FILTER_COMPLETED:
                filteredTasks = new ArrayList<>();
                for (Task task : allTasks) {
                    if (task.isCompleted()) {
                        filteredTasks.add(task);
                    }
                }
                break;
            case FILTER_PENDING:
                filteredTasks = new ArrayList<>();
                for (Task task : allTasks) {
                    if (!task.isCompleted()) {
                        filteredTasks.add(task);
                    }
                }
                break;
            case FILTER_ALL:
            default:
                filteredTasks = allTasks;
                break;
        }

        if (taskAdapter == null) {
            taskAdapter = new TaskAdapter(this, filteredTasks, this);
            recyclerViewTasks.setAdapter(taskAdapter);
        } else {
            taskAdapter.updateTasks(filteredTasks);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadTasks();
            if (requestCode == REQUEST_ADD_TASK) {
                Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show();
            } else if (requestCode == REQUEST_EDIT_TASK) {
                Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onTaskClick(Task task) {
        Intent intent = new Intent(this, TaskDetailsActivity.class);
        intent.putExtra("TASK_ID", task.getId());
        startActivityForResult(intent, REQUEST_VIEW_TASK);
    }

    @Override
    public void onTaskCompleted(Task task, boolean isCompleted) {

        task.setCompleted(isCompleted);
        taskRepository.updateTask(task);


        String message = isCompleted ? "Task marked as completed" : "Task marked as incomplete";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        if (currentFilter != FILTER_ALL) {
            loadTasks();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            loadTasks();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}