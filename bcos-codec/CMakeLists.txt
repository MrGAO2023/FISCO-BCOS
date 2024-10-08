#------------------------------------------------------------------------------
# Top-level CMake file for bcos-codec
# ------------------------------------------------------------------------------
# Copyright (C) 2021 FISCO BCOS.
# SPDX-License-Identifier: Apache-2.0
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ------------------------------------------------------------------------------

cmake_minimum_required(VERSION 3.12)
project(bcos-codec VERSION ${VERSION})

file(GLOB SRC_LIST "*.cpp")
file(GLOB HEADERS "*.h")

aux_source_directory(bcos-codec/abi SRC_LIST)
aux_source_directory(bcos-codec/scale SRC_LIST)

find_package(Microsoft.GSL CONFIG REQUIRED)

add_library(${CODEC_TARGET} ${SRC_LIST} ${HEADERS})
target_include_directories(${CODEC_TARGET} PUBLIC
    $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}>
    $<INSTALL_INTERFACE:include/bcos-codec>
)
target_link_libraries(${CODEC_TARGET} PUBLIC bcos-crypto Microsoft.GSL::GSL)
set_target_properties(${CODEC_TARGET} PROPERTIES UNITY_BUILD "ON")

if (TESTS)
    enable_testing()
    set(CTEST_OUTPUT_ON_FAILURE True)
    add_subdirectory(test)
endif()

# for code coverage
#if (COVERAGE)
#    include(Coverage)
#    config_coverage("cov" "'/usr*' '${CMAKE_CURRENT_SOURCE_DIR}/bcos-cmake-scripts*' '${CMAKE_CURRENT_SOURCE_DIR}/test/bcos-test*'")
#endif ()

include(GNUInstallDirs)
install(TARGETS ${CODEC_TARGET} EXPORT fiscobcosTargets ARCHIVE DESTINATION "${CMAKE_INSTALL_LIBDIR}")
install(DIRECTORY "bcos-codec" DESTINATION "${CMAKE_INSTALL_INCLUDEDIR}" FILES_MATCHING PATTERN "*.h")