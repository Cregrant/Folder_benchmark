# Folder_benchmark

This project will help you estimate a latency and a linear read speed for files in a folder.  
I made it to compare different NTFS compression algorithms using one folder with test files.  

**Note that your OS probably will cache test files in RAM memory.**

## Requirements
Make sure you have installed Java 8.  
Prepare your test folder with at least 6 identical files (use CTRL+C and CTRL+V).  
I recommend using a few hundred small files for a latency test.

## Usage
A latency test will be initiated if each file is less than 1 MB. Or a speed test otherwise.  
`java -jar Benchmark.jar <Absolute path to a folder>`

## Common test sequence
### Win
Clean up any RAM caches using [RAMMap](https://learn.microsoft.com/en-us/sysinternals/downloads/rammap) (Empty -> Empty Standby List)  
Run benchmark  

Repeat previous steps until your results become similar
