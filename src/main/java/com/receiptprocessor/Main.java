package com.receiptprocessor;

import com.receiptprocessor.ui.MainWindow;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Launch GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
