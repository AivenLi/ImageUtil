# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("opencv450")


set(libs ${CMAKE_SOURCE_DIR}/..)
include_directories(${libs}/cpp/include)

# Create imported target libcpufeatures
add_library(libcpufeatures STATIC IMPORTED)
set_target_properties(libcpufeatures PROPERTIES
        IMPORTED_LOCATION "${libs}/jniLibs/${ANDROID_ABI}/libcpufeatures.a")

# Create imported target ittnotify
add_library(ittnotify STATIC IMPORTED)
set_target_properties(ittnotify PROPERTIES
        INTERFACE_LINK_LIBRARIES "dl"
        )
set_target_properties(ittnotify PROPERTIES
        IMPORTED_LOCATION "${libs}/jniLibs/${ANDROID_ABI}/libittnotify.a")

# Create imported target tegra_hal
add_library(tegra_hal STATIC IMPORTED)
set_target_properties(tegra_hal PROPERTIES
        IMPORTED_LOCATION "${libs}/jniLibs/${ANDROID_ABI}/libtegra_hal.a")

# Create imported target tbb
add_library(tbb STATIC IMPORTED)
set_target_properties(tbb PROPERTIES
        INTERFACE_COMPILE_DEFINITIONS "TBB_USE_GCC_BUILTINS=1;__TBB_GCC_BUILTIN_ATOMICS_PRESENT=1;TBB_SUPPRESS_DEPRECATED_MESSAGES=1"
        INTERFACE_LINK_LIBRARIES "c;m;dl"
        )
set_target_properties(tbb PROPERTIES
        IMPORTED_LOCATION "${libs}/jniLibs/${ANDROID_ABI}/libtbb.a")

# Create imported target opencv_core
add_library(opencv_core STATIC IMPORTED)
set_target_properties(opencv_core PROPERTIES
        INTERFACE_LINK_LIBRARIES "\$<LINK_ONLY:dl>;\$<LINK_ONLY:m>;\$<LINK_ONLY:log>;\$<LINK_ONLY:tegra_hal>;\$<LINK_ONLY:tbb>;\$<LINK_ONLY:z>;\$<LINK_ONLY:libcpufeatures>;\$<LINK_ONLY:ittnotify>;\$<LINK_ONLY:tegra_hal>"
        )
set_target_properties(opencv_core PROPERTIES
        IMPORTED_LOCATION "${libs}/jniLibs/${ANDROID_ABI}/libopencv_core.a")

# Create imported target opencv_imgproc
add_library(opencv_imgproc STATIC IMPORTED)
set_target_properties(opencv_imgproc PROPERTIES
        INTERFACE_LINK_LIBRARIES "\$<LINK_ONLY:opencv_core>;opencv_core;\$<LINK_ONLY:dl>;\$<LINK_ONLY:m>;\$<LINK_ONLY:log>;\$<LINK_ONLY:tegra_hal>"
        )
set_target_properties(opencv_imgproc PROPERTIES
        IMPORTED_LOCATION "${libs}/jniLibs/${ANDROID_ABI}/libopencv_imgproc.a")


# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native-lib

        opencv_core
        opencv_imgproc
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
