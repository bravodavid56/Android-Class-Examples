package com.sargent.mark.todolist.data;

/**
 * Created by mark on 7/4/17.
 */

public class ToDoItem {
    private String description;
    private String dueDate;
    private String category;
    private int done;

    /**
     * This model class was updated to reflect the two additional columns, 'category' and 'done'
     * The constructor was updated to include these, and the getters and setters were added
     * to include them as well.
     */

    public ToDoItem(String description, String dueDate, String category, int done) {
        this.description = description;
        this.dueDate = dueDate;
        this.category = category;
        this.done = done;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getCategory() { return category; }

    public void setCategory(String category) { this.category = category; }

    public int getDone() {
        return done;
    }

    public void setDone(int done) {
        this.done = done;
    }
}
