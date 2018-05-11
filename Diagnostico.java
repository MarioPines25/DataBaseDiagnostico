

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.stream.Collectors;



public class Diagnostico {

	private final String DATAFILE = "C:/Users/MV_w7/Desktop/disease_data.data";
	private Connection conn;
	//private Statement st;

	/* Antes de comenzar a la manipulacion de la base de datos, creamos
	 * las estructuras de datos necesarias, para despues obtener cada uno 
	 * de los datos del archivo en la creacion de la base. Las clases
	 * serán :
	 * 1. Sintoma de la enfermedad
	 * 2. Enfermedades del archivo de datos y obtencion de los nombres
	 * 3. Obtencion de las fuentes de las enfermedades
	 * 
	 *  Para facilitar la obtencion de estos datos, las clases tendran
	 *  un metodo(decodificar) que separara mediante .split los datos del archivo de 
	 *  entrada
	 *  */

	private static class DatosFuente {
		public final String codigo;
		public final String nombre;

		private DatosFuente(String codigo, String nombre) {
			this.codigo = codigo;
			this.nombre = nombre;
		}

		public static DatosFuente codificar(String ent) {
			String[] d = ent.split("@");
			return new DatosFuente(d[0], d[1]);
		}
	}

	private static class DatosSintomas {
		public final String sintoma;
		public final String codigoSintoma;
		public final String tipoSemantico;

		private DatosSintomas(String sintoma, String codigoSintoma,
				String tipoSemantico) {
			this.sintoma = sintoma;
			this.codigoSintoma = codigoSintoma;
			this.tipoSemantico = tipoSemantico;
		}

		public static DatosSintomas codificar(String ent) {
			String[] d = ent.split(":");
			return new DatosSintomas(d[0], d[1], d[2]);
		}
	}


	private static class DatosEnfermedad {
		public final String nombreEnfermedad;
		public final Set<DatosFuente> fuentes;
		public final Set<DatosSintomas> sintomas;

		private DatosEnfermedad(String nombreEnfermedad, Set<DatosFuente> fuentes,
				Set<DatosSintomas> sintomas) {
			this.nombreEnfermedad = nombreEnfermedad;
			this.fuentes = fuentes;
			this.sintomas = sintomas;
		}


		public static DatosEnfermedad codificar(String ent) {
			String[] s = ent.split("=");
			String prim = s[0];
			String sintomas = s[1];

			s = prim.split(":");
			String nombreEnf = s[0];
			String codigos = s[1];
			return new DatosEnfermedad(
					nombreEnf,
					Arrays.stream(codigos.split(";")).map(DatosFuente::codificar).collect(Collectors.toSet()),
					Arrays.stream(sintomas.split(";")).map(DatosSintomas::codificar).collect(Collectors.toSet())
					);
		}
	}
	


	

	private void showMenu() {

		int option = -1;
		do {
			System.out.println("Bienvenido a sistema de diagn?stico\n");
			System.out.println("Selecciona una opci?n:\n");
			System.out.println("\t1. Creaci?n de base de datos y carga de datos.");
			System.out.println("\t2. Realizar diagn?stico.");
			System.out.println("\t3. Listar s?ntomas de una enfermedad.");
			System.out.println("\t4. Listar enfermedades y sus c?digos asociados.");
			System.out.println("\t5. Listar s?ntomas existentes en la BD y su tipo sem?ntico.");
			System.out.println("\t6. Mostrar estad?sticas de la base de datos.");
			System.out.println("\t7. Salir.");
			try {
				option = readInt();
				switch (option) {
				case 1:
					crearBD();
					break;
				case 2:
					realizarDiagnostico();
					break;
				case 3:
					listarSintomasEnfermedad();
					break;
				case 4:
					listarEnfermedadesYCodigosAsociados();
					break;
				case 5:
					listarSintomasYTiposSemanticos();
					break;
				case 6:
					mostrarEstadisticasBD();
					break;
				case 7:
					exit();
					break;
				}
			} catch (Exception e) {
				System.err.println("Opci?n introducida no v?lida!");
			}
		} while (option != 7);
		exit();
	}
	
	private void conectar() throws Exception {
		// implementar
		String drv="com.mysql.jdbc.Driver";
		Class.forName(drv);
		/* conexion a la BD */
		String serverAddress="localhost:3306";
		String db= "diagnostico";
		String user = "bddx";
		String pass = "bddx_pwd";
		String url= "jdbc:mysql://"+ serverAddress+"/";
		conn = DriverManager.getConnection(url, user, pass);
		conn.setCatalog(db);
		System.out.println("Conectado a la base de datos!");

	}
	
	private void desconectar() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}
	}

	private void exit() {
		System.out.println("Saliendo.. ?hasta otra!");
		desconectar();
		System.exit(0);
	}


	private void crearBD() throws Exception{
		String s;
		PreparedStatement p = null;
		try {
			if(conn==null) {
				conectar();
			}

			Connection com = conn;
			com.setAutoCommit(false);

			PreparedStatement pst = conn.prepareStatement("DROP DATABASE diagnostico;");
			pst.executeUpdate();
			PreparedStatement ps = conn.prepareStatement("CREATE DATABASE diagnostico;");
			ps.executeUpdate();

			System.out.println("\n");
			System.out.println("... Creando Tablas ...");
			System.out.println("\n");
			// Tabla disease:

			String disease = "CREATE TABLE diagnostico.disease (disease_id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255) UNIQUE)";
			p = conn.prepareStatement(disease);
			p.executeUpdate();
			p = conn.prepareStatement("ALTER TABLE diagnostico.disease ENGINE = InnoDB;");
			p.executeUpdate();
			p.close();	


			// Tabla symptom:
			String symptom ="CREATE TABLE diagnostico.symptom (cui VARCHAR(25) PRIMARY KEY, name VARCHAR(255) UNIQUE)";

			p = conn.prepareStatement(symptom);
			p.executeUpdate();
			p = conn.prepareStatement("ALTER TABLE diagnostico.symptom ENGINE = InnoDB;");
			p.executeUpdate();
			p.close();	

			// Tabla source
			String source = "CREATE TABLE diagnostico.source (source_id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255) UNIQUE)";
			p = conn.prepareStatement(source);
			p.executeUpdate();
			p = conn.prepareStatement("ALTER TABLE diagnostico.source ENGINE = InnoDB;");
			p.executeUpdate();
			p.close();	

			// Tabla codigo
			String code="CREATE TABLE diagnostico.code (code VARCHAR(255), source_id INT, " +
					"PRIMARY KEY (code, source_id), " +
					"FOREIGN KEY (source_id) REFERENCES source(source_id) ON UPDATE RESTRICT ON DELETE RESTRICT)";
			p = conn.prepareStatement(code);
			p.executeUpdate();
			p = conn.prepareStatement("ALTER TABLE diagnostico.code ENGINE = InnoDB;");
			p.executeUpdate();
			p.close();	

			// Tabla semantic_type
			String semantic_type = "CREATE TABLE diagnostico.semantic_type (semantic_type_id INT PRIMARY KEY AUTO_INCREMENT,cui VARCHAR(45) UNIQUE)";
			p = conn.prepareStatement(semantic_type);
			p.executeUpdate();
			p = conn.prepareStatement("ALTER TABLE diagnostico.semantic_type ENGINE = InnoDB;");
			p.executeUpdate();
			p.close();	

			// Tabla symptom_semantic_type
			String symptom_semantic_type = "CREATE TABLE diagnostico.symptom_semantic_type (cui VARCHAR(25), semantic_type_id INT, " +
					"PRIMARY KEY (cui, semantic_type_id), " +
					"FOREIGN KEY (cui) REFERENCES symptom(cui) ON UPDATE RESTRICT ON DELETE RESTRICT, " +
					"FOREIGN KEY (semantic_type_id) REFERENCES semantic_type(semantic_type_id) ON UPDATE RESTRICT ON DELETE RESTRICT)";
			p = conn.prepareStatement(symptom_semantic_type);
			p.executeUpdate();
			p = conn.prepareStatement("ALTER TABLE diagnostico.symptom_semantic_type ENGINE = InnoDB;");
			p.executeUpdate();
			p.close();	

			// Tabla disease_symptom
			String disease_symptom = "CREATE TABLE diagnostico.disease_symptom (disease_id INT, cui VARCHAR(25)," +
					"PRIMARY KEY (disease_id, cui)," +
					"FOREIGN KEY (disease_id) REFERENCES disease(disease_id) ON UPDATE RESTRICT ON DELETE RESTRICT," +
					"FOREIGN KEY (cui) REFERENCES symptom(cui) ON UPDATE RESTRICT ON DELETE RESTRICT)";
			p = conn.prepareStatement(disease_symptom);
			p.executeUpdate();
			p = conn.prepareStatement("ALTER TABLE diagnostico.disease_symptom ENGINE = InnoDB;");
			p.executeUpdate();
			p.close();	



			//Tabla disease_has_codigo
			String disease_has_code = "CREATE TABLE diagnostico.disease_has_code (disease_id INT, code VARCHAR(255), source_id INT, " +
					"PRIMARY KEY (disease_id, code, source_id), " +
					"FOREIGN KEY (disease_id) REFERENCES disease(disease_id) ON UPDATE RESTRICT ON DELETE RESTRICT, " +
					"FOREIGN KEY (code) REFERENCES code(code) ON UPDATE RESTRICT ON DELETE RESTRICT, " +
					"FOREIGN KEY (source_id) REFERENCES code(source_id) ON UPDATE RESTRICT ON DELETE RESTRICT)";
			p = conn.prepareStatement(disease_has_code);
			p.executeUpdate();
			p = conn.prepareStatement("ALTER TABLE diagnostico.disease_has_code ENGINE = InnoDB;");
			p.executeUpdate();
			p.close();	


			System.out.println("... Insertando datos ...");

			Set<DatosEnfermedad> diseases = readData().stream()
					.map(DatosEnfermedad::codificar)
					.collect(Collectors.toSet());

			Map<String, Integer> idFuente = new HashMap<>();
			Map<String, Integer> tiposSemanticos = new HashMap<>();
			Set<String> sintomasCn = new HashSet<>();
			for (DatosEnfermedad disease1 : diseases) {
				realizarActualizacion("INSERT INTO diagnostico.disease (name) VALUES (?)", disease1.nombreEnfermedad);
				int incrementado = ultimoIncrementado();

				for (DatosFuente source1 : disease1.fuentes) {
					Integer fuente = idFuente.get(source1.nombre);
					if (fuente == null) {
						realizarActualizacion("INSERT INTO diagnostico.source (name) VALUES (?)", source1.nombre);
						idFuente.put(source1.nombre, fuente = ultimoIncrementado());
					}
					realizarActualizacion("INSERT INTO diagnostico.code (code, source_id) VALUES (?, ?)", source1.codigo, fuente);
					realizarActualizacion("INSERT INTO diagnostico.disease_has_code VALUES (?, ?, ?)", incrementado, source1.codigo, fuente);
				}

				for (DatosSintomas symptom1 : disease1.sintomas) {

					boolean sintomaNew = false;
					if (sintomasCn.add(symptom1.codigoSintoma)) {
						sintomaNew = true;
						realizarActualizacion("INSERT INTO diagnostico.symptom VALUES (?, ?)", symptom1.codigoSintoma, symptom1.sintoma);
					}

					Integer tipoSemanticoInt = tiposSemanticos.get(symptom1.tipoSemantico);

					if (tipoSemanticoInt == null) {
						realizarActualizacion("INSERT INTO diagnostico.semantic_type (cui) VALUES (?)", symptom1.tipoSemantico);
						tiposSemanticos.put(symptom1.tipoSemantico, tipoSemanticoInt = ultimoIncrementado());
					}

					if (sintomaNew) {
						realizarActualizacion("INSERT INTO diagnostico.symptom_semantic_type VALUES (?, ?)", symptom1.codigoSintoma, tipoSemanticoInt);
					}
					realizarActualizacion("INSERT INTO diagnostico.disease_symptom VALUES (?, ?)", incrementado, symptom1.codigoSintoma);
				}
			}
			com.commit();
			System.out.println("\n");
			System.out.println("Base de datos creada, datos introducidos");
			System.out.println("\n");


		}catch(SQLException ex) {
			System.err.println(ex.getMessage());
		}

	}


	private void realizarDiagnostico() throws Exception{
		int n = 0;
		listarSintomas();
		String s = "(symptom.name= ";
		System.out.println("Ingrese sintoma: ");
		boolean terminado = false;
		while(!terminado){
			String entrada = readString();
			s+=entrada + ")";
			System.out.print("Ingresar otro sintoma?[s/n]");
			entrada = readString();

			if(!entrada.equals("n") && !entrada.equals("s")) {
				System.out.println("Error, introduce de nuevo el sintoma");
				boolean correcto = false;
				while (!correcto){
					System.out.print("[s/n]");
					entrada = readString();
					if(!entrada.equals("n") && !entrada.equals("s"))
						correcto = false;
					else
						correcto = true;
				}

			}
			if(entrada.equals("n")) {
				n++;
				s+= ")";
				terminado = true;
				break;
			}
			else {
				s += " AND (symptom.name= ";
				n++;
			}	

		}
		System.out.println("Gracias, aquí tiene su diagnóstico");


		String sintoma = "SELECT t1.name " +
				"FROM diagnstico.disease as t1 " +
				"INNER JOIN diagnostico.symptom AS t2 ON t1.disease_id = t2.disease_id " +
				"WHERE " + s + ";";

		Statement st =  conn.createStatement();
		ResultSet rs = st.executeQuery(sintoma);
		String enf=null;
		while(rs.next()){
			enf=rs.getString(1);
		}
		System.out.println(enf);

	}	

	/*
	Método listarSintomasEnfermedad: 
		-Método que sirve para, dada una enfermedad introducida
		por el usuario, muestre los síntomas asociados a dicha enfermedad.
		-Primeramente muestra las enfermedades con sus ID´s asociados.
		-El usuario debe introducir el ID de la enfermedad deseada.
		-Se muestran los síntomas asociados a la enfermedad.
	 */
	private void listarSintomasEnfermedad() throws Exception {
		try {
			conectar();

			String nombre = "SELECT (disease.name) "
					+ "FROM diagnostico.disease;";
			Statement st1 =  conn.createStatement();
			ResultSet rs1 = st1.executeQuery(nombre);

			String id = "SELECT (disease.disease_id) "
					+ "FROM diagnostico.disease;";
			Statement st2 = conn.createStatement();
			ResultSet rs2 = st2.executeQuery(id);

			while(rs1.next() && rs2.next()) {
				System.out.println(rs1.getObject(1) + "/" + rs2.getObject(1));
			}

			System.out.println("Ingrese el ID de la enfermedad");
			int entrada = readInt();

			String todo = "SELECT * FROM diagnostico.symptom "+	
					"INNER JOIN disease_symptom ON symptom.cui = disease_symptom.cui "+
					"WHERE disease_id= "+ entrada +";";
			Statement st3 = conn.createStatement();
			ResultSet rs3 = st3.executeQuery(todo);
			while(rs3.next()){
				System.out.println(rs3.getObject(1));
			}

		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
	}

	/*Método listarEnfermedadesYCodigosAsociados():
		-Método que muestra una lista de enfermedades con sus con sus códigos
		asociados de la tabla Source.
		-Se crea un Statement que muestra la lista con la información deseada.
	 */

	private void listarEnfermedadesYCodigosAsociados() throws Exception {
		try {
			conectar();

			String todo=
					"SELECT CONCAT(t2.name, '. CÃ³digos:', '\n  - ', GROUP_CONCAT(CONCAT(code, ' (de ', t3.name, ')') SEPARATOR '\n  - '), '\n') " +
							"FROM disease_has_code as t1 " +
							"INNER JOIN disease as t2 ON t1.disease_id = t1.disease_id " +
							"INNER JOIN source as t3 ON t1.source_id = t3.source_id " +
							"GROUP BY t1.disease_id;";
			Statement st3 = conn.createStatement();
			ResultSet rs3 = st3.executeQuery(todo);
			while(rs3.next()){
				System.out.println(rs3.getObject(1));
			}
		} catch (SQLException ex) {
			System.err.println(ex);
		}

	}

	/*Método listarSintomasYTiposSemanticos():
		-Muestra una lista con los sintomas y los tipos semanticos asociados.
	 */
	private void listarSintomasYTiposSemanticos() throws Exception { 
		try {
			conectar();
			String todo = "SELECT t1.cui, t2.name, t3.cui " +
					"FROM symptom_semantic_type AS t1 " +
					"INNER JOIN symptom AS t2 ON t1.cui = t2.cui " +
					"INNER JOIN semantic_type AS t3 ON t1.semantic_type_id = t3.semantic_type_id " +
					"ORDER BY t2.name ASC;";

			Statement st3 = conn.createStatement();
			ResultSet rs3 = st3.executeQuery(todo);
			while(rs3.next()){
				System.out.println("Codigo--> "+ rs3.getObject(1) + " || Sintoma--> " + 
						rs3.getObject(2) + 
						" || TipoSemantico--> " + rs3.getObject(3));
				System.out.println("\n");
			}
		} 
		catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
	}

	private void mostrarEstadisticasBD() throws Exception {
		try {
			conectar();
			System.out.println("---ESTADISTICAS---");

			//Sacamos el numero de enfermedades
			String numEnfermedades= "SELECT (disease_id) "
					+ "FROM diagnostico.disease;";
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(numEnfermedades);//ids de enfermedades
			int contador = 0;
			while(rs.next()) {
				contador ++;
			}
			System.out.println("El numero de enfermedades es: "+ contador);


			//Sacamos el numero de sintomas
			String numSintomas= "SELECT symptom.cui "
					+ "FROM diagnostico.symptom;";
			Statement st1 = conn.createStatement();
			ResultSet rs1 = st1.executeQuery(numSintomas);//ids de sintomas
			int cont = 0;
			while(rs1.next()) {
				cont++;

			}
			System.out.println("El numero de sintomas es: " + cont);

			//Sacamos la enfermedad con mas sintomas y su numero de sintomas
			String valoresMax = "SELECT t1.name, COUNT(*) AS cont " +
					"FROM disease AS t1 " +
					"INNER JOIN disease_symptom AS t2 ON t1.disease_id = t2.disease_id " +
					"GROUP BY t1.disease_id "+
					"ORDER BY cont ASC;";
			Statement st3 = conn.createStatement();
			ResultSet rs3 = st3.executeQuery(valoresMax);


			String enfMaxSint = null;
			int n=0; //Contador de enfermedades
			while (rs3.next()){
				enfMaxSint = rs3.getString(1);
				n= rs3.getInt(2);
			}
			System.out.println("Enfermedad con mas sintomas: " + enfMaxSint);
			System.out.println("Numero de sintomas de la enfermedad: " + n);

			//Sacamos de forma analoga la enfermedad con menos sintomas y su numero de sintomas
			String valoresMin = "SELECT t1.name, COUNT(*) AS con " +
					"FROM disease AS t1 " +
					"INNER JOIN disease_symptom AS t2 ON t1.disease_id = t2.disease_id " +
					"GROUP BY t1.disease_id "+
					"ORDER BY con DESC;";
			Statement st4 = conn.createStatement();
			ResultSet rs4 = st4.executeQuery(valoresMin);
			String enfMinSint = null;
			int nMin=0; //Contador de enfermedades
			while (rs4.next()){
				enfMinSint = rs4.getString(1);
				nMin = rs4.getInt(2);
			}
			System.out.println("Enfermedad con menos sintomas: " + enfMinSint);
			System.out.println("Numero de sintomas de la enfermedad: " + nMin);

			//Sacamos el numero medio de enfermedades
			String average = "SELECT AVG(x) FROM "+
					"(SELECT COUNT(*) AS x FROM disease_symptom GROUP BY disease_id) AS _;";
			Statement st5 = conn.createStatement();
			ResultSet rs5 = st5.executeQuery(average);
			int av=0;
			while(rs5.next()){
				av=rs5.getInt(1);
			}

			System.out.println("Numero medio de sintomas: " + av);
			System.out.println("\n");

			//Sacamos los semantic_types y cuantos sintomas hay en cada semantic type
			String semanticTypes= "SELECT t2.cui, COUNT(*) as cont "+
					"FROM symptom_semantic_type AS t1 " +
					"INNER JOIN semantic_type AS t2 ON t1.semantic_type_id = t2.semantic_type_id "+
					"GROUP BY t1.semantic_type_id ORDER BY cont DESC";
			Statement st6 = conn.createStatement();
			ResultSet rs6 = st6.executeQuery(semanticTypes);
			String sem = null;
			String tipo=null;
			while(rs6.next()){
				sem = rs6.getString(1);
				tipo=rs6.getString(2);
				System.out.println("CodigoSemantico: "+ sem + " Tipo: " + tipo);
			}

		}
		catch(SQLException ex){
			System.err.println(ex.getMessage());
		}
	}
	/**
	 * M?todo para leer n?meros enteros de teclado.
	 * 
	 * @return Devuelve el n?mero le?do.
	 * @throws Exception
	 *             Puede lanzar excepci?n.
	 */
	private int readInt() throws Exception {
		try {
			System.out.print("> ");
			return Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
		} catch (Exception e) {
			throw new Exception("Not number");
		}
	}

	/**
	 * M?todo para leer cadenas de teclado.
	 * 
	 * @return Devuelve la cadena le?da.
	 * @throws Exception
	 *             Puede lanzar excepci?n.
	 */
	private String readString() throws Exception {
		try {
			System.out.print("> ");
			return new BufferedReader(new InputStreamReader(System.in)).readLine();
		} catch (Exception e) {
			throw new Exception("Error reading line");
		}
	}

	/**
	 * M?todo para leer el fichero que contiene los datos.
	 * 
	 * @return Devuelve una lista de String con el contenido.
	 * @throws Exception
	 *             Puede lanzar excepci?n.
	 */
	private LinkedList<String> readData() throws Exception {
		LinkedList<String> data = new LinkedList<String>();
		BufferedReader bL = new BufferedReader(new FileReader(DATAFILE));
		while (bL.ready()) {
			data.add(bL.readLine());
		}
		bL.close();
		return data;
	}
	
	/*Método realizarActualizacion():
	-
 */
	
	private int realizarActualizacion(String consulta, Object... params) throws SQLException {
		try (PreparedStatement st = conn.prepareStatement(consulta)) {
			for (int i = 0; i < params.length; i++) {
				st.setObject(i + 1, params[i]);
			}
			return st.executeUpdate();
		}
	}

	private List<Object[]> realizarConsulta(String consulta, Object... params) throws SQLException {
		try (PreparedStatement st = conn.prepareStatement(consulta)) {
			for (int i = 0; i < params.length; i++) {
				st.setObject(i + 1, params[i]);
			}

			try (ResultSet list = st.executeQuery()) {
				int contCol = list.getMetaData().getColumnCount();
				List<Object[]> results = new LinkedList<>();

				while(list.next()) {
					Object[] cols = new Object[contCol];
					for (int i = 0; i < contCol; i++) {
						cols[i] = list.getObject(i + 1);
					}
					results.add(cols);
				}
				return results;
			}
		}
	}

	/* Metodo que devuelve el ultimo valor incrementado añadido en la base de datos */
	private int ultimoIncrementado() throws SQLException {
		return ((Number)realizarConsulta("SELECT LAST_INSERT_ID()").get(0)[0]).intValue();
	}
	
	private void listarSintomas(){
		String todo = "SELECT (Symptom.name) "
				+ "FROM diagnostico.symptom ORDER BY name ASC;";
		Statement st;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery(todo);
			while(rs.next()){
				System.out.println("Sintoma: "+ rs.getObject(1));
				System.out.println("\n");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		new Diagnostico().showMenu();
	}
}
