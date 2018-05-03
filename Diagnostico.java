import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;


public class Diagnostico {

	private final String DATAFILE = "data/disease_data.data";
	Connection conn;
	Statement st;
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
		String url= "jdbc:mysql://"+ serverAddress+"/"+db;
		conn = DriverManager.getConnection(url, user, pass);
		System.out.println("Conectado a la base de datos!");


	}

	private void crearBD() throws Exception{
		try {
			conectar();
			st.executeUpdate("CREATE SCHEMA `diagnostico` ; ");
			LinkedList<String> list = readData();
			String []enfermedades;
			String []codVoc;
			String []codigo;
			String[]enfSint;//array de enfermedades y sintomas


			for (int i = 0; i < list.size(); i++) {
				enfSint = list.get(i).split("=",2);
				//COMIENZO PARTE IZQUIERDA ARBOL
				enfermedades=enfSint[0].split(":");
				codVoc=enfermedades[1].split(";");

				for(int j=0;i<codVoc.length;j++){
					//conseguimos codigos y vocabularios
					codigo=codVoc[i].split("@");
				}
				//FIN PARTE IZQUIERDA ARBOL
				//COMIENZO PARTE DERECHA ARBOL
				String [] sintomas;
				String [] elementos;
				sintomas = enfSint[1].split(";");
				for (int j=0; i<sintomas.length;j++){
					//System.out.println(sintomas[i]);
					elementos= sintomas[j].split(":");
					for (int z=0; j< elementos.length;z++){
						//datos= elementos[j].split(":");
						System.out.println(elementos[z]); 
					}
				}
			}


		}catch(SQLException ex) {
			System.err.println(ex.getMessage());
		}
		conn.close();

	}
	private int numMaxSyntom() {
		return 0;//devuelve el numero de sintomas de la enfermedad con mas sintomas
	}

	private void realizarDiagnostico() throws Exception{
		int n = 0;
		listarSintomasCui();
		Scanner scanner = new Scanner (System.in);
		String[]numMax = new String[numMaxSyntom()];
		System.out.println("Ingrese cod_sintoma: ");
		for(int i = 0; i < numMax.length; i++) {
			String entrada = scanner.nextLine();
			numMax[i] += entrada;
			System.out.print("Ingresar otro sintoma?[s/n]");
			String respuesta = scanner.nextLine();
			if(!respuesta.equals("n") || !respuesta.equals("s")) {
				System.out.println("Introduce un sintoma");
				i--;
			}
			if(respuesta.equals("n")) {
				break;
			}
			else {
				n++;
			}	
		}


		String list = "";
		if (n>2) {
			for (int i = 0; i < n-2; i++ ) {
				list += numMax[i] + ", ";
			}
		}
		list += numMax[n-1];
		String sintomas = "SELECT symptom.nombre"
				+ "FROM Symptom"
				+ "WHERE sintomas = " + list + ";";
		scanner.close();
	}

	private void listarSintomasCui() { //metodo auxiliar para poder listar los sintomas y sus codigos (uso en realizarDiagnostico())
		try {
			st = conn.createStatement();
			String str = "SELECT (symptom.name, symptom.cui) "
					+ "FROM Symptom";
			ResultSet rs = st.executeQuery(str);
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
	}

	private void listarSintomasEnfermedad() {
		try {
			st = conn.createStatement();
			String str = "SELECT (disease.name) "
					+ "FROM Disease;";
			Scanner scanner = new Scanner (System.in);
			System.out.println("Ingrese Id de la enfermedad: ");
			String entrada = scanner.nextLine();
			String query = "SELECT (disease.id)"
					+ "FROM Disease"
					+ "WHERE disease_id =" + entrada +";";
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
	}


	private void listarEnfermedadesYCodigosAsociados() {
		try {
			st = conn.createStatement();
			String str = "SELECT (disease.name, code.code) "
					+ "FROM Disease;";
		} catch (SQLException ex) {
			System.err.println(ex);
		}
		/*Scanner scanner = new Scanner (System.in);
		System.out.println("Ingrese Id de la enfermedad: ");
		String entrada = scanner.nextLine();
		String query = "SELECT (disease.name)"
				+ "FROM Disease"
				+ "WHERE disease_id =" + entrada;*/
	}

	private void listarSintomasYTiposSemanticos() { //revisar
		try {
			st = conn.createStatement();
			String str = "SELECT (symptom.cui, semantic_type.semantic_type_id) "
					+ "FROM Symptom";
			ResultSet rs = st.executeQuery(str);
			
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
	}

	private void mostrarEstadisticasBD() {
		try {
		st = conn.createStatement();

		String numEnfermedades= "SELECT COUNT(disease.disease_id)"
				+ "FROM Disease;";
		ResultSet rs = st.executeQuery(numEnfermedades);
		
		String numSintomas= "SELECT COUNT (symptom.cui)"
				+ "FROM Symptom;";
		rs = st.executeQuery(numSintomas);
		
		String maxSympEnf= "SELECT COUNT (disease.disease_id) "
				+ "FROM DiseaseSympton WHERE MAX (symptom.cui);";
		rs = st.executeQuery(maxSympEnf);
		
		String minSympEnf= "SELECT COUNT (disease.disease_id) "
				+ "FROM DiseaseSymptom WHERE MIN(symptom.cui);";
		rs = st.executeQuery(minSympEnf);
		
		String avgSymp= "SELECT COUNT (disease.disease_id)"
				+ "FROM DiseaseSymptom WHERE AVG(symptom.cui);";
		rs = st.executeQuery(avgSymp);
		
		String semTypes= "SELECT (semantic.semantic_type_id)"
				+ "FROM Semantic";
		rs = st.executeQuery(semTypes);
		
		String numSemTypes= "SELECT COUNT (semantic.semantic_type_id)"
				+ "FROM Semantic;";
		rs = st.executeQuery(numSemTypes);
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
