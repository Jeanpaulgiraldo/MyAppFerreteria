package com.example.myapplication

import android.database.Cursor
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

data class ClienteModel(
    val codigo: Int,
    val nombre: String,
    val direccion: String,
    val telefono: String
)

data class ProductoModel(
    val codigo: Int,
    val fabricante: String,
    val valor: Double
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FerreteriaAppScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FerreteriaAppScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dbHelper = remember { FerreteriaSQLiteOpenHelper(context) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Clientes", "Productos")

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "Ferretería Rodamientos y Fierros",
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> ClientesScreen(dbHelper)
            1 -> ProductosScreen(dbHelper)
        }
    }
}

@Composable
fun ClientesScreen(dbHelper: FerreteriaSQLiteOpenHelper) {
    val context = LocalContext.current

    var codigoInput by remember { mutableStateOf("") }
    var nombreInput by remember { mutableStateOf("") }
    var direccionInput by remember { mutableStateOf("") }
    var telefonoInput by remember { mutableStateOf("") }

    var listaClientes by remember { mutableStateOf(listOf<ClienteModel>()) }

    fun cargarClientes() {
        val lista = mutableListOf<ClienteModel>()
        var cursor: Cursor? = null
        try {
            cursor = dbHelper.obtenerClientes()
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val codigo = cursor.getInt(cursor.getColumnIndexOrThrow("codigo_cliente"))
                    val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")) ?: ""
                    val direccion = cursor.getString(cursor.getColumnIndexOrThrow("direccion")) ?: ""
                    val telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono")) ?: ""
                    lista.add(ClienteModel(codigo, nombre, direccion, telefono))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar clientes: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            cursor?.close()
        }
        listaClientes = lista
    }

    LaunchedEffect(Unit) {
        cargarClientes()
    }

    fun limpiarCampos() {
        codigoInput = ""
        nombreInput = ""
        direccionInput = ""
        telefonoInput = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Gestión de Clientes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = codigoInput,
            onValueChange = { codigoInput = it },
            label = { Text("Código de Cliente (Buscar/Actualizar/Eliminar)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = nombreInput,
            onValueChange = { nombreInput = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = direccionInput,
            onValueChange = { direccionInput = it },
            label = { Text("Dirección") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = telefonoInput,
            onValueChange = { telefonoInput = it },
            label = { Text("Teléfono") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    if (nombreInput.isBlank()) {
                        Toast.makeText(context, "Ingrese el nombre del cliente", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val res = dbHelper.insertarCliente(nombreInput, direccionInput, telefonoInput)
                    if (res > 0) {
                        Toast.makeText(context, "Cliente guardado con ID $res", Toast.LENGTH_SHORT).show()
                        limpiarCampos()
                        cargarClientes()
                    } else {
                        Toast.makeText(context, "Error al registrar cliente", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Guardar")
            }

            Button(
                onClick = {
                    val codigo = codigoInput.toIntOrNull()
                    if (codigo == null) {
                        Toast.makeText(context, "Ingrese un código válido", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val cursor = dbHelper.buscarClientePorCodigo(codigo)
                    if (cursor != null && cursor.moveToFirst()) {
                        nombreInput = cursor.getString(cursor.getColumnIndexOrThrow("nombre")) ?: ""
                        direccionInput = cursor.getString(cursor.getColumnIndexOrThrow("direccion")) ?: ""
                        telefonoInput = cursor.getString(cursor.getColumnIndexOrThrow("telefono")) ?: ""
                        Toast.makeText(context, "Cliente encontrado", Toast.LENGTH_SHORT).show()
                        cursor.close()
                    } else {
                        Toast.makeText(context, "Cliente no encontrado", Toast.LENGTH_SHORT).show()
                        cursor?.close()
                    }
                }
            ) {
                Text("Buscar")
            }

            Button(
                onClick = {
                    val codigo = codigoInput.toIntOrNull()
                    if (codigo == null) {
                        Toast.makeText(context, "Ingrese un código de cliente", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val rows = dbHelper.actualizarCliente(codigo, nombreInput, direccionInput, telefonoInput)
                    if (rows > 0) {
                        Toast.makeText(context, "Cliente actualizado", Toast.LENGTH_SHORT).show()
                        limpiarCampos()
                        cargarClientes()
                    } else {
                        Toast.makeText(context, "No se pudo actualizar el cliente", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Actualizar")
            }

            OutlinedButton(
                onClick = {
                    val codigo = codigoInput.toIntOrNull()
                    if (codigo == null) {
                        Toast.makeText(context, "Ingrese un código para eliminar", Toast.LENGTH_SHORT).show()
                        return@OutlinedButton
                    }
                    val rows = dbHelper.eliminarCliente(codigo)
                    if (rows > 0) {
                        Toast.makeText(context, "Cliente eliminado", Toast.LENGTH_SHORT).show()
                        limpiarCampos()
                        cargarClientes()
                    } else {
                        Toast.makeText(context, "Cliente no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Eliminar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Lista de Clientes Registrados",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(listaClientes) { cliente ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Código: ${cliente.codigo}", fontWeight = FontWeight.Bold)
                        Text(text = "Nombre: ${cliente.nombre}")
                        Text(text = "Dirección: ${cliente.direccion}")
                        Text(text = "Teléfono: ${cliente.telefono}")
                    }
                }
            }
        }
    }
}

@Composable
fun ProductosScreen(dbHelper: FerreteriaSQLiteOpenHelper) {
    val context = LocalContext.current

    var codigoInput by remember { mutableStateOf("") }
    var fabricanteInput by remember { mutableStateOf("") }
    var valorInput by remember { mutableStateOf("") }

    var listaProductos by remember { mutableStateOf(listOf<ProductoModel>()) }

    fun cargarProductos() {
        val lista = mutableListOf<ProductoModel>()
        var cursor: Cursor? = null
        try {
            cursor = dbHelper.obtenerProductos()
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val codigo = cursor.getInt(cursor.getColumnIndexOrThrow("codigo_producto"))
                    val fabricante = cursor.getString(cursor.getColumnIndexOrThrow("fabricante")) ?: ""
                    val valor = cursor.getDouble(cursor.getColumnIndexOrThrow("valor_producto"))
                    lista.add(ProductoModel(codigo, fabricante, valor))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar productos: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            cursor?.close()
        }
        listaProductos = lista
    }

    LaunchedEffect(Unit) {
        cargarProductos()
    }

    fun limpiarCampos() {
        codigoInput = ""
        fabricanteInput = ""
        valorInput = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Gestión de Productos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = codigoInput,
            onValueChange = { codigoInput = it },
            label = { Text("Código de Producto (Buscar/Actualizar/Eliminar)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = fabricanteInput,
            onValueChange = { fabricanteInput = it },
            label = { Text("Fabricante") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = valorInput,
            onValueChange = { valorInput = it },
            label = { Text("Valor / Precio") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    val valor = valorInput.toDoubleOrNull()
                    if (valor == null) {
                        Toast.makeText(context, "Ingrese un valor numérico válido", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val res = dbHelper.insertarProducto(fabricanteInput, valor)
                    if (res > 0) {
                        Toast.makeText(context, "Producto registrado con ID $res", Toast.LENGTH_SHORT).show()
                        limpiarCampos()
                        cargarProductos()
                    } else {
                        Toast.makeText(context, "Error al registrar producto", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Guardar")
            }

            Button(
                onClick = {
                    val codigo = codigoInput.toIntOrNull()
                    if (codigo == null) {
                        Toast.makeText(context, "Ingrese un código válido", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val cursor = dbHelper.buscarProductoPorCodigo(codigo)
                    if (cursor != null && cursor.moveToFirst()) {
                        fabricanteInput = cursor.getString(cursor.getColumnIndexOrThrow("fabricante")) ?: ""
                        valorInput = cursor.getDouble(cursor.getColumnIndexOrThrow("valor_producto")).toString()
                        Toast.makeText(context, "Producto encontrado", Toast.LENGTH_SHORT).show()
                        cursor.close()
                    } else {
                        Toast.makeText(context, "Producto no encontrado", Toast.LENGTH_SHORT).show()
                        cursor?.close()
                    }
                }
            ) {
                Text("Buscar")
            }

            Button(
                onClick = {
                    val codigo = codigoInput.toIntOrNull()
                    val valor = valorInput.toDoubleOrNull()
                    if (codigo == null || valor == null) {
                        Toast.makeText(context, "Ingrese un código y valor válidos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val rows = dbHelper.actualizarProducto(codigo, fabricanteInput, valor)
                    if (rows > 0) {
                        Toast.makeText(context, "Producto actualizado", Toast.LENGTH_SHORT).show()
                        limpiarCampos()
                        cargarProductos()
                    } else {
                        Toast.makeText(context, "No se pudo actualizar el producto", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Actualizar")
            }

            OutlinedButton(
                onClick = {
                    val codigo = codigoInput.toIntOrNull()
                    if (codigo == null) {
                        Toast.makeText(context, "Ingrese un código para eliminar", Toast.LENGTH_SHORT).show()
                        return@OutlinedButton
                    }
                    val rows = dbHelper.eliminarProducto(codigo)
                    if (rows > 0) {
                        Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                        limpiarCampos()
                        cargarProductos()
                    } else {
                        Toast.makeText(context, "Producto no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Eliminar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Lista de Productos Registrados",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(listaProductos) { producto ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Código: ${producto.codigo}", fontWeight = FontWeight.Bold)
                        Text(text = "Fabricante: ${producto.fabricante}")
                        Text(text = "Valor: $${producto.valor}")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FerreteriaAppPreview() {
    MyApplicationTheme {
        FerreteriaAppScreen()
    }
}
