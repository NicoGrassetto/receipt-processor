package com.receiptprocessor.ui;

import com.receiptprocessor.ReceiptProcessor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class MainWindow extends JFrame {
    
    private final ReceiptProcessor processor;
    private File selectedDirectory;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    // UI Components
    private JLabel statusLabel;
    private JTable receiptsTable;
    private DefaultTableModel tableModel;
    private JButton viewDetailsButton;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public MainWindow() {
        this.processor = new ReceiptProcessor();
        initializeUI();
        loadReceipts();
    }

    private void initializeUI() {
        setTitle("Receipt Processor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton selectFolderButton = new JButton("Select Input Folder");
        selectFolderButton.addActionListener(e -> selectInputFolder());
        
        JButton processButton = new JButton("Process Receipts");
        processButton.addActionListener(e -> processReceipts());
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadReceipts());
        
        topPanel.add(selectFolderButton);
        topPanel.add(processButton);
        topPanel.add(refreshButton);

        // Table for receipts - dynamic columns
        String[] columnNames = {"ID", "Filename", "Date", "Preview"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        receiptsTable = new JTable(tableModel);
        receiptsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        receiptsTable.getColumnModel().getColumn(3).setPreferredWidth(300);
        
        JScrollPane scrollPane = new JScrollPane(receiptsTable);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        viewDetailsButton = new JButton("View Details");
        viewDetailsButton.setEnabled(false);
        viewDetailsButton.addActionListener(e -> viewSelectedReceipt());
        
        receiptsTable.getSelectionModel().addListSelectionListener(e -> {
            viewDetailsButton.setEnabled(receiptsTable.getSelectedRow() != -1);
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(viewDetailsButton);
        
        statusLabel = new JLabel("Ready");
        
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        // Add components to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void selectInputFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        if (selectedDirectory != null) {
            chooser.setCurrentDirectory(selectedDirectory);
        }
        
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedDirectory = chooser.getSelectedFile();
            statusLabel.setText("Selected: " + selectedDirectory.getAbsolutePath());
        }
    }

    private void processReceipts() {
        if (selectedDirectory == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select an input folder first.", 
                "No Folder Selected", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        statusLabel.setText("Processing receipts...");
        
        // Process in background thread
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return processor.processReceipts(selectedDirectory);
            }

            @Override
            protected void done() {
                try {
                    int count = get();
                    statusLabel.setText("Processed " + count + " new receipt(s)");
                    loadReceipts();
                    
                    if (count > 0) {
                        JOptionPane.showMessageDialog(MainWindow.this, 
                            "Successfully processed " + count + " receipt(s)", 
                            "Processing Complete", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(MainWindow.this, 
                            "No new receipts to process", 
                            "Processing Complete", 
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    statusLabel.setText("Error processing receipts");
                    JOptionPane.showMessageDialog(MainWindow.this, 
                        "Error: " + e.getMessage(), 
                        "Processing Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }

    private void loadReceipts() {
        statusLabel.setText("Loading receipts...");
        
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return processor.getAllReceipts();
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> receipts = get();
                    updateTable(receipts);
                    statusLabel.setText("Loaded " + receipts.size() + " receipt(s)");
                } catch (Exception e) {
                    statusLabel.setText("Error loading receipts");
                    JOptionPane.showMessageDialog(MainWindow.this, 
                        "Error: " + e.getMessage(), 
                        "Load Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }

    private void updateTable(List<Map<String, Object>> receipts) {
        tableModel.setRowCount(0);
        
        for (Map<String, Object> receipt : receipts) {
            // Extract key fields
            Object id = receipt.get("id");
            Object filename = receipt.get("filename");
            Object processedDate = receipt.get("processed_date");
            
            // Create a preview of the data (first few keys)
            StringBuilder preview = new StringBuilder();
            int count = 0;
            for (Map.Entry<String, Object> entry : receipt.entrySet()) {
                if (entry.getKey().equals("id") || entry.getKey().equals("filename") || 
                    entry.getKey().equals("processed_date")) {
                    continue;
                }
                if (count > 0) preview.append(", ");
                preview.append(entry.getKey()).append(": ");
                Object value = entry.getValue();
                if (value instanceof List) {
                    preview.append("[").append(((List<?>) value).size()).append(" items]");
                } else {
                    preview.append(value);
                }
                count++;
                if (count >= 2) break;
            }
            
            // Format date
            String dateStr = "";
            if (processedDate != null) {
                try {
                    LocalDateTime dt = LocalDateTime.parse(processedDate.toString());
                    dateStr = dt.format(DATE_FORMATTER);
                } catch (Exception e) {
                    dateStr = processedDate.toString();
                }
            }
            
            Object[] row = {
                id,
                filename,
                dateStr,
                preview.toString()
            };
            tableModel.addRow(row);
        }
    }

    private void viewSelectedReceipt() {
        int selectedRow = receiptsTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        Object idObj = tableModel.getValueAt(selectedRow, 0);
        int receiptId = idObj instanceof Double ? ((Double) idObj).intValue() : (int) idObj;
        String filename = (String) tableModel.getValueAt(selectedRow, 1);
        
        statusLabel.setText("Loading details for " + filename + "...");
        
        SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                return processor.getReceiptData(receiptId);
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> data = get();
                    showDetailsDialog(filename, data);
                    statusLabel.setText("Ready");
                } catch (Exception e) {
                    statusLabel.setText("Error loading details");
                    JOptionPane.showMessageDialog(MainWindow.this, 
                        "Error: " + e.getMessage(), 
                        "Details Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }

    private void showDetailsDialog(String filename, Map<String, Object> data) {
        JDialog dialog = new JDialog(this, "Receipt Details: " + filename, true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // JSON view in a text area for raw data
        JTextArea jsonArea = new JTextArea(gson.toJson(data));
        jsonArea.setEditable(false);
        jsonArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane jsonScroll = new JScrollPane(jsonArea);

        // If there's an "items" list, show it in a table
        if (data.containsKey("items") && data.get("items") instanceof List) {
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            
            // Items table
            List<?> items = (List<?>) data.get("items");
            if (!items.isEmpty() && items.get(0) instanceof Map) {
                JPanel tablePanel = new JPanel(new BorderLayout());
                tablePanel.add(new JLabel("Items:"), BorderLayout.NORTH);
                
                // Get column names from first item
                Map<?, ?> firstItem = (Map<?, ?>) items.get(0);
                String[] columnNames = firstItem.keySet().toArray(new String[0]);
                DefaultTableModel itemsModel = new DefaultTableModel(columnNames, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                
                // Add rows
                for (Object item : items) {
                    if (item instanceof Map) {
                        Map<?, ?> itemMap = (Map<?, ?>) item;
                        Object[] row = new Object[columnNames.length];
                        for (int i = 0; i < columnNames.length; i++) {
                            row[i] = itemMap.get(columnNames[i]);
                        }
                        itemsModel.addRow(row);
                    }
                }
                
                JTable itemsTable = new JTable(itemsModel);
                JScrollPane itemsScroll = new JScrollPane(itemsTable);
                tablePanel.add(itemsScroll, BorderLayout.CENTER);
                
                splitPane.setTopComponent(tablePanel);
            }
            
            // JSON view at bottom
            JPanel jsonPanel = new JPanel(new BorderLayout());
            jsonPanel.add(new JLabel("Raw JSON Data:"), BorderLayout.NORTH);
            jsonPanel.add(jsonScroll, BorderLayout.CENTER);
            splitPane.setBottomComponent(jsonPanel);
            
            splitPane.setDividerLocation(250);
            mainPanel.add(splitPane, BorderLayout.CENTER);
        } else {
            // Just show JSON if no items structure
            mainPanel.add(new JLabel("Raw JSON Data:"), BorderLayout.NORTH);
            mainPanel.add(jsonScroll, BorderLayout.CENTER);
        }

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
}
