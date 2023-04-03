import difflib
import re
import os
import json


def jaccard_similarity(file1, file2):
    with open(file1, 'r') as f1, open(file2, 'r') as f2:
        data1 = f1.read()
        data2 = f2.read()
        set1 = set(data1.split())
        set2 = set(data2.split())
        match = len(set1.intersection(set2))
        total = len(set1.union(set2))
        return match / total * 100


def find_match_percentage(file1, file2):
    with open(file1, 'r') as f1, open(file2, 'r') as f2:
        file1_contents = f1.read()
        file2_contents = f2.read()
        file1_contents = re.sub(r'(package|import).+', '', file1_contents)
        file2_contents = re.sub(r'(package|import).+', '', file2_contents)
        matcher = difflib.SequenceMatcher(lambda x: x in ["\n"], file1_contents, file2_contents)
        match_percentage = matcher.ratio() * 100
        return match_percentage


def find_similar_files(directory):
    processed = dict()
    for root, dirs, files in os.walk(directory):
        for zipped in zip(files, range(len(files))):
            file, i = zipped
            if file not in processed:
                processed[file] = []
            file_path = os.path.join(root, file)
            print(f"Processing file #{i}/{len(files)}")
            for comp_zipped in zip(files, range(len(files))):
                comp, j = comp_zipped
                comp_path = os.path.join(root, comp)
                if comp_path == file_path:
                    continue
                match = jaccard_similarity(file_path, comp_path)
                if match > 68:
                    processed[file].append(comp)
    return processed


file1 = '/Users/mansurminnikaev/IdeaProjects/starfish/scala/ObjectTransferStorage/src/main/resources/base/flatten'
pr = find_similar_files(file1)
print(json.dumps(pr))
