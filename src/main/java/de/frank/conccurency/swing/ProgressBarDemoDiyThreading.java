package de.frank.conccurency.swing;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProgressBarDemoDiyThreading {
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    /**
     * Create the GUI and show it. As with all GUI code, this must run
     * on the event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("ProgressBarDemo");
        frame.setPreferredSize(new Dimension(800, 600));
        //ensure System.exit is called an all threads are shutdown (even non-deamon threads)
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new ProgressBarDemoPanel();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static class ProgressBarDemoPanel extends JPanel {
        private final JProgressBar progressBar;
        private final JButton startButton;
        private final JButton stopButton;
        private final JTextArea taskOutput;

        public ProgressBarDemoPanel() {
            super(new BorderLayout());

            //Create the demo's UI.
            startButton = new JButton("Start");
            startButton.setActionCommand("start");
            stopButton = new JButton("stop");
            stopButton.setActionCommand("stop");
            stopButton.setEnabled(false);

            progressBar = new JProgressBar(0, 100);
            progressBar.setValue(0);

            //Call setStringPainted now so that the progress bar height
            //stays the same whether or not the string is shown.
            progressBar.setStringPainted(true);

            taskOutput = new JTextArea(5, 20);
            taskOutput.setMargin(new Insets(5, 5, 5, 5));
            taskOutput.setEditable(false);

            JPanel panel = new JPanel();
            panel.add(startButton);
            panel.add(stopButton);
            panel.add(progressBar);

            add(panel, BorderLayout.PAGE_START);
            add(new JScrollPane(taskOutput), BorderLayout.CENTER);
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            startButton.addActionListener(this::startNewBackgroundTask);
        }

        private void startNewBackgroundTask(ActionEvent event) {

            // We need to make sure we dont produces memory leaks by holding references to old tasks within some listener
            MyLongRunningBackgroundTaskWithProgress longRunningBackgroundTask
                    = new MyLongRunningBackgroundTaskWithProgress(startButton, stopButton, taskOutput, progressBar);

            stopButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //Danger of  memory leak! (keeping reference to past  longRunningBackgroundTask's)
                    // be sure to remove the action listener once we are done
                    longRunningBackgroundTask.cancel();
                    taskOutput.append("Trying to cancel long longRunningBackgroundTask ...");
                    stopButton.removeActionListener(this);
                }
            });

            //start the tasks -  this will automatically enqueue it in Swings own background processing thread pool and coordinate with EDT
            longRunningBackgroundTask.start();
        }
    }


    static class MyLongRunningBackgroundTaskWithProgress extends Thread {

        private final JButton startButton;
        private final JButton stopButton;
        private final JTextArea taskOutput;
        private final JProgressBar progressBar;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);

        public MyLongRunningBackgroundTaskWithProgress(JButton startButton, JButton stopButton, JTextArea taskOutput, JProgressBar progressBar) {
            this.startButton = startButton;
            this.stopButton = stopButton;
            this.taskOutput = taskOutput;
            this.progressBar = progressBar;
            this.setName("MyLongRunningBackgroundTaskWithProgress");
            this.setDaemon(true); //make sure this thread does not block application exit!
        }

        /*
         * Main task. Executed in background thread.
         */
        @Override
        public void run() {
            try {
                initializeProgress();

                List<String> results = runLongRunningTask();

                if (isCancelled()) {
                    SwingUtilities.invokeLater(() -> {
                        taskOutput.append("MyLongRunningBackgroundTask aborted - no results:\n");
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        taskOutput.append("MyLongRunningBackgroundTask results:\n");
                        taskOutput.append(String.join("\n  ", results));
                    });
                }
            } finally {
                //reset ui
                SwingUtilities.invokeLater(() -> {
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(0);
                });
            }
        }

        public List<String> runLongRunningTask() {

            //Sleep for at least one second to simulate "startup".
            simulateDelay(1000L);
            List<String> results = new ArrayList<>();
            int taskNo = 0;
            int progress = 0;
            while (progress < 100 && !isCancelled()) {
                //simulate a background task step with (optional) intermediate results
                String taskResult = simulateLongRunningTask(++taskNo, 1000L);
                results.add(taskResult);
                publish(taskResult);//(optional) publish intermediate processing results

                //Make random progress.
                progress += ThreadLocalRandom.current().nextInt(10, 20);
                setProgress(Math.min(progress, 100));
            }

            return results;
        }

        private void initializeProgress() {
            //Initialize progress property.
            SwingUtilities.invokeLater(() -> {
                taskOutput.append("MyLongRunningBackgroundTask initializing...\n");
                progressBar.setIndeterminate(true);
                progressBar.setValue(0);
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            });
        }


        private void publish(String taskResult) {
            SwingUtilities.invokeLater(() -> {
                taskOutput.append("Intermediate results form publish: " + taskResult);
            });
        }

        private void setProgress(int progress) {
            SwingUtilities.invokeLater(() -> {
                progressBar.setIndeterminate(false);
                progressBar.setValue(progress);
                taskOutput.append(String.format("Completed %d%% of task.\n", progress));
            });
        }

        private boolean isCancelled() {
            return cancelled.get();
        }

        public void cancel() {
            cancelled.set(true);
        }

        private String simulateLongRunningTask(int taskNumber, long l) {
            simulateDelay(1000L);
            return "Task_" + taskNumber + "_Mock_Result";
        }

        private static void simulateDelay(long time) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException ignore) {
            }
        }

    }
}