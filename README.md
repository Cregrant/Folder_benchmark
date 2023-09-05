# Folder_benchmark

This project will help you estimate latency and linear read speed for files in a folder.  
It was created to compare different NTFS compression algorithms using a single folder with test files.

**Please note that your operating system will likely cache the test files in RAM memory.**

## Requirements
Make sure you have Java 8 installed.  
Prepare a test folder with at least 6 identical files (you can use CTRL+C and CTRL+V).  
For latency testing, it is recommended to use a few hundred small files.  

## Usage
If each file is less than 1 MB, a latency test will be initiated. Otherwise, a speed test will be performed.  
To run the benchmark, use the following command:  
`java -jar Benchmark.jar <Absolute path to a folder>`

## Common test sequence
### Win
Clean up any RAM caches using [RAMMap](https://learn.microsoft.com/en-us/sysinternals/downloads/rammap) (Empty -> Empty Standby List)  
Run the benchmark.  

Repeat these steps until your results become similar.
