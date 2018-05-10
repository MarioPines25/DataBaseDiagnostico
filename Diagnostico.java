/*
Actualización del 10/05:
	-Se ha añadido el algoritmo de insertar datos: hay que hacer test
	-Hay que revisar sentencias SQL de creación de tablas "You have an error in your SQL syntax; 
	check the manual that corresponds to your MySQL server version for the right syntax to use near".
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;


public class Diagnostico {

	private final String DATAFILE = "C:/Users/MV_w7/Desktop/disease_data.data";
	private Connection conn;
	private Statement st;

	/* Antes de comenzar a la manipulacion de la base de datos, creamos
	 * las estructuras de datos necesarias, para despues obtener cada uno 
	 * de los datos del archivo en la creacion de la base. Las clases
	 * serán :
	 * 1. Sintoma de la enfermedad
	 * 2. Enfermedades del archivo de datos y obtencion de los nombres
	 * 3. Obtencion de los codigos&nombres
	 * 
	 *  Para facilitar la obtencion de estos datos, las clases tendran
	 *  un metodo(decodificar) que separara mediante .split los datos del archivo de 
	 *  entrada
	 *  */

	private static class dCodigoYnombre{

		public final String codigo;
		public final String nombre;

		private dCodigoYnombre(String codigo,String nombre){
			this.codigo=codigo;
			this.nombre=nombre;
		}
	}

	private static class dSintoma{
		public final String sintoma;
		public final String codSintoma;
		public final String semType;

		private dSintoma (String sintoma, String codSintoma, String semType){
			this.sintoma=sintoma;
			this.codSintoma=codSintoma;
			this.semType=semType;
		}

	}

	private static class dEnfermedad{
		public final String nombreEnfermedad;
		public final LinkedList<dCodigoYnombre> fuentes;
		public final LinkedList<dSintoma> sintomas;
		private dEnfermedad (String nombreEnfermedad, LinkedList<dCodigoYnombre> fuentes, LinkedList<dSintoma> sintomas){
			this.nombreEnfermedad=nombreEnfermedad;
			this.fuentes=fuentes;
			this.sintomas=sintomas;
		}


	}


	//Metodo auxiliar que limpia los elementos repetidos de la lista
	private LinkedList<dSintoma> eraseRepeated(LinkedList<dSintoma>list){
		for(int i=0;i<list.size();i++){
			for(int j=i+1;j<list.size();j++){
				if(list.get(i).codSintoma.equals(list.get(j).codSintoma)){
					list.remove(j);
				}
			}
		}
		return list;
	}

	private LinkedList<dSintoma> eraseRepeatedSemType(LinkedList<dSintoma>list){
		for(int i=0;i<list.size();i++){
			for(int j=i+1;j<list.size();j++){
				if(list.get(i).semType.equals(list.get(j).semType)){
					list.remove(j);
				}
			}
		}
		return list;
	}


	private void showMenu() {

		int option = -1;
		do {
			System.out.println("Bienvenido a sistema de diagn�stico\n");
			System.out.println("Selecciona una opci�n:\n");
			System.out.println("\t1. Creaci�n de base de datos y carga de datos.");
			System.out.println("\t2. Realizar diagn�stico.");
			System.out.println("\t3. Listar s�ntomas de una enfermedad.");
			System.out.println("\t4. Listar enfermedades y sus c�digos asociados.");
			System.out.println("\t5. Listar s�ntomas existentes en la BD y su tipo sem�ntico.");
			System.out.println("\t6. Mostrar estad�sticas de la base de datos.");
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
				System.err.println("Opci�n introducida no v�lida!");
			}
		} while (option != 7);
		exit();
	}

	private void exit() {
		System.out.println("Saliendo.. �hasta otra!");
		System.exit(0);
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
		conn.setCatalog("diagnostico");
		System.out.println("Conectado a la base de datos!");

	}

	private void crearBD() throws Exception{
		String s;
		PreparedStatement p = null;
		try {
			if(conn==null) {
				conectar();
			}

			PreparedStatement pst = conn.prepareStatement("DROP DATABASE diagnostico;");
			pst.executeUpdate();
			PreparedStatement ps = conn.prepareStatement("CREATE DATABASE diagnostico;");
			ps.executeUpdate();

			//CREACION DE TABLAS

			// Tabla disease:

			String disease = "CREATE TABLE diagnostico.disease (disease_id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255) UNIQUE)";
			p = conn.prepareStatement(disease);
			p.executeUpdate();
			p.close();	


			// Tabla symptom:
			String symptom ="CREATE TABLE diagnostico.symptom (cui VARCHAR(25) PRIMARY KEY, name VARCHAR(255) UNIQUE)";

			p = conn.prepareStatement(symptom);
			p.executeUpdate();
			p.close();	

			// Tabla source
			String source = "CREATE TABLE diagnostico.source (source_id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255) UNIQUE)";
			p = conn.prepareStatement(source);
			p.executeUpdate();
			p.close();	

			// Tabla code
			String code="CREATE TABLE diagnostico.code (code VARCHAR(255), source_id INT, " +
					"PRIMARY KEY (code, source_id), " +
					"FOREIGN KEY (source_id) REFERENCES source(source_id) ON UPDATE RESTRICT ON DELETE RESTRICT)";
			p = conn.prepareStatement(code);
			p.executeUpdate();
			p.close();	

			// Tabla semantic_type
			String semantic_type = "CREATE TABLE diagnostico.semantic_type (semantic_type_id INT PRIMARY KEY AUTO_INCREMENT,cui VARCHAR(45) UNIQUE)";
			p = conn.prepareStatement(semantic_type);
			p.executeUpdate();
			p.close();	

			// Tabla symptom_semantic_type
			String symptom_semantic_type = "CREATE TABLE diagnostico.symptom_semantic_type (cui VARCHAR(25), semantic_type_id INT, " +
					"PRIMARY KEY (cui, semantic_type_id), " +
					"FOREIGN KEY (cui) REFERENCES symptom(cui) ON UPDATE RESTRICT ON DELETE RESTRICT, " +
					"FOREIGN KEY (semantic_type_id) REFERENCES semantic_type(semantic_type_id) ON UPDATE RESTRICT ON DELETE RESTRICT)";
			p = conn.prepareStatement(symptom_semantic_type);
			p.executeUpdate();
			p.close();	

			// Tabla disease_symptom
			String disease_symptom = "CREATE TABLE diagnostico.disease_symptom (disease_id INT, cui VARCHAR(25)," +
					"PRIMARY KEY (disease_id, cui)," +
					"FOREIGN KEY (disease_id) REFERENCES disease(disease_id) ON UPDATE RESTRICT ON DELETE RESTRICT," +
					"FOREIGN KEY (cui) REFERENCES symptom(cui) ON UPDATE RESTRICT ON DELETE RESTRICT)";
			p = conn.prepareStatement(disease_symptom);
			p.executeUpdate();
			p.close();	



			//Tabla disease_has_code
			String disease_has_code = "CREATE TABLE diagnostico.disease_has_code (disease_id INT, code VARCHAR(255), source_id INT, " +
					"PRIMARY KEY (disease_id, code, source_id), " +
					"FOREIGN KEY (disease_id) REFERENCES disease(disease_id) ON UPDATE RESTRICT ON DELETE RESTRICT, " +
					"FOREIGN KEY (code) REFERENCES code(code) ON UPDATE RESTRICT ON DELETE RESTRICT, " +
					"FOREIGN KEY (source_id) REFERENCES code(source_id) ON UPDATE RESTRICT ON DELETE RESTRICT)";
			p = conn.prepareStatement(disease_has_code);
			p.executeUpdate();
			p.close();	


			//Obtencion de los datos segun DATA

			LinkedList<String>list = readData();
			LinkedList<dEnfermedad>todasLasEnfermedades = new LinkedList<dEnfermedad>();
			LinkedList<dCodigoYnombre>codigos= new LinkedList<dCodigoYnombre>();
			LinkedList<dSintoma> sintomas= new LinkedList<dSintoma>();
			for(int i=0; i<list.size();i++){

				String [] primeraSep = list.get(i).split("=");
				String [] segundaSep=primeraSep[0].split(":");
				String [] codVoc= segundaSep[1].split(";");
				for(int j=0;j<codVoc.length;j++){
					String[]datosCyV=codVoc[j].split("@");
					dCodigoYnombre cod = new dCodigoYnombre(datosCyV[0], datosCyV[1]);
					codigos.add(cod);
				}
				String [] parteDerecha=primeraSep[1].split(";");
				for(int j=0;j<parteDerecha.length;j++){
					String []datosSint=parteDerecha[j].split(":");
					dSintoma sint= new dSintoma (datosSint[0],datosSint[1],datosSint[2]);
					sintomas.add(sint);
				}
				dEnfermedad e=new dEnfermedad (segundaSep[0],codigos,sintomas);
				todasLasEnfermedades.add(e);
			}

			//Introduccion de los datos en tablas

			//tabla disease
			String query2 = "INSERT INTO diagnostico.disease (name) VALUES (?);";
			PreparedStatement pst2= conn.prepareStatement(query2);
			for(int i=0; i<todasLasEnfermedades.size();i++){
				pst2.setString(1, todasLasEnfermedades.get(i).nombreEnfermedad);
				pst2.executeUpdate();
			}

			//tabla sintoma

			String query3 ="INSERT INTO diagnostico.symptom (cui, name) VALUES (?,?)";
			LinkedList<dSintoma> sinRepetidos=eraseRepeated(sintomas);
			PreparedStatement pst3 = conn.prepareStatement(query3);
			for(int i = 0; i<sinRepetidos.size();i++){
				pst3.setString(1, sinRepetidos.get(i).codSintoma);
				pst3.setString(2, sinRepetidos.get(i).sintoma);
				pst3.executeUpdate();
			}

			//tabla semantic_type
			String query4 = "INSERT INTO diagnostico.semantic_type (cui) VALUES (?)";
			LinkedList<dSintoma> sinRepetidos2=eraseRepeatedSemType(sintomas);
			PreparedStatement pst4=conn.prepareStatement(query4);
			for(int i=0;i<sinRepetidos2.size();i++){
				pst4.setString(1, sinRepetidos2.get(i).semType);
				pst4.executeUpdate();
			}




		}catch(SQLException ex) {
			System.err.println(ex.getMessage());
		}

	}


	private void realizarDiagnostico() throws Exception{
		int n = 0;
		//listarSintomasCui();
		ArrayList<String> sintomas = new ArrayList<>();
		System.out.println("Ingrese cod_sintoma: ");


		for(int i = 0; i < 6; i++) {
			String entrada = readString();
			sintomas.add(entrada);
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

				i--;

			}
			if(entrada.equals("n")) {
				n++;
				break;
			}
			else {
				n++;
			}	

		}
		System.out.println("Gracias, aquí tiene su diagnóstico");
		String list = "";

		if(n == 1){
			list += "symptom.cui = "+ sintomas.get(0);
			System.out.println(list);
		}
		if (n == 2){
			list += "symptom.cui = " + sintomas.get(0) + " AND ";
			list += "symptom.cui = " + sintomas.get(1);
			System.out.println(list);

		}
		if (n>2) {
			for (int i = 0; i < n-1; i++ ) {
				list += "symptom.cui = " + sintomas.get(i) + " AND ";
			}
			list += "symptom.cui = " + sintomas.get(n-1);
			System.out.println(list);
		}

		String sintoma = "SELECT symptom.nombre"
				+ "FROM Symptom"
				+ "WHERE symptom.cui = " + list + ";";

		//		Statement st =  conn.createStatement();
		//		ResultSet rs = st.executeQuery(sintoma);
		//		 while(rs.next()){
		//			 System.out.println(rs.getObject(2));
		//		 }

	}

	private void listarSintomasCui() { //metodo auxiliar para poder listar los sintomas y sus codigos (uso en realizarDiagnostico())
		try {
			String sintomas = "SELECT (Symptom.name) "
					+ "FROM Symptom;";
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(sintomas);

			String codSintomas = "SELECT (Symptom.cui) "
					+ "FROM Symptom;";
			Statement st1 = conn.createStatement();
			ResultSet rs1 = st1.executeQuery(codSintomas);

			while(rs.next() && rs1.next()) {
				System.out.println("Sintoma: " + rs.getObject(1) + " , Codigo: " + rs1.getObject(1));	
			}

		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
	}

	private void listarSintomasEnfermedad() throws Exception {
		try {
			conectar();

			String nombre = "SELECT (disease.name) "
					+ "FROM Disease;";
			Statement st1 =  conn.createStatement();
			ResultSet rs1 = st1.executeQuery(nombre);

			String id = "SELECT (disease.disease_id) "
					+ "FROM Disease;";
			Statement st2 = conn.createStatement();
			ResultSet rs2 = st2.executeQuery(id);

			while(rs1.next() && rs2.next()) {
				System.out.println(rs1.getObject(1) + "/" + rs2.getObject(1));
			}

			System.out.println("Ingrese el ID de la enfermedad");
			int entrada = readInt();

			String cui = "SELECT cui " + "FROM disease_symptom "
					+ "WHERE disease_id= " + entrada + ";";
			PreparedStatement st3 = conn.prepareStatement(cui);
			ResultSet rs3 = st3.executeQuery(cui);

			while(rs3.next()) {
				String comp = rs3.getString(1);
				String cuicomp = "SELECT name " + "FROM Symptom " 
						+ "WHERE cui= " + comp + ";";
				Statement st4 = conn.createStatement();
				ResultSet rs4 = st4.executeQuery(cuicomp);
				if(rs4.next()) {
					System.out.println(rs4.getObject(1));
				}	
			}
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
	}


	private void listarEnfermedadesYCodigosAsociados() throws Exception {
		try {
			conectar();

			String id = "SELECT (disease.disease_id) "
					+ "FROM Disease;";
			Statement st =  conn.createStatement();
			ResultSet rs = st.executeQuery(id);

			while(rs.next()) {

				String id1 = rs.getString(1);
				String codeasociado   = "SELECT code " + "FROM Disease_has_code"
						+ "WHERE id= " + id1 + ";";
				Statement st1 = conn.createStatement();
				ResultSet rs1 = st1.executeQuery(codeasociado); //codes asociados al id

				String nombre   = "SELECT name " + "FROM Disease"
						+ "WHERE id= " + id1 + ";";
				Statement st2 = conn.createStatement();
				ResultSet rs2 = st2.executeQuery(nombre);

				while(rs1.next()) {
					String code1 = rs1.getString(1);
					String sourceasociado   = "SELECT source_id " + "FROM Code"
							+ "WHERE code= " + code1 + ";";
					Statement st3 = conn.createStatement();
					ResultSet rs3 = st3.executeQuery(sourceasociado); //source_id`s asociados a un code

					String sourceid = rs3.getString(1);
					String nombresource = "SELECT name " + "FROM Source"
							+ "WHERE source_id=" + sourceid + ";";
					Statement st4 = conn.createStatement();
					ResultSet rs4 = st4.executeQuery(nombresource);

					System.out.println(rs2.getObject(1) + "/" + rs1.getObject(1) + "," + rs4.getObject(1));
				}
			}
		} catch (SQLException ex) {
			System.err.println(ex);
		}

	}

	private void listarSintomasYTiposSemanticos() throws Exception { 
		try {
			conectar();

			String cuisymp = "SELECT (symptom.cui) "
					+ "FROM Symptom;";
			Statement st1 =  conn.createStatement();
			ResultSet rs1 = st1.executeQuery(cuisymp); //ids de los sintomas

			while(rs1.next()) {

				String nombre = "SELECT (symptom.name) "
						+ "FROM Symptom "+
						"WHERE cui= "+cuisymp+	";";
				Statement st =  conn.createStatement();
				ResultSet rs = st.executeQuery(nombre);//nombre sintomas asociado a id

				String cui1 = rs1.getString(1);
				String semanticasociado = "SELECT (symptom_semantic_type.semantic_type_id) "
						+ "FROM SymptomSemanticType "
						+ "WHERE cui= " +cui1+";";
				Statement st2 =  conn.createStatement();
				ResultSet rs2 = st2.executeQuery(semanticasociado); //semantics asociados a un cui

				System.out.println("Sintoma: " + rs.getObject(1) + " , Tipo Semantico: " + rs2.getObject(1) );
			}


		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
	}

	private void mostrarEstadisticasBD() throws Exception {
		try {
			conectar();

			String numEnfermedades= "SELECT (disease.disease_id)"
					+ "FROM Disease;";
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(numEnfermedades);//ids de enfermedades
			while(rs.next()) {
				int contador = 0;
				String numero = rs.getString(1);
				contador ++;
				System.out.println("El numero de enfermedades es: "+ contador);
			}

			String numSintomas= "SELECT (symptom.cui)"
					+ "FROM Symptom;";
			Statement st1 = conn.createStatement();
			ResultSet rs1 = st1.executeQuery(numSintomas);//ids de sintomas
			while(rs1.next()) {
				int contador = 0;
				String numero = rs1.getString(1);
				contador++;
				System.out.println("El numero de sintomas es: " + contador);
			}

			String maxSympEnf= "SELECT (disease.disease_id) "
					+ "FROM DiseaseSympton;";
			Statement st2 = conn.createStatement();
			ResultSet rs2 = st2.executeQuery(maxSympEnf);//ids de enfermedades
			while(rs2.next()) {
				String id = rs2.getString(1);
				String numcuiasociadoid = "SELECT COUNT (disease_symptom.cui) "
						+	"FROM DiseaseSymptom WHERE disease_id= " + id + ";";
				Statement st3 = conn.createStatement();
				ResultSet rs3 = st3.executeQuery(numcuiasociadoid);//num de cuis asociados a un id
				int comp = 0;
				while(rs3.next()) {
					int numcui = rs3.getInt(1);
					if(comp < numcui) {
						comp = numcui;
					}
				}
				String max = "SELECT (disase_symptom.disease_id)"
						+ "FROM DiseaseSymptom WHERE COUNT cui= "+ comp +";";
				Statement st4 = conn.createStatement();
				ResultSet rs4 = st4.executeQuery(max);

				int idfinal = rs4.getInt(1);

				String nombremaxENF = "SELECT (disase.name)"
						+ "FROM Disease WHERE disease.id= "+ idfinal +";";
				Statement st5 = conn.createStatement();
				ResultSet rs5 = st5.executeQuery(nombremaxENF);

				System.out.println("La enfermedad con mas sintomas es: "+ rs5.getObject(1));
			}

			String minSympEnf= "SELECT (disease.disease_id) "
					+ "FROM DiseaseSympton;";
			Statement st6 = conn.createStatement();
			ResultSet rs6 = st6.executeQuery(minSympEnf);//ids de enfermedades
			while(rs6.next()) {
				String id = rs6.getString(1);
				String numcuiasociadoid = "SELECT COUNT (disease_symptom.cui) "
						+	"FROM DiseaseSymptom WHERE disease_id= " + id + ";";
				Statement st7 = conn.createStatement();
				ResultSet rs7 = st7.executeQuery(numcuiasociadoid);//num de cuis asociados a un id

				int comp = rs7.getInt(1);
				while(rs7.next()) {
					int numcui = rs7.getInt(1);
					if(comp > numcui) {
						comp = numcui;
					}
				}
				String min = "SELECT (disase_symptom.disease_id)"
						+ "FROM DiseaseSymptom WHERE COUNT cui= "+ comp +";";
				Statement st8 = conn.createStatement();
				ResultSet rs8 = st8.executeQuery(min);

				int idfinal1 = rs8.getInt(1);

				String nombreminENF = "SELECT (disase.name)"
						+ "FROM Disease WHERE disease.id= "+ idfinal1 +";";
				Statement st9 = conn.createStatement();
				ResultSet rs9 = st9.executeQuery(nombreminENF);

				System.out.println("La enfermedad con menos sintomas es: "+ rs9.getObject(1));
			}

			String semTypes= "SELECT (semantic_type.semantic_type_id)"
					+ "FROM SemanticType";
			Statement  st10 = conn.createStatement();
			ResultSet  rs10 = st10.executeQuery(semTypes);//todos los ids semantic

			while(rs10.next()) {
				int semid = rs10.getInt(1);
				String numSym = "SELECT (symptom.cui) "
						+ "FROM SymptomSemanticType WHERE semantic_type_id= " + semid +";";
				Statement  st11 = conn.createStatement();
				ResultSet  rs11 = st11.executeQuery(numSym);//todos los sintomas asociados a un semantic

				String numSemTypes= "SELECT COUNT(semantic_type.semantic_type_id)"
						+ "FROM SemanticType";
				Statement  st12 = conn.createStatement();
				ResultSet  rs12 = st12.executeQuery(numSemTypes);//numero total de semantics

				System.out.println("Tipo Semantico: " + rs10.getObject(1) + ", Sintomas asociados: " + rs11.getObject(1));
				System.out.println("El numero total de Tipos Semanticos es: " + rs12.getObject(1));
			}
			int contador =0;
			int suma=0;
			int average=0;
			String avgsymp= "SELECT (disease.disease_id) "
					+ "FROM DiseaseSympton;";
			Statement st13 = conn.createStatement();
			ResultSet rs13 = st13.executeQuery(avgsymp);//ids de enfermedades
			while(rs13.next()) {
				String id = rs13.getString(1);
				String cuiavg = "SELECT COUNT (disease_symptom.cui) "
						+	"FROM DiseaseSymptom WHERE disease_id= " + id + ";";
				Statement st14 = conn.createStatement();
				ResultSet rs14 = st14.executeQuery(cuiavg);//num de cuis asociados a un id
				int sumador = rs14.getInt(1);
				contador++;
				suma=suma +sumador;
			}
			average=suma/contador;
			System.out.println("El numero medio de sintomas: "+average);


		}
		catch(SQLException ex){
			System.err.println(ex.getMessage());
		}
	}

	/**
	 * M�todo para leer n�meros enteros de teclado.
	 * 
	 * @return Devuelve el n�mero le�do.
	 * @throws Exception
	 *             Puede lanzar excepci�n.
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
	 * M�todo para leer cadenas de teclado.
	 * 
	 * @return Devuelve la cadena le�da.
	 * @throws Exception
	 *             Puede lanzar excepci�n.
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
	 * M�todo para leer el fichero que contiene los datos.
	 * 
	 * @return Devuelve una lista de String con el contenido.
	 * @throws Exception
	 *             Puede lanzar excepci�n.
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

	public static void main(String args[]) {
		new Diagnostico().showMenu();
	}
}
