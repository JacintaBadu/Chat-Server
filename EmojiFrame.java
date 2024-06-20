import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class EmojiFrame extends JFrame {
    private JTextField messageField;

    public EmojiFrame(JTextField messageField) {
        this.messageField = messageField;
        initialize();
    }

    private void initialize() {
        setTitle("Emoji Selector");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JPanel emojiPanel = new JPanel();
        emojiPanel.setLayout(new GridLayout(3, 3));

        // Adding emojis as buttons
        for (int i = 0x1F600; i <= 0x1F64F; i++) {
            String emoji = new String(Character.toChars(i));
            JButton emojiButton = new JButton(emoji);
            emojiButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Insert the emoji into the message field
                    messageField.setText(messageField.getText() + emoji);
                }
            });
            emojiPanel.add(emojiButton);
        }

        JScrollPane scrollPane = new JScrollPane(emojiPanel);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }
}
