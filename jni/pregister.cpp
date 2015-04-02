#include <jni.h>
#include <string.h>
#include<processtxt.h>

namespace example{
	//C使用如下，(*env)->NewStringUTF(env, "Hello from JNI!");

	static jint mainRegister(JNIEnv *env, jobject obj) {
		processFile();
		return 	0;

	}


	static int jniRegisterNativeMethods(JNIEnv *env,const char *className,
				JNINativeMethod* Methods, int numMethods){
			jclass clazz = env->FindClass(className);
			    if (clazz == NULL) {
			        return JNI_FALSE;
			    }

			    if (env->RegisterNatives(clazz, Methods, numMethods) < 0) {
			        return JNI_FALSE;
			    }
			    return JNI_TRUE;
		}

	static JNINativeMethod sMethod[] = {
			{"processFileNative","()I",(void*)mainRegister}
	};

	int register_Signal(JNIEnv *env) {
	    return jniRegisterNativeMethods(env, "com/example/tagidentification/ResultsActivity",
	            sMethod, sizeof(sMethod) / sizeof(sMethod[0]));
	}


}
