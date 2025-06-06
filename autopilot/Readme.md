# CMake 
- cmake -B build - przygotowuje build - nie buduje jeszcze
    - cmake -B build-debug -G Ninja -DCMAKE_BUILD_TYPE=Debug
    - cmake -B build-release -G Ninja -DCMAKE_BUILD_TYPE=Release
- cmake --build build - buduje
- ctest --output-on-failure - w katalogu build