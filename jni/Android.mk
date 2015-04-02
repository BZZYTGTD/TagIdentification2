LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := processtxt
LOCAL_SRC_FILES := processtxt.cpp

include $(BUILD_SHARED_LIBRARY)
