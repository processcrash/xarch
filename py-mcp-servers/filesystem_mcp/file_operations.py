"""
File operations with security controls - path traversal prevention
"""

import os
import shutil
import fnmatch
from pathlib import Path
from typing import Any, Dict, List


class FileOperations:
    """Secure file operations with path traversal prevention"""

    def __init__(self, allowed_path: str = "/tmp/xarch-files"):
        self.allowed_path = os.path.abspath(allowed_path)

        # Ensure allowed path exists
        os.makedirs(self.allowed_path, exist_ok=True)

    def _validate_path(self, path: str) -> str:
        """
        Validate and sanitize path to prevent path traversal attacks.
        Returns the absolute validated path.
        Raises ValueError if path is outside allowed directory.
        """
        # Get absolute path
        abs_path = os.path.abspath(os.path.join(self.allowed_path, path))

        # Check if path is within allowed directory
        if not abs_path.startswith(self.allowed_path):
            raise ValueError(f"Access denied: path '{path}' is outside allowed directory")

        return abs_path

    def list_directory(self, path: str, recursive: bool = False) -> List[Dict[str, Any]]:
        """List contents of a directory"""
        validated_path = self._validate_path(path)

        if not os.path.exists(validated_path):
            raise FileNotFoundError(f"Directory not found: {path}")

        if not os.path.isdir(validated_path):
            raise NotADirectoryError(f"Path is not a directory: {path}")

        files = []
        if recursive:
            for root, dirs, filenames in os.walk(validated_path):
                for filename in filenames:
                    full_path = os.path.join(root, filename)
                    rel_path = os.path.relpath(full_path, validated_path)
                    stat = os.stat(full_path)
                    files.append({
                        "name": filename,
                        "path": rel_path,
                        "size": stat.st_size,
                        "type": "file",
                    })
                for dirname in dirs:
                    full_path = os.path.join(root, dirname)
                    rel_path = os.path.relpath(full_path, validated_path)
                    stat = os.stat(full_path)
                    files.append({
                        "name": dirname,
                        "path": rel_path,
                        "size": 0,
                        "type": "directory",
                    })
        else:
            for entry in os.listdir(validated_path):
                full_path = os.path.join(validated_path, entry)
                stat = os.stat(full_path)
                files.append({
                    "name": entry,
                    "path": entry,
                    "size": stat.st_size if os.path.isfile(full_path) else 0,
                    "type": "directory" if os.path.isdir(full_path) else "file",
                })

        return files

    def read_file(self, path: str) -> str:
        """Read contents of a file"""
        validated_path = self._validate_path(path)

        if not os.path.exists(validated_path):
            raise FileNotFoundError(f"File not found: {path}")

        if not os.path.isfile(validated_path):
            raise IsADirectoryError(f"Path is a directory, not a file: {path}")

        with open(validated_path, 'r', encoding='utf-8') as f:
            return f.read()

    def write_file(self, path: str, content: str) -> Dict[str, Any]:
        """Write content to a file"""
        validated_path = self._validate_path(path)

        # Ensure parent directory exists
        parent_dir = os.path.dirname(validated_path)
        if parent_dir and not os.path.exists(parent_dir):
            os.makedirs(parent_dir, exist_ok=True)

        with open(validated_path, 'w', encoding='utf-8') as f:
            f.write(content)

        return {
            "path": path,
            "size": len(content),
            "written": True,
        }

    def delete(self, path: str) -> None:
        """Delete a file or directory"""
        validated_path = self._validate_path(path)

        if not os.path.exists(validated_path):
            raise FileNotFoundError(f"Path not found: {path}")

        if os.path.isdir(validated_path):
            shutil.rmtree(validated_path)
        else:
            os.remove(validated_path)

    def create_directory(self, path: str) -> Dict[str, Any]:
        """Create a new directory"""
        validated_path = self._validate_path(path)

        os.makedirs(validated_path, exist_ok=True)

        return {
            "path": path,
            "created": True,
        }

    def search_files(self, path: str, pattern: str, recursive: bool = False) -> List[str]:
        """Search for files matching a pattern"""
        validated_path = self._validate_path(path)

        if not os.path.exists(validated_path):
            raise FileNotFoundError(f"Directory not found: {path}")

        if not os.path.isdir(validated_path):
            raise NotADirectoryError(f"Path is not a directory: {path}")

        matches = []

        if recursive:
            for root, dirs, files in os.walk(validated_path):
                for filename in files:
                    if fnmatch.fnmatch(filename, pattern):
                        full_path = os.path.join(root, filename)
                        rel_path = os.path.relpath(full_path, validated_path)
                        matches.append(rel_path)
        else:
            for entry in os.listdir(validated_path):
                full_path = os.path.join(validated_path, entry)
                if os.path.isfile(full_path) and fnmatch.fnmatch(entry, pattern):
                    matches.append(entry)

        return matches

    def get_file_info(self, path: str) -> Dict[str, Any]:
        """Get information about a file or directory"""
        validated_path = self._validate_path(path)

        if not os.path.exists(validated_path):
            raise FileNotFoundError(f"Path not found: {path}")

        stat = os.stat(validated_path)

        return {
            "name": os.path.basename(validated_path),
            "path": path,
            "type": "directory" if os.path.isdir(validated_path) else "file",
            "size": stat.st_size,
            "created": stat.st_ctime,
            "modified": stat.st_mtime,
            "accessed": stat.st_atime,
        }

    def copy_file(self, source: str, destination: str) -> Dict[str, Any]:
        """Copy a file to a new location"""
        source_path = self._validate_path(source)
        dest_path = self._validate_path(destination)

        if not os.path.exists(source_path):
            raise FileNotFoundError(f"Source file not found: {source}")

        if not os.path.isfile(source_path):
            raise IsADirectoryError(f"Source is a directory, not a file: {source}")

        # Ensure destination parent directory exists
        dest_dir = os.path.dirname(dest_path)
        if dest_dir and not os.path.exists(dest_dir):
            os.makedirs(dest_dir, exist_ok=True)

        shutil.copy2(source_path, dest_path)

        return {
            "source": source,
            "destination": destination,
            "copied": True,
        }

    def move_file(self, source: str, destination: str) -> Dict[str, Any]:
        """Move a file to a new location"""
        source_path = self._validate_path(source)
        dest_path = self._validate_path(destination)

        if not os.path.exists(source_path):
            raise FileNotFoundError(f"Source file not found: {source}")

        # Ensure destination parent directory exists
        dest_dir = os.path.dirname(dest_path)
        if dest_dir and not os.path.exists(dest_dir):
            os.makedirs(dest_dir, exist_ok=True)

        shutil.move(source_path, dest_path)

        return {
            "source": source,
            "destination": destination,
            "moved": True,
        }