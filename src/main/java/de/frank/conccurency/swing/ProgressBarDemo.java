package de.frank.conccurency.swing;


import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ProgressBarDemo {
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
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
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new ProgressBarDemoPanel();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static class ProgressBarDemoPanel extends JPanel implements PropertyChangeListener {
        private final JProgressBar progressBar;
        private final JButton startButton;
        private final JTextArea taskOutput;

        public ProgressBarDemoPanel() {
            super(new BorderLayout());

            //Create the demo's UI.
            startButton = new JButton("Start");
            startButton.setActionCommand("start");


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
            panel.add(progressBar);

            add(panel, BorderLayout.PAGE_START);
            add(new JScrollPane(taskOutput), BorderLayout.CENTER);
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            startButton.addActionListener((event) -> {
                progressBar.setIndeterminate(true);
                startButton.setEnabled(false);
                //Instances of javax.swing.SwingWorker are not reusuable, so
                //we create new instances as needed.
                MyLongRunningBackgroundTaskWithProgress longRunningBackgroundTask
                        = new MyLongRunningBackgroundTaskWithProgress(startButton, taskOutput);
                longRunningBackgroundTask.addPropertyChangeListener(this);
                longRunningBackgroundTask.execute();
            });
        }


        /**
         * Invoked when task's progress property changes.
         */
        public void propertyChange(PropertyChangeEvent evt) {
            if ("progress".equals(evt.getPropertyName())) {
                int progress = (Integer) evt.getNewValue();
                progressBar.setIndeterminate(false);
                progressBar.setValue(progress);
                taskOutput.append(String.format("Completed %d%% of task.\n", progress));
            }
        }
    }


    static class MyLongRunningBackgroundTaskWithProgress extends SwingWorker<List<String>, String> {
        private final JTextArea taskOutput;
        private final JButton startButton;

        public MyLongRunningBackgroundTaskWithProgress(JButton startButton, JTextArea taskOutput) {
            this.startButton = startButton;
            this.taskOutput = taskOutput;
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
            while (progress < 100) {
                //simulate a background task step with (optional) intermediate results
                String taskResult = simulateLongRunningTask(++taskNo, 1000L);
                results.add(taskResult);
                publish(taskResult);//(optional) publish intermediate processing results

                //Make random progress.
                progress += ThreadLocalRandom.current().nextInt(5, 10);
                setProgress(Math.min(progress, 100));
            }
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
            taskOutput.append("Intermediate results form publish (" + chunks.size() + "): " + chunks + "\n");
        }

        /*
         * Executed in event dispatch thread
         */
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            startButton.setEnabled(true);
            taskOutput.append("Done!\n");
        }
    }
}