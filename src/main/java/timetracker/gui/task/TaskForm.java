package timetracker.gui.task;

import timetracker.data.GlobalVariables;
import timetracker.data.Task;
import timetracker.data.TimeInterval;
import timetracker.gui.MainForm;

import javax.swing.*;

public class TaskForm extends JFrame {
    private JPanel panel1;
    private JList<Task> taskList;
    private JPanel taskViewWrap;
    private JButton createButton;
    private JButton deleteButton;
    private JButton backButton;
    private JLabel taskName;
    private JPanel taskView;
    private JList<TimeInterval> timeIntervalsList;
    private JLabel projectLabel;
    private JLabel tagsLabel;
    private JLabel durationLabel;
    private JLabel timeIntervalsLabel;
    private JScrollPane timeIntervalsScrollPane;
    private JButton startButton;
    private JButton stopButton;
    private JLabel stoppwatchLabel;
    private JPanel stoppwatchPanel;
    private JButton deleteIntervalButton;

    DefaultListModel<Task> listModel = new DefaultListModel<>();
    DefaultListModel<TimeInterval> timeIntervalsListModel = new DefaultListModel<>();
    private StopwatchController stoppWatchController;

    public TaskForm() {
        super("TimeTracker");

        taskList.setModel(listModel);
        timeIntervalsList.setModel(timeIntervalsListModel);

        updateTaskList();
        addListener();

        taskView.setVisible(false);

        this.setContentPane(panel1);
        this.setMinimumSize(new java.awt.Dimension(800, 600));
        this.setSize(1000, 1000);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void updateTaskList() {
        listModel.clear();
        GlobalVariables.ID_TO_TASK_MAP.forEach((k, v) -> listModel.addElement(v));
    }

    private void updateTimeIntervalList() {
        timeIntervalsListModel.clear();
        Task task = taskList.getSelectedValue();
        if (task != null) {
            task.getTimeIntervals().forEach(timeInterval -> {
                        if (timeInterval.getEndTime() != null) {
                            timeIntervalsListModel.addElement(timeInterval);
                        }
                    }
            );
        }
    }

    private void addListener() {
        // List selection listener
        taskList.getSelectionModel().addListSelectionListener(e -> {
            Task task = taskList.getSelectedValue();
            if (task != null) {
                updateTaskView(task);
            }
        });

        // Create button listener
        createButton.addActionListener(e -> {
            CreateTaskDialog createTaskDialog = new CreateTaskDialog();
            createTaskDialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    listModel.add(listModel.getSize(), createTaskDialog.getTask());
                }
            });
        });

        // Delete button listener
        deleteButton.addActionListener(e -> {
            Task task = taskList.getSelectedValue();
            if (task != null) {
                // Confirm delete
                int dialogResult = JOptionPane.showConfirmDialog(
                        null,
                        "Are you sure you want to delete this task?",
                        "Warning",
                        JOptionPane.YES_NO_OPTION);
                if (dialogResult != JOptionPane.YES_OPTION) {
                    return;
                }
                task.remove();
                task.removeDatabase();
                listModel.removeElement(task);
                taskView.setVisible(false);
            }
        });

        // Back button listener
        backButton.addActionListener(e -> {
            new MainForm();
            this.dispose();
        });

        // Start button listener
        startButton.addActionListener(e -> {
            Task task = taskList.getSelectedValue();
            if (task != null) {
                task.start();
                stoppWatchController = new StopwatchController(task, stoppwatchLabel);
                stoppWatchController.start();
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
        });

        // Stop button listener
        stopButton.addActionListener(e -> {
            Task task = taskList.getSelectedValue();
            if (task != null) {
                task.stop();
                stoppWatchController.end();
                updateTimeIntervalList();
                durationLabel.setText("Duration: " + String.format("%d:%02d:%02d", task.getDuration().getSeconds() / 3600, (task.getDuration().getSeconds() % 3600) / 60, (task.getDuration().getSeconds() % 60)));
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });

        // Delete interval button listener
        deleteIntervalButton.addActionListener(e -> {
            TimeInterval timeInterval = timeIntervalsList.getSelectedValue();
            if (timeInterval != null) {
                // Confirm delete
                int dialogResult = JOptionPane.showConfirmDialog(
                        null,
                        "Are you sure you want to delete this time interval?",
                        "Warning",
                        JOptionPane.YES_NO_OPTION);
                if (dialogResult != JOptionPane.YES_OPTION) {
                    return;
                }
                timeInterval.remove();
                updateTimeIntervalList();
            }
        });
    }

    private void updateTaskView(Task task) {
        long duration = task.getDuration().getSeconds();
        taskName.setText(task.getName());
        projectLabel.setText("Project: " + (task.getProject() == null ? "None" : task.getProject().getName()));
        tagsLabel.setText("Tags: " + task.getTagsToString());
        durationLabel.setText("Duration: " + String.format("%d:%02d:%02d", duration / 3600, (duration % 3600) / 60, (duration % 60)));
        timeIntervalsListModel.clear();
        stoppwatchLabel.setText("0:00:00");
        updateTimeIntervalList();

        // Stop stopwatch if it is running
        if (stoppWatchController != null && stoppWatchController.isAlive()) {
            stoppWatchController.end();
        }
        stoppWatchController = new StopwatchController(task, stoppwatchLabel);

        // Enable/disable buttons
        if (task.isRunning()) {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            stoppWatchController.start();
        } else {
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }

        taskView.setVisible(true);
    }

}