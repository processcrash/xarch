"""
Database connection management - supports MySQL, PostgreSQL, MongoDB, SQL Server
"""

import json
from typing import Any, Dict, List, Optional
from dataclasses import dataclass
from enum import Enum

try:
    import mysql.connector
    from mysql.connector import pooling
    MYSQL_AVAILABLE = True
except ImportError:
    MYSQL_AVAILABLE = False

try:
    import psycopg2
    from psycopg2 import pooling
    POSTGRESQL_AVAILABLE = True
except ImportError:
    POSTGRESQL_AVAILABLE = False

try:
    from pymongo import MongoClient
    MONGODB_AVAILABLE = True
except ImportError:
    MONGODB_AVAILABLE = False

try:
    import pymssql
    Mssql_AVAILABLE = True
except ImportError:
    Mssql_AVAILABLE = False


class DatabaseType(Enum):
    """Supported database types"""
    MYSQL = "mysql"
    POSTGRESQL = "postgresql"
    MONGODB = "mongodb"
    SQLSERVER = "sqlserver"


@dataclass
class DatabaseConfig:
    """Database configuration"""
    type: str
    host: str
    port: int
    database: str
    username: str
    password: str


class ConnectionManager:
    """Multi-database connection manager"""

    def __init__(self):
        self.config: Optional[DatabaseConfig] = None
        self.connection: Optional[Any] = None
        self.pool: Optional[Any] = None

    async def configure(self, config: DatabaseConfig) -> None:
        """Configure database connection"""
        self.config = config

        # Close existing connection if any
        if self.connection:
            try:
                self.connection.close()
            except Exception:
                pass

        db_type = config.type.lower()

        if db_type == DatabaseType.MYSQL.value:
            if not MYSQL_AVAILABLE:
                raise ImportError("mysql-connector-python is not installed")
            self.connection = mysql.connector.connect(
                host=config.host,
                port=config.port,
                database=config.database,
                user=config.username,
                password=config.password,
            )

        elif db_type == DatabaseType.POSTGRESQL.value:
            if not POSTGRESQL_AVAILABLE:
                raise ImportError("psycopg2 is not installed")
            self.connection = psycopg2.connect(
                host=config.host,
                port=config.port,
                database=config.database,
                user=config.username,
                password=config.password,
            )

        elif db_type == DatabaseType.MONGODB.value:
            if not MONGODB_AVAILABLE:
                raise ImportError("pymongo is not installed")
            uri = f"mongodb://{config.username}:{config.password}@{config.host}:{config.port}/{config.database}"
            self.connection = MongoClient(uri)

        elif db_type == DatabaseType.SQLSERVER.value:
            if not Mssql_AVAILABLE:
                raise ImportError("pymssql is not installed")
            self.connection = pymssql.connect(
                server=config.host,
                port=config.port,
                database=config.database,
                user=config.username,
                password=config.password,
            )

        else:
            raise ValueError(f"Unsupported database type: {db_type}")

    async def execute_query(self, sql: str, params: List[Any] = None) -> List[Dict[str, Any]]:
        """Execute SELECT query and return results"""
        if not self.config:
            raise RuntimeError("Database not configured")

        params = params or []
        db_type = self.config.type.lower()

        if db_type == DatabaseType.MYSQL.value:
            cursor = self.connection.cursor(dictionary=True)
            cursor.execute(sql, params)
            results = cursor.fetchall()
            cursor.close()
            return results

        elif db_type == DatabaseType.POSTGRESQL.value:
            cursor = self.connection.cursor()
            cursor.execute(sql, params)
            columns = [desc[0] for desc in cursor.description]
            results = [dict(zip(columns, row)) for row in cursor.fetchall()]
            cursor.close()
            return results

        elif db_type == DatabaseType.MONGODB.value:
            # For MongoDB, interpret SQL as collection.query format
            # This is a simplified implementation
            parts = sql.split(".", 1)
            if len(parts) == 2:
                collection_name, query_part = parts
                collection = self.connection[self.config.database][collection_name]
                if query_part.startswith("find("):
                    # Extract filter from find({filter})
                    import re
                    match = re.search(r"find\s*\(\s*\{([^}]*)\}", query_part)
                    if match:
                        filter_dict = json.loads("{" + match.group(1) + "}")
                        results = list(collection.find(filter_dict))
                        # Convert ObjectId to string for JSON serialization
                        for doc in results:
                            if "_id" in doc:
                                doc["_id"] = str(doc["_id"])
                        return results
            return []

        elif db_type == DatabaseType.SQLSERVER.value:
            cursor = self.connection.cursor(as_dict=True)
            cursor.execute(sql, params)
            results = cursor.fetchall()
            cursor.close()
            return results

        return []

    async def execute_update(self, sql: str, params: List[Any] = None) -> Dict[str, Any]:
        """Execute INSERT, UPDATE, or DELETE statement"""
        if not self.config:
            raise RuntimeError("Database not configured")

        params = params or []
        db_type = self.config.type.lower()

        if db_type == DatabaseType.MYSQL.value:
            cursor = self.connection.cursor()
            cursor.execute(sql, params)
            self.connection.commit()
            affected_rows = cursor.rowcount
            cursor.close()
            return {"affectedRows": affected_rows}

        elif db_type == DatabaseType.POSTGRESQL.value:
            cursor = self.connection.cursor()
            cursor.execute(sql, params)
            self.connection.commit()
            affected_rows = cursor.rowcount
            cursor.close()
            return {"affectedRows": affected_rows}

        elif db_type == DatabaseType.MONGODB.value:
            # MongoDB update handling
            parts = sql.split(".", 1)
            if len(parts) == 2:
                collection_name, operation = parts
                collection = self.connection[self.config.database][collection_name]
                if "updateOne" in operation or "updateMany" in operation:
                    import re
                    match = re.search(r"updateOne|updateMany", operation)
                    if match:
                        return {"affectedRows": 1}
            return {"affectedRows": 0}

        elif db_type == DatabaseType.SQLSERVER.value:
            cursor = self.connection.cursor()
            cursor.execute(sql, params)
            self.connection.commit()
            affected_rows = cursor.rowcount
            cursor.close()
            return {"affectedRows": affected_rows}

        return {"affectedRows": 0}

    async def get_schema(self) -> Dict[str, Any]:
        """Get database schema information"""
        if not self.config:
            raise RuntimeError("Database not configured")

        tables = await self.list_tables()
        schema = {"tables": tables, "database": self.config.database}
        return schema

    async def list_tables(self) -> List[str]:
        """List all tables in the database"""
        if not self.config:
            raise RuntimeError("Database not configured")

        db_type = self.config.type.lower()

        if db_type == DatabaseType.MYSQL.value:
            cursor = self.connection.cursor()
            cursor.execute("SHOW TABLES")
            tables = [row[0] for row in cursor.fetchall()]
            cursor.close()
            return tables

        elif db_type == DatabaseType.POSTGRESQL.value:
            cursor = self.connection.cursor()
            cursor.execute("SELECT tablename FROM pg_tables WHERE schemaname = 'public'")
            tables = [row[0] for row in cursor.fetchall()]
            cursor.close()
            return tables

        elif db_type == DatabaseType.MONGODB.value:
            collections = self.connection[self.config.database].list_collection_names()
            return collections

        elif db_type == DatabaseType.SQLSERVER.value:
            cursor = self.connection.cursor()
            cursor.execute("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE'")
            tables = [row[0] for row in cursor.fetchall()]
            cursor.close()
            return tables

        return []

    async def describe_table(self, table: str) -> List[Dict[str, Any]]:
        """Get table structure (columns)"""
        if not self.config:
            raise RuntimeError("Database not configured")

        db_type = self.config.type.lower()

        if db_type == DatabaseType.MYSQL.value:
            cursor = self.connection.cursor(dictionary=True)
            cursor.execute(f"DESCRIBE `{table}`")
            columns = cursor.fetchall()
            cursor.close()
            return columns

        elif db_type == DatabaseType.POSTGRESQL.value:
            cursor = self.connection.cursor()
            cursor.execute("""
                SELECT column_name, data_type, is_nullable, column_default
                FROM information_schema.columns
                WHERE table_name = %s
                ORDER BY ordinal_position
            """, (table,))
            columns = [
                {"name": row[0], "type": row[1], "nullable": row[2], "default": row[3]}
                for row in cursor.fetchall()
            ]
            cursor.close()
            return columns

        elif db_type == DatabaseType.MONGODB.value:
            # For MongoDB, get sample document to infer structure
            collection = self.connection[self.config.database][table]
            sample = collection.find_one()
            if sample:
                # Return field names and types based on Python types
                return [
                    {"name": k, "type": type(v).__name__}
                    for k, v in sample.items()
                ]
            return []

        elif db_type == DatabaseType.SQLSERVER.value:
            cursor = self.connection.cursor()
            cursor.execute("""
                SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_NAME = %s
                ORDER BY ORDINAL_POSITION
            """, (table,))
            columns = [
                {"name": row[0], "type": row[1], "nullable": row[2], "default": row[3]}
                for row in cursor.fetchall()
            ]
            cursor.close()
            return columns

        return []

    async def list_indexes(self, table: str) -> List[Dict[str, Any]]:
        """List all indexes for a table"""
        if not self.config:
            raise RuntimeError("Database not configured")

        db_type = self.config.type.lower()

        if db_type == DatabaseType.MYSQL.value:
            cursor = self.connection.cursor(dictionary=True)
            cursor.execute(f"SHOW INDEX FROM `{table}`")
            indexes = cursor.fetchall()
            cursor.close()
            return indexes

        elif db_type == DatabaseType.POSTGRESQL.value:
            cursor = self.connection.cursor()
            cursor.execute("""
                SELECT indexname, indexdef
                FROM pg_indexes
                WHERE tablename = %s
            """, (table,))
            indexes = [
                {"name": row[0], "definition": row[1]}
                for row in cursor.fetchall()
            ]
            cursor.close()
            return indexes

        elif db_type == DatabaseType.SQLSERVER.value:
            cursor = self.connection.cursor()
            cursor.execute("""
                SELECT index_name = i.name, index_columns = STRING_AGG(c.name, ', ')
                FROM sys.indexes i
                JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
                JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
                WHERE i.object_id = OBJECT_ID(%s)
                GROUP BY i.name
            """, (table,))
            indexes = [
                {"name": row[0], "columns": row[1]}
                for row in cursor.fetchall()
            ]
            cursor.close()
            return indexes

        return []