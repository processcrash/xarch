/**
 * Database Connection Manager
 * Handles connections to MySQL, PostgreSQL, MongoDB, SQL Server
 */

import mysql from 'mysql2/promise';
import { Pool as PgPool } from 'pg';
import { MongoClient, Db } from 'mongodb';
import mssql, { Connection } from 'mssql';

export type DatabaseType = 'mysql' | 'postgresql' | 'mongodb' | 'sqlserver';

export interface DatabaseConfig {
  type: DatabaseType;
  host: string;
  port: number;
  database: string;
  username: string;
  password: string;
}

export class DatabaseConnectionManager {
  private mysqlPool: mysql.Pool | null = null;
  private pgPool: PgPool | null = null;
  private mongoClient: MongoClient | null = null;
  private mongoDb: Db | null = null;
  private mssqlPool: mssql.ConnectionPool | null = null;
  private currentConfig: DatabaseConfig | null = null;

  /**
   * Configure database connection
   */
  async configure(config: DatabaseConfig): Promise<void> {
    this.currentConfig = config;
    await this.closeAll();

    switch (config.type) {
      case 'mysql':
        this.mysqlPool = mysql.createPool({
          host: config.host,
          port: config.port,
          user: config.username,
          password: config.password,
          database: config.database,
          waitForConnections: true,
          connectionLimit: 10,
          queueLimit: 0,
        });
        break;

      case 'postgresql':
        this.pgPool = new PgPool({
          host: config.host,
          port: config.port,
          user: config.username,
          password: config.password,
          database: config.database,
          max: 20,
          idleTimeoutMillis: 30000,
        });
        break;

      case 'mongodb':
        const mongoUrl = `mongodb://${config.username}:${config.password}@${config.host}:${config.port}/${config.database}`;
        this.mongoClient = new MongoClient(mongoUrl);
        await this.mongoClient.connect();
        this.mongoDb = this.mongoClient.db(config.database);
        break;

      case 'sqlserver':
        this.mssqlPool = await mssql.connect({
          server: config.host,
          port: config.port,
          user: config.username,
          password: config.password,
          database: config.database,
          options: {
            encrypt: false,
            trustServerCertificate: true,
          },
        });
        break;
    }
  }

  /**
   * Execute SELECT query
   */
  async executeQuery(sql: string, params: any[] = []): Promise<any[]> {
    if (!this.currentConfig) {
      throw new Error('Database not configured');
    }

    switch (this.currentConfig.type) {
      case 'mysql':
        const [mysqlRows] = await this.mysqlPool!.query(sql, params);
        return mysqlRows as any[];

      case 'postgresql':
        const result = await this.pgPool!.query(sql, params);
        return result.rows;

      case 'mongodb':
        // For MongoDB, treat SQL as collection.query format
        // This is a simplified implementation
        throw new Error('Use executeMongoQuery for MongoDB');

      case 'sqlserver':
        const mssqlResult = await this.mssqlPool!.query(sql);
        return mssqlResult.recordset;

      default:
        throw new Error(`Unsupported database type: ${this.currentConfig.type}`);
    }
  }

  /**
   * Execute INSERT/UPDATE/DELETE
   */
  async executeUpdate(sql: string, params: any[] = []): Promise<{ affectedRows: number }> {
    if (!this.currentConfig) {
      throw new Error('Database not configured');
    }

    switch (this.currentConfig.type) {
      case 'mysql':
        const [mysqlResult] = await this.mysqlPool!.query(sql, params);
        return { affectedRows: (mysqlResult as mysql.ResultSetHeader).affectedRows };

      case 'postgresql':
        const pgResult = await this.pgPool!.query(sql, params);
        return { affectedRows: pgResult.rowCount || 0 };

      case 'mongodb':
        throw new Error('Use executeMongoUpdate for MongoDB');

      case 'sqlserver':
        const mssqlResult = await this.mssqlPool!.query(sql);
        return { affectedRows: mssqlResult.rowsAffected[0] };

      default:
        throw new Error(`Unsupported database type: ${this.currentConfig.type}`);
    }
  }

  /**
   * Get database schema information
   */
  async getSchema(): Promise<{ tables: any[] }> {
    if (!this.currentConfig) {
      throw new Error('Database not configured');
    }

    switch (this.currentConfig.type) {
      case 'mysql':
        const [tables] = await this.mysqlPool!.query(
          'SELECT TABLE_NAME, TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ?',
          [this.currentConfig.database]
        );
        return { tables };

      case 'postgresql':
        const pgResult = await this.pgPool!.query(
          `SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'`
        );
        return { tables: pgResult.rows };

      case 'mongodb':
        const collections = await this.mongoDb!.listCollections().toArray();
        return { tables: collections.map(c => ({ name: c.name })) };

      case 'sqlserver':
        const mssqlResult = await this.mssqlPool!.query(
          `SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_CATALOG = ?`,
          [this.currentConfig.database]
        );
        return { tables: mssqlResult.recordset };

      default:
        throw new Error(`Unsupported database type: ${this.currentConfig.type}`);
    }
  }

  /**
   * List tables
   */
  async listTables(): Promise<string[]> {
    if (!this.currentConfig) {
      throw new Error('Database not configured');
    }

    switch (this.currentConfig.type) {
      case 'mysql':
        const [tables] = await this.mysqlPool!.query(
          'SHOW TABLES'
        );
        return Object.values(tables[0] as Record<string, string>);

      case 'postgresql':
        const pgResult = await this.pgPool!.query(
          `SELECT tablename FROM pg_tables WHERE schemaname = 'public'`
        );
        return pgResult.rows.map(r => r.tablename);

      case 'mongodb':
        const collections = await this.mongoDb!.listCollections().toArray();
        return collections.map(c => c.name);

      case 'sqlserver':
        const mssqlResult = await this.mssqlPool!.query(
          `SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE'`
        );
        return mssqlResult.recordset.map((r: any) => r.TABLE_NAME);

      default:
        throw new Error(`Unsupported database type: ${this.currentConfig.type}`);
    }
  }

  /**
   * Describe table structure
   */
  async describeTable(tableName: string): Promise<any[]> {
    if (!this.currentConfig) {
      throw new Error('Database not configured');
    }

    switch (this.currentConfig.type) {
      case 'mysql':
        const [mysqlColumns] = await this.mysqlPool!.query(
          'DESCRIBE ??',
          [tableName]
        );
        return mysqlColumns as any[];

      case 'postgresql':
        const pgResult = await this.pgPool!.query(
          `SELECT column_name, data_type, is_nullable, column_default
           FROM information_schema.columns WHERE table_name = $1`,
          [tableName]
        );
        return pgResult.rows;

      case 'mongodb':
        // MongoDB collections don't have fixed schemas
        // Return sample document structure
        const sample = await this.mongoDb!.collection(tableName).findOne({});
        return sample ? Object.keys(sample).map(key => ({ field: key, type: typeof sample[key] })) : [];

      case 'sqlserver':
        const mssqlResult = await this.mssqlPool!.query(
          `SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT
           FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = @tableName`,
          { tableName }
        );
        return mssqlResult.recordset;

      default:
        throw new Error(`Unsupported database type: ${this.currentConfig.type}`);
    }
  }

  /**
   * List indexes for a table
   */
  async listIndexes(tableName: string): Promise<any[]> {
    if (!this.currentConfig) {
      throw new Error('Database not configured');
    }

    switch (this.currentConfig.type) {
      case 'mysql':
        const [mysqlIndexes] = await this.mysqlPool!.query(
          'SHOW INDEX FROM ??',
          [tableName]
        );
        return mysqlIndexes as any[];

      case 'postgresql':
        const pgResult = await this.pgPool!.query(
          `SELECT indexname, indexdef FROM pg_indexes WHERE tablename = $1`,
          [tableName]
        );
        return pgResult.rows;

      case 'mongodb':
        const indexes = await this.mongoDb!.collection(tableName).indexes();
        return indexes;

      case 'sqlserver':
        const mssqlResult = await this.mssqlPool!.query(
          `SELECT i.name, i.type_desc, c.name as column_name
           FROM sys.indexes i
           JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
           JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
           WHERE i.object_id = OBJECT_ID(@tableName)`,
          { tableName }
        );
        return mssqlResult.recordset;

      default:
        throw new Error(`Unsupported database type: ${this.currentConfig.type}`);
    }
  }

  /**
   * Close all connections
   */
  async closeAll(): Promise<void> {
    if (this.mysqlPool) {
      await this.mysqlPool.end();
      this.mysqlPool = null;
    }
    if (this.pgPool) {
      await this.pgPool.end();
      this.pgPool = null;
    }
    if (this.mongoClient) {
      await this.mongoClient.close();
      this.mongoClient = null;
      this.mongoDb = null;
    }
    if (this.mssqlPool) {
      await this.mssqlPool.close();
      this.mssqlPool = null;
    }
  }
}

export const connectionManager = new DatabaseConnectionManager();