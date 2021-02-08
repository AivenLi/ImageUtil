# ImageUtil
[![API](https://img.shields.io/badge/API-22%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=22)



>>### 基于OpenCV的Android图像处理工具软件。本项目采用Android Studio开发安卓APP，使用C++（OpenCV）编写图片处理算法。主要处理图片的对比度，饱和度以及对图片进行一定程度的清晰度增强。

>>### 在网上找了许多关于开发Android APP时引入OpenCV库的资料，大多数都只说了导入OpenCV的SDK。这样确实可以使用OpenCV库，而且使用起来非常简单。但本项目是要使用C++（OpenCV）处理图片，然后Java调用C++（通过JNI）。下文将介绍如何搭建这样的一个开发环境。



# 开发工具



## 一、Windows 10



## 二、Android Studio 4.1.2

>>## Android Studio 4.1.2或以上版本。链接：[Android Studio](https://developer.android.google.cn/studio/)



## 三、OpenCV for Android 4.5.0

>>## OpenCV for Android 4.5.0，链接：[OpenCV](https://sourceforge.net/projects/opencvlibrary/files/4.5.0/opencv-4.5.0-android-sdk.zip/download)



# 开发语言


## Android -- Java

>>### Android app 使用Java语言开发。


## OpenCV -- C++

>>### OpenCV 使用C++语言开发




# 设计方案

## 一、开发模式方案对比
>>###   本项目采用“Android + OpenCV + C++” 的开发模式。传统的“Android + OpenCV”开发模式都是下载OpenCV SDK后直接导入官方已经写好的SDK模块然后调用；这样确实省时省力，而且非常好用。但是这样也存在一些不足，尤其是这种开发模式完全不符合本项目的设计需求。下面给出对比。
>>### 1. 算法不可见，不可修改

>>>>#### OpenCV SDK完全是调用Java接口，图片处理算法不可见。

>>>>#### 本项目使用Java和C++分别编写Android app程序和图片处理算法。

>>### 2. 项目体积太大

>>>>#### OpenCV SDK导入后有1.3GB，整个项目有将近2GB的体积，项目太臃肿。

>>>>#### 本项目只移植OpenCV中必要的库文件，整个项目体积减少了1GB。

>>### 3. 方案优势

>>>>#### OpenCV SDK包含非常多的图片处理接口，而且处理算法都很好。而本项目的初衷主要是学习图片处理，如果直接使用SDK，那么就无从知其原理；即使了解其原理，也没有实际经验。因此采用“Android + OpenCV + C++”这种开发模式不仅能够掌握处理图片的原理，还非常符号“理论+实践”的学习理念。



## 二、项目结构设计方案对比

>>### 本项目采用“app + module”模块化方案进行开发，传统的“app”模块开发虽然构建速度快，但是项目模块的内聚性降低，耦合性升高，不太符合“高内聚低耦合”的软件设计原则。其中module将其命名为：“opencv450”，表示使用的OpenCV的版本为4.5.0

>>### 1. app模块

>>>>#### app模块只需要完成app的UI设计和业务逻辑处理，无需处理其他事物。

>>### 2. opencv450模块

>>>>#### 在本项目中，opencv450模块主要和底层（C++）互交，然后对其进行封装并对外提供接口。而app模块只需要和该opencv450模块互交即可，无需关心底层算法。

>>### 4. 模块调用示意图

![模块调用示意图](https://www.slwolf.com/img/android_module_call_prompt.png "模块调用示意图")

>>### 3. 方案优势

>>>>#### （1）将项目模块化，符合“高内聚低耦合”的软件设计原则。

>>>>#### （2）更新方便，如果以后需要对本项目做改动。例如使用更高版本的OpenCV库，只需要修改module即可；如果要修改app的UI，或者其他的东西，只需要修改app模块即可。




# 环境搭建

## 一、安装Android Studio。
>>### 可参考[安装Android Studio](https://blog.csdn.net/Y74364/article/details/96121530?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-11.control&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-11.control "安装Android Studio")



## 二、下载SDK

>>### 1. 打开Android Studio，点击左上角的“File”，再点击“Setting”。然后依次展开“Appearance & Behavior”、“System setting”，即可看到“Android SDK”。点击该选项，根据需要下载对应的SDK。一般来说现在开发安卓APP都是5.1以上，所以建议把5.1以上的SDK都安装了。步骤如下图所示：

![SDK安装](https://www.slwolf.com/img/sdk_1.png "SDK安装步骤")

![SDK安装](https://www.slwolf.com/img/sdk_2.png "SDK安装步骤")

>>### 2. 等待下载安装之后先不要重启软件，先把NDK安装了再重启Android Studio。



## 三、下载NDK和CMake

>>### 1. 步骤和安装SDK一样，在安装SDK的界面点击“SDK Tools”。在下面的列表中可看到“NDK”、“CMake”等选项。勾选并下载即可。如下图所示：

![NDK安装](https://www.slwolf.com/img/ndk_1.png "NDK安装步骤")

>>### 2. 下载完成之后Android Studio会自动安装，安装完成重启Android Studio即可。
>>### 3. 如果首次打开Android Studio，找不到上图中得选项，可先新建一个项目然后再进行上述操作。


# 构建项目
## 一、 项目结构

![项目结构](https://www.slwolf.com/img/project_struct.png "项目结构")



## 二、新建项目
>>### 1. File --> New --> New project

>>### 2.选择“Empty Activity”就可以了，不要选择“Native C++”。



## 三、新建模块
>>### 1. File --> New --> New module

>>### 2. 选择“Android Library”



## 三、移植OpenCV
>>### 1. 在模块的src/main/目录下新建“cpp”和“jniLibs”文件夹（名字可以自取）。如下图所示：

![模块目录结构](https://www.slwolf.com/img/android_opencv_module.png "模块目录结构")


>>### 2. 找到之前下载好的OpenCV目录（OpenCV-android-sdk），进入/sdk/native/jni，把该目录下的“include”文件夹复制到模块的/src/main/jniLibs中。如下图所示：

![复制include](https://www.slwolf.com/img/android_opencv_module_include.png "复制include")


>>### 3. 进入OpenCV目录：sdk/native/staticlibs，复制所有的文件到模块的jniLibs中。这里可以根据需要复制，如果不使用模拟器可以不用复制“x86和x86_64”，本项目没有配置模拟器的环境。

![复制库文件](https://www.slwolf.com/img/android_opencv_module_libs.png "复制库文件")


>>### 4. 进入OpenCV目录：/sdk/native/3rdparty/libs，该目录下的文件夹和staticlibs中的一样，将各个文件夹下的文件（都是“*.a”的文件）复制到模块中jniLibs中对应的文件夹里面即可。其实有一些库文件本项目用不到，但是为了后期更新方便，所以才复制所有的库文件。

>>### 5. 新建cpp和CMakeLists.txt文件，在模块的cpp目录下新建“native-lib.cpp”和“CMakeLists.txt”文件，其中cpp文件名可以任取。如下图：

![创建cpp文件](https://www.slwolf.com/img/android_opencv_module_cpp_make.png "创建cpp文件")

>>### 6. 配置C++的编译脚本：CMakeLists.txt，打开该文件，加入以下代码：

---

		cmake_minimum_required(VERSION 3.10.2)
	
		project("opencv450")
	
		set(libs ${CMAKE_SOURCE_DIR}/..)
		include_directories(${libs}/cpp/include)
	
		add_library(libcpufeatures STATIC IMPORTED)
		set_target_properties(libcpufeatures PROPERTIES
	    		IMPORTED_LOCATION 														"${libs}/jniLibs/${ANDROID_ABI}/libcpufeatures.a")
	
		add_library(ittnotify STATIC IMPORTED)
		set_target_properties(ittnotify PROPERTIES
	    		INTERFACE_LINK_LIBRARIES "dl"
	    		)
		set_target_properties(ittnotify PROPERTIES
	    		IMPORTED_LOCATION 														"${libs}/jniLibs/${ANDROID_ABI}/libittnotify.a")
	
		add_library(tegra_hal STATIC IMPORTED)
		set_target_properties(tegra_hal PROPERTIES
	    		IMPORTED_LOCATION 														"${libs}/jniLibs/${ANDROID_ABI}/libtegra_hal.a")
	
		add_library(tbb STATIC IMPORTED)
		set_target_properties(tbb PROPERTIES
	    		INTERFACE_COMPILE_DEFINITIONS		     "TBB_USE_GCC_BUILTINS=1;__TBB_GCC_BUILTIN_ATOMICS_PRESENT=1;TBB_SUPPRESS_DEPRECATED_MESSAGES=1"
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


>>### 需要注意的是，cpp文件名如果不是native-lib，则需要在上述代码中把native-lib改成cpp文件名。这样编译脚本才能找到C++源文件。



## 三、配置模块gradle

>>### 如下图所示：

![配置gradle](https://www.slwolf.com/img/android_opencv_module_config1.png "配置gradle")

>>### 双击图中选择的文件，在android --> defaultConfig中添加如下代码：

		externalNativeBuild {
	        cmake {
	            abiFilters 'armeabi-v7a','arm64-v8a'
	            cppFlags "-std=c++11","-frtti", "-fexceptions -lz"
	        }
	    }
	    ndk {
	        abiFilters 'armeabi-v7a','arm64-v8a'
	    }

>>### 如下图所示：

![配置gradle](https://www.slwolf.com/img/android_opencv_module_config2.png "配置gradle")

>>### 在android下添加如下代码：

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

>>### 如下图所示：

![配置gradle](https://www.slwolf.com/img/android_opencv_module_config3.png "配置gradle")

>>### 最后点击右上角的同步，等待项目构建。完成之后在模块中显示jniLibs和cpp了，如上文的“项目结构图”。


# 设计原理

## 一、JNI原理

>>### JNI是Java Native Interface（Java本地接口）的缩写。JNI作为java和操作系统间的一个直接接口，可以通过JNI使得java直接调用操作系统的资源。目前JNI只能通过c/C++实现，因为jni只是对操作系统资源调用的一个桥接过程。所以理论上在windows下只要是dll文件均可以被调用。java代码编译之后是运行在一个jvm里，所以java的任何操作对操作系统而言都是隔着一层虚拟机外壳，这点也正式java的优点，帮助java实现了“Write Once, Run Everywhere”的可移植性。但是使用了jni之后必须要明白这个“Write Once, Run Everywhere”要被打破，必须要实现不同的操作系统的各种jni版本。

>>### 1. JNI规则

>>#### 如下图所示，编写C++代码时，一个方法的命名需要符合下述规则（不考虑注释）：

![C++代码规则](https://www.slwolf.com/img/android_cpp_rule.png "C++代码规则")

>>>>#### （1）首先声明“extern "C"”，

>>>>#### （2）“JNIEXPORT returnType JNICALL”，其中returnType是函数返回类型，在这里是jintArray，即int类型的数组。当然，也可以设为void，即无返回值。

>>>>#### （3）“Java_package_javaClass_function”，package是对应的Java的包名称，javaClass是声明该函数的Java类名，function是函数名。

>>>>#### （4）参数：“JNIEnv *env和jobject”是固定参数，后面的参数才是Java层需要传递的参数。当然，可以不用传参数。

>>>>#### （5）Java需要声明要调用的C++函数，格式为：“public native returnType function(params);”，其中returnType要和C++中对应的function的returnType一致。如下图所示。其中public可以是private或者proected。

![Java代码规则](https://www.slwolf.com/img/android_java_rule.png "Java代码规则")

>>### 2. Java调用C++

>>>>#### 完成上述操作后，在Java中调用C++就像普通的调用方法即可。

## 二、图片处理原理



>>### 1. 在计算机中，一张图片可以使用矩阵来记录该图片的数据。OpenCV中使用类“Mat”来保存图片的数据，记录格式也是采用矩阵。该矩阵记录了图片的每一个像素点的各项参数，例如颜色参数。



>>### 2. 彩色图片，一张彩色图片的一个像素点有3~4个数值。如果是三通道，则这三个通道分别表示R、G、B（顺序不固定）；如果是四通道，除了表示RGB之外还有一个通道表示图片的Alpha的色彩空间，该通道一般用作不透明度参数。



>>### 3. 灰度图片，灰度是指当RGB这三个通道相等时的像素点是灰色的。也就是说当把一张图片处理为灰度图时只需要遍历图片的各个像素点，将其RGB通道的值都设为平均数即可。



>>### 4. 对比度，图片的对比度是指一幅图像中明暗区域最亮的白和最暗的黑不同亮度层级的测量，对比度中画面黑与白的比值差异范围越大代表对比越大，反之越小。增强对比度的方法有多种，本设计采用“自适应直方图均衡”的方式处理，[对比度参考文献](https://ieeexplore.ieee.org/abstract/document/4767166 "对比度参考文献")。



>>### 5. 饱和度，图片饱和度是指色彩的鲜艳程度，也称色彩的纯度。饱和度取决于该色中含色成分和消色成分（灰色）的比例。含色成分越大，饱和度越大；消色成分越大，饱和度越小。纯的颜色都是高度饱和的，如鲜红，鲜绿。混杂上白色，灰色或其他色调的颜色，是不饱和的颜色，如绛紫，粉红，黄褐等。完全不饱和的颜色根本没有色调，如黑白之间的各种灰色。



# 问题及解决方案

## 一、添加opencv450模块失败

>>### 如果opencv450模块没有被添加到项目中，可在app模块的gradle的dependencies中加入：

		implementation project(path: ':opencv450')


>>### 然后再在settting.gradle文件中加入：

		include ':opencv450'


>>### 即可。


## 二、无法下载gradle包

>>### 1. 先进入gradle[下载网页](https://services.gradle.org/distributions/ "gradle下载")下载对应的gradle，本项目使用的是gradle-6.5-bin。


>>### 2. 下载完成后不要解压，找到gradle存放目录。一般在C盘的Users/账户名称/.gradle目录下。也可以在Android Studio中查看：File --> Settings --> Build,Execution...下的gradle。


>>### 3. 将gradle包放到（复制剪切都行）该目录下的wrapper/dists。

>>### 4. 修改配置文件“gradle-wrapper.properties”，注释掉原来的（gradle-x.x-bin.zip这一行），然后新增一行：

		distributionUrl=file:///D:/.gradle/wrapper/dists/gradle-6.5-bin.zip

![离线安装gradle](https://www.slwolf.com/img/android_gradle_config.png "离线安装gradle")

>>### 这里的路径请根据实际情况修改。如果在Android Studio中找不到该文件，可直接打开项目的gradle/wrapper/文件夹找到该文件，使用记事本修改并保存。


>>### 5. 完成之后点击右上角的Sync now即可。如果在Android Studio中找不到“gradle-wrapper.properties”文件。按第四步完成之后在Android Studio中点击下图所示的图标

![重构项目](https://www.slwolf.com/img/android_remake_project.png "重构项目")

>>### 也可以重启Android Studio。


## 三、github下无法查看图片

>>### 进入：“C:\Windows\System32\drivers\etc”目录，找到hosts.ics文件，该文件也有可能不带后缀，总之就是hosts文件。点击用记事本打开，加入下列代码，然后保存退出即可。


		# GitHub Start 
		192.30.253.112    Build software better, together 
		192.30.253.119    gist.github.com
		151.101.184.133    assets-cdn.github.com
		151.101.184.133    raw.githubusercontent.com
		151.101.184.133    gist.githubusercontent.com
		151.101.184.133    cloud.githubusercontent.com
		151.101.184.133    camo.githubusercontent.com
		151.101.184.133    avatars0.githubusercontent.com
		151.101.184.133    avatars1.githubusercontent.com
		151.101.184.133    avatars2.githubusercontent.com
		151.101.184.133    avatars3.githubusercontent.com
		151.101.184.133    avatars4.githubusercontent.com
		151.101.184.133    avatars5.githubusercontent.com
		151.101.184.133    avatars6.githubusercontent.com
		151.101.184.133    avatars7.githubusercontent.com
		151.101.184.133    avatars8.githubusercontent.com
	
		# GitHub End


>>### 如果还是不行，可以下载markdown编辑器阅读本文的。这里推荐一款比较好用的markdown编辑器：[Typora](https://www.typora.io/#windows "Typora下载")。下载安装完成之后用Typora打开Readme.md文件即可。