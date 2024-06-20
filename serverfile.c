#include <stdlib.h>
#include <string.h>

typedef struct {
    int id;
    char *name;
    unsigned char *data;
    char *fileExtension;
} clientFile;

clientFile *create_client_file(int id, const char *name, const unsigned char *data, const char *fileExtension) {
    clientFile *file = malloc(sizeof(clientFile));
    if (file == NULL) {
        return NULL; // Memory allocation failed
    }
    
    // Allocate memory for name and copy the string
    file->name = malloc(strlen(name) + 1);
    if (file->name == NULL) {
        free(file);
        return NULL; // Memory allocation failed
    }
    strcpy(file->name, name);
    
    // Allocate memory for data and copy the bytes
    file->data = malloc(strlen(data));
    if (file->data == NULL) {
        free(file->name);
        free(file);
        return NULL; // Memory allocation failed
    }
    memcpy(file->data, data, strlen(data));
    
    // Allocate memory for fileExtension and copy the string
    file->fileExtension = malloc(strlen(fileExtension) + 1);
    if (file->fileExtension == NULL) {
        free(file->data);
        free(file->name);
        free(file);
        return NULL; // Memory allocation failed
    }
    strcpy(file->fileExtension, fileExtension);
    
    file->id = id;
    
    return file;
}

void destroy_client_file(clientFile *file) {
    if (file == NULL) {
        return;
    }
    free(file->name);
    free(file->data);
    free(file->fileExtension);
    free(file);
}
