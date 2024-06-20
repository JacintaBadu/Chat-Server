import clients.clientFile;
import clients.TicTacToe;
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
import java.awt.image.BufferedImage;



public class Client extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    private JTextField usernameField;
    private JTextField messageField;
    private JTextArea chatArea;
    private JTextArea clientListArea; // Area to display list of connected clients
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private Color sentColor = Color.BLUE;
    private Color LightRed = new Color(255, 209, 209);
    private Color MenuBarColor = new Color(173, 216, 230);
    private Color receivedColor = new Color(148, 0, 211); // Violet color
    private final String DOWNLOAD_DIR = "downloads/";
    static ArrayList<clientFile> clientFiles = new ArrayList<>();
    private EmojiFrame emojiFrame;
    private JLabel connectionStatusLabel; // Label to display connection status
    private JMenuBar menuBar;
    private Socket socket; // Added to store socket reference

    public Client() {
        setTitle("Chat Client");
        setSize(600, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        

        // Create a menu bar
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        menuBar.setBackground(MenuBarColor);

        // Create a "File" menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

       // Play Games For Fun
       JMenu gamesMenu = new JMenu("Games");
       menuBar.add(gamesMenu);

       // For settings
       JMenu settings = new JMenu("Settings");
       menuBar.add(settings);

       // Create "Color" menu item in the "Settings" menu
       JMenuItem colorMenuItem = new JMenuItem("Color");
       colorMenuItem.addActionListener(this);
       settings.add(colorMenuItem);


        // Add connection status label to the rightmost side of the menu bar
        connectionStatusLabel = new JLabel("", SwingConstants.CENTER);
        connectionStatusLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        menuBar.add(Box.createHorizontalGlue()); // Add glue to push connection status label to the right
        menuBar.add(connectionStatusLabel);

       JMenuItem ticTacToeItem = new JMenuItem("Tic Tac Toe");
       ticTacToeItem.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               playTicTacToe();
           }
       });
       gamesMenu.add(ticTacToeItem);

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
                if (!clientFiles.isEmpty()) {
                    // Assuming you want to download the first file in the list
                    clientFile selectedFile = clientFiles.get(0);
                    downloadFile(selectedFile.getName(), selectedFile.getData());
                } else {
                    // Handle case where no files are available for download
                    JOptionPane.showMessageDialog(Client.this, "No files available for download.", "Download Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        fileMenu.add(downloadFileItem);

        usernameField = new JTextField(20);
        messageField = new JTextField(20);
        JButton sendButton = new JButton("Send");
        JButton emojiButton = new JButton("Emoji"); // Emoji button
        sendButton.addActionListener(this);
        emojiButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Show the emoji frame when the emoji button is clicked
                emojiFrame.setVisible(true);
            }
        });
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT); // Align messages to the right
        chatArea.setForeground(Color.BLACK); // Set text area text color
        chatArea.setBackground(new Color(245, 245, 245)); // Very light gray

        JScrollPane scrollPane = new JScrollPane(chatArea);

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        // Assuming you have an image file named "light_green_pattern.png"
        ImageIcon imageIcon = new ImageIcon("backgound.png");
        JLabel backgroundLabel = new JLabel(imageIcon);
        backgroundLabel.setSize(600, 300); // Set desired size
        chatArea.add(backgroundLabel); // Add label to frame (assuming it's in a JFrame)

        // Create client list area
        clientListArea = new JTextArea();
        clientListArea.setEditable(true);
        clientListArea.setBorder(new EmptyBorder(5, 5, 5, 5)); // Add padding
        clientListArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT); // Align text to the right
        JScrollPane clientListScrollPane = new JScrollPane(clientListArea);
        clientListScrollPane.setPreferredSize(new Dimension(150, 0)); // Set preferred width

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(2, 1));
        inputPanel.add(usernameField);
        inputPanel.add(messageField);
        inputPanel.add(sendButton);
        inputPanel.add(emojiButton); // Add emoji button next to the send button


        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(chatPanel, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        mainPanel.add(clientListScrollPane, BorderLayout.EAST); // Add client list on the right

        add(mainPanel);

        setVisible(true);

        // Initialize EmojiFrame
        emojiFrame = new EmojiFrame(messageField);

       
        // Try to connect to the server
        connectToServer();  

    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost",8080);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(new IncomingReader(in, chatArea, clientListArea, clientFiles)).start();
            updateConnectionStatus(true);
        } catch (IOException ex) {
            ex.printStackTrace();
            updateConnectionStatus(false);
        }
    }

    private void updateConnectionStatus(boolean isConnected) {
        // Set the connection status indicator
        if (isConnected) {
            connectionStatusLabel.setIcon(new ImageIcon(createCircleImage(Color.GREEN, 10)));
            connectionStatusLabel.setToolTipText("Connected");
        } else {
            connectionStatusLabel.setIcon(new ImageIcon(createCircleImage(Color.RED, 10)));
            connectionStatusLabel.setToolTipText("Not Connected");
            // Server disconnected, clean up resources and display message
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            chatArea.append("Server disconnected.\n");
            chatArea.setForeground(LightRed); // Change color to indicate disconnection
        }
    }

    // Method to create a small circle image
    private Image createCircleImage(Color color, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.fillOval(0, 0, size, size);
        g2d.dispose();
        return img;
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
        } else if (e.getActionCommand().equals("Color")) {
            // Handle color menu item click
            JColorChooser Color = new JColorChooser();
            Color initialColor = menuBar.getBackground();
            Color selectedColor = JColorChooser.showDialog(this, "Select Menu Bar Color", initialColor);
            if (selectedColor != null) {
                menuBar.setBackground(selectedColor);
            }
        }
    }
    
    public void insertCalculatorOutput(int result) {
        messageField.setText(Integer.toString(result));
    }

    private void playTicTacToe() {
        // Instantiate the Tic Tac Toe game class
        TicTacToe ticTacToe = new TicTacToe();

        // Provide an option to quit the game
        int option = JOptionPane.showConfirmDialog(this, "Do you want to quit Tic Tac Toe?", "Quit Tic Tac Toe", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            // Perform any cleanup or actions needed to quit the game 
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
    

    // designing the round text box for the message to be received
    
    
    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
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
        private JTextArea clientListArea;
        private ArrayList<clientFile> clientFiles;

        public IncomingReader(BufferedReader in, JTextArea chatArea, JTextArea clientListArea, ArrayList<clientFile> clientFiles) {
            this.in = in;
            this.chatArea = chatArea;
            this.clientListArea = clientListArea;
            this.clientFiles = clientFiles;
    }

    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("USERNAME: ")) {
                    String username = message.substring(10);
                    chatArea.append(username + " has joined the chat.\n");
                    chatArea.setForeground(receivedColor);
                    updateClientList(); // Update client list when a new client joins
                } else if (message.startsWith("MESSAGE: ")) {
                    String[] parts = message.split(" ", 4);
                    String sender = parts[1];
                    String timestamp = parts[2];
                    String msg = parts[3];

                    // Check if the message is sent by the current user or not
                    if (sender.equals(username)) {
                        // If the message is sent by the current user, align it to the right
                        chatArea.append("[" + timestamp + "] " + sender + ": " + msg + "\n");
                        chatArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                    } else {
                        // If the message is received from another user, align it to the left
                        chatArea.append("[" + timestamp + "] " + sender + ": " + msg + "\n");
                        chatArea.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                    }
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
                    clientFiles.add(new clientFile(clientFiles.size(), fileName, fileContentBytes, getFileExtension(fileName)));
                }
            }
        } catch (IOException ex) {
            // Server disconnected
                updateConnectionStatus(false);
        }
    }

    private void updateClientList() {
        // Request client list from the server
        out.println("GET_CLIENTS");
    
        // Continuously listen for updates from the server regarding the client list
        new Thread(() -> {
            try {
                while (true) {
                    // Receive and update client list
                    String clientList = in.readLine();
                    if (clientList != null) {
                        SwingUtilities.invokeLater(() -> clientListArea.setText(clientList));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Client();
            }
        });
    }
}
