# Receipt Processor

A minimalistic Java application for processing receipt files (PDFs and images), extracting nutritional information, and storing data in a lightweight SQLite database with automatic Git version control.

## Features

- Simple GUI for selecting and processing receipts
- Scans directories for PDF and image files
- **Dynamic dictionary-based data structure** - UI and DB adapt to extraction function output
- Stores receipt data in SQLite as JSON (version-control friendly)
- **Automatic Git commits** - Database changes are auto-committed and pushed to GitHub
- Flexible display - Shows raw JSON and structured table views
- Stub interface for future OCR/AI integration

## Requirements

- Java 17 or higher
- Git (for automatic version control)

## Building

```bash
./gradlew build
```

## Running

```bash
./gradlew run
```

## Usage

1. Click "Select Input Folder" to choose a directory containing receipt files
2. Click "Process Receipts" to scan and process new receipts
3. View processed receipts in the table
4. Select a receipt and click "View Details" to see items and nutritional info

## Supported File Formats

- PDF: `.pdf`
- Images: `.jpg`, `.jpeg`, `.png`, `.gif`, `.bmp`

## Database & Version Control

The application uses SQLite stored in `receipts.db` at the project root. This file stores all receipt data as JSON and is automatically:
1. Saved to disk after each receipt is processed
2. Committed to Git with a descriptive message
3. Pushed to GitHub (if remote is configured)

### Setting up Git Auto-Push

```bash
# Initialize git repository (if not already done)
git init

# Add remote
git remote add origin https://github.com/yourusername/receipt-processor.git

# Set up authentication (use token or SSH)
git config credential.helper store

# First manual push
git add .
git commit -m "Initial commit"
git push -u origin main
```

After setup, the app will automatically commit and push `receipts.db` after processing each receipt.

## Data Structure

The extraction function returns a **dictionary (Map)** with flexible structure. The UI and database automatically adapt to whatever structure your extraction function returns.

**Example structure:**
```json
{
  "total_price": 45.20,
  "store_name": "Example Store",
  "date": "2025-11-02",
  "items": [
    {
      "name": "Item 1",
      "price": 12.99,
      "protein": 15.5,
      "calories": 250.0
    }
  ]
}
```

## Future Implementation

The `IReceiptExtractor` interface is currently using a stub implementation that returns mock dictionary data. Replace the `StubReceiptExtractor` class in `ReceiptProcessor.java` with your actual OCR/AI implementation.

**Your extraction function should return:** `Map<String, Object>` with any structure you need.

## Project Structure

```
receipt-processor/
├── pom.xml
├── receipts.db (created on first run)
├── src/main/java/com/receiptprocessor/
│   ├── Main.java                    # Entry point
│   ├── ReceiptProcessor.java        # Main processing logic
│   ├── FileScanner.java             # Directory scanner
│   ├── IReceiptExtractor.java       # Extractor interface (stub)
│   ├── model/
│   │   ├── Receipt.java            # Receipt model
│   │   └── Item.java               # Item model
│   ├── db/
│   │   └── DatabaseManager.java    # SQLite operations
│   └── ui/
│       └── MainWindow.java         # Simple Swing GUI
```
