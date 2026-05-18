/**
 * File Operations with Security Controls
 * Provides secure file system operations with path traversal prevention
 */

import * as fs from 'fs/promises';
import * as path from 'path';
import * as os from 'os';

export interface FileInfo {
  name: string;
  path: string;
  isDirectory: boolean;
  size: number;
  createdAt: Date;
  modifiedAt: Date;
  permissions: string;
}

export class FileOperations {
  private allowedBasePath: string;
  private allowedExtensions: string[];

  constructor(basePath: string = '/tmp/xarch-files', allowedExtensions: string[] = []) {
    this.allowedBasePath = basePath;
    this.allowedExtensions = allowedExtensions;
  }

  /**
   * Ensure base directory exists
   */
  async ensureBaseDir(): Promise<void> {
    try {
      await fs.mkdir(this.allowedBasePath, { recursive: true });
    } catch (error) {
      // Directory may already exist
    }
  }

  /**
   * Resolve path with security checks
   */
  private resolvePath(requestedPath: string): string {
    // Resolve to absolute path
    const absolutePath = path.isAbsolute(requestedPath)
      ? requestedPath
      : path.join(this.allowedBasePath, requestedPath);

    // Normalize path (resolve .., etc.)
    const normalizedPath = path.normalize(absolutePath);

    // Ensure path is within allowed base path (prevent path traversal)
    if (!normalizedPath.startsWith(this.allowedBasePath)) {
      throw new Error('Access denied: Path outside allowed directory');
    }

    return normalizedPath;
  }

  /**
   * Check if file extension is allowed
   */
  private checkExtension(filePath: string): void {
    if (this.allowedExtensions.length === 0) return;

    const ext = path.extname(filePath).toLowerCase();
    if (!this.allowedExtensions.includes(ext)) {
      throw new Error(`Access denied: File extension ${ext} not allowed`);
    }
  }

  /**
   * List directory contents
   */
  async listDirectory(dirPath: string, recursive: boolean = false): Promise<FileInfo[]> {
    await this.ensureBaseDir();
    const resolvedPath = this.resolvePath(dirPath);

    const stats = await fs.stat(resolvedPath);
    if (!stats.isDirectory()) {
      throw new Error('Not a directory');
    }

    const results: FileInfo[] = [];

    if (recursive) {
      await this.listRecursive(resolvedPath, results, dirPath);
    } else {
      const entries = await fs.readdir(resolvedPath);
      for (const entry of entries) {
        const entryPath = path.join(resolvedPath, entry);
        const entryStats = await fs.stat(entryPath);
        results.push(this.createFileInfo(entry, dirPath, entryStats));
      }
    }

    return results;
  }

  private async listRecursive(
    basePath: string,
    results: FileInfo[],
    baseName: string
  ): Promise<void> {
    const entries = await fs.readdir(basePath);
    for (const entry of entries) {
      const entryPath = path.join(basePath, entry);
      const entryStats = await fs.stat(entryPath);
      results.push(this.createFileInfo(entry, baseName, entryStats));

      if (entryStats.isDirectory()) {
        await this.listRecursive(entryPath, results, path.join(baseName, entry));
      }
    }
  }

  private createFileInfo(name: string, parentPath: string, stats: any): FileInfo {
    return {
      name,
      path: path.join(parentPath, name),
      isDirectory: stats.isDirectory(),
      size: stats.size,
      createdAt: stats.birthtime,
      modifiedAt: stats.mtime,
      permissions: stats.mode.toString(8).slice(-3),
    };
  }

  /**
   * Read file content
   */
  async readFile(filePath: string, encoding: BufferEncoding = 'utf-8'): Promise<string> {
    this.checkExtension(filePath);
    const resolvedPath = this.resolvePath(filePath);
    return fs.readFile(resolvedPath, encoding);
  }

  /**
   * Read file as buffer
   */
  async readFileBuffer(filePath: string): Promise<Buffer> {
    this.checkExtension(filePath);
    const resolvedPath = this.resolvePath(filePath);
    return fs.readFile(resolvedPath);
  }

  /**
   * Write file content
   */
  async writeFile(filePath: string, content: string): Promise<{ success: boolean; path: string }> {
    await this.ensureBaseDir();
    this.checkExtension(filePath);
    const resolvedPath = this.resolvePath(filePath);

    // Ensure parent directory exists
    const parentDir = path.dirname(resolvedPath);
    await fs.mkdir(parentDir, { recursive: true });

    await fs.writeFile(resolvedPath, content, 'utf-8');
    return { success: true, path: filePath };
  }

  /**
   * Delete file or directory
   */
  async delete(targetPath: string): Promise<{ success: boolean }> {
    const resolvedPath = this.resolvePath(targetPath);

    const stats = await fs.stat(resolvedPath);
    if (stats.isDirectory()) {
      await fs.rm(resolvedPath, { recursive: true });
    } else {
      await fs.unlink(resolvedPath);
    }

    return { success: true };
  }

  /**
   * Create directory
   */
  async createDirectory(dirPath: string): Promise<{ success: boolean; path: string }> {
    await this.ensureBaseDir();
    const resolvedPath = this.resolvePath(dirPath);
    await fs.mkdir(resolvedPath, { recursive: true });
    return { success: true, path: dirPath };
  }

  /**
   * Search files by pattern
   */
  async searchFiles(
    dirPath: string,
    pattern: string,
    recursive: boolean = false
  ): Promise<FileInfo[]> {
    await this.ensureBaseDir();
    const resolvedPath = this.resolvePath(dirPath);

    const results: FileInfo[] = [];
    await this.searchRecursive(resolvedPath, pattern, results, recursive);

    return results;
  }

  private async searchRecursive(
    basePath: string,
    pattern: string,
    results: FileInfo[],
    recursive: boolean
  ): Promise<void> {
    const entries = await fs.readdir(basePath);

    for (const entry of entries) {
      const entryPath = path.join(basePath, entry);
      const stats = await fs.stat(entryPath);

      // Match pattern (simple glob matching)
      const regex = new RegExp(pattern.replace(/\*/g, '.*').replace(/\?/g, '.'));
      if (regex.test(entry)) {
        results.push(this.createFileInfo(entry, basePath, stats));
      }

      if (recursive && stats.isDirectory()) {
        await this.searchRecursive(entryPath, pattern, results, recursive);
      }
    }
  }

  /**
   * Get file information
   */
  async getFileInfo(filePath: string): Promise<FileInfo> {
    const resolvedPath = this.resolvePath(filePath);
    const stats = await fs.stat(resolvedPath);
    return this.createFileInfo(path.basename(filePath), path.dirname(filePath), stats);
  }

  /**
   * Copy file
   */
  async copyFile(sourcePath: string, destPath: string): Promise<{ success: boolean; path: string }> {
    this.checkExtension(destPath);
    const resolvedSource = this.resolvePath(sourcePath);
    const resolvedDest = this.resolvePath(destPath);

    // Ensure parent directory exists
    const parentDir = path.dirname(resolvedDest);
    await fs.mkdir(parentDir, { recursive: true });

    await fs.copyFile(resolvedSource, resolvedDest);
    return { success: true, path: destPath };
  }

  /**
   * Move file
   */
  async moveFile(sourcePath: string, destPath: string): Promise<{ success: boolean; path: string }> {
    this.checkExtension(destPath);
    const resolvedSource = this.resolvePath(sourcePath);
    const resolvedDest = this.resolvePath(destPath);

    // Ensure parent directory exists
    const parentDir = path.dirname(resolvedDest);
    await fs.mkdir(parentDir, { recursive: true });

    await fs.rename(resolvedSource, resolvedDest);
    return { success: true, path: destPath };
  }
}

export const fileOperations = new FileOperations();