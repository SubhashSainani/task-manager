package com.example.taskmanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.model.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;
    private OnTaskClickListener listener;
    private Context context;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskCompleted(Task task, boolean isCompleted);
    }

    public TaskAdapter(Context context, List<Task> taskList, OnTaskClickListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.tvTitle.setText(task.getTitle());

        // Format and set due date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(task.getDueDate()));
        holder.tvDueDate.setText(formattedDate);

        holder.checkBoxCompleted.setChecked(task.isCompleted());

        if (task.isCompleted()) {
            holder.tvTitle.setAlpha(0.5f);
            holder.tvDueDate.setAlpha(0.5f);
        } else {
            holder.tvTitle.setAlpha(1.0f);
            holder.tvDueDate.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateTasks(List<Task> tasks) {
        this.taskList = tasks;
        notifyDataSetChanged();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDueDate;
        CheckBox checkBoxCompleted;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            checkBoxCompleted = itemView.findViewById(R.id.checkbox_completed);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTaskClick(taskList.get(position));
                }
            });

            checkBoxCompleted.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Task task = taskList.get(position);
                    boolean isChecked = checkBoxCompleted.isChecked();
                    task.setCompleted(isChecked);
                    listener.onTaskCompleted(task, isChecked);
                }
            });
        }
    }
}
