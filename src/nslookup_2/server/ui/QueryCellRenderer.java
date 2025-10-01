package nslookup_2.server.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/*
 *  This file is AI generated, and this file isn't important, it is just
 *  an additional to make the list item in ServerUI has more style
 *  and nothing more.
 *  */

public class QueryCellRenderer extends JPanel implements ListCellRenderer<String> {
    private final JLabel label;

    public QueryCellRenderer() {
        setLayout(new BorderLayout());

        label = new JLabel();
        label.setFont(new Font("Tahoma", Font.PLAIN, 14));
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // padding
        add(label, BorderLayout.CENTER);

        // default style
        Border border = BorderFactory.createLineBorder(new Color(135, 206, 250)); // sky blue
        setBorder(border);
        setBackground(new Color(224, 247, 255)); // light sky blue
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends String> list,
            String value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        label.setText(value);

        // dynamic background depending on selection
        if (isSelected) {
            setBackground(new Color(173, 216, 230)); // darker sky blue
        } else {
            setBackground(new Color(224, 247, 255));
        }

        return this;
    }
}
