LOCAL_PATH := $(call my-dir)

#-----< JNI Wrapper >-----
include $(CLEAR_VARS)

LOCAL_MODULE    := receiptAnalyzer
LOCAL_SRC_FILES := JniAnalyzeWrapper.c 
LOCAL_LDLIBS:= -ldl -llog

include $(BUILD_SHARED_LIBRARY)
