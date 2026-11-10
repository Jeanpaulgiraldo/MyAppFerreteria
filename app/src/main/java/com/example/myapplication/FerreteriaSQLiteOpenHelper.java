package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

/**
 * Clase helper para la gestión de la base de datos SQLite de la Ferretería Rodamientos y Fierros.
 * Implementa la estructura normalizada hasta la 3ra Forma Normal (3NF).
 */
public class FerreteriaSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ferreteria_rodamientos.db";
    private static final int DATABASE_VERSION = 1;

    // Sentencias SQL de creación
    private static final String CREATE_TABLE_CLIENTE =
            "CREATE TABLE cliente (" +
                    "codigo_cliente INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT NOT NULL, " +
                    "direccion TEXT, " +
                    "telefono TEXT" +
                    ");";

    private static final String CREATE_TABLE_PEDIDO =
            "CREATE TABLE pedido (" +
                    "codigo_pedido INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "descripcion TEXT, " +
                    "fecha_pedido TEXT NOT NULL, " +
                    "codigo_cliente INTEGER NOT NULL, " +
                    "FOREIGN KEY (codigo_cliente) REFERENCES cliente(codigo_cliente) ON DELETE CASCADE" +
                    ");";

    private static final String CREATE_TABLE_PRODUCTO =
            "CREATE TABLE producto (" +
                    "codigo_producto INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "fabricante TEXT, " +
                    "valor_producto REAL NOT NULL" +
                    ");";

    private static final String CREATE_TABLE_FACTURA =
            "CREATE TABLE factura (" +
                    "codigo_factura INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "fecha TEXT NOT NULL, " +
                    "valor_factura REAL NOT NULL, " +
                    "codigo_pedido INTEGER NOT NULL, " +
                    "FOREIGN KEY (codigo_pedido) REFERENCES pedido(codigo_pedido) ON DELETE CASCADE" +
                    ");";

    private static final String CREATE_TABLE_DETALLE_PEDIDO =
            "CREATE TABLE detalle_pedido (" +
                    "codigo_pedido INTEGER NOT NULL, " +
                    "codigo_producto INTEGER NOT NULL, " +
                    "cantidad INTEGER NOT NULL, " +
                    "precio_unitario REAL NOT NULL, " +
                    "PRIMARY KEY (codigo_pedido, codigo_producto), " +
                    "FOREIGN KEY (codigo_pedido) REFERENCES pedido(codigo_pedido) ON DELETE CASCADE, " +
                    "FOREIGN KEY (codigo_producto) REFERENCES producto(codigo_producto) ON DELETE RESTRICT" +
                    ");";

    public FerreteriaSQLiteOpenHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON;");
        db.execSQL(CREATE_TABLE_CLIENTE);
        db.execSQL(CREATE_TABLE_PEDIDO);
        db.execSQL(CREATE_TABLE_PRODUCTO);
        db.execSQL(CREATE_TABLE_FACTURA);
        db.execSQL(CREATE_TABLE_DETALLE_PEDIDO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS detalle_pedido");
        db.execSQL("DROP TABLE IF EXISTS factura");
        db.execSQL("DROP TABLE IF EXISTS producto");
        db.execSQL("DROP TABLE IF EXISTS pedido");
        db.execSQL("DROP TABLE IF EXISTS cliente");
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // ==========================================
    // OPERACIONES CRUD PARA CLIENTES
    // ==========================================

    public long insertarCliente(String nombre, String direccion, String telefono) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("direccion", direccion);
        values.put("telefono", telefono);

        long id = db.insert("cliente", null, values);
        db.close();
        return id;
    }

    public Cursor buscarClientePorCodigo(int codigo) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM cliente WHERE codigo_cliente = ?", new String[]{String.valueOf(codigo)});
    }

    public int actualizarCliente(int codigo, String nombre, String direccion, String telefono) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("direccion", direccion);
        values.put("telefono", telefono);

        int rows = db.update("cliente", values, "codigo_cliente = ?", new String[]{String.valueOf(codigo)});
        db.close();
        return rows;
    }

    public int eliminarCliente(int codigo) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("cliente", "codigo_cliente = ?", new String[]{String.valueOf(codigo)});
        db.close();
        return rows;
    }

    public Cursor obtenerClientes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM cliente ORDER BY codigo_cliente DESC", null);
    }

    // ==========================================
    // OPERACIONES CRUD PARA PRODUCTOS
    // ==========================================

    public long insertarProducto(String fabricante, double valorProducto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fabricante", fabricante);
        values.put("valor_producto", valorProducto);

        long id = db.insert("producto", null, values);
        db.close();
        return id;
    }

    public Cursor buscarProductoPorCodigo(int codigo) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM producto WHERE codigo_producto = ?", new String[]{String.valueOf(codigo)});
    }

    public int actualizarProducto(int codigo, String fabricante, double valorProducto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fabricante", fabricante);
        values.put("valor_producto", valorProducto);

        int rows = db.update("producto", values, "codigo_producto = ?", new String[]{String.valueOf(codigo)});
        db.close();
        return rows;
    }

    public int eliminarProducto(int codigo) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("producto", "codigo_producto = ?", new String[]{String.valueOf(codigo)});
        db.close();
        return rows;
    }

    public Cursor obtenerProductos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM producto ORDER BY codigo_producto DESC", null);
    }
}
