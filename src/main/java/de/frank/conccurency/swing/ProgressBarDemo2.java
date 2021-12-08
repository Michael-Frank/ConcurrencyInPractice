package de.frank.conccurency.swing;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

public class ProgressBarDemo2 {
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

            progressBar.setIndeterminate(true);
            startButton.setEnabled(false);

            //Instances of javax.swing.SwingWorker are not reusuable, so
            //we create new instances as needed.
            // However we need to make sure we dont produces memory leaks by holding references to old tasks within some listeners
            MyLongRunningBackgroundTaskWithProgress longRunningBackgroundTask
                    = new MyLongRunningBackgroundTaskWithProgress();
            longRunningBackgroundTask.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    //Invoked when task's progress property changes.
                    System.out.println("propertyChangeEvent:" + propertyChangeEvent.getPropertyName() + " v: " + propertyChangeEvent.getNewValue());
                    if ("state".equals(propertyChangeEvent.getPropertyName())) {
                        switch ((SwingWorker.StateValue) propertyChangeEvent.getNewValue()) {
                            case STARTED:
                                taskOutput.append("MyLongRunningBackgroundTask initializing...\n");
                                stopButton.setEnabled(true);
                                break;
                            case DONE:
                                taskOutput.append("MyLongRunningBackgroundTask finished\n");
                                Toolkit.getDefaultToolkit().beep();
                                try {
                                    List<String> results = longRunningBackgroundTask.get();
                                    taskOutput.append("MyLongRunningBackgroundTask results:\n");
                                    taskOutput.append(String.join("\n  ", results));
                                } catch (ExecutionException | InterruptedException | CancellationException e) {
                                    taskOutput.append("MyLongRunningBackgroundTask abborted - no results:\n");
                                }
                                startButton.setEnabled(true);
                                stopButton.setEnabled(false);
                                progressBar.setIndeterminate(false);
                                progressBar.setValue(0);
                                longRunningBackgroundTask.removePropertyChangeListener(this);
                                break;
                            case PENDING:
                                break;
                        }
                    } else if ("progress".equals(propertyChangeEvent.getPropertyName())) {
                        int progress = (Integer) propertyChangeEvent.getNewValue();
                        progressBar.setIndeterminate(false);
                        progressBar.setValue(progress);
                        taskOutput.append(String.format("Completed %d%% of task.\n", progress));
                    }
                }
            });

            stopButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //Danger of  memory leak! (keeping reference to past  longRunningBackgroundTask's)
                    // be sure to remove the action listner once we are done
                    longRunningBackgroundTask.cancel(true);
                    taskOutput.append("Trying to cancel long longRunningBackgroundTask ...");
                    longRunningBackgroundTask.addPropertyChangeListener((pce) -> {
                        System.out.println("registerting state done listener for stop buttion");
                        if ("state".equals(pce.getPropertyName())
                                && SwingWorker.StateValue.DONE == pce.getNewValue())
                            stopButton.removeActionListener(this);//it can take a while for the background thread to stop
                        System.out.println("Debug StopButton Listners: " + Arrays.toString(stopButton.getActionListeners()));
                    });
                }
            });

            //start the tasks -  this will automatically enqueue it in Swings own background processing thread pool and coordinate with EDT
            longRunningBackgroundTask.execute();
        }
    }


    static class MyLongRunningBackgroundTaskWithProgress extends SwingWorker<List<String>, String> {

        public MyLongRunningBackgroundTaskWithProgress() {
        }

        /*
         * Main task. Executed in background thread.
         */
        @Override
        public List<String> doInBackground() {
            List<String> results = new ArrayList<>();

            int progress = 0;
            //Initialize progress property.
            setProgress(0);
            //Sleep for at least one second to simulate "startup".

            simulateDelay(1000L);
            int taskNo = 0;
            while (progress < 100 && !isCancelled()) {
                //simulate a background task step with (optional) intermediate results
                String taskResult = simulateLongRunningTask(++taskNo, 1000L);
                results.add(taskResult);
                publish(taskResult);//(optional) publish intermediate processing results

                //Make random progress.
                progress += ThreadLocalRandom.current().nextInt(10, 20);
                setProgress(Math.min(progress, 100));
            }
            System.out.println("LongRunningBackgroundTask finished. Aborted?:" + isCancelled());
            return results;
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

        /**
         * Receives data chunks from the publish() method asynchronously on the Event Dispatch Thread.
         *
         * @param chunks
         */
        @Override
        protected void process(List<String> chunks) {
            //process
            // taskOutput.append("Intermediate results form publish ("+ chunks.size() + "): " + chunks+"\n");
        }

    }
}