cmake_minimum_required(VERSION 3.4.1)


add_library(keylogger SHARED keylogger.c)


target_link_libraries(keylogger log)
