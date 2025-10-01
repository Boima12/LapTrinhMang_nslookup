package nslookup_2.client.ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import nslookup_2.client.common.HistoryRecord;

public class historyDialog {

    /**
     * Show the history dialog with a given list of queries.
     * Returns the selected record (or null if canceled).
     */
    public static HistoryRecord showHistory(Component parent, List<HistoryRecord> history) {
        if (history == null || history.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "No history available.",
                    "History",
                    JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        // JList will call toString() on HistoryRecord for display
        JList<HistoryRecord> historyList = new JList<>(history.toArray(new HistoryRecord[0]));
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(historyList);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        String[] options = {"Select", "Cancel"};
        int option = JOptionPane.showOptionDialog(
                parent,
                scrollPane,
                "Query History",
                JOptionPane.NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (option == 0) { // "Select"
            return historyList.getSelectedValue();
        }

        return null; // canceled
    }
}
