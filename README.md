# ImageUtil
[![API](https://img.shields.io/badge/API-22%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=22)



>>### 基于OpenCV的Android图像处理工具软件。本项目采用Android Studio开发安卓APP，使用C++（OpenCV）编写图片处理算法。主要处理图片的对比度，饱和度以及对图片进行一定程度的清晰度增强。

>>### 在网上找了许多关于开发Android APP时引入OpenCV库的资料，大多数都只说了导入OpenCV的SDK。这样确实可以使用OpenCV库，而且使用起来非常简单。但本项目是要使用C++（OpenCV）处理图片，然后Java调用C++（通过JNI）。下文将介绍如何搭建这样的一个开发环境。



# 开发工具

>>## 一、Windows 10。

>>## 二、Android Studio 4.1.2及以上版本。链接：[Android Studio](https://developer.android.google.cn/studio/)

>>## 三、OpenCV for Android 4.5.0。链接：[OpenCV](https://sourceforge.net/projects/opencvlibrary/files/4.5.0/opencv-4.5.0-android-sdk.zip/download)



# 环境搭建

>>## 一、安装Android Studio。可参考[安装Android Studio](https://blog.csdn.net/Y74364/article/details/96121530?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-11.control&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-11.control "安装Android Studio")

>>## 二、下载SDK，SDK是开发安卓APP必要的。

>>>>### 1. 打开Android Studio，点击左上角的“File”，再点击“Setting”。然后依次展开“Appearance & Behavior”、“System setting”，即可看到“Android SDK”。点击该选项，根据需要下载对应的SDK。一般来说现在开发安卓APP都是5.1以上，所以建议把5.1以上的SDK都安装了。步骤如下图所示：

![SDK安装](https://www.slwolf.com/img/sdk_1.png "SDK安装步骤")
![SDK安装](https://www.slwolf.com/img/sdk_2.png "SDK安装步骤")

>>>>### 2. 等待下载安装之后先不要重启软件，先把NDK安装了再重启Android Studio。



>>## 三、下载NDK和CMake，NDK和CMake是在开发安卓APP需要用到C++时的工具。

>>>>### 1. 步骤和安装SDK一样，在安装SDK的界面点击“SDK Tools”。在下面的列表中可看到“NDK”、“CMake”等选项。勾选并下载即可。如下图所示：

![NDK安装](https://www.slwolf.com/img/ndk_1.png "NDK安装步骤")

>>>>### 2. 下载完成之后Android Studio会自动安装，安装完成重启Android Studio即可。
>>>>### 3. 如果首次打开Android Studio，找不到上图中得选项，可先新建一个项目然后再进行上述操作。


# 构建项目
>>## 项目结构如下图所示：

![项目结构](https://www.slwolf.com/img/project_struct.png "项目结构")

>>## 一、新建项目，一般选择“Empty Activity”就可以了，不要选择“Native C++”。



>>## 二、等待项目构建完成之后，再新建一个模块，该模块的作用是：

>>>>### 1. 调用底层（C++）；

>>>>### 2. 封装并对外提供接口。

>>## 创建模块方法：File --> New --> New module。选择Android Library确定等待构建完成即可。



>>## 三、引入OpenCV到模块中。注意：是模块！！！。
>>>>### 1. 在模块的src/main/目录下新建“cpp”和“jniLibs”文件夹（名字可以自取）。如下图所示：

![模块目录结构](https://www.slwolf.com/img/android_opencv_module.png "模块目录结构")


>>>>### 2. 找到之前下载好的OpenCV目录（OpenCV-android-sdk），进入/sdk/native/jni，把该目录下的“include”文件夹复制到模块的/src/main/jniLibs中。如下图所示：

![复制include](https://www.slwolf.com/img/android_opencv_module_include.png "复制include")


>>>>### 3. 进入OpenCV目录：sdk/native/staticlibs，复制所有的文件到模块的jniLibs中。这里可以根据需要复制，如果不使用模拟器可以不用复制“x86和x86_64”，本项目没有配置模拟器的环境。

![复制库文件](https://www.slwolf.com/img/android_opencv_module_libs.png "复制库文件")


>>>>### 4. 进入OpenCV目录：/sdk/native/3rdparty/libs，该目录下的文件夹和staticlibs中的一样，将各个文件夹下的文件（都是“*.a”的文件）复制到模块中jniLibs中对应的文件夹里面即可。其实有一些库文件本项目用不到，但是为了后期更新方便，所以才复制所有的库文件。

>>>>### 5. 新建cpp和CMakeLists.txt文件，在模块的cpp目录下新建“native-lib.cpp”和“CMakeLists.txt”文件，其中cpp文件名可以任取。如下图：

![创建cpp文件](https://www.slwolf.com/img/android_opencv_module_cpp_make.png "创建cpp文件")

>>>>### 6. 配置C++的编译脚本：CMakeLists.txt，打开该文件，加入以下代码：

---

cmake_minimum_required(VERSION 3.10.2)

project("opencv450")

set(libs ${CMAKE_SOURCE_DIR}/..)
include_directories(${libs}/cpp/include)

add_library(libcpufeatures STATIC IMPORTED)
set_target_properties(libcpufeatures PROPERTIES
        IMPORTED_LOCATION "${libs}/jniLibs/${ANDROID_ABI}/libcpufeatures.a")

add_library(ittnotify STATIC IMPORTED)
set_target_properties(ittnotify PROPERTIES
        INTERFACE_LINK_LIBRARIES "dl"
        )
set_target_properties(ittnotify PROPERTIES
        IMPORTED_LOCATION "${libs}/jniLibs/${ANDROID_ABI}/libittnotify.a")

add_library(tegra_hal STATIC IMPORTED)
set_target_properties(tegra_hal PROPERTIES
        IMPORTED_LOCATION "${libs}/jniLibs/${ANDROID_ABI}/libtegra_hal.a")

add_library(tbb STATIC IMPORTED)
set_target_properties(tbb PROPERTIES
        INTERFACE_COMPILE_DEFINITIONS "TBB_USE_GCC_BUILTINS=1;__TBB_GCC_BUILTIN_ATOMICS_PRESENT=1;TBB_SUPPRESS_DEPRECATED_MESSAGES=1"
        INTERFACE_LINK_LIBRARIES "c;m;dl"
        )
set_target_properties(tbb PROPERTIES
        IMPORTED_LOCATION "${libs}/jniLibs/${ANDROID_ABI}/libtbb.a")

add_library(opencv_core STATIC IMPORTED)
set_target_properties(opencv_core PROPERTIES
        INTERFACE_LINK_LIBRARIES "\$<LINK_ONLY:dl>;\$<LINK_ONLY:m>;\$<LINK_ONLY:log>;\$<LINK_ONLY:tegra_hal>;\$<LINK_ONLY:tbb>;\$<LINK_ONLY:z>;\$<LINK_ONLY:libcpufeatures>;\$<LINK_ONLY:ittnotify>;\$<LINK_ONLY:tegra_hal>"
        )
set_target_properties(opencv_core PROPERTIES
        IMPORTED_LOCATION "${libs}/jniLibs/${ANDROID_ABI}/libopencv_core.a")

add_library(opencv_imgproc STATIC IMPORTED)
set_target_properties(opencv_imgproc PROPERTIES
        INTERFACE_LINK_LIBRARIES "\$<LINK_ONLY:opencv_core>;opencv_core;\$<LINK_ONLY:dl>;\$<LINK_ONLY:m>;\$<LINK_ONLY:log>;\$<LINK_ONLY:tegra_hal>"
        )
set_target_properties(opencv_imgproc PROPERTIES
        IMPORTED_LOCATION "${libs}/jniLibs/${ANDROID_ABI}/libopencv_imgproc.a")

add_library(
        native-lib

        SHARED
        
        native-lib.cpp)

find_library(

        log-lib
        
        log)

target_link_libraries(

        native-lib
        
        opencv_core
        
        opencv_imgproc
        
        ${log-lib})

---


>>>>### 上述代码如果复制不了可在本项目中查看源文件。需要注意的是，如果你的cpp文件名不是native-lib，则需要在上述代码中把native-lib改成你的cpp文件名。这样编译脚本才能找到你的C++源文件。


>>## 三、配置模块gradle

>>>>### 如下图所示：

![配置gradle](https://www.slwolf.com/img/android_opencv_module_config1.png "配置gradle")

>>>>### 双击图中选择的文件，在android --> defaultConfig中添加如下代码：

		externalNativeBuild {
	        cmake {
	            abiFilters 'armeabi-v7a','arm64-v8a'
	            cppFlags "-std=c++11","-frtti", "-fexceptions -lz"
	        }
	    }
	    ndk {
	        abiFilters 'armeabi-v7a','arm64-v8a'
	    }

>>>>### 如下图所示：

![配置gradle](https://www.slwolf.com/img/android_opencv_module_config2.png "配置gradle")

>>>>### 在android下添加如下代码：

	// android 节点下
	externalNativeBuild {
	    cmake {
	        path "src/main/cpp/CMakeLists.txt"
	        version "3.10.2"
	    }
	}
	// 新建jniLibs存放静态库
	sourceSets {
	    main {
	        jniLibs.srcDirs = ['src/main/jniLibs', 'lib']
	    }
	}

>>>>### 如下图所示：

![配置gradle](https://www.slwolf.com/img/android_opencv_module_config3.png "配置gradle")

>>>>### 最后点击右上角的同步，等待项目构建。完成之后在模块中显示jniLibs和cpp了，如上文的“项目结构图”。



# 导入本项目

>>## 点击本页面[项目地址](https://github.com/AivenLi/imageUtil "项目地址")右上角的“Code”，会有几种下载方式：
>>>>### 1. 克隆，即复制项目地址，然后在Android Studio中导入项目。在Android Studio中点击菜单栏的“VCS”，然后再点击“Get from version control”，粘贴刚才复制的地址点击确认即可。不过这种方式需要安装git插件。

>>>>#### 首先安装git bash。[点我查看教程](https://blog.csdn.net/qq_36667170/article/details/79085301 "安装git bash教程")

>>>>#### 安装git bash之后，在Android Studio中依次点击File --> Setting，然后展开“Version control”，点击“git”。找到git bash的安装目录，进入bin目录，点击git.exe确定，然后再点击test，测试是否安装成功。显示版本号表示安装成功。
>>>>#### 或者在桌面（或者其他文件夹）鼠标右键点击“Git bash here”，输入命令：“git clone https://github.com/AivenLi/imageUtil.git”，回车等待克隆完成，然后打开Android Studio导入项目

对比度算法设计：
	参考文献：https://ieeexplore.ieee.org/abstract/document/4767166
