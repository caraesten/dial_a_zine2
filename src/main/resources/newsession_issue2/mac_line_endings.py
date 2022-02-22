#!/usr/bin/python

import os

def fix_file(file_path):
    if file_path.endswith(".txt"):
        with open(file_path, "r+") as f:
            text_contents = f.read()
            f.seek(0)
            new_contents = text_contents.replace('\n', '\r')
            f.write(new_contents)
            f.truncate()
            

for file in os.listdir("."):
    if os.path.isdir(file) and file != '.git':
        for sub_dir_file in os.listdir(file):
            fix_file(os.path.join(file, sub_dir_file))
    else:
        fix_file(file)
