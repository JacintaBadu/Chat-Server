package clients;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.net.Socket;

public class Client2 extends JFrame implements ActionListener {
    private JTextField usernameField;
    private JTextField messageField;
    private JTextArea chatArea;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private Color sentColor = Color.BLUE;
    private Color receivedColor = new Color(148, 0, 211); // Violet color
    private final String DOWNLOAD_DIR = "downloads/";
    static ArrayList<clientFile> myFiles = new ArrayList<>();

    public Client2() {
        setTitle("Chat Client2");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a menu bar
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // Create a "File" menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        // Create a "Send File" item in the "File" menu
        JMenuItem sendFileItem = new JMenuItem("Send File");
        sendFileItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Handle send file action
                sendFile();
            }
        });
        fileMenu.add(sendFileItem);

  // Create a "Download File" item in the "File" menu
    JMenuItem downloadFileItem = new JMenuItem("Download File");
    downloadFileItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            // Check if a file is selected for download
            if (!myFiles.isEmpty()) {
                // Assuming you want to download the first file in the list
                clientFile selectedFile = myFiles.get(0);
                downloadFile(selectedFile.getName(), selectedFile.getData());
            } else {
                // Handle case where no files are available for download
                JOptionPane.showMessageDialog(Client2.this, "No files available for download.", "Download Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    });
    fileMenu.add(downloadFileItem);



        usernameField = new JTextField(20);
        messageField = new JTextField(20);
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(this);

        chatArea = new JTextArea();
        chatArea.setEditable(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout());
        panel1.add(new JLabel("Username: "), BorderLayout.WEST);
        panel1.add(usernameField, BorderLayout.CENTER);

        JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout());
        panel2.add(new JLabel("Message: "), BorderLayout.WEST);
        panel2.add(messageField, BorderLayout.CENTER);
        panel2.add(sendButton, BorderLayout.EAST);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(2, 1));
        inputPanel.add(panel1);
        inputPanel.add(panel2);

        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        setVisible(true);

        try (Socket socket = new Socket("localhost", 12345);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

        // Your code to use the socket, out, and in goes here

    } catch (IOException ex) {
        ex.printStackTrace();
}

    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Send")) {
            if (usernameField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a username.");
                return;
            }
            if (username == null) {
                username = usernameField.getText();
                out.println("USERNAME: " + username);
                usernameField.setEditable(false);
            }
            sendMessage();
        }
    }

    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            // Get the current timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
            String timestamp = sdf.format(new Date());

            // Append the message with username and timestamp to the chat area before sending it
            chatArea.append("[" + timestamp + "] " + username + ": " + message + "\n");

            // Set color of sent message to blue
            chatArea.setForeground(sentColor);

            // Send the message with username and timestamp
            out.println("MESSAGE: " + username + " " + timestamp + " " + message);
            messageField.setText("");
        }
    }

    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (FileInputStream fileInputStream = new FileInputStream(selectedFile)) {
                byte[] fileContentBytes = new byte[(int) selectedFile.length()];
                fileInputStream.read(fileContentBytes);
    
                // Send the file content to the server
                out.println("FILE: " + selectedFile.getName());
                out.println(fileContentBytes.length);
                out.println(Base64.getEncoder().encodeToString(fileContentBytes));
    
                chatArea.append("File sent: " + selectedFile.getName() + "\n");
            } catch (IOException ex) {
                ex.printStackTrace();
                chatArea.append("Error sending file: " + ex.getMessage() + "\n");
            }
        }
    }
    

    private void downloadFile(String fileName, byte[] fileContentBytes) {
        int option = JOptionPane.showConfirmDialog(this, "Do you want to download file: " + fileName + "?", "File Download", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            String filePath = DOWNLOAD_DIR + fileName;
            try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
                fileOutputStream.write(fileContentBytes);
                JOptionPane.showMessageDialog(this, "File downloaded successfully to: " + filePath, "Download Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error downloading file: " + fileName, "Download Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return ""; // No extension found
        }
        return fileName.substring(dotIndex + 1);
    }
    
    private class IncomingReader implements Runnable {
        private BufferedReader in;
        private JTextArea chatArea;
        private ArrayList<clientFile> myFiles;
    
        public IncomingReader(BufferedReader in, JTextArea chatArea, ArrayList<clientFile> myFiles) {
            this.in = in;
            this.chatArea = chatArea;
            this.myFiles = myFiles;
        }
    
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("USERNAME: ")) {
                        String username = message.substring(10);
                        chatArea.append(username + " has joined the chat.\n");
                        chatArea.setForeground(receivedColor);
                    } else if (message.startsWith("MESSAGE: ")) {
                        String[] parts = message.split(" ", 4);
                        String sender = parts[1];
                        String timestamp = parts[2];
                        String msg = parts[3];
                        chatArea.append("[" + timestamp + "] " + sender + ": " + msg + "\n");
                        chatArea.setForeground(receivedColor);
                    } else if (message.startsWith("FILE: ")) {
                        String fileName = message.substring(6);
                        int fileContentLength = Integer.parseInt(in.readLine());
                        byte[] fileContentBytes = Base64.getDecoder().decode(in.readLine());
    
                        // Display the file in the chat area as a clickable link
                        chatArea.append("File received: " + fileName + "\n");
                        chatArea.setForeground(receivedColor);
                        chatArea.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                downloadFile(fileName, fileContentBytes);
                            }
                        });                        
    
                        // Add the file to the list of files
                        myFiles.add(new clientFile(myFiles.size(), fileName, fileContentBytes, getFileExtension(fileName)));

                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Client2();
            }
        });
    }
}
