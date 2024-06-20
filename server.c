
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <pthread.h>
#include <sys/stat.h>
#include <dirent.h>
#include <time.h>

#define MAX_CLIENTS 10
#define MAX_USERNAME_LENGTH 50
#define PORT 8080
#define FILE_STORAGE_FOLDER "file_storage"

// Struct to hold client information
struct ClientInfo {
    int socket;
    char username[MAX_USERNAME_LENGTH];
    char ip_address[INET_ADDRSTRLEN];
    int port;
};

// Struct to hold file information
struct FileInfo {
    char sender[MAX_USERNAME_LENGTH];
    char receiver[MAX_USERNAME_LENGTH];
    char filename[256];
    time_t timestamp;
};

// Linked list to store all connected clients
struct Node {
    struct ClientInfo data;
    struct Node* next;
};

struct Node* head = NULL;

// Function to add a client to the linked list
void add_client(struct ClientInfo client) {
    struct Node* new_node = (struct Node*)malloc(sizeof(struct Node));
    new_node->data = client;
    new_node->next = head;
    head = new_node;
}

// Function to create folder for file storage if not exists
void create_file_storage_folder() {
    struct stat st = {0};
    if (stat(FILE_STORAGE_FOLDER, &st) == -1) {
        mkdir(FILE_STORAGE_FOLDER, 0700);
    }
}

// Function to send the list of connected clients to all clients
void send_client_list(int client_socket) {
    char client_list[1024] = {0};
    struct Node* current = head;

    strcat(client_list, "CLIENTS:");
    while (current != NULL) {
        strcat(client_list, " ");
        strcat(client_list, current->data.username);
        current = current->next;
    }

    // Send the client list to the specified client
    send(client_socket, client_list, strlen(client_list), 0);
}

// Function to remove a client from the linked list
void remove_client(int client_socket) {
    struct Node* current = head;
    struct Node* prev = NULL;

    // Find the client node in the linked list
    while (current != NULL && current->data.socket != client_socket) {
        prev = current;
        current = current->next;
    }
   
    // If the client node is found, remove it from the linked list
    if (current != NULL) {
        if (prev != NULL) {
            prev->next = current->next;
        } else {
            head = current->next;
        }
        free(current);
    }
}


// Function to handle incoming messages from clients
void *handle_client(void *arg) {
    int client_socket = *((int *)arg);
    char buffer[1024] = {0};
    char username[MAX_USERNAME_LENGTH];

    // Force the client to enter their username
    if (recv(client_socket, username, sizeof(username), 0) <= 0) {
        perror("Failed to receive username");
        close(client_socket);
        pthread_exit(NULL);
    }

    // Get client address and port
    struct sockaddr_in client_addr;
    socklen_t addr_len = sizeof(client_addr);
    getpeername(client_socket, (struct sockaddr*)&client_addr, &addr_len);
    char ip_address[INET_ADDRSTRLEN];
    inet_ntop(AF_INET, &client_addr.sin_addr, ip_address, INET_ADDRSTRLEN);
    int port = ntohs(client_addr.sin_port);

    // Store the client's information
    struct ClientInfo clientInfo;
    clientInfo.socket = client_socket;
    strcpy(clientInfo.username, username);
    strcpy(clientInfo.ip_address, ip_address);
    clientInfo.port = port;

    // Add client to the linked list
    add_client(clientInfo);

    printf("%s connected from %s:%d.\n", username, ip_address, port);

    // Send the list of connected clients to the newly connected client
    send_client_list(client_socket);

    // Loop to handle incoming messages
    while (1) {
        if (recv(client_socket, buffer, sizeof(buffer), 0) <= 0) {
            perror("Receive failed");
            close(client_socket);
            pthread_exit(NULL);
        }// Check if the client wants to quit
        if (strcmp(buffer, "quit") == 0) {
            // Inform other clients about the disconnection
            struct Node* current = head;
            while (current != NULL) {
                if (current->data.socket != client_socket) {
                    char disconnect_msg[1024];
                    sprintf(disconnect_msg, "%s has disconnected.", username);
                    send(current->data.socket, disconnect_msg, strlen(disconnect_msg), 0);
                }
                current = current->next;
            }

            // Remove the client from the linked list
            // (You need to implement this function)
            remove_client(client_socket);

            printf("%s disconnected.\n", username);
            close(client_socket);
            pthread_exit(NULL);
        }



        // Check if the message is a file
        if (strstr(buffer, "FILE:") != NULL) {
            // Parse the filename from the message
            char filename[256];
            sscanf(buffer, "FILE: %s", filename);

            // Receive file content length
            int file_content_length;
            if (recv(client_socket, &file_content_length, sizeof(int), 0) <= 0) {
                perror("Failed to receive file content length");
                close(client_socket);
                pthread_exit(NULL);
            }

            // Receive file content
            char file_content[file_content_length];
            if (recv(client_socket, file_content, sizeof(file_content), 0) <= 0) {
                perror("Failed to receive file content");
                close(client_socket);
                pthread_exit(NULL);
            }

            // Calculate the length of the filepath
            size_t filepath_len = strlen(FILE_STORAGE_FOLDER) + strlen(filename) + 2; // +2 for '/' and null terminator

            // Dynamically allocate memory for filepath
            char *filepath = malloc(filepath_len);
            if (filepath == NULL) {
                perror("Memory allocation failed");
                close(client_socket);
                pthread_exit(NULL);
            }

            // Construct the filepath
            snprintf(filepath, filepath_len, "%s/%s", FILE_STORAGE_FOLDER, filename);

            // Store the file on the server
            FILE *file_ptr;
            file_ptr = fopen(filepath, "wb");
            fwrite(file_content, sizeof(char), file_content_length, file_ptr);
            fclose(file_ptr);

            // Log the file information
            struct FileInfo file_info;
            strcpy(file_info.sender, username);
            sscanf(buffer, "FILE: %*s %s", file_info.receiver);
            strcpy(file_info.filename, filename);
            file_info.timestamp = time(NULL);
            printf("File '%s' received from %s and stored as '%s'.\n", filename, username, filepath);

            // Send confirmation to sender
            send(client_socket, "File sent successfully.", strlen("File sent successfully."), 0);

            // Free dynamically allocated memory
            free(filepath);
        } else {
            // Broadcast the message to all other clients
            struct Node* current = head;
            while (current != NULL) {
                if (current->data.socket != client_socket) {
                    send(current->data.socket, buffer, strlen(buffer), 0);
                }
                current = current->next;
            }
        }
    }

    return NULL;
}

int main() {
    int server_socket, client_socket;
    struct sockaddr_in server_addr, client_addr;
    socklen_t client_len = sizeof(client_addr);
    pthread_t tid;

    // Create folder for file storage if not exists
    create_file_storage_folder();

    // Create socket
    if ((server_socket = socket(AF_INET, SOCK_STREAM, 0)) == 0) {
        perror("Socket creation failed");
        exit(EXIT_FAILURE);
    }

    // Set server address parameters
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = INADDR_ANY;
    server_addr.sin_port = htons(PORT);

    // Bind socket to address
    if (bind(server_socket, (struct sockaddr *)&server_addr, sizeof(server_addr)) < 0) {
        perror("Bind failed");
        exit(EXIT_FAILURE);
    }

    // Listen for incoming connections
    if (listen(server_socket, 3) < 0) {
        perror("Listen failed");
        exit(EXIT_FAILURE);
    }

    printf("Server listening on port %d...\n", PORT);

    // Accept incoming connections and handle them in separate threads
    while ((client_socket = accept(server_socket, (struct sockaddr *)&client_addr, &client_len)) > 0) {
        pthread_create(&tid, NULL, handle_client, &client_socket);
    }

    if (client_socket < 0) {
        perror("Accept failed");
        exit(EXIT_FAILURE);
    }

    return 0;
}
