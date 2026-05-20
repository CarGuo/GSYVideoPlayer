#!/usr/bin/env python3
# Usage: find_xy.py <ui.xml path> <exact title>
import re, sys
xml = open(sys.argv[1]).read()
target = sys.argv[2]
for m in re.finditer(r'text="([^"]*)"[^>]*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml):
    if m.group(1) == target:
        x = (int(m.group(2)) + int(m.group(4))) // 2
        y = (int(m.group(3)) + int(m.group(5))) // 2
        print(f"{x} {y}")
        break
